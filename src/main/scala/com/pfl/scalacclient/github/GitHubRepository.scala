package com.pfl.scalacclient.github

import cats.effect.Concurrent
import cats.effect.kernel.Sync
import cats.implicits._
import com.pfl.scalacclient.config.ServiceConfig
import com.pfl.scalacclient.error.instances._
import com.pfl.scalacclient.model._
import eu.timepit.refined.api.Refined
import eu.timepit.refined.numeric._
import org.http4s.BasicCredentials
import org.http4s.EntityDecoder
import org.http4s.Headers
import org.http4s.MediaType
import org.http4s.Method._
import org.http4s.Request
import org.http4s.Response
import org.http4s.Status
import org.http4s.Uri
import org.http4s.circe._
import org.http4s.client.Client
import org.http4s.headers.Accept
import org.http4s.headers.Authorization
import org.typelevel.log4cats.Logger

private[github] trait GitHubRepository[F[_]] {
  def getRepositories(
      organisation: Organisation,
      pageSize: Refined[Int, Positive],
      pageNo: Refined[Int, Positive]
  ): F[List[Repo]]
  def getContributors(
      organisation: Organisation,
      repo: Repo,
      pageSize: Refined[Int, Positive],
      pageNo: Refined[Int, Positive]
  ): F[List[User]]
}

private[github] final class LiveGitHubRepository[
    F[_]: Concurrent: Logger
] private (
    val client: Client[F],
    serviceConfig: ServiceConfig
) extends GitHubRepository[F]
    with CirceDecoders {

  val reqHeaders = Headers(
    Authorization(
      BasicCredentials(serviceConfig.user.value, serviceConfig.token.value)
    ),
    Accept(MediaType.application.json)
  )

  private val GITHUB_URL: String = "https://api.github.com"

  implicit def RepoEntityDecoder: EntityDecoder[F, Repo] = jsonOf
  implicit def RepoListEntityDecoder: EntityDecoder[F, List[Repo]] = jsonOf
  implicit def UserEntityDecoder: EntityDecoder[F, User] = jsonOf
  implicit def UserListEntityDecoder: EntityDecoder[F, List[User]] = jsonOf

  private def responseHandler[T](
      o: Organisation
  )(r: Response[F])(implicit e: EntityDecoder[F, List[T]]): F[List[T]] =
    r match {
      case r @ Response(Status(200), _, _, _, _) => r.as[List[T]]
      case Response(Status(204), _, _, _, _)     => List[T]().empty.pure[F]
      case Response(Status(404), _, _, _, _) =>
        Concurrent[F]
          .raiseError[List[T]](
            NotFoundErr(o.value.value)
          )
      case r =>
        Logger[F].warn(r.toString()) >> Concurrent[F]
          .raiseError[List[T]](
            GitHubErr("unexpected response from github. Try again later")
          )
    }

  override def getRepositories(
      organisation: Organisation,
      pageSize: Refined[Int, Positive],
      pageNo: Refined[Int, Positive]
  ): F[List[Repo]] =
    for {
      url <- Uri
        .fromString(
          s"""${GITHUB_URL}/orgs/${organisation.value.value}/repos?per_page=${pageSize.value}&page=${pageNo.value}"""
        )
        .liftTo[F]
      request = Request[F](
        GET,
        url,
        headers = reqHeaders
      )
      resp <- client.run(request).use(responseHandler[Repo](organisation)(_))
    } yield (resp)

  override def getContributors(
      organisation: Organisation,
      repo: Repo,
      pageSize: Refined[Int, Positive],
      pageNo: Refined[Int, Positive]
  ): F[List[User]] = for {
    url <- Uri
      .fromString(
        s"""${GITHUB_URL}/repos/${organisation.value.value}/${repo.value.value}/contributors?per_page=${pageSize.value}&page=${pageNo.value}"""
      )
      .liftTo[F]
    request = Request[F](
      GET,
      url,
      headers = reqHeaders
    )
    resp <- client.run(request).use { responseHandler[User](organisation)(_) }
  } yield (resp)

}

object LiveGitHubRepository {

  def make[F[_]: Sync: Concurrent: Logger](
      client: Client[F],
      serviceConfig: ServiceConfig
  ) =
    Sync[F].delay { new LiveGitHubRepository[F](client, serviceConfig) }
}
