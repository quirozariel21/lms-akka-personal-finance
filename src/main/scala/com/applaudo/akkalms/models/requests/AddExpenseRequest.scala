package com.applaudo.akkalms.models.requests

import com.applaudo.akkalms.enums.Currencies
import com.applaudo.akkalms.models.requests.AddExpenseRequest.amountValidator
import sttp.tapir.Schema.annotations.{description, format, validate}
import sttp.tapir.Validator

import java.time.LocalDate



object AddExpenseRequest {
  val amountValidator = Validator.min(BigDecimal(0.01))
}

case class AddExpenseRequest(categoryId: Long,
                             subcategoryId: Long,
                             note: Option[String],
                             @validate(amountValidator)
                             amount: BigDecimal,
                             @description("Type of currency")
                             currency: Currencies.Currency,
                             @format("yyyy-MM-dd")
                             expenseDate: LocalDate)

