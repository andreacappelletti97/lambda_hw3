package lambda


import com.amazonaws.regions.Region
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.model.GetObjectRequest
import HelperUtils.{CreateLogger, ObtainConfigReference}
import java.io.{BufferedReader, InputStreamReader}
import scala.io.Source
import scala.collection.JavaConverters._
import scala.collection.Searching._
import scala.collection.mutable.ArrayBuffer
import java.security.MessageDigest
import org.json4s.DefaultFormats
import org.json4s.native.Serialization.write
import java.security.MessageDigest
import java.math.BigInteger

class GetLogMessages

object GetLogMessages {
  val logger = CreateLogger(classOf[GetLogMessages])
  val config = ObtainConfigReference("config") match {
    case Some(value) => value
    case None => throw new RuntimeException("Cannot obtain a reference to the config data.")
  }

  private val semiColon = config.getString("config.api.semiColon")
  private val newLine = config.getString("config.api.newLine")
  private val dot = config.getString("config.api.dot")

  //Perform the getLogMessage function operation and return the result in MD5
  def handle(request: APIGatewayProxyRequestEvent, context: Context): Response = {
    //Get params from the get request
    val time = request.getPathParameters.get("time")
    val delta = request.getPathParameters.get("delta")
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

  //Encode log messages found in MD5
  def md5HashString(s: String): String = {
    val md = MessageDigest.getInstance(config.getString("config.api.encryptionMethod"))
    val digest = md.digest(s.getBytes)
    val bigInt = new BigInteger(1,digest)
    val hashedString = bigInt.toString(16)
    hashedString
  }

  //Adapt the format of the time in order to search for deltas
  def formatAdapter(time : String): String ={
    if(time.size == 1) {
      return config.getString("config.api.format") + time
    } else {
      return time
    }
  }

  //Look for deltas and time, return the time of those messages found
  def deltaHandler(timeArray: Array[String], time : String, delta: String): Array[String] ={
    val timeBuffer = ArrayBuffer[String]()
    //Split the delta into hours, minutes, seconds, millis
    val deltaHours = Integer.parseInt(delta.split(semiColon)(0))
    val deltaMinutes = Integer.parseInt(delta.split(semiColon)(1))
    val deltaSeconds = Integer.parseInt((delta.split(semiColon)(2)).split(newLine)(0))
    val deltaMillis = Integer.parseInt(delta.split(newLine)(1))
    //Split the time into hours, minutes, seconds, millis
    val timeHours = Integer.parseInt(time.split(semiColon)(0))
    val timeMinutes = Integer.parseInt(time.split(semiColon)(1))
    val timeSeconds = Integer.parseInt(time.split(semiColon)(2).split(newLine)(0))
    val timeMillis = Integer.parseInt(time.split(newLine)(1))
    //Hours
    val hoursIncrement = timeHours + deltaHours
    val hoursDecrement = timeHours - deltaHours
    //Minutes
    val minutesIncrement = timeMinutes + deltaMinutes
    val minutesDecrement = timeMinutes - deltaMinutes
    //Seconds
    val secondsIncrement = timeSeconds + deltaSeconds
    val secondsDecrement = timeSeconds - deltaSeconds
    //Millis
    val millisIncrement = timeMillis + deltaMillis
    val millisDecrement = timeMillis - deltaMillis
    //Compute increments and decrements
    val hoursIncremented = formatAdapter(hoursIncrement.toString)+semiColon +formatAdapter(timeMinutes.toString)+ semiColon +formatAdapter(timeSeconds.toString) +dot+ formatAdapter(timeMillis.toString)
    val hoursDecremented = formatAdapter(hoursDecrement.toString)+semiColon +formatAdapter(timeMinutes.toString)+ semiColon +formatAdapter(timeSeconds.toString) +dot+ formatAdapter(timeMillis.toString)
    val minutesIncremented = formatAdapter(timeHours.toString)+semiColon +formatAdapter(minutesIncrement.toString)+ semiColon +formatAdapter(timeSeconds.toString) +dot+ formatAdapter(timeMillis.toString)
    val minutesDecremented = formatAdapter(timeHours.toString)+semiColon +formatAdapter(minutesDecrement.toString)+ semiColon +formatAdapter(timeSeconds.toString) +dot+ formatAdapter(timeMillis.toString)
    val secondsIncremented = formatAdapter(timeHours.toString)+semiColon +formatAdapter(timeMinutes.toString)+ semiColon +formatAdapter(secondsIncrement.toString)  +dot+ formatAdapter(timeMillis.toString)
    val secondsDecremented = formatAdapter(timeHours.toString)+semiColon +formatAdapter(timeMinutes.toString)+ semiColon +formatAdapter(secondsDecrement.toString) +dot+ formatAdapter(timeMillis.toString)
    val millisIncremented = formatAdapter(timeHours.toString)+semiColon +formatAdapter(timeMinutes.toString)+ semiColon +formatAdapter(timeSeconds.toString) +dot+ formatAdapter(millisIncrement.toString)
    val millisDecremented = formatAdapter(timeHours.toString)+semiColon +formatAdapter(timeMinutes.toString)+ semiColon +formatAdapter(timeSeconds.toString) +dot+ formatAdapter(millisDecrement.toString)

    //Check if the current timestamp is present
    if(binarySearchTime(timeArray, time)){
      timeBuffer += time
    }
    //Increment hours with delta
    if(deltaHours!=0 && binarySearchTime(timeArray, hoursIncremented)){
      timeBuffer += hoursIncremented
    }
    //Decrement hours with delta
    if(deltaHours!=0 && binarySearchTime(timeArray, hoursDecremented)){
      timeBuffer += hoursDecremented
    }
    //Increment minutes with delta
    if(deltaMinutes!=0 && binarySearchTime(timeArray, minutesIncremented)){
      timeBuffer += minutesIncremented
    }
    //Decrement minutes with delta
    if(deltaMinutes!=0 && binarySearchTime(timeArray, minutesDecremented)){
      timeBuffer += minutesDecremented
    }
    //Increment seconds with delta
    if(deltaSeconds!=0 && binarySearchTime(timeArray, secondsIncremented)){
      timeBuffer += secondsIncremented
    }
    //Decrement seconds with delta
    if(deltaSeconds!=0 && binarySearchTime(timeArray, secondsDecremented)){
      timeBuffer += secondsDecremented
    }
    //Increment millis with delta
    if(deltaMillis!=0 && binarySearchTime(timeArray, millisIncremented)){
      timeBuffer += millisIncremented
    }
    //Decrement millis with delta
    if(deltaMillis!=0 && binarySearchTime(timeArray, millisDecremented)){
      timeBuffer += millisDecremented
    }
      val timeFoundArray = timeBuffer.toArray
    return timeBuffer.toArray
  }

  //This function performs a binary search into the time of the logs data, the times are already oredered
  //https://github.com/scala/scala/blob/v2.11.0-M3/src/library/scala/collection/Searching.scala
  def binarySearchTime(timeArray: Array[String], searchQuery: String) : Boolean = {
    logger.info("Binary search has started...")
    val searchResult = timeArray.search(searchQuery)
    if(searchResult.toString.contains("Found"))
      logger.info("Found...")
      return true
    logger.info("Not found...")
    false
  }

  //Function to retrieve the log file locally
  def readFile(filename: String): Array[String] = {
    val readmeText : Iterator[String] = Source.fromResource(filename).getLines
    val logBuffer = ArrayBuffer[String]()
    readmeText.foreach(token => logBuffer+=token)
    logBuffer.toArray
  }

  //Reading file from S3 bucket
  def readFileFromS3(): Array[AnyRef]  ={
    logger.info("Getting content from S3 bucket...")
    val bucketName = config.getString("config.s3.bucketName")
    val fileName = config.getString("config.s3.file")
    //Init s3 amazon
    val s3 = AmazonS3ClientBuilder.standard()
      .withRegion(config.getString("config.s3.region")) // The first region to try your request against
      .build();
    //Get object from S3 bucket
    val myObject = s3.getObject(new GetObjectRequest(bucketName, fileName))
    val inputStream = myObject.getObjectContent()
    val reader = new BufferedReader(new InputStreamReader(inputStream))
    val myArray = reader.lines().toArray()
    myArray
  }

  //Reponse in Json encoding
  case class JsonResponse(found: Boolean, messages: Array[String])

  //Class to build the response code
  case class Response(body: String, headers: Map[String,String], statusCode: Int = config.getInt("config.api.successStatusCode")) {
    logger.info("Return response...")
    def javaHeaders: java.util.Map[String, String] = headers.asJava
  }


}

