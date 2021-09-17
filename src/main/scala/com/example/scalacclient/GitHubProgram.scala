package com.example.scalacclient

import eu.timepit.refined.refineMV
import eu.timepit.refined.numeric.Positive
import cats.implicits._
import eu.timepit.refined.api.Refined
import cats.Parallel
import cats.kernel.Monoid
import cats.effect.kernel.Sync

final class GitHubProgram[F[_]: Parallel: Sync](
    val gitHubRepo: GitHubRepository[F]
) {
  def listRepos(organisation: Organisation): F[List[Repo]] = {
    def repoAux(
        organisation: Organisation,
        pageNo: Int Refined Positive = refineMV(1)
    ): F[List[Repo]] =
      for {
        resultPage <- gitHubRepo.getRepositories(organisation, pageNo)
        nextPage <-
          if (resultPage.size < 100) List().pure[F]
          else
            Sync[F].defer(
              repoAux(
                organisation,
                Refined.unsafeApply[Int, Positive](pageNo.value + 1)
              )
            )
      } yield (resultPage ::: nextPage)

    repoAux(organisation)
  }

  def listContributors(
      organisation: Organisation,
      repo: Repo
  ): F[List[User]] = {
    def contributorsAux(
        organisation: Organisation,
        repo: Repo,
        pageNo: Int Refined Positive = refineMV(1)
    ): F[List[User]] =
      for {
        resultPage <- gitHubRepo.getContributors(organisation, repo, pageNo)
        nextPage <-
          if (resultPage.size < 100) List().pure[F]
          else
            Sync[F].defer(
              contributorsAux(
                organisation,
                repo,
                Refined.unsafeApply[Int, Positive](pageNo.value + 1)
              )
            )
      } yield (resultPage ::: nextPage)

    contributorsAux(organisation, repo)
  }

  implicit val userListMonoid: Monoid[List[User]] = new Monoid[List[User]] {
    def combine(x: List[User], y: List[User]): List[User] = x ::: y

    def empty: List[User] = List()
  }

  def listContributors(organisation: Organisation): F[List[User]] = {

    for {
      allRepos <- listRepos(organisation)
      results <- allRepos
        .parTraverse(repo => listContributors(organisation, repo))
      allContrib = results
        .reduce(_ ::: _)
        .groupMapReduce(_.login)(_.contributions.value)(_ + _)
        .map { case (login, sum) =>
          User(login, Refined.unsafeApply[Int, Positive](sum))
        }
        .toList
        .sortWith(_.contributions.value > _.contributions.value)
    } yield (allContrib)
  }

}
