package com.example.scalacclient

import org.http4s.client.Client
import cats.effect.kernel.Sync
import eu.timepit.refined.api.Refined
import eu.timepit.refined.numeric._
import org.http4s.Request
import org.http4s.Uri
import cats.implicits._
import org.http4s.Method._
import org.http4s.Headers
import org.http4s.headers.Authorization
import org.http4s.BasicCredentials
import org.http4s.headers.Accept
import org.http4s.MediaType
import org.http4s.circe._
import org.http4s.EntityDecoder
import io.circe.Decoder
import _root_.io.circe.generic.semiauto._
import eu.timepit.refined.types.string
import cats.effect.Concurrent
// import eu.timepit.refined.collection.NonEmpty
// import eu.timepit.refined._
import io.circe.refined._

trait GitHubRepository[F[_]] {
  def getRepositories(
      organisation: Organisation,
      pageNo: Int Refined Positive
  ): F[List[Repo]]
  def getContributors(
      organisation: Organisation,
      repo: Repo,
      pageNo: Int Refined Positive
  ): F[List[User]]
}

final class LiveGitHubRepository[
    F[_]: Concurrent
] private (
    val client: Client[F]
) extends GitHubRepository[F] {

  private val GITHUB_URL: String = "https://api.github.com"

  implicit val LoginDecoder: Decoder[Login] =
    Decoder[string.NonEmptyString].map(Login.apply)

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
      pageNo: Refined[Int, Positive]
  ): F[List[Repo]] =
    for {
      url <- Uri
        .fromString(
          s"""${GITHUB_URL}/orgs/${organisation.value.value}/repos?per_page=100&page=${pageNo.value}"""
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
      pageNo: Refined[Int, Positive]
  ): F[List[User]] = for {
    url <- Uri
      .fromString(
        s"""${GITHUB_URL}/repos/${organisation.value.value}/${repo.value.value}/contributors?per_page=100&page=${pageNo.value}"""
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
