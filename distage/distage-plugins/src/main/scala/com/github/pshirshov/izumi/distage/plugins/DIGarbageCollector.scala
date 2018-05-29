package com.github.pshirshov.izumi.distage.plugins

import com.github.pshirshov.izumi.distage.model.definition.SimpleModuleDef
import com.github.pshirshov.izumi.distage.model.plan.ExecutableOp.ProxyOp.{InitProxy, MakeProxy}
import com.github.pshirshov.izumi.distage.model.plan.ExecutableOp.CreateSet
import com.github.pshirshov.izumi.distage.model.plan.ExecutableOp.{ImportDependency, WiringOp}
import com.github.pshirshov.izumi.distage.model.plan.{ExecutableOp, FinalPlan, FinalPlanImmutableImpl}
import com.github.pshirshov.izumi.distage.model.reflection.universe.RuntimeDIUniverse
import com.github.pshirshov.izumi.fundamentals.platform.language.Quirks

import scala.annotation.tailrec
import scala.collection.mutable


trait DIGarbageCollector {
  def gc(plan: FinalPlan, isRoot: RuntimeDIUniverse.DIKey => Boolean): FinalPlan
}

object DIGarbageCollector {
  def isRoot(roots: Set[RuntimeDIUniverse.DIKey])(key: RuntimeDIUniverse.DIKey): Boolean = {
    roots.contains(key)
  }
}

object NullDiGC extends DIGarbageCollector {
  override def gc(plan: FinalPlan, isRoot: RuntimeDIUniverse.DIKey => Boolean): FinalPlan = {
    Quirks.discard(isRoot)
    plan
  }
}

object TracingDIGC extends DIGarbageCollector {
  override def gc(plan: FinalPlan, isRoot: RuntimeDIUniverse.DIKey => Boolean): FinalPlan = {
    val toLeave = mutable.HashSet[RuntimeDIUniverse.DIKey]()
    toLeave ++= plan.steps.map(_.target).filter(isRoot)
    allDeps(plan.steps.map(v => v.target -> v).toMap, toLeave.toSet, toLeave)
    val refinedPlan = SimpleModuleDef(plan.definition.bindings.filter(b => toLeave.contains(b.key)))
    val steps = plan.steps.filter(s => toLeave.contains(s.target))
    FinalPlanImmutableImpl(refinedPlan, steps)
  }

  @tailrec
  private def allDeps(ops: Map[RuntimeDIUniverse.DIKey, ExecutableOp], depsToTrace: Set[RuntimeDIUniverse.DIKey], deps: mutable.HashSet[RuntimeDIUniverse.DIKey]): Unit = {
    // TODO: inefficient

    val newDeps = depsToTrace.map(ops.apply).flatMap {
      case w: WiringOp =>
        w.wiring.associations.map(_.wireWith)
      case c: CreateSet =>
        c.members
      case p: InitProxy =>
        p.dependencies
      case _: MakeProxy =>
        Seq.empty
      case _: ImportDependency =>
        Seq.empty
      case _ =>
        Seq.empty
    }

    if (newDeps.nonEmpty) {
      deps ++= newDeps
      allDeps(ops, newDeps, deps)
    }
  }
}