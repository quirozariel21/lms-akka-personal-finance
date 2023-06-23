package com.applaudo.akkalms.entities

import java.time.LocalDateTime

case class Category(id: Long,
                    name: String,
                    description: Option[String],
                    createdAt: LocalDateTime,
                    parentId: Option[Long],
                    isActive: Boolean,
                    subcategories: Option[String])
