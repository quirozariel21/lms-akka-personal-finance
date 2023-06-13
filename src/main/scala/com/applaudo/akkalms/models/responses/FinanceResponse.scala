package com.applaudo.akkalms.models.responses

case class FinanceResponse(id: Long,
                           year: Int,
                           month: String,
                           incomes: List[IncomeResponse])
