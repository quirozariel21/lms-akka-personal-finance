package com.applaudo.akkalms.models.errors

/**
 * Trait for custom http code handling for error cases.
 */
sealed trait ErrorInfo

/**
 * Represents http 204
 * @param msg
 * @param code
 */
case object NoContent extends ErrorInfo

/**
 * Represents http 400
 * @param msg
 */
case class NotFound(msg: String, code: Int) extends ErrorInfo

/**
 * Represents http 403
 * @param msg
 */
case class Forbidden(msg: String, code: Int) extends ErrorInfo

/**
 * Represents http 400
 * @param msg
 */
case class BadRequest(msg: String, code: Int) extends ErrorInfo

/**
 * Represents http 500
 * @param msg
 */
case class InternalServerError(msg: String, code: Int) extends ErrorInfo

/** Default case. */
case class ErrorMessage(msg: String) extends ErrorInfo