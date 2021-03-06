package com.github.pshirshov.izumi.idealingua.translator.toscala.types

import com.github.pshirshov.izumi.idealingua.model.common.{IndefiniteId, TypeName, TypePath}
import com.github.pshirshov.izumi.idealingua.model.il.ast.typed.Service
import com.github.pshirshov.izumi.idealingua.translator.toscala.STContext

import scala.meta._

final case class ServiceContext(ctx: STContext, svc: Service) {

  object BIO {
    val t = t"IRTBIO"
    val n = q"IRTBIO"
  }

  object F {
    val t = t"F"
    val p = tparam"F[_, _]"
  }

  object Ctx {
    val t = t"C"
    val p = tparam"C"
  }

  val typeName: TypeName = svc.id.name

  val basePath = TypePath(svc.id.domain, Seq(typeName))
  val svcBaseTpe: ScalaType = ctx.conv.toScala(IndefiniteId(svc.id.domain.toPackage, s"$typeName"))

  private def typeId(name: String): ScalaType = {
    ctx.conv.toScala(IndefiniteId(svc.id.domain.toPackage, name))
  }

  val svcServerTpe: ScalaType = typeId(s"${typeName}Server")
  val svcClientTpe: ScalaType = typeId(s"${typeName}Client")

  val svcWrappedServerTpe: ScalaType = typeId(s"${typeName}WrappedServer")
  val svcWrappedClientTpe: ScalaType = typeId(s"${typeName}WrappedClient")

  val svcMethods: ScalaType = svcBaseTpe
  val svcCodecs: ScalaType = typeId(s"${typeName}Codecs")
}
