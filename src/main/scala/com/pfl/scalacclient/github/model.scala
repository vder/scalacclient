package com.pfl.scalacclient.github

import eu.timepit.refined.api.Refined
import eu.timepit.refined.numeric.Positive
import eu.timepit.refined.refineMV

object model {
  case class PageNo(value: Int Refined Positive) {
    def next = PageNo(
      Refined.unsafeApply[Int, Positive](value.value + 1)
    )
  }
  case class PageSize(value: Int Refined Positive)

  object PageNo {
    val default = PageNo(refineMV[Positive](1))
  }

  object PageSize {
    val default = PageSize(refineMV[Positive](100))
  }
}
