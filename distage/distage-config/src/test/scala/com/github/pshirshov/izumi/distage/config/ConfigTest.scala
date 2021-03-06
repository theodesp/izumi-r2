package com.github.pshirshov.izumi.distage.config

import com.github.pshirshov.configapp._
import com.github.pshirshov.izumi.distage.config.model.AppConfig
import com.typesafe.config._
import distage.{Injector, ModuleDef}
import org.scalatest.WordSpec

import scala.collection.immutable.ListSet
import scala.collection.mutable

class ConfigTest extends WordSpec {
  def mkModule(): ConfigModule = {
    mkModule(ConfigFactory.load())
  }

  def mkModule(path: String): ConfigModule = {
    mkModule(ConfigFactory.load(path))
  }

  def mkModule(config: Config): ConfigModule = {
    val appConfig = AppConfig(config)
    new ConfigModule(appConfig, ConfigInjectorConfig(enableScalars = true))
  }

  "Config resolver" should {
    "resolve config references" in {
      val injector = Injector(mkModule())
      val plan = injector.plan(TestConfigApp.definition)

      val context = injector.produce(plan)

      assert(context.get[HttpServer1].listenOn.port == 8081)
      assert(context.get[HttpServer2].listenOn.port == 8082)
      assert(context.get[HttpServer3].listenOn.port == 8083)

      assert(context.get[DataPuller1].target.port == 9001)
      assert(context.get[DataPuller2].target.port == 9002)
      assert(context.get[DataPuller3].target.port == 9003)

      assert(context.get[TestAppService]("puller4").asInstanceOf[DataPuller1].target.port == 10010)
      assert(context.get[TestAppService]("puller5").asInstanceOf[DataPuller2].target.port == 10020)
      assert(context.get[TestAppService]("puller6").asInstanceOf[DataPuller3].target.port == 9003)
      assert(context.get[Set[TestAppService]].size == 9)
    }

    "resolve config references in set elements" in {
      val injector = Injector(mkModule())
      val plan = injector.plan(TestConfigApp.setDefinition)

      val context = injector.produce(plan)

      assert(context.get[Set[TestAppService]].head.asInstanceOf[DataPuller1].target.port == 9001)
    }

    "resolve config maps" in {
      val config = AppConfig(ConfigFactory.load("map-test.conf"))
      val injector = Injector(new ConfigModule(config))
      val plan = injector.plan(TestConfigReaders.mapDefinition)

      val context = injector.produce(plan)

      assert(context.get[Service[MapCaseClass]].conf.mymap.isInstanceOf[mutable.ListMap[_, _]])
      assert(context.get[Service[MapCaseClass]].conf.mymap.keySet == Set("service1", "service2", "service3"))
      assert(context.get[Service[MapCaseClass]].conf.mymap.values.forall(_.host == "localhost"))
    }

    "resolve config lists" in {
      val injector = Injector(mkModule("list-test.conf"))
      val plan = injector.plan(TestConfigReaders.listDefinition)

      val context = injector.produce(plan)

      assert(context.get[Service[ListCaseClass]].conf.mylist.isInstanceOf[IndexedSeq[_]])
      assert(context.get[Service[ListCaseClass]].conf.mylist.head.isInstanceOf[ListSet[_]])
      assert(context.get[Service[ListCaseClass]].conf.mylist.head ==
        Set(
          Wrapper(HostPort(80, "localhost"))
          , Wrapper(HostPort(8080, "localhost"))
          , Wrapper(HostPort(8888, "localhost"))
        )
      )
    }

    "resolve config options" in {
      val injector = Injector(mkModule("opt-test.conf"))
      val plan = injector.plan(TestConfigReaders.optDefinition)

      val context = injector.produce(plan)

      assert(context.get[Service[OptionCaseClass]].conf == OptionCaseClass(optInt = None))
    }

    "Inject config works for trait methods" in {
      import ConfigFixtures._

      val injector = Injector(mkModule("fixtures-test.conf"))

      val definition = new ModuleDef {
        make[TestDependency]
        make[TestTrait]
      }
      val plan = injector.plan(definition)
      val context = injector.produce(plan)

      assert(context.get[TestTrait].x == TestDependency(TestConf(false)))
      assert(context.get[TestTrait].testConf == TestConf(true))
      assert(context.get[TestDependency] == TestDependency(TestConf(false)))
    }

    "Inject config works for concrete and abstract factory products and factory methods" in {
      import ConfigFixtures._

      val injector = Injector(mkModule("fixtures-test.conf"))

      val definition = new ModuleDef {
        make[TestDependency]
        make[TestFactory]
        make[TestGenericConfFactory[TestConfAlias]]
      }
      val plan = injector.plan(definition)
      val context = injector.produce(plan)

      val factory = context.get[TestFactory]
      assert(factory.make(5) == ConcreteProduct(TestConf(true), 5))
      assert(factory.makeTrait().testConf == TestConf(true))
      assert(factory.makeTraitWith().asInstanceOf[AbstractProductImpl].testConf == TestConf(true))

      assert(context.get[TestDependency] == TestDependency(TestConf(false)))

      assert(context.get[TestGenericConfFactory[TestConf]].x == TestDependency(TestConf(false)))
      assert(context.get[TestGenericConfFactory[TestConf]].make().testConf == TestConf(false))
    }

  }

}

