package com.applaudo.akkalms

import cats.effect.{IO, Resource}
import com.typesafe.config.ConfigFactory
import pureconfig._
import pureconfig.generic.auto._
import pureconfig.module.catseffect.syntax._


package object config {

  case class PostgresConfig(driver: String,
                            url: String,
                            username: String,
                            password: String,
                            threadPoolSize: Int)

  case class DatabaseConfig(postgres: PostgresConfig)

  case class Config(database: DatabaseConfig)

  object Config {
    def load(configFile: String = "application.conf"): Resource[IO, Config] = {
      Resource.eval(ConfigSource.fromConfig(ConfigFactory.load(configFile)).loadF[IO, Config]())
    }
  }
}
