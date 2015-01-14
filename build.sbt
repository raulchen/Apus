import com.typesafe.sbt.SbtMultiJvm
import com.typesafe.sbt.SbtMultiJvm.MultiJvmKeys.MultiJvm

import AssemblyKeys._

assemblySettings

jarName in assembly := "apus.jar"


val akkaVersion = "2.3.6"

val dependencies = Seq(
  //========= Scala
  "com.typesafe.akka" %% "akka-cluster" % akkaVersion,
  "com.typesafe.akka" %% "akka-contrib" % akkaVersion,
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
  "org.scala-lang.modules" %% "scala-xml" % "1.0.2",
  "com.github.kxbmap" %% "configs" % "0.2.2",
  //========= Java
  "io.netty" % "netty-all" % "4.0.24.Final",
  "com.fasterxml" % "aalto-xml" % "0.9.8",
  "org.fusesource" % "sigar" % "1.6.4",
  "ch.qos.logback" % "logback-classic" % "1.0.13",
  //======== Test
  "org.scalatest" %% "scalatest" % "2.2.1" % "test",
  "com.typesafe.akka" %% "akka-multi-node-testkit" % akkaVersion
)

val scalaCompileOptions = Seq("-encoding", "UTF-8", "-target:jvm-1.6", "-feature", "-unchecked", "-Xlog-reflective-calls", "-Xlint")
val javaCompileOptions = Seq("-source", "1.6", "-target", "1.6", "-Xlint:unchecked", "-Xlint:deprecation")
val javaRunOptions = Seq("-Djava.library.path=./sigar", "-Xms128m", "-Xmx1024m")

val project = Project(
  id = "apus",
  base = file("."),
  settings = Project.defaultSettings ++ SbtMultiJvm.multiJvmSettings ++ Seq(
    name := """apus""",
    version := "1.0-SNAPSHOT",
    scalaVersion := "2.11.2",
    scalacOptions in Compile ++= scalaCompileOptions,
    javacOptions in Compile ++= javaCompileOptions,
    libraryDependencies ++= dependencies,
    javaOptions in run ++= javaRunOptions,
    Keys.fork in run := true,  
    mainClass in (Compile, run) := Some("apus.Main"),
    // make sure that MultiJvm test are compiled by the default test compilation
    compile in MultiJvm <<= (compile in MultiJvm) triggeredBy (compile in Test),
    // disable parallel tests
    parallelExecution in Test := false,
    // make sure that MultiJvm tests are executed by the default test target, 
    // and combine the results from ordinary test and multi-jvm tests
    executeTests in Test <<= (executeTests in Test, executeTests in MultiJvm) map {
      case (testResults, multiNodeResults)  =>
        val overall =
          if (testResults.overall.id < multiNodeResults.overall.id)
            multiNodeResults.overall
          else
            testResults.overall
        Tests.Output(overall,
          testResults.events ++ multiNodeResults.events,
          testResults.summaries ++ multiNodeResults.summaries)
    }
  )
) configs (MultiJvm)
