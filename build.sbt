import scala.language.postfixOps
import scala.sys.process.*

ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "net.davidwiles"

val tagLocal = taskKey[String]("Tag the docker build as 'local'")
def tagLocal(oldTag: String, newTag: String): String = s"docker tag $oldTag $newTag" !!

val scala3 = "3.6.2"

val scalatestVersion = "3.2.19"
val scalamockVersion = "6.0.0"
val tapirVersion = "1.11.11"
val jBcryptVersion = "0.4"
val jjwtVersion = "0.12.2"
val logbackVersion = "1.5.12"
val scalaLoggingVersion = "3.9.4"
val configVersion = "1.4.3"
val scalikejdbcVersion = "4.0.0"
val hikariCPVersion = "5.0.1"
val postgresVersion = "42.7.2"
val pekkoVersion = "1.1.2"
val catsVersion = "2.13.0"

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
    name := "flashcards-api",
    libraryDependencies ++=
      Seq(
        "com.softwaremill.sttp.tapir" %% "tapir-core"               % tapirVersion,
        "com.softwaremill.sttp.tapir" %% "tapir-pekko-http-server"  % tapirVersion,
        "org.apache.pekko"            %% "pekko-actor-typed"        % pekkoVersion,
        "com.softwaremill.sttp.tapir" %% "tapir-json-circe"         % tapirVersion,
        "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle"  % tapirVersion,
        "com.softwaremill.sttp.tapir" %% "tapir-prometheus-metrics" % tapirVersion,
        "ch.qos.logback"              %  "logback-classic"          % logbackVersion,
        "com.typesafe.scala-logging"  %% "scala-logging"            % scalaLoggingVersion,
        "com.typesafe"                %  "config"                   % configVersion,
        "org.mindrot"                 %  "jbcrypt"                  % jBcryptVersion,
        "io.jsonwebtoken"             %  "jjwt"                     % jjwtVersion,
        "org.scalikejdbc"             %% "scalikejdbc"              % scalikejdbcVersion,
        "org.scalikejdbc"             %% "scalikejdbc-config"       % scalikejdbcVersion,
        "com.zaxxer"                  %  "HikariCP"                 % hikariCPVersion,
        "org.postgresql"              %  "postgresql"               % postgresVersion,
        "org.typelevel"               %% "cats-core"                % catsVersion,
      ),
  )
  .dependsOn(common.jvm)

lazy val client = (project in file("client"))
  .enablePlugins(ScalaJSPlugin)
  .settings(commonSettings)
  .settings(
    name := "flashcards-client",
  )
  .dependsOn(common.js)

lazy val root = (project in file("."))
  .aggregate(common.js, common.jvm, server, client)
