package com.github.pshirshov.izumi.idealingua.runtime.rpc.http4s

import com.github.pshirshov.izumi.logstage.api.IzLogger
import org.http4s.dsl._

import scala.language.higherKinds

class Http4sRuntime[IO[_, _]]
(
  override protected val logger: IzLogger
)
  extends Http4sContext
    with WithHttp4sLoggingMiddleware
    with WithHttp4sClient
    with WithHttp4sServer {


  override type BIO[E, V] = IO[E, V]
  override protected val dsl: Http4sDsl[CIO] = org.http4s.dsl.io
}
