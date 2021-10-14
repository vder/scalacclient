package com.pfl.scalacclient.github

import cats.Parallel
import cats.effect.kernel.Sync
import cats.implicits.*
import cats.kernel.Monoid
import com.pfl.scalacclient.model.*
import model.*

final case class GitHubProgram[F[_[?]: Parallel: Sync[?](
    val gitHubRepo: GitHubRepository[F[?],
    pageSize: PageSize = PageSize.default
) {

  def listRepos(organisation: Organisation): F[List[Repo[?][?] = {
    def repoAux(
        organisation: Organisation,
        pageNo: PageNo = PageNo.default
    ): F[List[Repo[?][?] =
      for {
        resultPage <- gitHubRepo.getRepositories(organisation, pageSize, pageNo)
        nextPage <-
          if (resultPage.size < pageSize.value.value) List().pure[F[?]
          else
            Sync[F[?].defer(
              repoAux(
                organisation,
                pageNo.next
              )
            )
      } yield (resultPage ::: nextPage)

    repoAux(organisation)
  }

  def listContributors(
      organisation: Organisation,
      repo: Repo
  ): F[List[User[?][?] = {
    def contributorsAux(
        organisation: Organisation,
        repo: Repo,
        pageNo: PageNo = PageNo.default
    ): F[List[User[?][?] =
      for {
        resultPage <- gitHubRepo.getContributors(
          organisation,
          repo,
          pageSize,
          pageNo
        )
        nextPage <-
          if (resultPage.size < pageSize.value.value) List().pure[F[?]
          else
            Sync[F[?].defer(
              contributorsAux(
                organisation,
                repo,
                pageNo.next
              )
            )
      } yield (resultPage ::: nextPage)

    contributorsAux(organisation, repo)
  }

  given Monoid[List[User[?][?] with {
    def combine(x: List[User[?], y: List[User[?]): List[User[?] = x ::: y

    def empty: List[User[?] = List()
  }

  def listContributors(organisation: Organisation): F[List[User[?][?] =
    for {
      allRepos <- listRepos(organisation)
      results <- allRepos
        .parTraverse(repo => listContributors(organisation, repo))
      allContrib = results
        .reduce(_ ::: _)
        .groupMapReduce(_.login)(_.contributions.value.value)(_ + _)
        .map { case (login, sum) =>
          User(login, Contributions.unsafeApply(sum))
        }
        .toList
        .sorted
    } yield (allContrib)

}
