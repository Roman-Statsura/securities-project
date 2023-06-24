package com.srs.securities.domain

import java.util.UUID

object history {
  case class History(
    id: UUID = UUID.randomUUID(),
    secid: String,
    tradedate: String,
    numtrades: Double,
    open: Option[Double],
    close: Option[Double]
  )
}
