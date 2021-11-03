package lambda
import HelperUtils.ObtainConfigReference
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ConfigTestSuite extends AnyFlatSpec with Matchers {

  val config = ObtainConfigReference("config") match {
    case Some(value) => value
    case None => throw new RuntimeException("Cannot obtain a reference to the config data.")
  }
  behavior of "configuration parameters module"

  //This test checks if the right configuration is loaded

  it should "obtain the right response type" in {
    config.getString("config.api.responseType") shouldBe "application/json"
  }

  it should "Set the right encryption method" in {
    config.getString("config.api.encryptionMethod") shouldBe "MD5"
  }

  it should "Set the right success code" in {
    config.getInt("config.api.successStatusCode") shouldBe 200
  }

  it should "Set the right error code" in {
    config.getInt("config.api.badRequestStatusCode") shouldBe 400
  }
}