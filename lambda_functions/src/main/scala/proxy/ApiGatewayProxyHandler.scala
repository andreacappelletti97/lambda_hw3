package proxy

import HelperUtils.{CreateLogger, ObtainConfigReference}
import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}
import com.amazonaws.services.lambda.runtime.events.{APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent}
import lambda.{CheckLogMessagePresencePost, CheckLogPresenceFunction, GetLogMessages, GetLogMessagesPost}

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
    //Detect the HTTP method used for the request
    val requestMethod : String = requestEvent.getHttpMethod
    val requestPath : String = requestEvent.getPath.split(config.getString("config.api.slash"))(1)
    requestMethod match {
      case "POST" => {
        val requestBody = requestEvent.getBody
        val splittedTime = (requestBody.split(config.getString("config.api.and")))(0)
        val splittedDelta = (requestBody.split(config.getString("config.api.and")))(1)
        val time = splittedTime.split(config.getString("config.api.equal"))(1)
        val delta = splittedDelta.split(config.getString("config.api.equal"))(1)
        println(time)
        println(delta)
        requestPath match {
          case "checkLogPresence" => {
            logger.info("Get log messages function is called with POST method")
            val response = CheckLogMessagePresencePost.handle(time, delta)
            return new APIGatewayProxyResponseEvent().withBody(response.body).withStatusCode(response.statusCode).withHeaders(response.javaHeaders)
          }
          case "getLogMessages" => {
            logger.info("Get log messages function is called with POST method")
            val response = GetLogMessagesPost.handle(time, delta)
            return new APIGatewayProxyResponseEvent().withBody(response.body).withStatusCode(response.statusCode).withHeaders(response.javaHeaders)
          }
          case _  => {
            logger.error("Invalid request")
            val headersMap = new util.HashMap[String, String]() {}
            new APIGatewayProxyResponseEvent().withBody(config.getString("config.api.badRequestBody")).withStatusCode(config.getInt("config.api.badRequestStatusCode")).withHeaders(headersMap)
          }
        }
      }
      case "GET" => {
        System.out.println(requestEvent.getHttpMethod)
        requestPath match {
          case "checkLogPresence" => {
            logger.info("Get log messages function is called")
            val response = CheckLogPresenceFunction.handle(requestEvent, context)
            return new APIGatewayProxyResponseEvent().withBody(response.body).withStatusCode(response.statusCode).withHeaders(response.javaHeaders)
          }
          case "getLogMessages" => {
            logger.info("Get log messages function is called")
            val response = GetLogMessages.handle(requestEvent, context)
            return new APIGatewayProxyResponseEvent().withBody(response.body).withStatusCode(response.statusCode).withHeaders(response.javaHeaders)
          }
          case _  => {
            logger.error("Invalid request")
            val headersMap = new util.HashMap[String, String]() {}
            new APIGatewayProxyResponseEvent().withBody(config.getString("config.api.badRequestBody")).withStatusCode(config.getInt("config.api.badRequestStatusCode")).withHeaders(headersMap)
          }
        }
      }
      case _  => {
        logger.error("Invalid request")
        val headersMap = new util.HashMap[String, String]() {}
        new APIGatewayProxyResponseEvent().withBody(config.getString("config.api.badRequestBody")).withStatusCode(config.getInt("config.api.badRequestStatusCode")).withHeaders(headersMap)
      }
    }

     }
}
