package com.github.pshirshov.izumi.distage.planning

import com.github.pshirshov.izumi.distage.definition.Binding
import com.github.pshirshov.izumi.distage.model.plan.{DodgyPlan, NextOps}

trait PlanMergingPolicy {
  def extendPlan(currentPlan: DodgyPlan, binding: Binding, currentOp: NextOps): DodgyPlan
}