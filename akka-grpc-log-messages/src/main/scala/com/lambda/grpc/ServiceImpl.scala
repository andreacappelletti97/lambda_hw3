package com.lambda.grpc

import scala.concurrent.Future
import scalaj.http.{Http, HttpOptions, HttpResponse}
import akka.actor.typed.ActorSystem
import com.typesafe.config.ConfigFactory


class ServiceImpl(system: ActorSystem[_]) extends LogMessageService {
  object Config {
    val conf = ConfigFactory.load()
    def apply() = conf
  }
  private implicit val sys: ActorSystem[_] = system
  override def checkLogPresence(request: LogMessageRequest): Future[LogMessageReply] = {
      //Get param from the config
    val endpoint = Config().getString("akka.grpc.client.api.endpoint")
    val timeOut = Config().getInt("akka.grpc.client.api.timeOut")
    val readTimeOut = Config().getInt("akka.grpc.client.api.readTimeOut")
    //Build the path for the request to AWS lambda
    val requestPath = s"${endpoint}/${request.time}/${request.delta}"
    //Make the request and get the response
    val response: HttpResponse[String] = Http(requestPath)
      .option(HttpOptions.connTimeout(timeOut))
      .option(HttpOptions.readTimeout(readTimeOut)).asString
    System.out.println(response.body)
    Future.successful(LogMessageReply(response.body))
  }

}

