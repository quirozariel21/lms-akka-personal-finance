package com.applaudo.akkalms.models.responses

case class AddFinanceResponse(id: Int, year: Int, month: String, incomes: List[AddIncomeResponse])
