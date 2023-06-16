package com.applaudo.akkalms.model_db

import java.time.LocalDateTime

case class Category(id: Long,
                    name: String,
                    description: Option[String],
                    createdAt: LocalDateTime,
                    subcategoryId: Option[Long],
                    isActive: Boolean,
                    subcategories: Option[String])
