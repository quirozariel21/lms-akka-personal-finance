package com.applaudo.akkalms

import scala.concurrent.Future
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.{Directives, Route}
import sttp.tapir.server.akkahttp.AkkaHttpServerInterpreter
import sttp.tapir._
import sttp.tapir.swagger.bundle.SwaggerInterpreter
import com.applaudo.akkalms.modules.MainModule
import com.typesafe.scalalogging.LazyLogging

import scala.io.StdIn

/**
 * Application's init point.
 * Extends mainModule, which contains all wirings and starts the server.
 */
class TapirRoutes extends LazyLogging with MainModule {

  // mostly for execution context
  import actorSystem.dispatcher
  val endpointList = List(
    financeController.financeRoutes,
    expenseController.expenseRoutes,
    categoryController.categoryRoutes
  ).flatten

  //val swaggerEndpoints = SwaggerInterpreter().fromEndpoints[Future](endpointList, "Personal Finance API", "1.0.0")
  //val swaggerEndpoints = SwaggerInterpreter().fromEndpoints[Future](endpointList.map(_.endpoint), "Personal Finance API", "1.0")

  val swaggerUIRoute =
    AkkaHttpServerInterpreter().toRoute(
      SwaggerInterpreter().fromEndpoints[Future](
        List(
          financeController.createFinanceEndpoint,
          financeController.patchFinanceEndpoint,
          financeController.getFinanceEndpoint,

          expenseController.getExpenseByIdEndpoint,
          expenseController.deleteExpenseByIdEndpoint,
          expenseController.addExpenseEndpoint,
          expenseController.patchExpenseEndpoint,
          expenseController.listExpensesEndpoint,

          categoryController.createCategoryEndpoint,
          categoryController.getCategoriesEndpoint
        ), "Personal Finance API", "1.0.0")
    )

  // starting the server
  val resultRoute = {
    import akka.http.scaladsl.server.Directives._
    concat(financeController.createFinanceRoute,
          financeController.updateFinanceRoute,
          financeController.getFinanceRoute,

          expenseController.getExpenseByIdRoute,
          expenseController.deleteExpenseByIdRoute,
          expenseController.addExpenseRoute,
          expenseController.updateExpenseRoute,
          expenseController.listExpensesRoute,

          categoryController.createCategoryRoute,
          categoryController.getCategoriesRoute,
      swaggerUIRoute)
  }

  /**
   * Starts server using route above.
   */
  def init(): Unit = {
    val bindingFuture = Http().newServerAt("localhost", 8080).bind(resultRoute)
    logger.info(s"Server online at http://localhost:8080/")
    logger.info("Press RETURN to stop...")
    StdIn.readLine()
    logger.info("Going offline....")
    bindingFuture
      .flatMap(_.unbind())
      .onComplete(_ => actorSystem.terminate())
  }

}
