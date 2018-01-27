package com.github.pshirshov.izumi.distage.model.exceptions

import com.github.pshirshov.izumi.distage.TypeFull

class UnsafeCallArgsMismatched(message: String, val expected: Seq[TypeFull], val actual: Seq[Any]) extends DIException(message, null)