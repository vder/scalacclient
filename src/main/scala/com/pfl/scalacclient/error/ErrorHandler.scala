package com.pfl.scalacclient.error

import cats.data.{Kleisli, OptionT}
import cats.{MonadThrow, ApplicativeError}
import org.http4s.dsl.Http4sDsl
import org.http4s.{HttpRoutes, Response}

trait ErrorHandler[F[_], E <: Throwable] {
  def handle(
      otherHandler: PartialFunction[Throwable, F[Response[F]]]
  )(routes: HttpRoutes[F]): HttpRoutes[F]

  def basicHandle(routes: HttpRoutes[F]): HttpRoutes[F] =
    handle(PartialFunction.empty)(routes)
}

object LiveHttpErrorHandler {

  def apply[F[_]: MonadThrow]: ErrorHandler[F, Throwable] =
    new ErrorHandler[F, Throwable] {
      val dsl = new Http4sDsl[F] {}
      import dsl._
      val A: ApplicativeError[F, Throwable] = summon

      val handler: PartialFunction[Throwable, F[Response[F]]] = {
        case instances.BadRequestErr =>
          BadRequest(ErrorMessage("BASIC-001", "Invalid request"))

        case instances.NotFoundErr(id) =>
          NotFound(
            ErrorMessage(
              "BASIC-002",
              s"resource with given id ${id} does not exist"
            )
          )

        case instances.GitHubErr(msg) =>
          ServiceUnavailable(
            ErrorMessage(
              "BASIC-003",
              msg
            )
          )
      }

      override def handle(
          otherHandler: PartialFunction[Throwable, F[Response[F]]]
      )(routes: HttpRoutes[F]): HttpRoutes[F] =
        Kleisli { req =>
          val finalHandler = handler orElse otherHandler
          OptionT {
            A.handleErrorWith(routes.run(req).value)(e =>
              A.map(finalHandler(e))(Option(_))
            )
          }
        }

    }
}
