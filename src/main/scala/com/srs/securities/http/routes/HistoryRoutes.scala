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
import com.srs.securities.domain.history.*
import com.srs.securities.logging.syntax.*
import com.srs.securities.http.responses.*
import com.srs.securities.http.validation.syntax.*

import com.srs.securities.http.dto.*

class HistoryRoutes[F[_]: Concurrent: Logger] private (
    histories: Histories[F],
    securities: Securities[F]
) extends HttpValidationDsl[F] {

  private val allHistoriesRoute: HttpRoutes[F] = HttpRoutes.of[F] { case GET -> Root =>
    for {
      historiesList <- histories.all()
      resp          <- Ok(historiesList)
    } yield resp
  }

  private val findHistoryByIdRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root / UUIDVar(id) =>
      histories.findById(id) flatMap {
        case Some(history) => Ok(history)
        case None          => NotFound(FailureResponse(s"History $id not found"))
      }
  }

  private val findHistoriesBySecidRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root / secid =>
      for {
        historiesList <- histories.findBySecid(secid)
        resp          <- Ok(historiesList)
      } yield resp
  }

  private val createHistoryRoute: HttpRoutes[F] = HttpRoutes.of[F] {
    case req @ POST -> Root / "create" =>
      req.validate[HistoryDto] { historyDto =>
        securities.findBySecid(historyDto.secid).map(_.nonEmpty) flatMap {
          case true => histories.create(historyDto.toDomain).flatMap(Created(_))
          case flase =>
            NotFound(
              FailureResponse(
                s"Cannot create history with ${historyDto.secid}, becase there is no security with such secid"
              )
            )
        }
      }
  }

  private val updateHistoryRoute: HttpRoutes[F] = HttpRoutes.of[F] {
    case req @ PUT -> Root / UUIDVar(id) =>
      req.validate[HistoryDto] { historyDto =>
        histories.findById(id) flatMap {
          case None =>
            NotFound(FailureResponse(s"Cannot update history $id: not found"))
          case Some(his) =>
            if (his.secid == historyDto.secid)
              histories.update(historyDto.toDomainWithId(id)) *> Ok()
            else {
              securities.findBySecid(historyDto.secid).map(_.nonEmpty) flatMap {
                case true => histories.update(historyDto.toDomainWithId(id)) *> Ok()
                case false =>
                  NotFound(
                    FailureResponse(
                      s"Cannot update history with id: $id, becase does not have any securities with secid: ${historyDto.secid}"
                    )
                  )
              }
            }
        }
      }
  }

  private val deleteHistoryRoute: HttpRoutes[F] = HttpRoutes.of[F] {
    case req @ DELETE -> Root / UUIDVar(id) =>
      histories.findById(id) flatMap {
        case None =>
          NotFound(FailureResponse(s"Cannot delete history $id: not found"))
        case Some(_) => histories.delete(id) *> Ok()
      }
  }

  private val deleteHistoryBySecidRoute: HttpRoutes[F] = HttpRoutes.of[F] {
    case req @ DELETE -> Root / "bySecid" / secid =>
      histories.findBySecid(secid).flatMap { list => 
        if (list.isEmpty)
          NotFound(FailureResponse(s"Cannot delete history with secid $secid: not found"))
        else
          list.traverse(h => histories.delete(h.id)) *> Ok()
      }
  }

  val routes = Router(
    "/histories" -> (allHistoriesRoute <+> findHistoryByIdRoutes <+> findHistoriesBySecidRoutes <+> 
      createHistoryRoute <+> updateHistoryRoute <+> deleteHistoryRoute <+> deleteHistoryBySecidRoute)
  )
}

object HistoryRoutes {
  def apply[F[_]: Concurrent: Logger](histories: Histories[F], securities: Securities[F]) =
    new HistoryRoutes[F](histories, securities)
}
