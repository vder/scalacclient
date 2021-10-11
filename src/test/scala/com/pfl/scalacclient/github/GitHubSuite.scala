package com.pfl.scalacclient.github

import com.pfl.scalacclient.model._
import eu.timepit.refined.numeric.Positive
import eu.timepit.refined.api.Refined
import munit.ScalaCheckEffectSuite
import org.scalacheck.effect.PropF
import scala.util.Random
import com.pfl.scalacclient.generators._
import munit.CatsEffectSuite
import eu.timepit.refined.collection.NonEmpty
import cats.effect.IO
import model._
import eu.timepit.refined.types.numeric.PosInt

class GitHubSuite extends CatsEffectSuite with ScalaCheckEffectSuite {

  val random = new Random

  test("github api concatenates multipaged results for Repo") {
    PropF.forAllF(nonEmptyGen(repoGen)) { (repos: List[Repo]) =>
      val program =
        new GitHubProgram(
          new GitHubRepository[IO]() {

            override def getContributors(
                organisation: Organisation,
                repo: Repo,
                pageSize: PageSize,
                pageNo: PageNo
            ): IO[List[User]] = ???

            override def getRepositories(
                organisation: Organisation,
                pageSize: PageSize,
                pageNo: PageNo
            ): IO[List[Repo]] = IO {
              repos
                .sortBy(_.value.value)
                .drop(pageSize.value.value * (pageNo.value.value - 1))
                .take(pageSize.value.value)
            }

          },
          PageSize(Refined.unsafeApply[Int, Positive](1))
        )
      val organisation =
        Organisation(Refined.unsafeApply[String, NonEmpty]("a"))
      program.listRepos(organisation).map { result =>
        assertEquals(
          result,
          repos.sortBy(_.value.value)
        )
      }
    }
  }

  test("github api concatenates users for several Repos") {
    PropF.forAllF(gitHubRepoGen) { (data: Map[Repo, List[User]]) =>
      val testRepository = new TestGitHubRepository(data)
      val program =
        new GitHubProgram(
          testRepository,
          PageSize(Refined.unsafeApply[Int, Positive](1))
        )

      val expectedResults: List[User] = data.toList
        .flatMap(_._2)
        .groupMapReduce(_.login)(_.contributions.value.value)(_ + _)
        .toList
        .map { case (login, contribInt) =>
          User(login, Contributions.unsafeApply(contribInt))
        }
        .sorted

      val organisation =
        Organisation(Refined.unsafeApply[String, NonEmpty]("a"))
      program.listContributors(organisation).map { result =>
        assertEquals(result, expectedResults)
      }
    }
  }

}
