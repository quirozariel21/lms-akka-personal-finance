package com.applaudo.akkalms

import scala.concurrent.Future
import akka.http.scaladsl.Http
import sttp.tapir.server.akkahttp.AkkaHttpServerInterpreter
import sttp.tapir.swagger.bundle.SwaggerInterpreter
import com.applaudo.akkalms.modules.MainModule
import com.typesafe.scalalogging.LazyLogging
import scala.io.StdIn
import akka.http.scaladsl.server.Directives._
import sttp.tapir.AnyEndpoint

/**
 * Application's init point.
 * Extends mainModule, which contains all wirings and starts the server.
 */
class TapirRoutes extends LazyLogging with MainModule {

  // mostly for execution context
  import actorSystem.dispatcher

  val endpointList: List[AnyEndpoint] = List(
    financeController.financeEndpoints,
    expenseController.expenseEndpoints,
    categoryController.categoryEndpoints
  ).flatten

  val swaggerUIRoute = AkkaHttpServerInterpreter()
    .toRoute(
      SwaggerInterpreter().fromEndpoints[Future](endpointList, "Personal Finance API", "1.0.0")
    )

  val routeList = List(
    financeController.financeRoutes,
    expenseController.expenseRoutes,
    categoryController.categoryRoutes,
  )

  /**
   * Result route. Contains all active endpoints and this route will be bound to the server.
   */
  var resultRoute = routeList.flatten.reduce((r1, r2) => r1 ~ r2) ~ swaggerUIRoute


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
