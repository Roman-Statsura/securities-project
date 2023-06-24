package com.srs.securities.http.dto

import com.srs.securities.domain.history.*
import java.util.UUID

case class HistoryDto(
    secid: String,
    tradedate: String,
    numtrades: Double,
    open: Option[Double],
    close: Option[Double]
  ) {
    def toDomain =
      History(secid = secid, tradedate = tradedate, numtrades = numtrades, open = open, close = close)

    def toDomainWithId(id: UUID) = toDomain.copy(id = id)
  }
