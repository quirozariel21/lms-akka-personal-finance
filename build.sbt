name := "lms-akka-personal-finance"

version := "1.0"

scalaVersion := "2.13.1"

lazy val akkaVersion = "2.8.2"
lazy val akkaHttpVersion = "10.5.2"
lazy val tapirVersion = "1.4.0"
lazy val macwireVersion = "2.5.8"
lazy val doobieVersion = "1.0.0-RC1"
lazy val PureConfigVersion = "0.17.1"
lazy val FlywayVersion = "9.2.0"
lazy val ScalaTestVersion = "3.1.1"
lazy val ScalaMockVersion = "4.4.0"

lazy val leveldbVersion = "0.7"
lazy val leveldbjniVersion = "1.8"

// Run in a separate JVM, to make sure sbt waits until all threads have
// finished before returning.
// If you want to keep the application running while executing other
// sbt tasks, consider https://github.com/spray/sbt-revolver/
fork := true

libraryDependencies ++= Seq(
  // akka http
  "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,

  // local levelDB stores
  "org.iq80.leveldb" % "leveldb" % leveldbVersion,
  "org.fusesource.leveldbjni" % "leveldbjni-all" % leveldbjniVersion,

  // tapir
  "com.softwaremill.sttp.tapir" %% "tapir-akka-http-server" % tapirVersion,
  "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % tapirVersion,
  "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle" % tapirVersion,
  "com.softwaremill.sttp.tapir" %% "tapir-sttp-stub-server" % tapirVersion,

  //MacWire
  "com.softwaremill.macwire" %% "macros" % macwireVersion % "provided",
  "com.softwaremill.macwire" %% "macrosakka" % macwireVersion % "provided",
  "com.softwaremill.macwire" %% "util" % macwireVersion,
  "com.softwaremill.macwire" %% "proxy" % macwireVersion,

  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.5",

  //Doobie
  "org.tpolecat" %% "doobie-core"      % doobieVersion,

  // And add any of these as needed
  "org.tpolecat" %% "doobie-hikari"    % doobieVersion,          // HikariCP transactor.
  "org.tpolecat" %% "doobie-postgres"  % doobieVersion,          // Postgres driver 42.3.1 + type mappings.
  "org.tpolecat" %% "doobie-specs2"    % doobieVersion % "test", // Specs2 support for typechecking statements.
  "org.tpolecat" %% "doobie-scalatest" % doobieVersion % "test",  // ScalaTest support for typechecking statements.

  //Flyway
  "org.flywaydb"          %  "flyway-core"          % FlywayVersion,

  "io.estatico" %% "newtype" % "0.4.4",

  //Akka persistence
  "com.typesafe.akka" %% "akka-persistence-typed"     % akkaVersion,


  "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion % Test,
  "org.scalatest" %% "scalatest" % "3.1.1" % Test,
  "org.scalamock" %% "scalamock" % ScalaMockVersion % "test"


)
