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
import com.srs.securities.domain.security.*
import com.srs.securities.logging.syntax.*
import com.srs.securities.http.responses.*

class SecurityRoutes[F[_]: Concurrent: Logger] private (securities: Securities[F])
    extends Http4sDsl[F] {

  private val allSecuritiesRoute: HttpRoutes[F] = HttpRoutes.of[F] { case GET -> Root =>
    for {
      securitiesList <- securities.all()
      resp           <- Ok(securitiesList)
    } yield resp
  }

  private val findSecurityRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root / UUIDVar(id) =>
      securities.find(id) flatMap {
        case Some(security) => Ok(security)
        case None           => NotFound(FailureResponse(s"Security $id not found"))
      }
  }

  val routes = Router(
    "/securities" -> (allSecuritiesRoute <+> findSecurityRoutes)
  )
}

object SecurityRoutes {
  def apply[F[_]: Concurrent: Logger](securities: Securities[F]) =
    new SecurityRoutes[F](securities)
}
