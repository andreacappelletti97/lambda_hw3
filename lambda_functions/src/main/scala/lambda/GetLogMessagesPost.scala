package lambda

import lambda.GetLogMessages.{deltaHandler, md5HashString, readFileFromS3}
import org.json4s.DefaultFormats
import org.json4s.native.Serialization.write
import scala.collection.JavaConverters._
import scala.collection.mutable.ArrayBuffer
import HelperUtils.{CreateLogger, ObtainConfigReference}

class GetLogMessagesPost

object GetLogMessagesPost {

  val logger = CreateLogger(classOf[GetLogMessagesPost])
  val config = ObtainConfigReference("config") match {
    case Some(value) => value
    case None => throw new RuntimeException("Cannot obtain a reference to the config data.")
  }

  def handle(time:String, delta:String) : Response = {
    //Read file from AWS S3 bucket
    val logFile = readFileFromS3()
    //Populate array buffer with file content
    val logTimeStampBuffer = ArrayBuffer[String]()
    logFile.foreach(token =>
      logTimeStampBuffer += token.toString.split(" ")(0))
    //Cast the array buffer to array
    val logTimeStampArray = logTimeStampBuffer.toArray
    //Get the log messages detected using a binarySearch algorithm
    val deltaDetected = deltaHandler(logTimeStampArray, time, delta)
    val finalResponse = ArrayBuffer[String]()
    deltaDetected.foreach{
      timeDetected =>
        logFile.foreach(
          logMessage => if(logMessage.toString.contains(timeDetected)){
            finalResponse += md5HashString(logMessage.toString)
          }
        )
    }
    //Prepare Json response
    val responseArray = finalResponse.toArray
    val jsonResponse = JsonResponse(!responseArray.isEmpty, responseArray)
    implicit val formats: DefaultFormats = DefaultFormats
    val jsonString = write(jsonResponse)
    //Return result
    logger.info("Returning results...")
    Response(jsonString, Map(config.getString("config.api.contentType") -> config.getString("config.api.responseType")))

  }

  //Reponse in Json encoding
  case class JsonResponse(found: Boolean, messages: Array[String])

  //Class to build the response code
  case class Response(body: String, headers: Map[String,String], statusCode: Int = config.getInt("config.api.successStatusCode")) {
    logger.info("Return response...")
    def javaHeaders: java.util.Map[String, String] = headers.asJava
  }

}
