package com.pfl.scalacclient.github

import com.pfl.scalacclient.model._
import eu.timepit.refined.numeric
import eu.timepit.refined.api.Refined
import cats.effect.IO

final case class TestGitHubRepository(
    data: Map[Repo, List[User]]
) extends GitHubRepository[IO] {

  override def getRepositories(
      organisation: Organisation,
      pageSize: Refined[Int, numeric.Positive],
      pageNo: Refined[Int, numeric.Positive]
  ): IO[List[Repo]] = IO.pure {
    data.keySet.toList
      .sortBy(_.value.value)
      .drop(pageSize.value * (pageNo.value - 1))
      .take(pageSize.value)

  }

  override def getContributors(
      organisation: Organisation,
      repo: Repo,
      pageSize: Refined[Int, numeric.Positive],
      pageNo: Refined[Int, numeric.Positive]
  ): IO[List[User]] = IO {
    data
      .getOrElse(repo, List())
      .sortWith(_.contributions.value.value > _.contributions.value.value)
      .drop(pageSize.value * (pageNo.value - 1))
      .take(pageSize.value)

  }
}
