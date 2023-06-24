package com.srs.securities.http.dto

import com.srs.securities.domain.security.*
import java.util.UUID

case class SecurityDto(
    secid: String,
    regnumber: String,
    name: String,
    emitent_title: String
) {
  def toSec =
    Security(secid = secid, regnumber = regnumber, name = name, emitent_title = emitent_title)

  def toSecWithId(id: UUID) = toSec.copy(id = id)
}
