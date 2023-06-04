package com.applaudo.akkalms.models.requests

case class UpdateFinanceRequest(id: Int,
                                year: Int,
                                month: String,
                                incomes: List[UpdateIncomeRequest])
