package com.github.pshirshov.izumi.logstage.distage

import com.github.pshirshov.izumi.distage.model.LoggerHook
import com.github.pshirshov.izumi.distage.model.definition.{Binding, StandardModuleDef, TrivialModuleDef}
import com.github.pshirshov.izumi.distage.model.planning.PlanningObserver
import com.github.pshirshov.izumi.logstage.api.IzLogger
import com.github.pshirshov.izumi.logstage.api.Log.CustomContext
import com.github.pshirshov.izumi.logstage.api.logger.LogRouter

class LogstageModule(router: LogRouter) extends StandardModuleDef {
  private val customizations = TrivialModuleDef
    .bind[LogRouter](router)
    .bind(CustomContext.empty)
    .bind[IzLogger]
    .bind[PlanningObserver].as[PlanningObserverLoggingImpl]
    .bind[LoggerHook].as[LoggerHookLoggingImpl]

  override def bindings: Set[Binding] = customizations.bindings
}