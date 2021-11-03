//#full-example
package com.lambda.grpc

import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.actor.typed.ActorSystem
import com.typesafe.config._
import org.scalatest.BeforeAndAfterAll
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.concurrent.duration._

class LogMessageServiceImplSpec
  extends AnyWordSpec
  with BeforeAndAfterAll
  with Matchers
  with ScalaFutures {

  val testKit = ActorTestKit()

  implicit val patience: PatienceConfig = PatienceConfig(scaled(5.seconds), scaled(100.millis))

  implicit val system: ActorSystem[_] = testKit.system
  val service = new ServiceImpl(system)

  override def afterAll(): Unit = {
    testKit.shutdownTestKit()
  }

  object Config {
    val conf = ConfigFactory.load()
    def apply() = conf
  }

  "LogMessageServiceImplementation" should {
    "reply to single request" in {
      val reply = service.checkLogPresence(LogMessageRequest("01:10:40.134", "00:00:03.000"))
      reply.futureValue should ===(LogMessageReply("{\"found\":true}"))
    }
    "Config endpoint" in {
      Config().getString("akka.grpc.client.api.endpoint").equals("https://i89ssvhs3d.execute-api.us-west-1.amazonaws.com/Prod/checkLogPresence")
    }
    "Config timeOut" in {
      Config().getString("akka.grpc.client.api.timeOut") === 10000
    }
    "Config readtimeOut" in {
      Config().getString("akka.grpc.client.api.timeOut") === 50000
    }
    "Config port" in {
      Config().getString("akka.grpc.client.port") === 8080
    }
  }
}
//#full-example
