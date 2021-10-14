package com.pfl.scalacclient.github

import com.pfl.scalacclient.model._
import cats.effect.IO
import model._

final case class TestGitHubRepository(
    data: Map[Repo, List[User]]
) extends GitHubRepository[IO] {

  override def getRepositories(
      organisation: Organisation,
      pageSize: PageSize,
      pageNo: PageNo
  ): IO[List[Repo]] = IO.pure {
    data.keySet.toList
      .sortBy(_.value.value)
      .drop(pageSize.value.value * (pageNo.value.value - 1))
      .take(pageSize.value.value)

  }

  override def getContributors(
      organisation: Organisation,
      repo: Repo,
      pageSize: PageSize,
      pageNo: PageNo
  ): IO[List[User]] = IO {
    data
      .getOrElse(repo, List())
      .sorted
      .drop(pageSize.value.value * (pageNo.value.value - 1))
      .take(pageSize.value.value)

  }
}
