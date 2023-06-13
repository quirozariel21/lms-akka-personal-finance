package com.applaudo.akkalms.models.errors

/**
 * Trait for custom http code handling for error cases.
 */
sealed trait ErrorInfo {
  val msg: String
}

/**
 * Represents http 400
 * @param msg
 */
case class NotFound(msg: String) extends ErrorInfo

/**
 * Represents http 403
 * @param msg
 */
case class Forbidden(msg: String) extends ErrorInfo

/**
 * Represents http 400
 * @param msg
 */
case class BadRequest(msg: String) extends ErrorInfo

/**
 * Represents http 500
 * @param msg
 */
case class InternalServerError(msg: String) extends ErrorInfo