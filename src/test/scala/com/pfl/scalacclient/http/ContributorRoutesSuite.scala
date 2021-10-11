package com.pfl.scalacclient.http

import cats.effect.IO
import cats.implicits._
import org.http4s.Method._
import org.http4s._
import org.http4s.circe._
import org.http4s.client.dsl.io._
import org.http4s.implicits._
import org.scalacheck.effect.PropF
import suite.HttpTestSuite
import com.pfl.scalacclient.github.CirceDecoders
import com.pfl.scalacclient.error.LiveHttpErrorHandler
import com.pfl.scalacclient.model.User
import com.pfl.scalacclient.http.CirceEncoders
import com.pfl.scalacclient.model._
import com.pfl.scalacclient.github.GitHubRepository
import com.pfl.scalacclient.error.instances
import com.pfl.scalacclient.github.GitHubProgram
import cats.effect.kernel.Sync
import org.typelevel.log4cats.slf4j.Slf4jLogger
import com.pfl.scalacclient.error.ErrorMessage
import com.pfl.scalacclient.arbitraries.given
import com.pfl.scalacclient.generators._
import com.pfl.scalacclient.github.TestGitHubRepository
import com.pfl.scalacclient.github.model._
import org.typelevel.log4cats.Logger

class ContributorRoutesSuite
    extends HttpTestSuite
    with CirceDecoders
    with CirceEncoders {

  given EntityEncoder[IO, User] = jsonEncoderOf
  given unsafeLogger[F[_]: Sync]: Logger[F] = Slf4jLogger.getLogger[F]

  val errHandler = LiveHttpErrorHandler[IO]

  val uri = uri"api/v1/projects/"

  val notFoundGitRepo = new GitHubRepository[IO] {
    override def getRepositories(
        organisation: Organisation,
        pageSize: PageSize,
        pageNo: PageNo
    ): IO[List[Repo]] =
      IO.raiseError(instances.NotFoundErr(organisation.value.value))

    override def getContributors(
        organisation: Organisation,
        repo: Repo,
        pageSize: PageSize,
        pageNo: PageNo
    ): IO[List[User]] = ???

  }
  val gitErrMessage = "test Message"
  val gitErrorRepo = new GitHubRepository[IO] {
    override def getRepositories(
        organisation: Organisation,
        pageSize: PageSize,
        pageNo: PageNo
    ): IO[List[Repo]] =
      IO.raiseError(instances.GitHubErr(gitErrMessage))

    override def getContributors(
        organisation: Organisation,
        repo: Repo,
        pageSize: PageSize,
        pageNo: PageNo
    ): IO[List[User]] = ???

  }

  test("returns NotFound when org doesnot exist") {
    PropF.forAllF { (organisation: Organisation) =>
      val routes =
        new ContributorRoutes[IO](
          new ContributorService(new GitHubProgram(notFoundGitRepo))
        ).routes(errHandler)

      GET(
        Uri.unsafeFromString(
          s"org/${organisation.value}/contributors"
        )
      ).pure[IO].flatMap { req =>
        assertHttp(routes, req)(
          Status.NotFound,
          ErrorMessage(
            "BASIC-002",
            s"resource with given id ${organisation.value.value} does not exist"
          )
        )
      }
    }
  }

  test("returns ServiceUnavailable when gitapi does not work") {
    PropF.forAllF { (organisation: Organisation) =>
      val routes =
        new ContributorRoutes[IO](
          new ContributorService(new GitHubProgram(gitErrorRepo))
        ).routes(errHandler)

      GET(
        Uri.unsafeFromString(
          s"org/${organisation.value}/contributors"
        )
      ).pure[IO].flatMap { req =>
        assertHttp(routes, req)(
          Status.ServiceUnavailable,
          ErrorMessage(
            "BASIC-003",
            gitErrMessage
          )
        )
      }
    }
  }

  test("returns BadRequest when org_name not specified") {
    PropF.forAllF { (_: Organisation) =>
      val routes =
        new ContributorRoutes[IO](
          new ContributorService(new GitHubProgram(TestGitHubRepository(Map())))
        ).routes(errHandler)

      GET(
        Uri.unsafeFromString(
          s"org//contributors"
        )
      ).pure[IO].flatMap { req =>
        assertHttp(routes, req)(
          Status.BadRequest,
          ErrorMessage("BASIC-001", "Invalid request")
        )
      }
    }
  }

  test("returns proper data for given organisation name") {
    PropF.forAllF(repoGen, nonEmptyGen(userGen)) {
      (repo: Repo, contributors: List[User]) =>
        val routes =
          new ContributorRoutes[IO](
            new ContributorService(
              new GitHubProgram(TestGitHubRepository(Map(repo -> contributors)))
            )
          ).routes(errHandler)

        GET(
          Uri.unsafeFromString(
            s"org/name/contributors"
          )
        ).pure[IO].flatMap { req =>
          assertHttp(routes, req)(
            Status.Ok,
            contributors.sorted
          )
        }
    }
  }

}
