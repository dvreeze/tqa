
name := "tqa"

organization := "eu.cdevreeze.tqa"

version := "0.4.10"

scalaVersion := "2.12.3"

crossScalaVersions := Seq("2.12.3", "2.11.11")

// See: Toward a safer Scala
// http://downloads.typesafe.com/website/presentations/ScalaDaysSF2015/Toward%20a%20Safer%20Scala%20@%20Scala%20Days%20SF%202015.pdf

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature", "-Xfatal-warnings", "-Xlint")

(unmanagedSourceDirectories in Compile) ++= {
  val vers = scalaBinaryVersion.value
  val base = baseDirectory.value

  if (vers.contains("2.12")) Seq(base / "src" / "main" / "scala-2.12")
  else if (vers.contains("2.11")) Seq(base / "src" / "main" / "scala-2.11") else Seq()
}

(unmanagedSourceDirectories in Test) ++= {
  val vers = scalaBinaryVersion.value
  val base = baseDirectory.value

  if (vers.contains("2.12")) Seq(base / "src" / "test" / "scala-2.12")
  else if (vers.contains("2.11")) Seq(base / "src" / "test" / "scala-2.11") else Seq()
}

libraryDependencies += "eu.cdevreeze.yaidom" %% "yaidom" % "1.6.4"

libraryDependencies += "org.scalactic" %% "scalactic" % "3.0.3"

libraryDependencies += "net.sf.saxon" % "Saxon-HE" % "9.7.0-18"

libraryDependencies += "com.google.guava" % "guava" % "22.0"

libraryDependencies += "com.google.code.findbugs" % "jsr305" % "1.3.9"

libraryDependencies += "junit" % "junit" % "4.12" % "test"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.3" % "test"

libraryDependencies += "org.scalacheck" %% "scalacheck" % "1.13.5" % "test"

libraryDependencies += ("joda-time" % "joda-time" % "2.9.9" % "test").intransitive()

libraryDependencies += ("org.joda" % "joda-convert" % "1.8.1" % "test").intransitive()


// resolvers += "Artima Maven Repository" at "http://repo.artima.com/releases"

// addCompilerPlugin("com.artima.supersafe" %% "supersafe" % "1.0.3")

publishMavenStyle := true

publishTo := {
  val vers = version.value

  val nexus = "https://oss.sonatype.org/"

  if (vers.trim.endsWith("SNAPSHOT")) {
    Some("snapshots" at nexus + "content/repositories/snapshots")
  } else {
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
  }
}

publishArtifact in Test := false

pomIncludeRepository := { repo => false }

pomExtra := {
  <url>https://github.com/dvreeze/tqa</url>
  <licenses>
    <license>
      <name>Apache License, Version 2.0</name>
      <url>http://www.apache.org/licenses/LICENSE-2.0.txt</url>
      <distribution>repo</distribution>
      <comments>Yaidom is licensed under Apache License, Version 2.0</comments>
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
}
