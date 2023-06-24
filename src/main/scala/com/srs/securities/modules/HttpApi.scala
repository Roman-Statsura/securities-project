package com.srs.securities.modules

import org.http4s.*
import cats.effect.*
import cats.data.OptionT
import org.http4s.dsl.*
import org.http4s.dsl.impl.*
import org.http4s.server.*
import cats.*
import cats.implicits.*
import org.typelevel.log4cats.Logger

import com.srs.securities.config.*
import com.srs.securities.http.routes.*
import com.srs.securities.core.*

class HttpApi[F[_]: Concurrent: Logger] private (core: Core[F]) {
  private val securityRoutes = SecurityRoutes[F](core.securities, core.histories).routes
  private val historyRoutes  = HistoryRoutes[F](core.histories, core.securities).routes
  private val summaryRoutes  = SummaryRoutes[F](core.summaries).routes

  val endpoints = Router(
    "/api" -> (securityRoutes <+> historyRoutes <+> summaryRoutes)
  )
}

object HttpApi {
  def apply[F[_]: Async: Logger](
      core: Core[F]
  ): Resource[F, HttpApi[F]] =
    val httpF = new HttpApi[F](core).pure[F]
    Resource.eval(httpF)
}
