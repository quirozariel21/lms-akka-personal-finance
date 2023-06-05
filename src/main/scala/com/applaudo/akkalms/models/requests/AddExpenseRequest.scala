package com.applaudo.akkalms.models.requests

import java.time.LocalDateTime

case class AddExpenseRequest(categoryId: Int,
                             subcategoryId: Int,
                             note: Option[String],
                             amount: BigDecimal,
                             currency: String,
                             expenseDate: LocalDateTime)
