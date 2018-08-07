package com.github.pshirshov.izumi.logstage.api.rendering

import com.github.pshirshov.izumi.logstage.api.Log


trait RenderingPolicy {
  def render(entry: Log.Entry): String
}

object RenderingPolicy {
  case class PolicyConfig
  (
    withColors: Boolean = true,
    withExceptions: Boolean = true,
    prettyPrint: Boolean = false,
    renderingLayout: Option[String] = None,
  )
}
