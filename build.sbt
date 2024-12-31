import scala.language.postfixOps
import scala.sys.process.*

ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "net.davidwiles"

val scala3 = "3.6.2"

val scalatestVersion = "3.2.19"
val scalamockVersion = "6.0.0"

val tagLocal = taskKey[String]("Tag the docker build as 'local'")

def tagLocal(oldTag: String, newTag: String): String = s"docker tag $oldTag $newTag" !!

val testDependencies = Seq(
  "org.scalatest" %% "scalatest" % scalatestVersion % Test,
  "org.scalamock" %% "scalamock" % scalamockVersion % Test,
)

val compileOpts = Seq("-Xfatal-warnings", "-deprecation", "-feature", "-unchecked")

val commonSettings = Seq(
  scalaVersion := scala3,
  libraryDependencies ++= testDependencies,
  scalacOptions ++= compileOpts,
  scalafmtOnCompile := true,
  Test / fork := true,
  Test / parallelExecution := true,
)

val dockerSettings = Seq(
  dockerBaseImage         := "amazoncorretto:21",
  dockerExposedPorts      += 8080,
  Docker / daemonUserUid  := None,
  Docker / daemonUser     := "daemon",
  Docker / daemonGroupGid := None,
  tagLocal                := tagLocal(s"${name.value}:${version.value}", s"${name.value}:local")
)

lazy val common = (crossProject(JSPlatform, JVMPlatform) in file("common"))
  .settings(commonSettings)
  .settings(
    name := "common",
  )

lazy val server = (project in file("server"))
  .settings(commonSettings, dockerSettings)
  .enablePlugins(DockerPlugin)
  .enablePlugins(JavaAppPackaging)
  .settings(
    name := "server",
  )
  .dependsOn(common.jvm)

lazy val client = (project in file("client"))
  .enablePlugins(ScalaJSPlugin)
  .settings(commonSettings)
  .settings(
    name := "client",
  )
  .dependsOn(common.js)

lazy val root = (project in file("."))
  .aggregate(common.js, common.jvm, server, client)
