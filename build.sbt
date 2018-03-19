
// Building both for JVM and JavaScript runtimes.

// To convince SBT not to publish any root level artifacts, I had a look at how scala-java-time does it.
// See https://github.com/cquiroz/scala-java-time/blob/master/build.sbt as a "template" for this build file.


val scalaVer = "2.12.4"

// I wanted to cross-build for Scala 2.13.0-M2 as well, but then miss library scalajs-jsjoda-as-java-time

val crossScalaVer = Seq(scalaVer, "2.11.12")

lazy val commonSettings = Seq(
  name         := "tqa",
  description  := "Extensible XBRL taxonomy query API",
  organization := "eu.cdevreeze.tqa",
  version      := "0.8.0-SNAPSHOT",

  scalaVersion       := scalaVer,
  crossScalaVersions := crossScalaVer,

  scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature", "-Xfatal-warnings", "-Xlint"),

  publishArtifact in Test := false,
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

  libraryDependencies += "eu.cdevreeze.yaidom" %%% "yaidom" % "1.8.0-M1",

  libraryDependencies += "org.scalactic" %%% "scalactic" % "3.0.4",

  libraryDependencies += "org.scalatest" %%% "scalatest" % "3.0.4" % "test"
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

lazy val tqa = crossProject.crossType(CrossType.Full).in(file("."))
  .settings(commonSettings: _*)
  .jvmSettings(
    // This is the HE release of Saxon. You may want to use the EE release instead.

    libraryDependencies += "net.sf.saxon" % "Saxon-HE" % "9.8.0-8",

    libraryDependencies += "com.google.guava" % "guava" % "22.0",

    libraryDependencies += "com.google.code.findbugs" % "jsr305" % "1.3.9",

    libraryDependencies += "org.scala-lang.modules" %%% "scala-xml" % "1.0.6",

    libraryDependencies += "org.scala-lang.modules" %%% "scala-java8-compat" % "0.8.0" % "optional",

    libraryDependencies += "org.scalameta" %%% "scalameta" % "2.0.1" % "test",

    libraryDependencies += "junit" % "junit" % "4.12" % "test",

    mimaPreviousArtifacts := Set("eu.cdevreeze.tqa" %%% "tqa" % "0.7.0")
  )
  .jsSettings(
    // Do we need this jsEnv?
    jsEnv := new org.scalajs.jsenv.jsdomnodejs.JSDOMNodeJSEnv(),

    libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "0.9.4",

    // We use some JDK 8 java.time classes in shared code ...

    libraryDependencies += "com.zoepepper" %%% "scalajs-jsjoda" % "1.1.1",

    libraryDependencies += "com.zoepepper" %%% "scalajs-jsjoda-as-java-time" % "1.1.1",

    libraryDependencies += "com.lihaoyi" %%% "scalatags" % "0.6.7" % "optional",

    jsDependencies += "org.webjars.npm" % "js-joda" % "1.1.8" / "dist/js-joda.js" minified "dist/js-joda.min.js",

    parallelExecution in Test := false,

    mimaPreviousArtifacts := Set("eu.cdevreeze.tqa" %%% "tqa" % "0.7.0")
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
