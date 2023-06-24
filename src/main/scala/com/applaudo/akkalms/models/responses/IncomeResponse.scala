package com.applaudo.akkalms.models.responses

case class IncomeResponse(id: Long,
                          incomeType: String,
                          amount: BigDecimal,
                          currency: String,
                          note: Option[String])
