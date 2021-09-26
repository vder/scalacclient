package com.pfl.scalacclient.http

import cats.effect.{Sync, Temporal}
import cats.implicits._
import com.pfl.scalacclient.error.ErrorHandler
import com.pfl.scalacclient.error.instances._
import com.pfl.scalacclient.model._
import eu.timepit.refined._
import eu.timepit.refined.collection.NonEmpty
import org.http4s.EntityEncoder
import org.http4s.HttpRoutes
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import org.typelevel.log4cats.Logger
import org.http4s.server.middleware.Caching
import scala.concurrent.duration.Duration

final class ContributorRoutes[
    F[_]: Logger: Temporal
](
    contributorService: ContributorService[F]
) extends CirceEncoders {

  implicit def encodeUser: EntityEncoder[F, User] = jsonEncoderOf
  implicit def encodeUserList: EntityEncoder[F, List[User]] = jsonEncoderOf

  private[this] val prefixPath = "/org/"

  val dsl = new Http4sDsl[F] {}
  import dsl._

  // val middle=
  val httpRoutes: HttpRoutes[F] =
    HttpRoutes.of { case GET -> Root / organisation / "contributors" =>
      for {
        organisationName <- refineV[NonEmpty](organisation)
          .leftMap(_ => BadRequestErr)
          .liftTo[F]
        _ <- Logger[F].debug(organisationName.toString())
        results <- contributorService.listContributors(organisationName)
        response <- Ok(results)
      } yield response

    }

  def routes(handler: ErrorHandler[F, Throwable]) =
    Caching.publicCache(
      Duration.Inf,
      Router(
        prefixPath -> handler.basicHandle(httpRoutes)
      )
    )
}

object ContributorRoutes {
  def make[F[_]: Sync: Logger: Temporal](
      contributorService: ContributorService[F]
  ) = Sync[F].delay { new ContributorRoutes(contributorService) }
}
