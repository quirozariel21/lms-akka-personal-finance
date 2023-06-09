package com.applaudo.akkalms.modules

import akka.actor.ActorSystem
import com.applaudo.akkalms.controllers.{BaseController, CategoryController, ExpenseController, FinanceController}
import com.applaudo.akkalms.dao.{CategoryDaoImpl, ExpenseDaoImpl, FinanceDaoImpl, IncomeDaoImpl}
import com.applaudo.akkalms.errors.ErrorHandler
import com.applaudo.akkalms.services.FinanceService
import com.softwaremill.macwire.wire
import com.typesafe.config.{Config, ConfigFactory}

trait MainModule {

  implicit val actorSystem = ActorSystem("PersonalFinance")
  import actorSystem.dispatcher
  //implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  lazy val config: Config = ConfigFactory.load() // for macwire not necessary to wire everything, having required component in scope is enough.
  lazy val errorHandler     = wire[ErrorHandler]
  lazy val baseController = wire[BaseController]
  lazy val financeController = wire[FinanceController]
  lazy val expenseController = wire[ExpenseController]
  lazy val categoryController = wire[CategoryController]
  lazy val progressQueries = wire[CategoryDaoImpl]
  lazy val financeDao = wire[FinanceDaoImpl]
  lazy val incomeDao = wire[IncomeDaoImpl]
  lazy val expenseDao = wire[ExpenseDaoImpl]
  lazy val financeService = wire[FinanceService]

}
