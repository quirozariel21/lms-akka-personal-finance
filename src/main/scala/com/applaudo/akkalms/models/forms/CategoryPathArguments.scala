package com.applaudo.akkalms.models.forms

import sttp.tapir.EndpointIO.annotations.{endpointInput, path}
import sttp.tapir.Schema.annotations.description

@endpointInput("category/{categoryId}")
case class CategoryPathArguments(
                                @path
                                @description("ID of the category")
                                categoryId: Long
                                )
