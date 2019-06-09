import com.typesafe.sbt.SbtMultiJvm.multiJvmSettings
import com.typesafe.sbt.SbtMultiJvm.MultiJvmKeys.MultiJvm

val akkaVersion = "2.5.22"

lazy val root = (project in file(".")).
settings (
  name := "GroupService",
  version := "1.0",
  scalaVersion := "2.12.8",
  scalacOptions in ThisBuild ++= Seq("-unchecked", "-deprecation", "-feature", "-Xlog-reflective-calls", "-Xlint"),
  javacOptions in Compile ++= Seq("-Xlint:unchecked", "-Xlint:deprecation"),
  javaOptions in run ++= Seq("-Xms128m", "-Xmx1024m", "-Djava.library.path=./target/native"),
  resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
  libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor" % akkaVersion,
      "com.typesafe.akka" %% "akka-remote" % akkaVersion,
      "com.typesafe.akka" %% "akka-cluster" % akkaVersion,
      "com.typesafe.akka" %% "akka-cluster-metrics" % akkaVersion,
      "com.typesafe.akka" %% "akka-cluster-tools" % akkaVersion,
      "com.typesafe.akka" %% "akka-multi-node-testkit" % akkaVersion,
      "org.scalatest" %% "scalatest" % "3.0.7" % Test,
      "io.kamon" % "sigar-loader" % "1.6.6-rev002"),
  fork in run := true,
  parallelExecution in Test := false,
  licenses := Seq(("CC0", url("http://creativecommons.org/publicdomain/zero/1.0")))
)
.configs (MultiJvm)

