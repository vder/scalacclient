package com.pfl.scalacclient

import cats.effect.{IO, IOApp}
import org.http4s.blaze.client.BlazeClientBuilder
import org.http4s.blaze.server.BlazeServerBuilder
import cats.implicits._
import org.http4s.implicits._
import scala.concurrent.ExecutionContext.global
import com.pfl.scalacclient.github.LiveGitHubRepository
import com.pfl.scalacclient.github.GitHubProgram
import _root_.eu.timepit.refined.numeric.Positive
import eu.timepit.refined._
import com.pfl.scalacclient.http.ContributorRoutes
import com.pfl.scalacclient.http.ContributorService
import cats.effect.kernel.Sync
import org.typelevel.log4cats.slf4j.Slf4jLogger
object Main extends IOApp.Simple {

  val pageSize = refineMV[Positive](100)

  implicit def unsafeLogger[F[_]: Sync] = Slf4jLogger.getLogger[F]

  override def run =
    BlazeClientBuilder[IO](global).resource.use { case client =>
      for {
        routes <- BasicRoutes.make[IO]
        githubRepo <- LiveGitHubRepository.make(client)
        githubApi = GitHubProgram(githubRepo, pageSize)
        service <- ContributorService.make(githubApi)
        contributorRoutes <- ContributorRoutes.make(service)
        httpApp = (routes.routes <+> contributorRoutes.routes).orNotFound
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
