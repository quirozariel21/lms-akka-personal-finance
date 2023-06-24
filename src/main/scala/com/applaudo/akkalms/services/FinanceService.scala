package com.applaudo.akkalms.services

import com.applaudo.akkalms.dao.{FinanceDaoImpl, IncomeDaoImpl}
import com.applaudo.akkalms.entities.GenerateSummaryEntity
import com.applaudo.akkalms.models.errors.{BadRequest, ErrorInfo, NoContent}
import com.applaudo.akkalms.models.forms.{FinancePathArguments, GetFinancesEndpointArguments}
import com.applaudo.akkalms.models.requests.{AddFinanceRequest, UpdateFinanceRequest}
import com.applaudo.akkalms.models.responses.{BalanceResponse, CategorySummaryReportResponse, FinanceResponse, GenerateSummaryResponse, GetIncomeResponse, IncomeResponse, SubcategoryResponse}
import com.typesafe.scalalogging.LazyLogging
import sttp.model.StatusCode

import scala.collection.mutable.ListBuffer
import scala.concurrent.{ExecutionContext, Future}

class FinanceService(financeDao:FinanceDaoImpl,
                     incomeDao: IncomeDaoImpl)(implicit ec: ExecutionContext) extends LazyLogging {

  def createFinance(financeRequest: AddFinanceRequest): Future[Either[ErrorInfo, (FinanceResponse, StatusCode)]] =
    Future {
      logger.info("Adding a new Personal Finance")
      val financesVal = financeDao.findByYearAndMonth(Some(financeRequest.year), Some(financeRequest.month))
      /*if(financesVal.nonEmpty) {
        logger.info("Personal finance invalid!")
        Left(BadRequest(s"Personal Finance with year:${financeRequest.year} and month: ${financeRequest.month.toString} already exists", StatusCode.BadRequest.code))
      } else {
        // TODO research rollback, for example if fails some income it should be rollback
        val financeResDao = financeDao.save(financeRequest)
        val ids: List[Int] = financeRequest.incomes.map(income => incomeDao.save(financeResDao.id, income))
        val incomesResponse = ids.map(id => {
          incomeDao.findById(id) match {
            case Some(i) => IncomeResponse(i.id, i.name, i.amount, i.currency, i.note)
            case None => throw new IllegalArgumentException("Some error happened")
          }
        })
        val addFinanceResponse= FinanceResponse(financeResDao.id, financeResDao.year, financeResDao.month, incomesResponse)
        Right(addFinanceResponse -> StatusCode.Created)
      }*/
      financeDao.findByYearAndMonth(Some(financeRequest.year), Some(financeRequest.month)) match {
        case p :: ps =>
          logger.info("Personal finance invalid!")
          //TODO change error to 422
          Left(BadRequest(s"Personal Finance with year:${financeRequest.year} and month: ${financeRequest.month.toString} already exists", StatusCode.BadRequest.code))
        case Nil =>
          // TODO research rollback, for example if fails some income it should be rollback
          val financeResDao = financeDao.save(financeRequest)
          val ids: List[Int] = financeRequest.incomes.map(income => incomeDao.save(financeResDao.id, income))
          val incomesResponse = ids.map(id => {
            incomeDao.findById(id) match {
              case Some(i) => IncomeResponse(i.id, i.name, i.amount, i.currency, i.note)
              case None => throw new IllegalArgumentException(s"incomeId: ${id} not found")
            }
          })
          val incomeTotalAmount = incomesResponse.map(_.amount).sum
          val currency = incomesResponse.map(_.currency).head
          logger.info("Income total amount: {} {}", incomeTotalAmount, currency)
          val getIncomeResponse = GetIncomeResponse(incomeTotalAmount, currency, incomesResponse)
          val addFinanceResponse= FinanceResponse(financeResDao.id, financeResDao.year, financeResDao.month, getIncomeResponse)
          Right(addFinanceResponse -> StatusCode.Created)
      }
    }

  def updateFinance(finance: UpdateFinanceRequest): Future[Either[ErrorInfo, (FinanceResponse, StatusCode)]] =
    Future {
      val incomeResponse: IncomeResponse = IncomeResponse(finance.incomes.head.id, finance.incomes.head.incomeType, finance.incomes.head.amount, finance.incomes.head.currency, finance.incomes.head.note)
      val list: List[IncomeResponse] = List(incomeResponse)
      val incomeTotalAmount = list.map(_.amount).sum
      val currency = list.map(_.currency).head
      logger.info("Income total amount: {} {}", incomeTotalAmount, currency)
      val getIncomeResponse = GetIncomeResponse(incomeTotalAmount, currency, list)
      val addFinanceResponse: FinanceResponse = FinanceResponse(finance.id, finance.year, finance.month, getIncomeResponse)
      Right[ErrorInfo, (FinanceResponse, StatusCode)](addFinanceResponse -> StatusCode.Ok)
    }

  def getFinance(queryArgs: GetFinancesEndpointArguments): Future[Either[ErrorInfo, List[FinanceResponse]]] =
    Future {
      logger.info(s"Getting personal finances by year: '${queryArgs.year}' and/or month: '${queryArgs.month}'")

      val personalFinances = financeDao.findByYearAndMonth(queryArgs.year, queryArgs.month)
      logger.info("Is Empty: {}", personalFinances.isEmpty)
      if(personalFinances.isEmpty) {
        Left(NoContent)
      } else {
        val response = personalFinances.map(elem => {
          val incomeList = elem.incomes.split("#").toList.map( e => {
            val incomeValues = e.split('|')

            IncomeResponse(incomeValues(0).toLong, incomeValues(1),
              BigDecimal(incomeValues(3)), incomeValues(2),
              Some(incomeValues(4)))
          })
          val incomeTotalAmount = incomeList.map(_.amount).sum
          val currency = incomeList.map(_.currency).head
          logger.info("Income total amount: {} {}", incomeTotalAmount, currency)
          val getIncomeResponse = GetIncomeResponse(incomeTotalAmount, currency, incomeList)
          FinanceResponse(elem.id, elem.year, elem.month, getIncomeResponse)
        })
        Right(response)
      }
    }

  def generateSummary(pathArguments: FinancePathArguments): Future[Either[ErrorInfo, GenerateSummaryResponse]] = {
    logger.info("Generating summary report for the personal finance id: {}", pathArguments.financeId)
    financeDao.generateSummary(pathArguments.financeId) match {
      case Nil => Future.successful[Either[ErrorInfo, GenerateSummaryResponse]](Left(NoContent))
      case p :: ps =>
        val mapCategory: Map[String, List[GenerateSummaryEntity]] = ps.groupBy(_.categoryName)
        logger.info("mapCategory: {}", mapCategory)
        val finance = financeDao.findById(pathArguments.financeId).get

        var categories = new ListBuffer[CategorySummaryReportResponse]
        for((k, v) <- mapCategory)
        {
          //where k is key and v is value
          print("Key:"+k+", ")
          println("Value:"+v)

          val subcategories = v.map(v => SubcategoryResponse(v.subcategoryId, v.subcategoryName, v.note, v.amount, v.currency, v.expensedDate))
          val totalAmount = v.map(_.amount).sum
          val category = v.findLast(v => v.categoryName == k).get
          val categoryObj =CategorySummaryReportResponse(category.categoryId, category.categoryName, subcategories, totalAmount, category.currency)
          categories += categoryObj
        }
        val totalSpent = categories.map(_.totalAmount).sum
        val balance = BalanceResponse(finance.totalReceived, totalSpent, finance.totalReceived - totalSpent)

        val response = GenerateSummaryResponse(finance.id, finance.year, finance.month, balance, categories.toList)
        Future.successful[Either[ErrorInfo, GenerateSummaryResponse]](Right(response))
    }
  }

}
