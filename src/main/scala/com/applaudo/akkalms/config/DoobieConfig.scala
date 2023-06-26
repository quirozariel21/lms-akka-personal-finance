package com.applaudo.akkalms.config

import cats.effect.IO
import com.typesafe.config.{Config, ConfigFactory}
import doobie.Transactor
import org.flywaydb.core.Flyway


object DoobieConfig {

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

}
