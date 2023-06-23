package com.applaudo.akkalms.entities

import java.time.LocalDate

case class GenerateSummaryEntity(id: Long,
                                 year: Int,
                                 month: String,
                                 expenseId: Long,
                                 categoryId: Long,
                                 categoryName: String,
                                 subcategoryId: Long,
                                 subcategoryName: String,
                                 amount: BigDecimal,
                                 currency: String,
                                 note: Option[String],
                                 expensedDate: LocalDate)
