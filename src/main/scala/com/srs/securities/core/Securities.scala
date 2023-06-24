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

import com.srs.securities.domain.security.*

trait Securities[F[_]] {
  def find(id: UUID): F[Option[Security]]
  def all(): F[List[Security]]
  def create(security: Security): F[UUID]
  def update(security: Security): F[Option[Security]]
  def delete(id: UUID): F[Boolean]
}

final class LiveSecurities[F[_]: MonadCancelThrow: Logger] private (xa: Transactor[F])
    extends Securities[F] {
  override def find(id: UUID): F[Option[Security]] =
    sql"SELECT * FROM securities WHERE id = ${id}"
      .query[Security]
      .option
      .transact(xa)

  override def all(): F[List[Security]] =
    sql"SELECT * FROM securities"
      .query[Security]
      .to[List]
      .transact(xa)

  override def create(security: Security): F[UUID] =
    sql"""
    INSERT INTO securities (
      secid,
      regnumber,
      name,
      emitent_title
    ) VALUES (
      ${security.secid},
      ${security.regnumber},
      ${security.name},
      ${security.emitent_title}
    )
    """.update.run
      .transact(xa)
      .map(_ => security.id)

  override def update(security: Security): F[Option[Security]] =
    for {
      _ <- sql"""
            UPDATE securities SET
              secid = ${security.secid},
              regnumber = ${security.regnumber},
              name = ${security.name},
              emitent_title = ${security.emitent_title}
            WHERE id = ${security.id}  
          """.update.run.transact(xa)
      maybeSecurity <- find(security.id)
    } yield maybeSecurity

  override def delete(id: UUID): F[Boolean] =
    sql"DELETE FROM securities where id=${id}".update.run.transact(xa).map(_ > 0)
}

object LiveSecurities {
  def apply[F[_]: MonadCancelThrow: Logger](xa: Transactor[F]): F[LiveSecurities[F]] =
    new LiveSecurities[F](xa).pure[F]
}
