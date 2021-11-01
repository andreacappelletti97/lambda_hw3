package com.lambda.grpc

import scala.concurrent.Future
import scalaj.http.{Http, HttpOptions, HttpResponse}
import akka.actor.typed.ActorSystem


class ServiceImpl(system: ActorSystem[_]) extends LogMessageService {
  private implicit val sys: ActorSystem[_] = system
  override def checkLogPresence(request: LogMessageRequest): Future[LogMessageReply] = {
    val requestPath = s"http://127.0.0.1:3000/checkLogPresence/${request.time}/${request.delta}"
    val response: HttpResponse[String] = Http(requestPath)
      .option(HttpOptions.connTimeout(10000))
      .option(HttpOptions.readTimeout(50000)).asString
    System.out.println(response.body)
    Future.successful(LogMessageReply(response.body))
  }

}

