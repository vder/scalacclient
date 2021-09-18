package com.pfl.scalacclient.github

import cats.Parallel
import cats.effect.kernel.Sync
import cats.implicits._
import cats.kernel.Monoid
import com.pfl.scalacclient.model._
import eu.timepit.refined.api.Refined
import eu.timepit.refined.numeric.Positive
import eu.timepit.refined.refineMV
import eu.timepit.refined.types.numeric

final case class GitHubProgram[F[_]: Parallel: Sync](
    val gitHubRepo: GitHubRepository[F],
    pageSize: numeric.PosInt = refineMV[Positive](100)
) {

  def listRepos(organisation: Organisation): F[List[Repo]] = {
    def repoAux(
        organisation: Organisation,
        pageNo: Int Refined Positive = refineMV(1)
    ): F[List[Repo]] =
      for {
        resultPage <- gitHubRepo.getRepositories(organisation, pageSize, pageNo)
        nextPage <-
          if (resultPage.size < pageSize.value) List().pure[F]
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
        resultPage <- gitHubRepo.getContributors(
          organisation,
          repo,
          pageSize,
          pageNo
        )
        nextPage <-
          if (resultPage.size < pageSize.value) List().pure[F]
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
        .groupMapReduce(_.login)(_.contributions.value.value)(_ + _)
        .map { case (login, sum) =>
          User(login, Contributions(Refined.unsafeApply[Int, Positive](sum)))
        }
        .toList
        .sortWith(_.contributions.value.value > _.contributions.value.value)
    } yield (allContrib)
  }

}
