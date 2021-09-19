package com.pfl.scalacclient.error

import io.circe.generic.semiauto._
import io.circe.{Decoder, Encoder}
import org.http4s.EntityEncoder
import org.http4s.circe._

case class ErrorMessage(code: String, message: String)
object ErrorMessage {

  implicit val errorMessageDecoder: Decoder[ErrorMessage] =
    deriveDecoder[ErrorMessage]
  implicit val errorMessageEncoder: Encoder[ErrorMessage] =
    deriveEncoder[ErrorMessage]

  implicit def errMessageEntityEncoder[F[_]]: EntityEncoder[F, ErrorMessage] =
    jsonEncoderOf[F, ErrorMessage]
}
