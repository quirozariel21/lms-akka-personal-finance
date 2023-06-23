package com.applaudo.akkalms.db

import akka.actor.{Actor, ActorLogging}
import com.applaudo.akkalms.models.requests._
import com.applaudo.akkalms.models.responses._

import scala.collection.mutable.ListBuffer

object PersonalFinanceDB {

  case class AddFinance(addFinanceRequest: AddFinanceRequest)
}

class PersonalFinanceDB extends Actor with ActorLogging {
  import PersonalFinanceDB._

  var finances: Map[Long, FinanceResponse] = Map()
  var currentFinanceId: Long = 0

  override def receive: Receive = {

    case AddFinance(addFinanceRequest) =>
      log.info(s"Adding finance $addFinanceRequest with id $currentFinanceId")

   /*   for(in <- addFinanceRequest.incomes) {
        var incomeResponse = IncomeResponse(1, in.incomeType, in.amount, in.currency, in.note)
      }*/

      val incomes = new ListBuffer[IncomeResponse]()
      addFinanceRequest.incomes.foreach{ in =>
        val incomeResponse = IncomeResponse(1, "in.incomeType", in.amount, "in.currency", in.note)
        incomes += incomeResponse
      }

      /*val financeResponse: FinanceResponse = FinanceResponse(currentFinanceId,
        addFinanceRequest.year, "addFinanceRequest.month", incomes.toList)
      finances = finances + (currentFinanceId -> financeResponse)
      log.info(s"Finances: $finances")
      currentFinanceId += 1*/
  }
}