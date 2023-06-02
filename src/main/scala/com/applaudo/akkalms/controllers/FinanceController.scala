package com.applaudo.akkalms.controllers

import akka.http.scaladsl.server.Route
import com.applaudo.akkalms.models.requests.AddFinanceRequest
import com.applaudo.akkalms.models.responses.{AddFinanceResponse, AddIncomeResponse}
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.{Endpoint, statusCode}
import sttp.tapir.generic.auto._
import io.circe.generic.auto._
import sttp.model.StatusCode
import sttp.tapir.server.akkahttp.AkkaHttpServerInterpreter

import scala.concurrent.{ExecutionContext, Future}

class FinanceController(baseController: BaseController)(implicit ec: ExecutionContext) {

  val createFinanceEndpoint: Endpoint[Unit, AddFinanceRequest, Unit, (AddFinanceResponse, StatusCode), Any] =
    baseController.baseEndpoint()
      .post
      .in("finance")
      .summary("Add a new personal finance")
      .tag("Finance")
      .in(
        jsonBody[AddFinanceRequest]//.schema
          .description("Finance object that needs to be added")
        //.example(Any, "")
      )
      //.out(statusCode.description(StatusCode.Created, "Created"))
      .out(jsonBody[AddFinanceResponse])
      .out(statusCode.description(StatusCode.Created, "Created"))
  //.errorOut(statusCode)

  // converting an endpoint to a route (providing server-side logic); extension method comes from imported packages
  val createFinanceRoute: Route =
    AkkaHttpServerInterpreter().toRoute(createFinanceEndpoint.serverLogic(createFinanceLogic))

  def createFinanceLogic(finance: AddFinanceRequest): Future[Either[Unit, (AddFinanceResponse, StatusCode)]] =
    Future {
      val addIncomeResponse: AddIncomeResponse = AddIncomeResponse(1, finance.incomes.head.incomeType, finance.incomes.head.amount, finance.incomes.head.currency, "")
      val list: List[AddIncomeResponse] = List(addIncomeResponse)
      val addFinanceResponse: AddFinanceResponse = AddFinanceResponse(1, finance.year, finance.month, list)
      Right[Unit, (AddFinanceResponse, StatusCode)](addFinanceResponse -> StatusCode.Created)
    }

  /** Convenient way to assemble endpoints from the controller and then concat this route to main route. */
  val financeRoutes: List[Route] = List(createFinanceRoute)

}
