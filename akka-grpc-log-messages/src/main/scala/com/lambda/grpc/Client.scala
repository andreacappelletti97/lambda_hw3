package com.lambda.grpc

//#import
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Failure
import scala.util.Success
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.grpc.GrpcClientSettings

//#import

//#client-request-reply
object Client {

  def main(args: Array[String]): Unit = {
    implicit val sys: ActorSystem[_] = ActorSystem(Behaviors.empty, "Client")
    implicit val ec: ExecutionContext = sys.executionContext

    val client = LogMessageServiceClient(GrpcClientSettings.fromConfig("grpc.LogMessageService"))
    singleRequestReply("01:10:40.134", "00:00:03.000")

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

