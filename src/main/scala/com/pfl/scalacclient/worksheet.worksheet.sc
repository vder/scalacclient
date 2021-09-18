//import io.circe.Decoder
//import io.circe.Json
//import scala.concurrent.ExecutionContext.global

import _root_.eu.timepit.refined.api.Refined
import _root_.eu.timepit.refined.numeric.Positive
//import _root_.io.circe.refined._
import _root_.org.http4s.MediaType
import _root_.org.http4s._
import _root_.org.http4s.client.Client
import _root_.org.http4s.client.JavaNetClientBuilder
import _root_.org.http4s.client.dsl.io._
import _root_.org.http4s.dsl.io._
import _root_.org.http4s.headers._
import cats.effect._
import cats.effect.unsafe.IORuntime
import cats.effect.unsafe.implicits.global
//import _root_.io.circe.refined._
import com.pfl.scalacclient._
import com.pfl.scalacclient.github._
import com.pfl.scalacclient.model._
import eu.timepit.refined._
import eu.timepit.refined.auto._
import eu.timepit.refined.collection.NonEmpty
import java.util.concurrent.Executors

// implicit val cs: ContextShift[IO] = IO.contextShift(global)
// implicit val timer: Timer[IO] = IO.timer(global)
val blockingPool = Executors.newFixedThreadPool(5)

val runtime: IORuntime = cats.effect.unsafe.IORuntime.global

val request2 = GET(
  Uri.unsafeFromString(
    """https://api.github.com/repos/http4s/http4s/contributors?page=4&per_page=100"""
  ),
  Authorization(
    BasicCredentials("vder", "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")
  ),
  Accept(MediaType.application.json)
)

val httpClient: Client[IO] = JavaNetClientBuilder[IO].create

val repo = LiveGitHubRepository.make[IO](httpClient)

val org = Organisation(refineMV[NonEmpty]("http4s"))
val pageno = refineMV[Positive](1)

val repoIO = for {
  r <- repo
  resp <- r.getRepositories(
    Organisation(refineMV[NonEmpty]("http4s")),
    refineMV[Positive](1)
  )
} yield resp

val contribIO = for {
  r <- repo
  resp <- r.getContributors(
    Organisation(refineMV[NonEmpty]("http4s")),
    Repo(refineMV[NonEmpty]("http4s")),
    refineMV[Positive](1)
  )
} yield resp
// implicit val RepoDecoder: Decoder[Repo] =
//   Decoder
//     .forProduct1("name")(Repo.apply)

// implicit def RepoEntityDecoder: EntityDecoder[IO, Repo] = jsonOf

// implicit def RepoListEntityDecoder: EntityDecoder[IO, List[Repo]] = jsonOf

repoIO.unsafeRunSync()

contribIO.unsafeRunSync()

val allContrib = for {
  repo <- LiveGitHubRepository.make[IO](httpClient)
  github = new GitHubProgram(repo)
  allRepos <- github.listRepos(Organisation(refineMV[NonEmpty]("http4s")))
  allContributors <- github.listContributors(
    Organisation(refineMV[NonEmpty]("http4s"))
    //,Repo(refineMV[NonEmpty]("http4s"))
  )
} yield (
  allContributors,
  allRepos.size
)

val c = allContrib.map(_._1).unsafeRunSync()
