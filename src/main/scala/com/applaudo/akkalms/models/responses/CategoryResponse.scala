package com.applaudo.akkalms.models.responses

import java.time.LocalDateTime

case class CategoryResponse(id: Long,
                            name: String,
                            description: Option[String],
                            createdAt: LocalDateTime,
                            parentId: Option[Long],
                            isActive: Boolean,
                            subcategories: Option[List[CategoryResponse]])
