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

class CheckLogPresenceFunction

object CheckLogPresenceFunction {
  val logger = CreateLogger(classOf[CheckLogPresenceFunction])
  val config = ObtainConfigReference("config") match {
    case Some(value) => value
    case None => throw new RuntimeException("Cannot obtain a reference to the config data.")
  }

  def handle(request: APIGatewayProxyRequestEvent, context: Context): Response = {
    logger.info("handling %s %s, remaining time is %d ms".format(request.getHttpMethod, request.getPath, context.getRemainingTimeInMillis))
    logger.info(s"""environment = ${sys.env.getOrElse("env", "n/a")}""")
    //Get params from the get request
    val time = request.getPathParameters.get("time")
    val delta = request.getPathParameters.get("delta")
    //Read file from AWS S3 bucket
    //val logFile = readFileFromS3()
    val logFile = readFile("input.log")
    //Populate array buffer with file content
    val logTimeStampBuffer = ArrayBuffer[String]()
    logFile.foreach(token =>
      logTimeStampBuffer += token.toString.split(" ")(0))
    //Cast the array buffer to array
    val logTimeStampArray = logTimeStampBuffer.toArray
    deltaHandler(logTimeStampArray, time, delta)
    val result = binarySearchTime(logTimeStampArray, time)
    //Return result
    logger.info("Returning results...")
    Response(result.toString, Map("Content-Type" -> "text/plain"))
  }

  def formatAdapter(time : String): String ={
    if(time.size == 1) {
      return "0" + time
    } else {
      return time
    }
  }

  def deltaHandler(timeArray: Array[String], time : String, delta: String): Unit ={
    val deltaHours = Integer.parseInt(delta.split(":")(0))
    val deltaMinutes = Integer.parseInt(delta.split(":")(1))
    val deltaSeconds = Integer.parseInt((delta.split(":")(2)).split("\\.")(0))
    val deltaMillis = Integer.parseInt(delta.split("\\.")(1))

    val timeHours = Integer.parseInt(time.split(":")(0))
    val timeMinutes = Integer.parseInt(time.split(":")(1))
    val timeSeconds = Integer.parseInt(time.split(":")(2).split("\\.")(0))
    val timeMillis = Integer.parseInt(time.split("\\.")(1))

    val hoursIncrement = timeHours + deltaMinutes
    val hoursDecrement = timeHours - deltaMinutes

    val minutesIncrement = timeMinutes + deltaHours
    val minutesDecrement = timeMinutes - deltaHours

    val secondsIncrement = timeSeconds + deltaSeconds
    val secondsDecrement = timeSeconds - deltaSeconds

    binarySearchTime(timeArray, hoursIncrement.toString+":" +timeMinutes.toString+ ":" +timeSeconds.toString)
    binarySearchTime(timeArray, hoursDecrement.toString+":" +timeMinutes.toString+ ":" +timeSeconds.toString)
    binarySearchTime(timeArray, timeHours.toString+":" +minutesIncrement.toString+ ":" +timeSeconds.toString)
    binarySearchTime(timeArray, timeHours.toString+":" +minutesDecrement.toString+ ":" +timeSeconds.toString)
    System.out.println("seconds increment is")
    System.out.println("checking for " + formatAdapter(timeHours.toString)+":" +timeMinutes.toString+ ":" +secondsIncrement.toString +"."+ timeMillis.toString)
    System.out.println(binarySearchTime(timeArray, timeHours.toString+":" +timeMinutes.toString+ ":" +secondsIncrement.toString +"."+ timeMillis.toString))
    binarySearchTime(timeArray, timeHours.toString+":" +timeMinutes.toString+ ":" +secondsDecrement.toString)

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
  def readFile(filename: String): Iterator[String] = {
    val readmeText : Iterator[String] = Source.fromResource(filename).getLines
    readmeText
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

  //Class to build the response code
  case class Response(body: String, headers: Map[String,String], statusCode: Int = 200) {
    logger.info("Return response...")
    def javaHeaders: java.util.Map[String, String] = headers.asJava
  }

}

