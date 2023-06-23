package com.applaudo.akkalms.models.responses

case class CategorySummaryReportResponse(id: Long,
                                         name:String,
                                         subcategories: List[SubcategoryResponse],
                                         totalAmount: BigDecimal,
                                         currency: String)
