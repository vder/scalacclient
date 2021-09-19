package com.pfl.scalacclient

import cats.effect.{IO, IOApp}
import org.http4s.blaze.client.BlazeClientBuilder
import org.http4s.blaze.server.BlazeServerBuilder
import cats.implicits._
import org.http4s.implicits._
import pureconfig.module.catseffect.syntax._
import scala.concurrent.ExecutionContext.global
import com.pfl.scalacclient.github.LiveGitHubRepository
import com.pfl.scalacclient.github.GitHubProgram
import _root_.eu.timepit.refined.numeric.Positive
import eu.timepit.refined._
import com.pfl.scalacclient.http.ContributorRoutes
import com.pfl.scalacclient.http.ContributorService
import cats.effect.kernel.Sync
import org.typelevel.log4cats.slf4j.Slf4jLogger
import _root_.pureconfig.ConfigSource
import cats.effect.kernel.Resource
import org.http4s.client.Client
import com.pfl.scalacclient.config.ServiceConfig
import com.pfl.scalacclient.error.LiveHttpErrorHandler
object Main extends IOApp.Simple {

  val pageSize = refineMV[Positive](100)

  implicit def unsafeLogger[F[_]: Sync] = Slf4jLogger.getLogger[F]

  val resources: Resource[IO, (Client[IO], ServiceConfig)] =
    for {
      client <- BlazeClientBuilder[IO](global).resource
      serviceConfig <- Resource.eval(
        ConfigSource.default.at("service").loadF[IO, ServiceConfig]()
      )
    } yield (client, serviceConfig)

  override def run =
    resources.use { case (client, config) =>
      for {
        routes <- BasicRoutes.make[IO]
        githubRepo <- LiveGitHubRepository.make(client, config)
        githubApi = GitHubProgram(githubRepo, pageSize)
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
          .serve
          .compile
          .drain
      } yield ()

    }
}
