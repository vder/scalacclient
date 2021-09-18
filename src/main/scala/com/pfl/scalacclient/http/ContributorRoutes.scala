package com.pfl.scalacclient.http

import cats.effect.Sync
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import org.http4s.HttpRoutes
import org.http4s.circe._
import cats.implicits._
import eu.timepit.refined._
import eu.timepit.refined.collection.NonEmpty
import cats.MonadError
import org.http4s.EntityEncoder
import com.pfl.scalacclient.model._
import io.circe.generic.semiauto._
import io.circe.Encoder
import org.typelevel.log4cats.Logger

final class ContributorRoutes[F[_]: MonadError[*[_], Throwable]: Logger](
    contributorService: ContributorService[F]
) {

  implicit val UserDecoder: Encoder[User] =
    deriveEncoder[User]
  implicit val loginDecoder: Encoder[Login] =
    Encoder[String].contramap(_.value.value)

  implicit val contribDecoder: Encoder[Contributions] =
    Encoder[Int].contramap(_.value.value)

  implicit def encodeUser: EntityEncoder[F, User] = jsonEncoderOf
  implicit def encodeUserList: EntityEncoder[F, List[User]] = jsonEncoderOf

  private[this] val prefixPath = "/org/"

  val dsl = new Http4sDsl[F] {}
  import dsl._

  val httpRoutes: HttpRoutes[F] =
    HttpRoutes.of { case GET -> Root / organisation / "contributors" =>
      for {
        organisationName <- refineV[NonEmpty](organisation)
          .leftMap(
            new RuntimeException(_)
          )
          .liftTo[F]
        _ <- Logger[F].info(organisationName.toString())
        results <- contributorService.listContributors(organisationName)
        response <- Ok(results)
      } yield response

    }

  def routes = Router(prefixPath -> httpRoutes)
}

object ContributorRoutes {
  def make[F[_]: Sync: Logger](
      contributorService: ContributorService[F]
  ) = Sync[F].delay { new ContributorRoutes(contributorService) }
}
