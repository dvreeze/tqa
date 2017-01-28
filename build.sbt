
name := "tqa"

organization := "eu.cdevreeze.tqa"

version := "0.2.0-SNAPSHOT"

scalaVersion := "2.11.8"

crossScalaVersions := Seq("2.11.8", "2.12.1")

// See: Toward a safer Scala
// http://downloads.typesafe.com/website/presentations/ScalaDaysSF2015/Toward%20a%20Safer%20Scala%20@%20Scala%20Days%20SF%202015.pdf

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature", "-Xfatal-warnings", "-Xlint")

(unmanagedSourceDirectories in Compile) <++= (scalaBinaryVersion, baseDirectory) apply { case (version, base) =>
  if (version.contains("2.12")) Seq(base / "src" / "main" / "scala-2.12")
  else if (version.contains("2.11")) Seq(base / "src" / "main" / "scala-2.11") else Seq()
}

(unmanagedSourceDirectories in Test) <++= (scalaBinaryVersion, baseDirectory) apply { case (version, base) =>
  if (version.contains("2.12")) Seq(base / "src" / "test" / "scala-2.12")
  else if (version.contains("2.11")) Seq(base / "src" / "test" / "scala-2.11") else Seq()
}

libraryDependencies += "eu.cdevreeze.yaidom" %% "yaidom" % "1.6.1"

libraryDependencies += "net.sf.saxon" % "Saxon-HE" % "9.7.0-10"

libraryDependencies += "junit" % "junit" % "4.12" % "test"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.0" % "test"

libraryDependencies += "org.scalacheck" %% "scalacheck" % "1.13.4" % "test"

libraryDependencies += ("joda-time" % "joda-time" % "2.9.5" % "test").intransitive()

libraryDependencies += ("org.joda" % "joda-convert" % "1.8.1" % "test").intransitive()

libraryDependencies += "com.google.guava" % "guava" % "20.0" % "test"

libraryDependencies += "com.google.code.findbugs" % "jsr305" % "3.0.1" % "test"


// resolvers += "Artima Maven Repository" at "http://repo.artima.com/releases"

// addCompilerPlugin("com.artima.supersafe" %% "supersafe" % "1.0.3")

publishMavenStyle := true

publishTo <<= version { (v: String) =>
  val nexus = "https://oss.sonatype.org/"
  if (v.trim.endsWith("SNAPSHOT"))
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
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
