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
import com.srs.securities.http.validation.syntax.*

import com.srs.securities.http.dto.*
import com.srs.securities.domain.security

class SecurityRoutes[F[_]: Concurrent: Logger] private (securities: Securities[F])
    extends HttpValidationDsl[F] {

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

  private val createSecurityRoute: HttpRoutes[F] = HttpRoutes.of[F] {
    case req @ POST -> Root / "create" =>
      req.validate[SecurityDto] { securityDto =>
        for {
          id   <- securities.create(securityDto.toSec)
          resp <- Created(id)
        } yield resp
      }
  }

  private val updateSecurityRoute: HttpRoutes[F] = HttpRoutes.of[F] {
    case req @ PUT -> Root / UUIDVar(id) =>
      req.validate[SecurityDto] { securityDto =>
        securities.find(id) flatMap {
          case None =>
            NotFound(FailureResponse(s"Cannot update security $id: not found"))
          case Some(sec) =>
            securities.update(securityDto.toSecWithId(id)) *> Ok()
        }
      }
  }

  private val deleteSecurityRoute: HttpRoutes[F] = HttpRoutes.of[F] {
    case req @ DELETE -> Root / UUIDVar(id) =>
      securities.find(id) flatMap {
        case None =>
          NotFound(FailureResponse(s"Cannot delete security $id: not found"))
        case Some(_) => securities.delete(id) *> Ok()
      }
  }

  val routes = Router(
    "/securities" -> (allSecuritiesRoute <+> findSecurityRoutes <+> createSecurityRoute <+> updateSecurityRoute <+> deleteSecurityRoute)
  )
}

object SecurityRoutes {
  def apply[F[_]: Concurrent: Logger](securities: Securities[F]) =
    new SecurityRoutes[F](securities)
}
