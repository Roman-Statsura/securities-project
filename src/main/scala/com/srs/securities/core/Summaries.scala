package com.srs.securities.core

import cats.*
import cats.implicits.*
import cats.effect.*
import doobie.*
import doobie.implicits.*
import doobie.postgres.implicits.*
import doobie.util.*
import doobie.util.fragment.Fragment
import java.util.UUID
import org.typelevel.log4cats.Logger

import com.srs.securities.domain.summary.*

trait Summaries[F[_]] {
  def summary(date: String): F[List[Summary]]
}

final class LiveSummaries[F[_]: MonadCancelThrow: Logger] private (xa: Transactor[F])
    extends Summaries[F] {
  override def summary(date: String): F[List[Summary]] =
    sql"""
      SELECT securities.secid, securities.regnumber, securities.name, securities.emitent_title,
        histories.tradedate, histories.numtrades, histories.open, histories.close
      FROM securities 
      JOIN histories ON securities.secid = histories.secid
      WHERE histories.tradedate =${date}
    """
      .query[Summary]
      .to[List]
      .transact(xa)
}

object LiveSummaries {
  def apply[F[_]: MonadCancelThrow: Logger](xa: Transactor[F]): F[LiveSummaries[F]] =
    new LiveSummaries[F](xa).pure[F]
}
