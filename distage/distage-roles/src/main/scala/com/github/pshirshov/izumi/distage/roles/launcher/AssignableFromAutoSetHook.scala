package com.github.pshirshov.izumi.distage.roles.launcher

import com.github.pshirshov.izumi.distage.model.plan.{ExecutableOp, SemiPlan}
import com.github.pshirshov.izumi.distage.model.planning.PlanningHook
import com.github.pshirshov.izumi.distage.model.reflection.universe.RuntimeDIUniverse.{DIKey, Tag, Wiring, mirror, u}

class AssignableFromAutoSetHook[T: Tag] extends PlanningHook {
  protected val setElemetTag: u.TypeTag[T] = Tag[T].tag
  protected val setElementClass: Class[_] = mirror.runtimeClass(setElemetTag.tpe)
  protected val setElementKey: DIKey = DIKey.get[T]

  protected val setKey: DIKey = DIKey.get[Set[T]]

  override def phase10PostFinalization(plan: SemiPlan): SemiPlan = {
    val newMembers = scala.collection.mutable.ArrayBuffer[DIKey.SetElementKey]()

    val newSteps = plan.steps.flatMap {
      op =>
        val opTargetClass = mirror.runtimeClass(op.target.tpe.tpe)
        if (setElementClass.isAssignableFrom(opTargetClass)) {
          val elementKey = DIKey.SetElementKey(op.target, plan.steps.size + newMembers.size, setKey.tpe)
          newMembers += elementKey
          Seq(op, ExecutableOp.WiringOp.ReferenceKey(elementKey, Wiring.UnaryWiring.Reference(op.target.tpe, op.target, weak = true), op.origin))
        } else if (op.target == setKey) {
          Seq.empty
        } else {
          Seq(op)
        }
    }

    val newSetKeys: scala.collection.immutable.Set[DIKey] = newMembers.toSet
    val newSetOp = ExecutableOp.CreateSet(setKey, setKey.tpe, newSetKeys, None)
    SemiPlan(plan.definition, newSteps :+ newSetOp)
  }
}
