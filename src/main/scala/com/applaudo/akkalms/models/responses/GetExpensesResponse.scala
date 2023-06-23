package com.applaudo.akkalms.models.responses

case class GetExpensesResponse(
                              totalAmount: BigDecimal,
                              currency: String,
                              expenses: List[ExpenseResponse]
                              )
