package com.pfl.scalacclient.github

import eu.timepit.refined.api.Refined
import eu.timepit.refined.auto._
//import eu.timepit.refined._

import eu.timepit.refined.numeric._
object model {
  case class PageNo(value: Int Refined Positive) {
    def next = PageNo(
      Refined.unsafeApply[Int, Positive](value.value + 1)
    )
  }
  case class PageSize(value: Int Refined Positive)

  object PageNo {

    val default = PageNo(Refined.unsafeApply(1))
  }

  object PageSize {
    val default = PageSize(Refined.unsafeApply(100))
  }
}
