package com.srs.securities.domain

object summary {
  case class Summary(
    secid: String,
    regnumber: String,
    name: String,
    emitent_title: String,
    tradedate: String,
    numtrades: Double,
    open: Option[Double],
    close: Option[Double]
  )
}
