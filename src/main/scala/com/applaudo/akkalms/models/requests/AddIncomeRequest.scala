package com.applaudo.akkalms.models.requests

case class AddIncomeRequest(incomeType: String,
                            amount: BigDecimal,
                            currency: String,
                            note: Option[String])
