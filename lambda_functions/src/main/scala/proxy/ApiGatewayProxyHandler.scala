package proxy

import HelperUtils.{CreateLogger, ObtainConfigReference}
import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}
import com.amazonaws.services.lambda.runtime.events.{APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent}
import lambda.{CheckLogPresenceFunction, GetLogMessages}

import java.util
import java.util.{HashMap, Map}

class ApiGatewayProxyHandler extends RequestHandler[APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent] {
  val logger = CreateLogger(classOf[GetLogMessages])
  val config = ObtainConfigReference("config") match {
    case Some(value) => value
    case None => throw new RuntimeException("Cannot obtain a reference to the config data.")
  }

  override def handleRequest(requestEvent: APIGatewayProxyRequestEvent, context: Context): APIGatewayProxyResponseEvent = {
    System.out.println("handle this request!!!")

    if (requestEvent.getPath.contains("checkLogPresence")) {
      logger.info("Check log presence function is called")
      val response = CheckLogPresenceFunction.handle(requestEvent, context)
      return new APIGatewayProxyResponseEvent().withBody(response.body).withStatusCode(response.statusCode).withHeaders(response.javaHeaders)
    }
    else if (requestEvent.getPath.contains("getLogMessages")) {
      logger.info("Get log messages function is called")
      val response = GetLogMessages.handle(requestEvent, context)
      return new APIGatewayProxyResponseEvent().withBody(response.body).withStatusCode(response.statusCode).withHeaders(response.javaHeaders)
    }
    val headersMap = new util.HashMap[String, String]() {}
    new APIGatewayProxyResponseEvent().withBody(config.getString("config.api.badRequestBody")).withStatusCode(config.getInt("config.api.badRequestStatusCode")).withHeaders(headersMap)
  }
}
