package com.applaudo.akkalms.models.responses

case class AddIncomeResponse(id: Int,
                             incomeType: String,
                             amount: BigDecimal,
                             currency: String,
                             note: String)
