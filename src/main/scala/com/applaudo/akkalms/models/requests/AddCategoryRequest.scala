package com.applaudo.akkalms.models.requests

case class AddCategoryRequest(name: String,
                              description: Option[String],
                              parentId: Option[Long])
