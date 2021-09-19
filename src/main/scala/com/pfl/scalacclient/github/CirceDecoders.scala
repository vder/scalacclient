package com.pfl.scalacclient.github

import io.circe.Decoder
import com.pfl.scalacclient.model._
import eu.timepit.refined.types.string.NonEmptyString
import eu.timepit.refined.types.numeric.PosInt
import io.circe.generic.semiauto._
import io.circe.refined._

trait CirceDecoders {

  implicit val LoginDecoder: Decoder[Login] =
    Decoder[NonEmptyString].map(Login.apply)

  implicit val ContributionsDecoder: Decoder[Contributions] =
    Decoder[PosInt].map(Contributions.apply)

  implicit val RepoDecoder: Decoder[Repo] =
    Decoder
      .forProduct1("name")(Repo.apply)

  implicit val UserDecoder: Decoder[User] = deriveDecoder[User]
}
