package com.applaudo.akkalms.models.requests

case class AddCategoryRequest(name: String,
                              description: Option[String],
                              subcategoryId: Option[Long])
