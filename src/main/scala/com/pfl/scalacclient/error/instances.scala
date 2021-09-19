package com.pfl.scalacclient.error

import scala.util.control.NoStackTrace

object instances {

  case object BadRequestErr extends NoStackTrace
  case class NotFoundErr(organisation: String) extends NoStackTrace
  case class GitHubErr(messagge: String) extends NoStackTrace

}
