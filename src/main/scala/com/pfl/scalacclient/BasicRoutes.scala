package com.pfl.scalacclient

import cats.effect.Sync
import cats.implicits.*
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import org.http4s.HttpRoutes

final class BasicRoutes[F[_[?]: Sync[?] {

  private[this[?] val prefixPath = "/api/v1/"

  val httpRoutes: HttpRoutes[F[?] = {
    val dsl = new Http4sDsl[F[?] {}
    import dsl.*
    HttpRoutes.of[F[?] { case GET -> Root / "test" =>
      for {
        response <- Ok(s"its alive")
      } yield response

    }
  }

  val routes = Router(
    prefixPath -> httpRoutes
  )
}

object BasicRoutes {
  def make[F[_[?]: Sync[?] =
    Sync[F[?].delay { new BasicRoutes() }
}
