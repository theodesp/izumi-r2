package com.github.pshirshov.izumi.idealingua.model

trait TypeId {
  def pkg: Package

  def name: TypeName
}

trait Builtin extends TypeId {
}

object TypeId {
  final val prelude: Package = Seq.empty

  case object TString extends Builtin {
    override def pkg: Package = prelude

    override def name: TypeName = TypeName("str")
  }

  case object TInt32 extends Builtin {
    override def pkg: Package = prelude

    override def name: TypeName = TypeName("i32")
  }

  case object TInt64 extends Builtin {
    override def pkg: Package = prelude

    override def name: TypeName = TypeName("i64")
  }

}