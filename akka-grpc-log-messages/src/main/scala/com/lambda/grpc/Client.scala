package com.lambda.grpc

//#import
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Failure
import scala.util.Success
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.grpc.GrpcClientSettings
import com.typesafe.config.ConfigFactory

//#import

//#client-request-reply
object Client {
  object Config {
    val conf = ConfigFactory.load()
    def apply() = conf
  }
  def main(args: Array[String]): Unit = {
    implicit val sys: ActorSystem[_] = ActorSystem(Behaviors.empty, "Client")
    implicit val ec: ExecutionContext = sys.executionContext

    val client = LogMessageServiceClient(GrpcClientSettings.fromConfig("grpc.LogMessageService"))
    val time = Config().getString("akka.grpc.client.api.time")
    val delta = Config().getString("akka.grpc.client.api.delta")
    singleRequestReply(time, delta)

    def singleRequestReply(time: String, delta:String): Unit = {
      println(s"Performing request: $time and $delta")
      val reply = client.checkLogPresence(LogMessageRequest(time, delta))

      reply.onComplete {
        case Success(msg) =>
          println(msg)
        case Failure(e) =>
          println(s"Error: $e")
      }
    }
  }

}

