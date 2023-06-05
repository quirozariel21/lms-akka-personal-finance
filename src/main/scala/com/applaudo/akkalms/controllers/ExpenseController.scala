package com.applaudo.akkalms.controllers

import akka.http.scaladsl.server.Route
import com.applaudo.akkalms.models.forms.ExpensePathArguments
import com.applaudo.akkalms.models.requests.{AddExpenseRequest, UpdateExpenseRequest}
import com.applaudo.akkalms.models.responses.ExpenseResponse
import sttp.tapir.{Endpoint, path}
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe.jsonBody
import io.circe.generic.auto._
import sttp.model.StatusCode
import sttp.tapir._
import sttp.tapir.generic.Derived
import sttp.tapir.server.akkahttp.AkkaHttpServerInterpreter

import java.time.LocalDateTime
import scala.concurrent.{ExecutionContext, Future}

class ExpenseController(baseController: BaseController)(implicit ec: ExecutionContext) {

/*  implicit val custom: Schema[UpdateExpenseEndpointArguments] = implicitly[Derived[Schema[UpdateExpenseEndpointArguments]]].value
    .modify(_.financeId)(_.description(""))
    .modify(_.request)(_.description(""))*/

  val getExpenseByIdEndpoint: Endpoint[Unit, ExpensePathArguments, Unit, ExpenseResponse, Any] =
    baseController.baseEndpoint()
      .get
      .in(EndpointInput.derived[ExpensePathArguments])
      .summary("Info for a specific expense")
      .tag("Expense")
      .out(
        jsonBody[ExpenseResponse]
      )

  val deleteExpenseByIdEndpoint: Endpoint[Unit, ExpensePathArguments, Unit, StatusCode, Any] =
    baseController.baseEndpoint()
      .delete
      .in(EndpointInput.derived[ExpensePathArguments])
      .summary("Delete a specific expense")
      .tag("Expense")
      .out(statusCode.description(StatusCode.NoContent,"Successful deleted the expense"))

  val addExpenseEndpoint: Endpoint[Unit, (Long, AddExpenseRequest), Unit, (ExpenseResponse, StatusCode), Any] =
    baseController.baseEndpoint()
      .post
      .in("finance" / path[Long]("financeId").description("ID of the finance that needs to be updated")
        .and("expense")
        .and(
          jsonBody[AddExpenseRequest]
            .description("Expense object that needs to be added")
            .example(AddExpenseRequest(1, 2, None, 100.23, "USD", LocalDateTime.now))
        ))
      .summary("Add a new expense to the personal finance")
      .tag("Expense")
      .out(jsonBody[ExpenseResponse])
      .out(statusCode.description(StatusCode.Created, "Successful created the expense"))

  val patchExpenseEndpoint: Endpoint[Unit, (Long, UpdateExpenseRequest), Unit, (ExpenseResponse, StatusCode), Any] =
    baseController.baseEndpoint()
      .patch
      .in("finance" / path[Long]("financeId").description("ID of the finance that needs to be updated")
        .and("expense")
        .and(
          jsonBody[UpdateExpenseRequest]
            .description("Expense object that needs to be updated")
            .example(UpdateExpenseRequest(1, 1, 2, None, 100.23, "USD", LocalDateTime.now))
        ))
      .summary("Update an expense")
      .tag("Expense")
      .out(jsonBody[ExpenseResponse])
      .out(statusCode.description(StatusCode.Ok, "Successful updated the expense"))

  val listExpensesEndpoint: Endpoint[Unit, Long, Unit, List[ExpenseResponse], Any] =
    baseController.baseEndpoint()
      .get
      .in("finance" / path[Long]("financeId").description("ID of the finance that needs to be updated")
        .and("expense")
      )
      .summary("Returns a list of expenses")
      .tag("Expense")
      .out(jsonBody[List[ExpenseResponse]])

  val getExpenseByIdRoute: Route =
    AkkaHttpServerInterpreter().toRoute(getExpenseByIdEndpoint.serverLogic(getExpenseByIdLogic))

  def getExpenseByIdLogic(pathArguments: ExpensePathArguments): Future[Either[Unit, ExpenseResponse]] =
    Future {
      val expenseResponse = ExpenseResponse(pathArguments.expenseId, 1, 2, None, 76.8, "USD", LocalDateTime.now, pathArguments.financeId)
      Right[Unit, ExpenseResponse](expenseResponse)
    }

  val deleteExpenseByIdRoute: Route =
    AkkaHttpServerInterpreter().toRoute(deleteExpenseByIdEndpoint.serverLogic(deleteExpenseByIdLogic))

  def deleteExpenseByIdLogic(pathArguments: ExpensePathArguments): Future[Either[Unit, StatusCode]] =
    Future {
      Right[Unit, StatusCode](StatusCode.NoContent)
    }

  val addExpenseRoute: Route =
    AkkaHttpServerInterpreter().toRoute(addExpenseEndpoint.serverLogic(addExpenseLogic))

  def addExpenseLogic(params: (Long, AddExpenseRequest)): Future[Either[Unit, (ExpenseResponse, StatusCode)]] =
    Future {
      val expenseRequest = params._2
      val expenseResponse = ExpenseResponse(1, expenseRequest.categoryId, expenseRequest.subcategoryId, None, expenseRequest.amount, expenseRequest.currency, LocalDateTime.now, params._1)
      Right[Unit, (ExpenseResponse, StatusCode)](expenseResponse -> StatusCode.Created)
    }

  val updateExpenseRoute: Route =
    AkkaHttpServerInterpreter().toRoute(patchExpenseEndpoint.serverLogic(updateExpenseLogic))

  def updateExpenseLogic(params: (Long, UpdateExpenseRequest)): Future[Either[Unit, (ExpenseResponse, StatusCode)]] =
    Future {
      val expenseRequest = params._2
      val expenseResponse = ExpenseResponse(expenseRequest.id, expenseRequest.categoryId, expenseRequest.subcategoryId,
        expenseRequest.note, expenseRequest.amount, expenseRequest.currency, expenseRequest.expenseDate, params._1)
      Right[Unit, (ExpenseResponse, StatusCode)](expenseResponse -> StatusCode.Ok)
    }

  val listExpensesRoute: Route =
    AkkaHttpServerInterpreter().toRoute(listExpensesEndpoint.serverLogic(listExpensesLogic))

  def listExpensesLogic(financeId: Long): Future[Either[Unit, List[ExpenseResponse]]] =
    Future {
      val expenseResponse = ExpenseResponse(1, 1, 2, None, 76.8, "USD", LocalDateTime.now, financeId)
      Right[Unit, List[ExpenseResponse]](List(expenseResponse))
    }

  /** Convenient way to assemble endpoints from the controller and then concat this route to main route. */
  val expenseRoutes: List[Route] = List(getExpenseByIdRoute, deleteExpenseByIdRoute,
                                        addExpenseRoute, updateExpenseRoute, listExpensesRoute)
}
