
// Building both for JVM and JavaScript runtimes.

// To convince SBT not to publish any root level artifacts, I had a look at how scala-java-time does it.
// See https://github.com/cquiroz/scala-java-time/blob/master/build.sbt as a "template" for this build file.

// shadow sbt-scalajs' crossProject and CrossType from Scala.js 0.6.x

import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}

// Note that 2.12.5 does not work for Scalatest in sbt (https://github.com/scalatest/scalatest/issues/1342).

val scalaVer = "3.0.0"
val crossScalaVer = Seq(scalaVer, "2.13.6")

ThisBuild / description  := "Extensible XBRL taxonomy query API"
ThisBuild / organization := "eu.cdevreeze.tqa"
ThisBuild / version      := "0.11.0-SNAPSHOT"

ThisBuild / scalaVersion       := scalaVer
ThisBuild / crossScalaVersions := crossScalaVer

ThisBuild / scalacOptions ++= (CrossVersion.partialVersion(scalaVersion.value) match {
  case (Some((3, _))) =>
    Seq("-unchecked", "-source:3.0-migration")
  case _ =>
    Seq("-Wconf:cat=unused-imports:w,cat=unchecked:w,cat=deprecation:w,cat=feature:w,cat=lint:w", "-Ytasty-reader", "-Xsource:3")
})

ThisBuild / Test / publishArtifact := false
ThisBuild / publishMavenStyle := true

ThisBuild / publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
  }

ThisBuild / pomExtra := pomData
ThisBuild / pomIncludeRepository := { _ => false }

ThisBuild / libraryDependencies += "eu.cdevreeze.yaidom" %%% "yaidom" % "1.12.0"

ThisBuild / libraryDependencies += "org.scala-lang.modules" %%% "scala-xml" % "2.0.0"

ThisBuild / libraryDependencies += "org.scalactic" %%% "scalactic" % "3.2.9"

ThisBuild / libraryDependencies += "org.scalatest" %%% "scalatest" % "3.2.9" % "test"

lazy val root = project.in(file("."))
  .aggregate(tqaJVM /*, tqaJS */)
  .settings(
    name                 := "tqa",
    // Thanks, scala-java-time, for showing us how to prevent any publishing of root level artifacts:
    // No, SBT, we don't want any artifacts for root. No, not even an empty jar.
    publish              := {},
    publishLocal         := {},
    publishArtifact      := false,
    Keys.`package`       := file(""))

lazy val tqa = crossProject(/* JSPlatform, */ JVMPlatform)
  .crossType(CrossType.Full)
  .in(file("."))
  .jvmSettings(
    // This is the HE release of Saxon. You may want to use the EE release instead.

    libraryDependencies += "net.sf.saxon" % "Saxon-HE" % "9.9.1-8",

    libraryDependencies += "com.github.ben-manes.caffeine" % "caffeine" % "2.9.0",

    libraryDependencies += "com.google.code.findbugs" % "jsr305" % "3.0.2", // Why needed?

    libraryDependencies += "org.scala-lang.modules" %%% "scala-parallel-collections" % "1.0.3",

    mimaPreviousArtifacts := Set("eu.cdevreeze.tqa" %%% "tqa" % "0.10.0")
  )
/*
  .jsSettings(    // Do we need this jsEnv?
    jsEnv := new org.scalajs.jsenv.jsdomnodejs.JSDOMNodeJSEnv(),

    // Hopefully for3Use2_13 soon not needed anymore
    libraryDependencies += ("org.scala-js" %%% "scalajs-dom" % "1.1.0").cross(CrossVersion.for3Use2_13),

    libraryDependencies += "io.github.cquiroz" %%% "scala-java-time" % "2.3.0",

    // Hopefully for3Use2_13 soon not needed anymore
    libraryDependencies += ("com.lihaoyi" %%% "scalatags" % "0.9.4" % Optional).cross(CrossVersion.for3Use2_13),

    Test / parallelExecution := false,

    mimaPreviousArtifacts := Set("eu.cdevreeze.tqa" %%% "tqa" % "0.10.0")
  )
*/

lazy val tqaJVM = tqa.jvm
// lazy val tqaJS = tqa.js

lazy val pomData =
  <url>https://github.com/dvreeze/tqa</url>
  <licenses>
    <license>
      <name>Apache License, Version 2.0</name>
      <url>http://www.apache.org/licenses/LICENSE-2.0.txt</url>
      <distribution>repo</distribution>
      <comments>TQA is licensed under Apache License, Version 2.0</comments>
    </license>
  </licenses>
  <scm>
    <connection>scm:git:git@github.com:dvreeze/tqa.git</connection>
    <url>https://github.com/dvreeze/tqa.git</url>
    <developerConnection>scm:git:git@github.com:dvreeze/tqa.git</developerConnection>
  </scm>
  <developers>
    <developer>
      <id>dvreeze</id>
      <name>Chris de Vreeze</name>
      <email>chris.de.vreeze@caiway.net</email>
    </developer>
  </developers>
