package com.srs.securities.domain

import java.util.UUID

object security {
  case class Security(
      id: UUID,
      secid: String,
      regnumber: String,
      name: String,
      emitent_title: String
  )
}
