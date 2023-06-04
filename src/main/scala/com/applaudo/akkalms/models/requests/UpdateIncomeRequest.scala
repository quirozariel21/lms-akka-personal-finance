package com.applaudo.akkalms.models.requests

case class UpdateIncomeRequest(id: Int,
                               incomeType: String,
                               amount: BigDecimal,
                               currency: String,
                               note: Option[String])
