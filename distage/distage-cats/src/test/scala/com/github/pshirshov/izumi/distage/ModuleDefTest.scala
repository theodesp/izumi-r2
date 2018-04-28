package com.github.pshirshov.izumi.distage

import com.github.pshirshov.izumi.distage.Fixtures._
import com.github.pshirshov.izumi.distage.model.definition.Bindings.binding
import com.github.pshirshov.izumi.distage.model.definition.{Bindings, ModuleDef, ModuleBuilder, TrivialModuleDef}
import com.github.pshirshov.izumi.distage.definition.MagicDSL._
import org.scalatest.WordSpec
import DIStageInstances._
import cats.implicits._

class ModuleDefTest extends WordSpec {
  "cats instances for ContextDefinition" should {
    "allow monoidal operations between different types of binding dsls" in {
      import Case1._

      val mod1: ModuleDef = new ModuleBuilder {
        bind[TestClass]
      }

      val mod2: ModuleDef = TrivialModuleDef
        .bind[TestCaseClass2]

      val mod3: ModuleDef = TrivialModuleDef
        .magic[TestDependency1]
        .magic[NotInContext]

      val mod4: ModuleDef = Set(
        binding(TestInstanceBinding())
      )

      val mod5: ModuleDef = (TrivialModuleDef
        + Bindings.binding[TestDependency0, TestImpl0]
        )

      val combinedModules = Vector(mod1, mod2, mod3, mod4, mod5).combineAll

      val plusModules = mod5 |+| mod4 |+| mod3 |+| mod2 |+| mod1

      val complexModule = TrivialModuleDef
        .bind[TestClass]
        .bind[TestDependency0].as[TestImpl0]
        .bind[TestCaseClass2]
        .bind(TestInstanceBinding())
        .++(mod3) // function pointer equality on magic trait providers

      assert(combinedModules === complexModule)
      assert(plusModules === complexModule)
    }
  }
}