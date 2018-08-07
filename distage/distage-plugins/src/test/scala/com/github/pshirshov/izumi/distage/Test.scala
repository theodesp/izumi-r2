package com.github.pshirshov.izumi.distage

import org.scalatest._

trait TestKit extends WordSpec {

  val genFixture: () => Any

  class IzumiTest[Fixture](implicit ev: genFixture.type <:< (() => Fixture)) {
    val fixture: Fixture = ev(genFixture).apply()
  }

}

class Test extends TestKit {

  case class Fixture(int: Int)

  final val genFixture = () => Fixture(5)

  "Fixture" should {
    "be 5" in new IzumiTest {

      assert(fixture.int == 5)
    }

    "not 7" in new IzumiTest {
      import fixture._

      assert(int != 7)
    }
  }

}

