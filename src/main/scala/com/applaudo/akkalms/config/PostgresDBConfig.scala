package com.applaudo.akkalms.config

import cats.effect.IO
import com.typesafe.config.{Config, ConfigFactory}
import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import doobie.Transactor
import doobie.hikari.HikariTransactor
import pureconfig.ConfigConvert.fromReaderAndWriter
import pureconfig.ConfigSource
import pureconfig.generic.auto._
import pureconfig.generic.DerivedConfigReader.anyValReader

import scala.concurrent.ExecutionContext
import pureconfig.ConvertHelpers._

import org.flywaydb.core.api.configuration.FluentConfiguration
import org.flywaydb.core.api.Location
import org.flywaydb.core.Flyway
import scala.jdk.CollectionConverters._

import cats.effect.Sync
import cats.implicits._

object PostgresDBConfig {

  val config: Config = ConfigFactory.load().getConfig("database")
  val driver: String = config.getString("postgres.driver")
  val url: String = config.getString("postgres.url")
  val username: String = config.getString("postgres.username")
  val password: String = config.getString("postgres.password")

  def xa() = Transactor.fromDriverManager[IO](
    driver,
    url,
    username,
    password
  )

  val flyway = Flyway.configure.dataSource(
    url,
    username,
    password
  )
    .group(true)
    .outOfOrder(false)
    //.table()
    .baselineOnMigrate(true)

  flyway.mixed(true).load().migrate().migrationsExecuted

/*  private def logValidationErrorsIfAny(m: FluentConfiguration): Unit = {
    val validated = m.ignorePendingMigrations(true)
      .load()
      .validateWithResult()

    if (!validated.validationSuccessful)
      for (error <- validated.invalidMigrations.asScala)
        logger.warn(s"""
                       |Failed validation:
                       |  - version: ${error.version}
                       |  - path: ${error.filepath}
                       |  - description: ${error.description}
                       |  - errorCode: ${error.errorDetails.errorCode}
                       |  - errorMessage: ${error.errorDetails.errorMessage}
        """.stripMargin.strip)
  }*/
}
