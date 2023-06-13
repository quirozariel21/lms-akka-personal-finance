package com.applaudo.akkalms.modules

import akka.actor.ActorSystem
import com.applaudo.akkalms.controllers.{BaseController, CategoryController, ExpenseController, FinanceController}
import com.applaudo.akkalms.dao.CategoryDaoImpl
import com.softwaremill.macwire.wire
import com.typesafe.config.{Config, ConfigFactory}

trait MainModule {

  implicit val actorSystem = ActorSystem("PersonalFinance")
  import actorSystem.dispatcher
  //implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  lazy val config: Config = ConfigFactory.load() // for macwire not necessary to wire everything, having required component in scope is enough.
  lazy val baseController = wire[BaseController]
  lazy val financeController = wire[FinanceController]
  lazy val expenseController = wire[ExpenseController]
  lazy val progressQueries = wire[CategoryDaoImpl]
  lazy val categoryController = wire[CategoryController]
}
