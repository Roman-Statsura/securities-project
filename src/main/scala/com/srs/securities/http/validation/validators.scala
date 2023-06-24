package com.srs.securities.http.validation

import cats.*
import cats.data.*
import cats.data.Validated.*
import cats.implicits.*
import java.net.URL
import scala.util.{Try, Success, Failure}

import com.srs.securities.domain.security.*
import com.srs.securities.http.dto.*

object validators {

  sealed trait ValidationFailure(val errorMessage: String)
  case class EmptyField(fieldName: String) extends ValidationFailure(s"'$fieldName' is empty")

  type ValidationResult[A] = ValidatedNel[ValidationFailure, A]

  trait Validator[A] {
    def validate(value: A): ValidationResult[A]
  }

  def validateRequired[A](field: A, fieldName: String)(
      required: A => Boolean
  ): ValidationResult[A] =
    if (required(field)) field.validNel
    else EmptyField(fieldName).invalidNel

  given securityValidator: Validator[SecurityDto] = (security: SecurityDto) => {
    val SecurityDto(
      secid,
      regnumber,
      name,
      emitent_title
    ) = security

    val validSecid        = validateRequired(secid, "secid")(_.nonEmpty)
    val validRegnumber    = validateRequired(regnumber, "regnumber")(_.nonEmpty)
    val validName         = validateRequired(name, "name")(_.nonEmpty)
    val validEmitentTitle = validateRequired(emitent_title, "emitent_title")(_.nonEmpty)

    (
      validSecid,
      validRegnumber,
      validName,
      validEmitentTitle
    ).mapN(SecurityDto.apply)
  }
}
