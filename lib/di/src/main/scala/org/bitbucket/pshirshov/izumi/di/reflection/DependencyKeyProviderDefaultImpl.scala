package org.bitbucket.pshirshov.izumi.di.reflection

import org.bitbucket.pshirshov.izumi.di.TypeSymb
import org.bitbucket.pshirshov.izumi.di.model.{DIKey, EqualitySafeType}

class DependencyKeyProviderDefaultImpl extends DependencyKeyProvider {

  override def keyFromMethod(methodSymbol: TypeSymb): DIKey = DIKey.TypeKey(EqualitySafeType(methodSymbol.info.resultType))

  override def keyFromParameter(parameterSymbol: TypeSymb): DIKey = DIKey.TypeKey(EqualitySafeType(parameterSymbol.info))
}