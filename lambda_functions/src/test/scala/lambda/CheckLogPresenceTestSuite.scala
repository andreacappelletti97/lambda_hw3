package lambda
import HelperUtils.{CreateLogger, ObtainConfigReference}
import org.scalatest.PrivateMethodTester
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
class CheckLogPresenceTestSuite  extends AnyFlatSpec with Matchers with PrivateMethodTester {

  val logger = CreateLogger(classOf[CheckLogPresenceTestSuite])

  val config = ObtainConfigReference("config") match {
    case Some(value) => value
    case None => throw new RuntimeException("Cannot obtain a reference to the config data.")
  }

  //This test checks the behavior of the isEmpty utility function used in Job3Mapper

  behavior of "Format function"

  "formatAdapter" should "return the right format with 1" in {
    val time = "0"
    assert(CheckLogPresenceFunction.formatAdapter(time).equals( "00"))
  }

  "formatAdapter" should "return the right format with 2" in {
    val time = "12"
    assert(CheckLogPresenceFunction.formatAdapter(time).equals( "12"))
  }
}


