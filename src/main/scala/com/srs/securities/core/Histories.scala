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

import com.srs.securities.domain.history.*

trait Histories[F[_]] {
  def findBySecid(secid: String): F[List[History]]
  def findById(id: UUID): F[Option[History]]
  def all(): F[List[History]]
  def create(history: History): F[UUID]
  def update(history: History): F[Option[History]]
  def delete(id: UUID): F[Boolean]
  def deleteBySecid(secid: String): F[Boolean]
}

final class LiveHistories[F[_]: MonadCancelThrow: Logger] private (xa: Transactor[F])
    extends Histories[F] {

  override def findById(id: UUID): F[Option[History]] =
    sql"SELECT * FROM histories WHERE id = ${id}"
      .query[History]
      .option
      .transact(xa)

  override def findBySecid(secid: String): F[List[History]] =
    sql"SELECT * FROM histories WHERE secid = ${secid}"
      .query[History]
      .to[List]
      .transact(xa)

  override def all(): F[List[History]] =
    sql"SELECT * FROM histories"
      .query[History]
      .to[List]
      .transact(xa)

  override def create(history: History): F[UUID] =
    sql"""
    INSERT INTO histories (
      id,
      secid,
      tradedate,
      numtrades,
      open,
      close
    ) VALUES (
      ${history.id},
      ${history.secid},
      ${history.tradedate},
      ${history.numtrades},
      ${history.open},
      ${history.close}
    )
    """.update.run
      .transact(xa)
      .map(_ => history.id)

  override def update(history: History): F[Option[History]] =
    for {
      _ <- sql"""
            UPDATE histories SET
              secid = ${history.secid},
              tradedate = ${history.tradedate},
              numtrades = ${history.numtrades},
              open = ${history.open},
              close = ${history.close}
            WHERE id = ${history.id}  
          """.update.run.transact(xa)
      maybeHistory <- findById(history.id)
    } yield maybeHistory

  override def delete(id: UUID): F[Boolean] =
    sql"DELETE FROM histories where id=${id}".update.run.transact(xa).map(_ > 0)  

  override def deleteBySecid(secid: String): F[Boolean] =
    sql"DELETE FROM histories where secid=${secid}".update.run.transact(xa).map(_ > 0)
}

object LiveHistories {
  def apply[F[_]: MonadCancelThrow: Logger](xa: Transactor[F]): F[LiveHistories[F]] =
    new LiveHistories[F](xa).pure[F]
}
