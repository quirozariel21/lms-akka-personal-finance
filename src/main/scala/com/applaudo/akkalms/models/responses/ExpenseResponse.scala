package com.applaudo.akkalms.models.responses

import java.time.LocalDate

case class ExpenseResponse(id: Long,
                           categoryId: Long,
                           subcategoryId: Long,
                           note: Option[String],
                           amount: BigDecimal,
                           currency: String,
                           expenseDate: LocalDate,
                           financeId: Long)
