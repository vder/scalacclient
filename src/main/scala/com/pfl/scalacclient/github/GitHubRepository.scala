package com.pfl.scalacclient.github

import cats.effect.Concurrent
import cats.effect.kernel.Sync
import cats.implicits._
import com.pfl.scalacclient.model._
import eu.timepit.refined.api.Refined
import eu.timepit.refined.numeric._
import eu.timepit.refined.types.string
import io.circe.Decoder
import io.circe.generic.semiauto._
import io.circe.refined._
import org.http4s.BasicCredentials
import org.http4s.EntityDecoder
import org.http4s.Headers
import org.http4s.MediaType
import org.http4s.Method._
import org.http4s.Request
import org.http4s.Uri
import org.http4s.circe._
import org.http4s.client.Client
import org.http4s.headers.Accept
import org.http4s.headers.Authorization
import eu.timepit.refined.types.numeric

private[github] trait GitHubRepository[F[_]] {
  def getRepositories(
      organisation: Organisation,
      pageSize: Refined[Int, Positive],
      pageNo: Int Refined Positive
  ): F[List[Repo]]
  def getContributors(
      organisation: Organisation,
      repo: Repo,
      pageSize: Refined[Int, Positive],
      pageNo: Int Refined Positive
  ): F[List[User]]
}

private[github] final class LiveGitHubRepository[
    F[_]: Concurrent
] private (
    val client: Client[F]
) extends GitHubRepository[F] {

  private val GITHUB_URL: String = "https://api.github.com"

  implicit val LoginDecoder: Decoder[Login] =
    Decoder[string.NonEmptyString].map(Login.apply)

  implicit val ContributionsDecoder: Decoder[Contributions] =
    Decoder[numeric.PosInt].map(Contributions.apply)

  implicit val RepoDecoder: Decoder[Repo] =
    Decoder
      .forProduct1("name")(Repo.apply)

  implicit val UserDecoder: Decoder[User] = deriveDecoder[User]

  implicit def RepoEntityDecoder: EntityDecoder[F, Repo] = jsonOf
  implicit def RepoListEntityDecoder: EntityDecoder[F, List[Repo]] = jsonOf
  implicit def UserEntityDecoder: EntityDecoder[F, User] = jsonOf
  implicit def UserListEntityDecoder: EntityDecoder[F, List[User]] = jsonOf

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
        headers = Headers(
          Authorization(
            BasicCredentials("vder", "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")
          ),
          Accept(MediaType.application.json)
        )
      )
      resp <- client.expect[List[Repo]](request)
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
      headers = Headers(
        Authorization(
          BasicCredentials("vder", "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")
        ),
        Accept(MediaType.application.json)
      )
    )
    resp <- client.expect[List[User]](request)
  } yield (resp)
}

object LiveGitHubRepository {

  def make[F[_]: Sync: Concurrent](
      client: Client[F]
  ) =
    Sync[F].delay { new LiveGitHubRepository[F](client) }
}
