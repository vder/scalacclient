package com.pfl.scalacclient.github

import com.pfl.scalacclient.model._
import eu.timepit.refined.numeric.Positive
import eu.timepit.refined.refineMV
import munit.ScalaCheckEffectSuite
import org.scalacheck.effect.PropF
import scala.util.Random
import com.pfl.scalacclient.generators._
import munit.CatsEffectSuite
import eu.timepit.refined.collection.NonEmpty
import cats.effect.IO
import eu.timepit.refined.api.Refined

class GitHubSuite extends CatsEffectSuite with ScalaCheckEffectSuite {

  val random = new Random

  test("github api concatenates multipaged results for Repo") {
    PropF.forAllF(nonEmptyGen(repoGen)) { (repos: List[Repo]) =>
      val program =
        new GitHubProgram(
          new GitHubRepository[IO]() {

            override def getContributors(
                organisation: Organisation,
                repo: Repo,
                pageSize: Refined[Int, Positive],
                pageNo: Refined[Int, Positive]
            ): IO[List[User]] = ???

            override def getRepositories(
                organisation: Organisation,
                pageSize: Refined[Int, Positive],
                pageNo: Int Refined Positive
            ): IO[List[Repo]] = IO {
              repos
                .sortBy(_.value.value)
                .drop(pageSize.value * (pageNo.value - 1))
                .take(pageSize.value)
            }

          },
          refineMV[Positive](1)
        )
      val organisation = Organisation(refineMV[NonEmpty]("a"))
      program.listRepos(organisation).map { result =>
        assertEquals(
          result,
          repos.sortBy(_.value.value)
        )
      }
    }
  }

  test("github api concatenates users for several Repos") {
    PropF.forAllF(gitHubRepoGen) { (data: Map[Repo, List[User]]) =>
      val testRepository = new TestGitHubRepository(data)
      val program =
        new GitHubProgram(
          testRepository,
          refineMV[Positive](1)
        )

      val expectedResults: List[User] = data.toList
        .flatMap(_._2)
        .groupMapReduce(_.login)(_.contributions.value.value)(_ + _)
        .toList
        .map { case (login, contribInt) =>
          User(login, Contributions(Refined.unsafeApply(contribInt)))
        }
        .sorted

      val organisation = Organisation(refineMV[NonEmpty]("a"))
      program.listContributors(organisation).map { result =>
        assertEquals(result, expectedResults)
      }
    }
  }

}
