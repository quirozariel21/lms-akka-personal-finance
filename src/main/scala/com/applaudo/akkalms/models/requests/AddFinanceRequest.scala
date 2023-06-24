package com.applaudo.akkalms.models.requests

import com.applaudo.akkalms.enums.Months
import com.applaudo.akkalms.models.requests.AddFinanceRequest.yearValidation
import sttp.tapir.Schema.annotations.validate
import sttp.tapir.Validator
object AddFinanceRequest {
  val yearValidation = Validator.inRange(2020, 2100)
}

case class AddFinanceRequest(@validate(yearValidation)
                             year: Int,
                             month: Months.Month,
                             incomes: List[AddIncomeRequest])

