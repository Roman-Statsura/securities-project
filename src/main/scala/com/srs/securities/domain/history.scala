package com.srs.securities.domain

object history {
  case class History(
    secid: String,
    tradedate: String,
    numtrades: Double,
    open: Double,
    close: Double
  )
}
