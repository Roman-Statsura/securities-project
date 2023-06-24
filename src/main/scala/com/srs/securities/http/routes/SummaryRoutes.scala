package com.srs.securities.http.routes

import io.circe.generic.auto.*
import org.http4s.circe.CirceEntityCodec.*
import scala.language.implicitConversions

import org.http4s.*
import cats.effect.*
import org.http4s.dsl.*
import org.http4s.dsl.impl.*
import org.http4s.server.*
import cats.*
import cats.implicits.*
import org.typelevel.log4cats.Logger

import java.util.UUID
import fs2.io.Watcher.EventType.Created

import com.srs.securities.core.*
import com.srs.securities.domain.summary.*

class SummaryRoutes[F[_]: Concurrent: Logger] private (summaries: Summaries[F])
    extends Http4sDsl[F] {
  private val findSummaryRoutes: HttpRoutes[F] = HttpRoutes.of[F] { case GET -> Root / dateStr =>
    for {
      s    <- summaries.summary(dateStr)
      resp <- Ok(s)
    } yield resp

  }

  val routes = Router(
    "/summary" -> (findSummaryRoutes)
  )
}

object SummaryRoutes {
  def apply[F[_]: Concurrent: Logger](summaries: Summaries[F]) =
    new SummaryRoutes[F](summaries)
}
