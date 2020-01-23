
// Building both for JVM and JavaScript runtimes.

// To convince SBT not to publish any root level artifacts, I had a look at how scala-java-time does it.
// See https://github.com/cquiroz/scala-java-time/blob/master/build.sbt as a "template" for this build file.

// shadow sbt-scalajs' crossProject and CrossType from Scala.js 0.6.x

import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}

// Note that 2.12.5 does not work for Scalatest in sbt (https://github.com/scalatest/scalatest/issues/1342).

val scalaVer = "2.13.1"

val crossScalaVer = Seq(scalaVer, "2.12.10")

lazy val commonSettings = Seq(
  name         := "tqa",
  description  := "Extensible XBRL taxonomy query API",
  organization := "eu.cdevreeze.tqa",
  version      := "0.9.0-SNAPSHOT",

  scalaVersion       := scalaVer,
  crossScalaVersions := crossScalaVer,

  scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature", "-Xfatal-warnings", "-Xlint", "-target:jvm-1.8"),

  Test / publishArtifact := false,
  publishMavenStyle := true,

  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value)
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases"  at nexus + "service/local/staging/deploy/maven2")
    },

  pomExtra := pomData,
  pomIncludeRepository := { _ => false },

  libraryDependencies += "eu.cdevreeze.yaidom" %%% "yaidom" % "1.10.1",

  libraryDependencies += "org.scala-lang.modules" %%% "scala-xml" % "1.2.0",

  libraryDependencies += "org.scala-lang.modules" %%% "scala-collection-compat" % "2.1.3",

  libraryDependencies += "org.scalactic" %%% "scalactic" % "3.1.0",

  libraryDependencies += "org.scalatest" %%% "scalatest" % "3.1.0" % "test"
)

lazy val root = project.in(file("."))
  .aggregate(tqaJVM, tqaJS)
  .settings(commonSettings: _*)
  .settings(
    name                 := "tqa",
    // Thanks, scala-java-time, for showing us how to prevent any publishing of root level artifacts:
    // No, SBT, we don't want any artifacts for root. No, not even an empty jar.
    publish              := {},
    publishLocal         := {},
    publishArtifact      := false,
    Keys.`package`       := file(""))

lazy val tqa = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Full)
  .in(file("."))
  .settings(commonSettings: _*)
  .jvmSettings(
    // This is the HE release of Saxon. You may want to use the EE release instead.

    libraryDependencies += "net.sf.saxon" % "Saxon-HE" % "9.9.1-6",

    libraryDependencies += "com.github.ben-manes.caffeine" % "caffeine" % "2.8.1",

    libraryDependencies += "com.google.code.findbugs" % "jsr305" % "3.0.2",

    libraryDependencies ++= {
      scalaBinaryVersion.value match {
        case "2.13" => Seq()
        case _      => Seq("org.scala-lang.modules" %%% "scala-java8-compat" % "0.9.0")
      }
    },

    libraryDependencies ++= {
      scalaBinaryVersion.value match {
        case "2.13" => Seq()
        case _      => Seq("org.scalameta" %%% "scalameta" % "4.3.0" % "test")
      }
    },

    Compile / unmanagedSourceDirectories += {
      val sourceDir = (Compile / sourceDirectory).value
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, n)) if n >= 13 => sourceDir / "scala-2.13+"
        case _                       => sourceDir / "scala-2.13-"
      }
    },

    Test / unmanagedSourceDirectories += {
      val sourceDir = (Test / sourceDirectory).value
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, n)) if n >= 13 => sourceDir / "scala-2.13+"
        case _                       => sourceDir / "scala-2.13-"
      }
    },

    mimaPreviousArtifacts := Set("eu.cdevreeze.tqa" %%% "tqa" % "0.8.11")
  )
  .jsSettings(    // Do we need this jsEnv?
    jsEnv := new org.scalajs.jsenv.jsdomnodejs.JSDOMNodeJSEnv(),

    libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "0.9.8",

    libraryDependencies += "io.github.cquiroz" %%% "scala-java-time" % "2.0.0-RC3",

    libraryDependencies += "com.lihaoyi" %%% "scalatags" % "0.8.4" % "optional",

    Compile / unmanagedSourceDirectories += {
      val sourceDir = (Compile / sourceDirectory).value
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, n)) if n >= 13 => sourceDir / "scala-2.13+"
        case _                       => sourceDir / "scala-2.13-"
      }
    },

    Test / parallelExecution := false,

    mimaPreviousArtifacts := Set("eu.cdevreeze.tqa" %%% "tqa" % "0.8.11")
  )

lazy val tqaJVM = tqa.jvm
lazy val tqaJS = tqa.js

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
