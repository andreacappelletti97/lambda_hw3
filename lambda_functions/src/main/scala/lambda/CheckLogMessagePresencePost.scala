package lambda

import HelperUtils.{CreateLogger, ObtainConfigReference}
import lambda.CheckLogPresenceFunction.{deltaHandler, readFileFromS3}
import org.json4s.DefaultFormats
import org.json4s.native.Serialization.write
import scala.collection.JavaConverters._
import scala.collection.mutable.ArrayBuffer

class CheckLogMessagePresencePost

object CheckLogMessagePresencePost  {

  val logger = CreateLogger(classOf[CheckLogMessagePresencePost])
  val config = ObtainConfigReference("config") match {
    case Some(value) => value
    case None => throw new RuntimeException("Cannot obtain a reference to the config data.")
  }

  def handle(time:String, delta:String): Response ={
    val logFile = readFileFromS3()
    val logTimeStampBuffer = ArrayBuffer[String]()
    logFile.foreach(token =>
      logTimeStampBuffer += token.toString.split(" ")(0))
    val logTimeStampArray = logTimeStampBuffer.toArray
    val result = deltaHandler(logTimeStampArray, time, delta)
    val jsonResponse = JsonResponse(result)
    implicit val formats: DefaultFormats = DefaultFormats
    val jsonString = write(jsonResponse)
    //Return result
    logger.info("Returning results...")
    Response(jsonString, Map(config.getString("config.api.contentType") -> config.getString("config.api.responseType")))
  }

  //Reponse in Json encoding
  case class JsonResponse(found: Boolean)

  //Class to build the response code
  case class Response(body: String, headers: Map[String,String], statusCode: Int = config.getInt("config.api.successStatusCode")) {
    logger.info("Return response...")
    def javaHeaders: java.util.Map[String, String] = headers.asJava
  }
}
