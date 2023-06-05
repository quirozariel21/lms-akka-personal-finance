package com.applaudo.akkalms.models.responses

import java.time.LocalDateTime

case class ExpenseResponse(id: Long,
                           categoryId: Long,
                           subcategoryId: Long,
                           note: Option[String],
                           amount: BigDecimal,
                           currency: String,
                           expenseDate: LocalDateTime,
                           financeId: Long)
