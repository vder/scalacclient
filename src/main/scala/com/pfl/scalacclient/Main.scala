package com.pfl.scalacclient

import cats.effect.kernel.Resource
import cats.effect.kernel.Sync
import cats.effect.{IO, IOApp}
import cats.implicits._
import com.pfl.scalacclient.config.ServiceConfig
import com.pfl.scalacclient.error.LiveHttpErrorHandler
import com.pfl.scalacclient.github.GitHubProgram
import com.pfl.scalacclient.github.LiveGitHubRepository
import com.pfl.scalacclient.http.ContributorRoutes
import com.pfl.scalacclient.http.ContributorService
import org.http4s.blaze.client.BlazeClientBuilder
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.client.Client
import org.http4s.implicits._
import org.typelevel.log4cats.slf4j.Slf4jLogger
import scala.concurrent.ExecutionContext.global
import scala.concurrent.duration._
import cats.effect.std.Semaphore
import org.typelevel.log4cats.Logger
object Main extends IOApp.Simple {

  implicit def unsafeLogger[F[_]: Sync]: Logger[F] = Slf4jLogger.getLogger[F]

  val resources: Resource[IO, Client[IO]] =
    BlazeClientBuilder[IO](global)
      .withMaxWaitQueueLimit(2048)
      .withMaxTotalConnections(64)
      .withIdleTimeout(Duration.Inf)
      .withResponseHeaderTimeout(Duration.Inf)
      .resource

  override def run =
    resources.use { case (client) =>
      for {
        routes <- BasicRoutes.make[IO]
        semaphore <- Semaphore[IO](64)
        config <- ServiceConfig.config[IO]
        _ <- Logger[IO].info(config.toString())
        githubRepo <- LiveGitHubRepository.make(client, semaphore, config)
        githubApi = GitHubProgram(githubRepo)
        service <- ContributorService.make(githubApi)
        contributorRoutes <- ContributorRoutes.make(service)
        errHandler = LiveHttpErrorHandler[IO]
        httpApp = (routes.routes <+> contributorRoutes.routes(
          errHandler
        )).orNotFound
        _ <- BlazeServerBuilder[IO](global)
          .bindHttp(
            config.port.value,
            "0.0.0.0"
          )
          .withHttpApp(httpApp)
          .withResponseHeaderTimeout(600.seconds)
          .withIdleTimeout(601.seconds)
          .serve
          .compile
          .drain
      } yield ()

    }
}
