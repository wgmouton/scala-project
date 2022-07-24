import sbt.librarymanagement.ConflictWarning

val scala3Version = "3.1.3"
//val scala3Version = "2.13.4"
val AkkaVersion = "2.6.19"
val AkkaHttpVersion = "10.2.9"
val CirceV         = "0.14.2"
val AkkaHttpCirceV = "1.39.2"


lazy val root = (project.in(file(".")))
  .settings(
    name := "clearscore",
    version := "0.1.0-SNAPSHOT",
    organization    := "com.wgmouton",

    scalaVersion := scala3Version,

    conflictWarning := ConflictWarning.disable,

    resolvers += Resolver.typesafeIvyRepo("releases"),
    fork := true,


      // Normal Scala 3 Libraries
    libraryDependencies ++= Seq(
      "rocks.heikoseeberger" %% "slf4s" % "0.3.0",
      "io.circe"          %% "circe-core" % CirceV,
      "io.circe"          %% "circe-parser" % CirceV,
      "io.circe"          %% "circe-generic" % CirceV,
      "ch.qos.logback"    % "logback-classic"              % "1.2.11",
      "org.typelevel"     %% "cats-core"                   % "2.8.0",

      // Dependencies used for testing
      "org.scalameta"     %% "munit"                    % "0.7.29"        % Test,
      "com.typesafe.akka" %% "akka-actor-testkit-typed" % AkkaVersion     % Test,
      "org.scalatest"     %% "scalatest"                % "3.2.12"        % Test
    ),

    // Scala 2 cross compiled lanagues
    libraryDependencies ++= Seq(
//      "com.typesafe.akka" %% "akka-cluster-singleton" % AkkaVersion,
      "com.typesafe.akka" %% "akka-actor-typed"            % AkkaVersion,
      "com.typesafe.akka" %% "akka-remote"                 % AkkaVersion,
      "com.typesafe.akka" %% "akka-cluster-typed"          % AkkaVersion,
      "com.typesafe.akka" %% "akka-cluster-sharding-typed" % AkkaVersion,
      "com.typesafe.akka" %% "akka-persistence-typed"      % AkkaVersion,
      "com.typesafe.akka" %% "akka-stream"                 % AkkaVersion,
      "de.heikoseeberger" %% "akka-http-circe"          % AkkaHttpCirceV,
      "com.typesafe.akka" %% "akka-http"                % AkkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-spray-json"     % AkkaHttpVersion,


      // Dependencies used for testing
      "com.typesafe.akka" %% "akka-http-testkit"        % AkkaHttpVersion % Test,
    ).map(_.cross(CrossVersion.for3Use2_13))
  )

//lazy val akkaHttpVersion = "10.2.9"
//lazy val akkaVersion    = "2.6.19"
//
//// Run in a separate JVM, to make sure sbt waits until all threads have
//// finished before returning.
//// If you want to keep the application running while executing other
//// sbt tasks, consider https://github.com/spray/sbt-revolver/

//
//lazy val root = (project in file(".")).
//  settings(
//    inThisBuild(List(
//      organization    := "com.wgmouton",
//      scalaVersion    := "2.13.4"
//    )),
//    name := "test",
//    libraryDependencies ++= Seq(
//      "com.typesafe.akka" %% "akka-http"                % akkaHttpVersion,
//      "com.typesafe.akka" %% "akka-http-spray-json"     % akkaHttpVersion,
//      "com.typesafe.akka" %% "akka-actor-typed"         % akkaVersion,
//      "com.typesafe.akka" %% "akka-stream"              % akkaVersion,
//      "ch.qos.logback"    % "logback-classic"           % "1.2.3",
//
//      "com.typesafe.akka" %% "akka-http-testkit"        % akkaHttpVersion % Test,
//      "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion     % Test,
//      "org.scalatest"     %% "scalatest"                % "3.1.4"         % Test
//    )
//  )
