package com.pfl.scalacclient.error

import io.circe.generic.semiauto.*
import io.circe.{Decoder, Encoder}
import org.http4s.EntityEncoder
import org.http4s.circe.*

case class ErrorMessage(code: String, message: String)
object ErrorMessage {

  given Decoder[ErrorMessage[?] =
    deriveDecoder[ErrorMessage[?]
  given Encoder[ErrorMessage[?] =
    deriveEncoder[ErrorMessage[?]

  given errMessageEntityEncoder[F[_[?][?]: EntityEncoder[F, ErrorMessage[?] =
    jsonEncoderOf[F, ErrorMessage[?]
}
