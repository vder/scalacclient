import Dependencies.Libraries._

ThisBuild / scalaVersion := "2.13.16"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "com.pfl"
ThisBuild / organizationName := "pfl"

//Test / parallelExecution := false

lazy val root = (project in file("."))
  .settings(
    name := "scalacclient",
    scalaVersion := "2.13.6",
    libraryDependencies ++= Seq(
      catsEffect,
      circe,
      //    circeDerivation,
      //    circeExtras,
      circeFs2,
      circeParser,
      circeRefined,
      ciris,
      cirisRefined,
      http4sCirce,
      http4sDsl,
      http4sClient,
      http4sServer,
      logback,
      mUnit,
      mUnitCE,
      mUnitScalacheck,
      refined,
      // refinedCats,
      scalaCheckEffect,
      scalaCheckEffectMunit,
      slf4j
    ),
    // https://mvnrepository.com/artifact/org.scalameta/semanticdb-scalac
//libraryDependencies += "org.scalameta" %% "semanticdb-scalac" % "4.4.28"
    // addCompilerPlugin(kindProjector),
    // addCompilerPlugin(betterMonadicFor),
    addCompilerPlugin(
      "org.scalameta" % "semanticdb-scalac" % "4.4.28" cross CrossVersion.full
    ),
    scalacOptions ++= Seq(
      "-deprecation",
      "-encoding",
      "UTF-8",
      "-language:higherKinds",
      "-language:postfixOps",
      "-feature",
      //  "-Xfatal-warnings",
      // "-Ykind-projector",
      "-Yrangepos"
    )
  )
