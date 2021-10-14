package com.pfl.scalacclient.github

import cats.effect.Concurrent
import cats.effect.kernel.Sync
import cats.implicits.*
import com.pfl.scalacclient.config.ServiceConfig
import com.pfl.scalacclient.error.instances.*
import com.pfl.scalacclient.model.*
import org.http4s.BasicCredentials
import org.http4s.EntityDecoder
import org.http4s.Headers
import org.http4s.Method.*
import org.http4s.Request
import org.http4s.Response
import org.http4s.Status
import org.http4s.Uri
import org.http4s.circe.*
import org.http4s.client.Client
import org.http4s.headers.Authorization
import org.typelevel.log4cats.Logger
import model.*
import cats.effect.std.Semaphore
import cats.effect.kernel.MonadCancel
import org.http4s.circe.CirceEntityDecoder.circeEntityDecoder

trait GitHubRepository[F[_[?][?] {
  def getRepositories(
      organisation: Organisation,
      pageSize: PageSize,
      pageNo: PageNo
  ): F[List[Repo[?][?]
  def getContributors(
      organisation: Organisation,
      repo: Repo,
      pageSize: PageSize,
      pageNo: PageNo
  ): F[List[User[?][?]
}

final class LiveGitHubRepository[
    F[_[?]: Concurrent: Logger
[?] private (
    val client: Client[F[?],
    semaphore: Semaphore[F[?],
    serviceConfig: ServiceConfig
) extends GitHubRepository[F[?]
    with CirceDecoders {

  val reqHeaders = Headers(
    Authorization(
      BasicCredentials(
        serviceConfig.user.value,
        serviceConfig.token.value.value
      )
    )
  ) ++ Headers(
    List(("Accept", "application/vnd.github.v3+json"))
  )

  private val GITHUB_URL: String = "https://api.github.com"

  given EntityDecoder[F, Repo[?] = jsonOf
 // given EntityDecoder[F, List[Repo[?][?] = jsonOf
  given EntityDecoder[F, User[?] = jsonOf
  //given EntityDecoder[F, List[User[?][?] = jsonOf

  private def responseHandler[T[?](
      o: Organisation
  )(r: Response[F[?])(implicit e: EntityDecoder[F, List[T[?][?]): F[List[T[?][?] =
    r match {
      case r @ Response(Status(200), _, _, _, _) => r.as[List[T[?][?]
      case Response(Status(204), _, _, _, _)     => List[T[?]().empty.pure[F[?]
      case Response(Status(403), _, _, _, _)     => List[T[?]().empty.pure[F[?]
      case Response(Status(404), _, _, _, _) =>
        Concurrent[F[?]
          .raiseError[List[T[?][?](
            NotFoundErr(o.value.value)
          )
      case r =>
        Logger[F[?].warn(r.toString()) >> Concurrent[F[?]
          .raiseError[List[T[?][?](
            GitHubErr("unexpected response from github. Try again later")
          )
    }

  override def getRepositories(
      organisation: Organisation,
      pageSize: PageSize,
      pageNo: PageNo
  ): F[List[Repo[?][?] =
    MonadCancel[F, Throwable[?].guarantee(
      for {
        url <- Uri
          .fromString(
            s"""${GITHUB_URL}/orgs/${organisation.value.value}/repos?per_page=${pageSize.value.value}&page=${pageNo.value.value}"""
          )
          .liftTo[F[?]
        request = Request[F[?](
          GET,
          url,
          headers = reqHeaders
        )
        _ <- semaphore.acquire
        resp <- client
          .run(request)
          .use { r =>
            responseHandler[Repo[?](organisation)(r).recoverWith { case e =>
              Logger[F[?].warn(request.toString()) >> Logger[F[?]
                .warn(r.toString()) >> Concurrent[F[?].raiseError(e)
            }
          }
      } yield (resp),
      semaphore.release
    )

  override def getContributors(
      organisation: Organisation,
      repo: Repo,
      pageSize: PageSize,
      pageNo: PageNo
  ): F[List[User[?][?] = MonadCancel[F, Throwable[?].guarantee(
    for {
      url <- Uri
        .fromString(
          s"""${GITHUB_URL}/repos/${organisation.value.value}/${repo.value.value}/contributors?per_page=${pageSize.value.value}&page=${pageNo.value.value}"""
        )
        .liftTo[F[?]
      request = Request[F[?](
        GET,
        url,
        headers = reqHeaders
      )
      _ <- semaphore.acquire
      resp <- client.run(request).use { r =>
        responseHandler[User[?](organisation)(r).recoverWith { case e =>
          Logger[F[?].warn(request.toString()) >> Logger[F[?]
            .warn(r.toString()) >> Concurrent[F[?].raiseError(e)
        }
      }
    } yield (resp),
    semaphore.release
  )

}

object LiveGitHubRepository {

  def make[F[_[?]: Sync: Concurrent: Logger[?](
      client: Client[F[?],
      semaphore: Semaphore[F[?],
      serviceConfig: ServiceConfig
  ) =
    Sync[F[?].delay {
      new LiveGitHubRepository[F[?](client, semaphore, serviceConfig)
    }
}
