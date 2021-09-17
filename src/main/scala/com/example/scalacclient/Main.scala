package com.example.scalacclient

import cats.effect.{IO, IOApp}
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.implicits._
import scala.concurrent.ExecutionContext.global
import org.http4s.blaze.client.BlazeClientBuilder
object Main extends IOApp.Simple {

  override def run =
    BlazeClientBuilder[IO](global).resource.use { case _ =>
      for {
        routes <- BasicRoutes.make[IO]
        httpApp = routes.routes.orNotFound
        _ <- BlazeServerBuilder[IO](global)
          .bindHttp(
            9999,
            "0.0.0.0"
          )
          .withHttpApp(httpApp)
          .serve
          .compile
          .drain
      } yield ()

    }
}
