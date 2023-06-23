package com.applaudo.akkalms.controllers

import akka.http.scaladsl.server.Route
import com.applaudo.akkalms.dao.{ExpenseDaoImpl, FinanceDaoImpl}
import com.applaudo.akkalms.enums.Currencies
import com.applaudo.akkalms.models.errors.{BadRequest, ErrorInfo, NoContent, NotFound}
import com.applaudo.akkalms.models.forms.ExpensePathArguments
import com.applaudo.akkalms.models.requests.{AddExpenseRequest, UpdateExpenseRequest}
import com.applaudo.akkalms.models.responses.{ExpenseResponse, GetExpensesResponse}
import com.typesafe.scalalogging.LazyLogging
import io.circe.{Decoder, Encoder}
import sttp.tapir.{Endpoint, path}
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe.jsonBody
import io.circe.generic.auto._
import sttp.model.StatusCode
import sttp.tapir._
import sttp.tapir.server.akkahttp.AkkaHttpServerInterpreter

import java.time.LocalDate
import scala.concurrent.{ExecutionContext, Future}

class ExpenseController(baseController: BaseController,
                        financeDao: FinanceDaoImpl,
                        expenseDao: ExpenseDaoImpl)(implicit ec: ExecutionContext) extends LazyLogging {

  implicit val enumCurrencyDecoder: Decoder[Currencies.Currency] = Decoder.decodeEnumeration(Currencies)
  implicit val enumCurrencyEncoder: Encoder[Currencies.Currency] = Encoder.encodeEnumeration(Currencies)

  val getExpenseByIdEndpoint: Endpoint[Unit, ExpensePathArguments, ErrorInfo, ExpenseResponse, Any] =
    baseController.baseEndpoint()
      .get
      .in(EndpointInput.derived[ExpensePathArguments])
      .summary("Info for a specific expense")
      .tag("Expense")
      .out(
        jsonBody[ExpenseResponse]
      )

  val deleteExpenseByIdEndpoint: Endpoint[Unit, ExpensePathArguments, ErrorInfo, StatusCode, Any] =
    baseController.baseEndpoint()
      .delete
      .in(EndpointInput.derived[ExpensePathArguments])
      .summary("Delete a specific expense")
      .tag("Expense")
      .out(statusCode.description(StatusCode.NoContent,"Successful deleted the expense"))

  val addExpenseEndpoint: Endpoint[Unit, (Long, AddExpenseRequest), ErrorInfo, (ExpenseResponse, StatusCode), Any] =
    baseController.baseEndpoint()
      .post
      .in("finance" / path[Long]("financeId").description("ID of the finance that needs to be updated")
        .and("expense")
        .and(
          jsonBody[AddExpenseRequest]
            .description("Expense object that needs to be added")
            .example(AddExpenseRequest(1, 2, None, 100.23, Currencies.USD, LocalDate.now))
        ))
      .summary("Add a new expense to the personal finance")
      .tag("Expense")
      .out(jsonBody[ExpenseResponse])
      .out(statusCode.description(StatusCode.Created, "Successful created the expense"))

  val patchExpenseEndpoint: Endpoint[Unit, (Long, UpdateExpenseRequest), ErrorInfo, (ExpenseResponse, StatusCode), Any] =
    baseController.baseEndpoint()
      .patch
      .in("finance" / path[Long]("financeId").description("ID of the finance that needs to be updated")
        .and("expense")
        .and(
          jsonBody[UpdateExpenseRequest]
            .description("Expense object that needs to be updated")
            .example(UpdateExpenseRequest(1, 1, 2, None, 100.23, Currencies.USD.toString, LocalDate.now))
        ))
      .summary("Update an expense")
      .tag("Expense")
      .out(jsonBody[ExpenseResponse])
      .out(statusCode.description(StatusCode.Ok, "Successful updated the expense"))

  val listExpensesEndpoint: Endpoint[Unit, Long, ErrorInfo, GetExpensesResponse, Any] =
    baseController.baseEndpoint()
      .get
      .in("finance" / path[Long]("financeId").description("ID of the finance that needs to be updated")
        .and("expense")
      )
      .summary("Returns a list of expenses")
      .tag("Expense")
      .out(jsonBody[GetExpensesResponse])


  val getExpenseByIdRoute: Route =
    AkkaHttpServerInterpreter().toRoute(getExpenseByIdEndpoint.serverLogic(getExpenseByIdLogic))

  def getExpenseByIdLogic(pathArguments: ExpensePathArguments): Future[Either[ErrorInfo, ExpenseResponse]] = {
    logger.info("Getting expense with id: {}", pathArguments.expenseId)
    val expense = expenseDao.findById(pathArguments.expenseId)

    expense match {
      case Some(e) =>
        val expenseResponse = ExpenseResponse(e.id, e.categoryId, e.subcategoryId,
                                              e.note, e.amount, e.currency,
                                              e.expenseDate, e.personalFinanceId)
        Future.successful(Right(expenseResponse))
      case None =>
        logger.info("Expense with id: {} not found", pathArguments.expenseId)
        Future.successful[Either[ErrorInfo, ExpenseResponse]](Left(NotFound(s"Expense with id: ${pathArguments.expenseId} not found", StatusCode.NotFound.code)))
    }
  }

  val deleteExpenseByIdRoute: Route =
    AkkaHttpServerInterpreter().toRoute(deleteExpenseByIdEndpoint.serverLogic(deleteExpenseByIdLogic))

  def deleteExpenseByIdLogic(pathArguments: ExpensePathArguments): Future[Either[ErrorInfo, StatusCode]] = {
    logger.info("Deleting expense with id: {}", pathArguments.expenseId)
    val expenseVal = expenseDao.findById(pathArguments.expenseId)
    expenseVal match {
      case Some(e) => Future.successful(Right(StatusCode.NoContent))
      case None => Future.successful[Either[ErrorInfo, StatusCode]](Left(NotFound(s"Expense with id: ${pathArguments.expenseId} not found", StatusCode.NotFound.code)))
    }
  }

  val addExpenseRoute: Route =
    AkkaHttpServerInterpreter().toRoute(addExpenseEndpoint.serverLogic(addExpenseLogic))

  def addExpenseLogic(params: (Long, AddExpenseRequest)): Future[Either[ErrorInfo, (ExpenseResponse, StatusCode)]] = {
    val personalFinanceId = params._1
    val expenseRequest = params._2
    logger.info("Adding a new Expense for the personalFinanceId: {}", personalFinanceId)
    val financeVal = financeDao.findById(personalFinanceId)
    logger.info("Personal finance with id: {} found", financeVal.get)
    financeVal match {
      case Some(fin) =>
        val expense = expenseDao.save(expenseRequest, personalFinanceId)
        val expenseResponse = ExpenseResponse(expense.id, expense.categoryId, expense.subcategoryId,
                                              expense.note, expense.amount, expense.currency,
                                              expense.expenseDate, fin.id)
        Future.successful[Either[ErrorInfo, (ExpenseResponse, StatusCode)]](Right(expenseResponse -> StatusCode.Created))
      case None =>
        Future.successful[Either[ErrorInfo, (ExpenseResponse, StatusCode)]](Left(BadRequest(s"Personal finance with id: $personalFinanceId is invalid", StatusCode.BadRequest.code)))
    }
  }

  val updateExpenseRoute: Route =
    AkkaHttpServerInterpreter().toRoute(patchExpenseEndpoint.serverLogic(updateExpenseLogic))

  def updateExpenseLogic(params: (Long, UpdateExpenseRequest)): Future[Either[ErrorInfo, (ExpenseResponse, StatusCode)]] = Future {
    val personalFinanceId = params._1
    val expenseRequest = params._2
    logger.info("Updating expense with id: {}", expenseRequest.id)
    val financeVal = financeDao.findById(personalFinanceId)
    financeVal match {
      case None => Left(BadRequest(s"Personal finance with id: $personalFinanceId is invalid", StatusCode.BadRequest.code))
      case Some(fin) =>
        // TODO: complete logic to update
        val expenseResponse = ExpenseResponse(expenseRequest.id, expenseRequest.categoryId, expenseRequest.subcategoryId,
          expenseRequest.note, expenseRequest.amount, expenseRequest.currency, expenseRequest.expenseDate, params._1)
        Right(expenseResponse -> StatusCode.Ok)
    }
  }

  val listExpensesRoute: Route =
    AkkaHttpServerInterpreter().toRoute(listExpensesEndpoint.serverLogic(listExpensesLogic))

  def listExpensesLogic(financeId: Long): Future[Either[ErrorInfo, GetExpensesResponse]] = {
    logger.info("Getting expenses for the personalFinanceId: {}", financeId)
    val expenseRes =  expenseDao.findByPersonalFinanceId(financeId)
    if(expenseRes.isEmpty) {
      Future.successful[Either[ErrorInfo, GetExpensesResponse]](Left(NoContent))
    } else {
      val expenses = expenseRes.map( exp => {
        ExpenseResponse(exp.id, exp.categoryId, exp.subcategoryId, exp.note, exp.amount, exp.currency, exp.expenseDate, exp.personalFinanceId)
      })
      val totalAmountAndCurrency = expenseDao.sumTotalAmount(financeId)
      val response = GetExpensesResponse(totalAmountAndCurrency._1, totalAmountAndCurrency._2, expenses)
      Future.successful[Either[ErrorInfo, GetExpensesResponse]](Right(response))
    }
  }

  val expenseEndpoints: List[AnyEndpoint] = List(getExpenseByIdEndpoint, deleteExpenseByIdEndpoint,
    addExpenseEndpoint, patchExpenseEndpoint, listExpensesEndpoint)

  /** Convenient way to assemble endpoints from the controller and then concat this route to main route. */
  val expenseRoutes: List[Route] = List(getExpenseByIdRoute, deleteExpenseByIdRoute,
                                        addExpenseRoute, updateExpenseRoute, listExpensesRoute)
}
