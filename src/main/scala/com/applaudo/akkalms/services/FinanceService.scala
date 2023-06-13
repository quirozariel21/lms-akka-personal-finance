package com.applaudo.akkalms.services

import com.applaudo.akkalms.models.requests.AddFinanceRequest
import com.applaudo.akkalms.models.responses.FinanceResponse
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.{ExecutionContext, Future}

object PersistentPersonalFinance {
  case class AddFinance(finance: AddFinanceRequest)
  case object OperationSuccess

}

class FinanceService()(implicit ec: ExecutionContext) extends LazyLogging {

  /*def createFinance(AddFinanceRequest: AddFinanceRequest): Future[Either[Unit, FinanceResponse]] = {

  }*/
}
