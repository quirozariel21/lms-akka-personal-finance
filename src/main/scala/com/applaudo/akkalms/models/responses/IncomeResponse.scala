package com.applaudo.akkalms.models.responses

case class IncomeResponse(id: Int,
                          incomeType: String,
                          amount: BigDecimal,
                          currency: String,
                          note: Option[String])
