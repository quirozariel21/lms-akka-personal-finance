package com.applaudo.akkalms.models.requests

import com.applaudo.akkalms.enums.{Currencies, IncomeTypes}
import com.applaudo.akkalms.models.requests.AddExpenseRequest.amountValidator
import sttp.tapir.Schema.annotations.validate
import sttp.tapir.Validator

object UpdateIncomeRequest {
  val amountValidator = Validator.min(BigDecimal(0.01))
}

case class UpdateIncomeRequest(id: Option[Int],
                               incomeType: IncomeTypes.IncomeType,
                               @validate(amountValidator)
                               amount: BigDecimal,
                               currency: Currencies.Currency,
                               note: Option[String])
