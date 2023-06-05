package com.applaudo.akkalms.models.requests

import java.time.LocalDateTime

case class UpdateExpenseRequest(id: Int,
                                categoryId: Int,
                                subcategoryId: Int,
                                note: Option[String],
                                amount: BigDecimal,
                                currency: String,
                                expenseDate: LocalDateTime)
