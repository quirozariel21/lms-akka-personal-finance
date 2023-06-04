package com.applaudo.akkalms.controllers

import akka.http.scaladsl.server.Route
import com.applaudo.akkalms.models.forms.GetFinancesEndpointArguments
import com.applaudo.akkalms.models.requests.{AddFinanceRequest, AddIncomeRequest, UpdateFinanceRequest, UpdateIncomeRequest}
import com.applaudo.akkalms.models.responses.{FinanceResponse, IncomeResponse}
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.{Endpoint, EndpointInput, statusCode}
import sttp.tapir.generic.auto._
import io.circe.generic.auto._
import sttp.model.StatusCode
import sttp.tapir.server.akkahttp.AkkaHttpServerInterpreter

import scala.concurrent.{ExecutionContext, Future}

class FinanceController(baseController: BaseController)(implicit ec: ExecutionContext) {

  val createFinanceEndpoint: Endpoint[Unit, AddFinanceRequest, Unit, (FinanceResponse, StatusCode), Any] =
    baseController.baseEndpoint()
      .post
      .in("finance")
      .summary("Add a new personal finance")
      .tag("Finance")
      .in(
        jsonBody[AddFinanceRequest]
          .description("Finance object that needs to be added")
          .example(AddFinanceRequest(2022, "JANUARY", List(AddIncomeRequest("SALARY", 1000.27, "USD", None))))
      )
      .out(jsonBody[FinanceResponse])
      .out(statusCode.description(StatusCode.Created, "Successful created the personal finance"))
  //.errorOut(statusCode)

  val patchFinanceEndpoint: Endpoint[Unit, UpdateFinanceRequest, Unit, (FinanceResponse, StatusCode), Any] =
    baseController.baseEndpoint()
      .patch
      .in("finance")
      .summary("Update a personal finance")
      .tag("Finance")
      .in(
        jsonBody[UpdateFinanceRequest]
          .description("Finance object that needs to be updated")
          .example(UpdateFinanceRequest(1, 2022, "JANUARY", List(UpdateIncomeRequest(1, "SALARY", 1000.27, "USD", None))))
      )
      .out(jsonBody[FinanceResponse])
      .out(statusCode.description(StatusCode.Ok, "Successful updated the personal finance"))

  val getFinanceEndpoint: Endpoint[Unit, GetFinancesEndpointArguments, Unit, List[FinanceResponse], Any] =
    baseController.baseEndpoint()
      .get
      .in("finance")
      .summary("Returns a list of personal finances")
      .tag("Finance")
      .in(EndpointInput.derived
        [GetFinancesEndpointArguments]) // arguments described in that class
      .out(
        jsonBody[List[FinanceResponse]]
      )


  // converting an endpoint to a route (providing server-side logic); extension method comes from imported packages
  val createFinanceRoute: Route =
    AkkaHttpServerInterpreter().toRoute(createFinanceEndpoint.serverLogic(createFinanceLogic))

  val updateFinanceRoute: Route =
    AkkaHttpServerInterpreter().toRoute(patchFinanceEndpoint.serverLogic(updateFinanceLogic))

  val getFinanceRoute: Route =
    AkkaHttpServerInterpreter().toRoute(getFinanceEndpoint.serverLogic(getFinanceLogic))

  def createFinanceLogic(finance: AddFinanceRequest): Future[Either[Unit, (FinanceResponse, StatusCode)]] =
    Future {
      val addIncomeResponse: IncomeResponse = IncomeResponse(1, finance.incomes.head.incomeType, finance.incomes.head.amount, finance.incomes.head.currency, finance.incomes.head.note)
      val list: List[IncomeResponse] = List(addIncomeResponse)
      val addFinanceResponse: FinanceResponse = FinanceResponse(1, finance.year, finance.month, list)
      Right[Unit, (FinanceResponse, StatusCode)](addFinanceResponse -> StatusCode.Created)
    }

  def updateFinanceLogic(finance: UpdateFinanceRequest): Future[Either[Unit, (FinanceResponse, StatusCode)]] =
    Future {
      val incomeResponse: IncomeResponse = IncomeResponse(finance.incomes.head.id, finance.incomes.head.incomeType, finance.incomes.head.amount, finance.incomes.head.currency, finance.incomes.head.note)
      val list: List[IncomeResponse] = List(incomeResponse)
      val addFinanceResponse: FinanceResponse = FinanceResponse(finance.id, finance.year, finance.month, list)
      Right[Unit, (FinanceResponse, StatusCode)](addFinanceResponse -> StatusCode.Ok)
    }

  def getFinanceLogic(queryArgs: GetFinancesEndpointArguments): Future[Either[Unit, List[FinanceResponse]]] =
    Future {
      val income = IncomeResponse(1, "SALARY", 1000.27, "USD", None)
      val financeResp = FinanceResponse(1, 2023, "JANUARY", List(income))
      val response = List(financeResp)
      Right[Unit, List[FinanceResponse]](response)
    }

  /** Convenient way to assemble endpoints from the controller and then concat this route to main route. */
  val financeRoutes: List[Route] = List(createFinanceRoute, updateFinanceRoute, getFinanceRoute)

}
