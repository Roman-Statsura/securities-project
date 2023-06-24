package com.srs.securities.modules

import cats.*
import cats.implicits.*
import cats.effect.*
import doobie.util.transactor.Transactor

import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import com.srs.securities.core.*
import com.srs.securities.config.*

final class Core[F[_]] private (val securities: Securities[F])

// postgres -> jobs -> core -> app
object Core {
  def apply[F[_]: Async: Logger](
      xa: Transactor[F]
  ): Resource[F, Core[F]] = {
    val coreF = for {
      securities <- LiveSecurities[F](xa)
    } yield new Core(securities)

    Resource.eval(coreF)
  }
}
