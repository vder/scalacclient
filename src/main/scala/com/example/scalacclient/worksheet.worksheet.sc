import cats.effect.unsafe.IORuntime
//import io.circe.Json
import java.util.concurrent.Executors
import org.http4s.client.JavaNetClientBuilder
import org.http4s.client.Client
import org.http4s.client.dsl.io._

import io.circe.Decoder
import io.circe.generic.semiauto._

import org.http4s.headers._
import org.http4s.MediaType
import cats.effect._
import org.http4s._
import org.http4s.dsl.io._
//import scala.concurrent.ExecutionContext.global
import org.http4s.circe._
import cats.implicits._
import cats.effect.unsafe.implicits.global

// implicit val cs: ContextShift[IO] = IO.contextShift(global)
// implicit val timer: Timer[IO] = IO.timer(global)
val blockingPool = Executors.newFixedThreadPool(5)

val runtime: IORuntime = cats.effect.unsafe.IORuntime.global

case class Login(value: String)
case class User(login: Login, contributions: Int)

implicit val LoginDecoder: Decoder[Login] =
  Decoder[String].map(Login.apply)

implicit val UserDecoder: Decoder[User] = deriveDecoder[User]

implicit def UserEntityDecoder: EntityDecoder[IO, User] = jsonOf

implicit def UserListEntityDecoder: EntityDecoder[IO, List[User]] = jsonOf

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

val respIO = httpClient.expect[List[User]](request2)

val json = respIO.unsafeRunSync()

json.size

def getContibutors(client: Client[IO], page: Int): IO[List[User]] = {
  val req = Uri
    .fromString(
      s"""https://api.github.com/repos/spring-projects/spring-framework/contributors?page=${page}&per_page=100"""
    )
    .liftTo[IO]
    .map(uri =>
      GET(
        uri,
        Authorization(
          BasicCredentials("vder", "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")
        ),
        Accept(MediaType.application.json)
      )
    )

  for {
    response <- client.expect[List[User]](req)
    continuation <-
      if (response.size < 100) IO.pure(List())
      else IO.defer(getContibutors(client, page + 1))
  } yield response ::: continuation
}

println(getContibutors(httpClient, 1).unsafeRunSync().size)
