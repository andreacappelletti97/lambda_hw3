package lambda


import com.amazonaws.regions.Region
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.model.GetObjectRequest

import java.io.{BufferedReader, InputStreamReader}
import scala.io.Source
import scala.collection.JavaConverters._
import scala.collection.Searching._
import scala.collection.mutable.ArrayBuffer

/**
 * Scala entrypoint for the API Gateway Lambda function
 */
object CheckLogPresenceFunction {

  /**
   * Handle a Lambda request indirectly via the API Gateway
   * @param request the Java HTTP request
   * @param context the Java Lambda context
   * @return the HTTP response
   */
  def handle(request: APIGatewayProxyRequestEvent, context: Context): Response = {
    println("handling %s %s, remaining time is %d ms".format(
      request.getHttpMethod, request.getPath, context.getRemainingTimeInMillis))
    println(s"""environment = ${sys.env.getOrElse("env", "n/a")}""")
    val time = request.getPathParameters.get("time")
    val delta = request.getPathParameters.get("delta")
    //val myFile = readFile("input.log")
    //Read file from AWS S3 bucket
    val logFile = readFileFromS3()
    //Populate array buffer with file content
    val logTimeStampBuffer = ArrayBuffer[String]()
    logFile.foreach(token =>
      logTimeStampBuffer += token.toString.split(" ")(0))
    //Cast the array buffer to array
    val logTimeStampArray = logTimeStampBuffer.toArray

    //logTimeStampArray.foreach(elem => System.out.println(elem))
    //System.out.println("search result")
    val result = binarySearchTime(logTimeStampArray, time)
    //System.out.println(result)
    /*
    System.out.println("map log")
    val m = myFile.map(c => c.split(",")(0) -> c).toMap
    m.foreach(token => System.out.println(token._1 + " " + token._2))
*/
    Response(result.toString, Map("Content-Type" -> "text/plain"))
  }


  //This function performs a binary search into the time of the logs data, the times are already oredered
  //https://github.com/scala/scala/blob/v2.11.0-M3/src/library/scala/collection/Searching.scala
  def binarySearchTime(timeArray: Array[String], searchQuery: String) : Boolean = {
    val searchResult = timeArray.search(searchQuery)
    if(searchResult.toString.contains("Found"))
      return true
    false
  }

  //Function to retrieve the log file locally
  def readFile(filename: String): Iterator[String] = {
    val readmeText : Iterator[String] = Source.fromResource(filename).getLines
    readmeText
  }

  //Reading file from S3 bucket
  def readFileFromS3(): Array[AnyRef]  ={
    System.out.println("getting content")
    val bucketName = "aws-sam-cli-managed-default-samclisourcebucket-1gfrc8jdssnc9"
    val fileName = "input.log"
    val s3 = AmazonS3ClientBuilder.standard()
      .withRegion("us-west-1") // The first region to try your request against
      .build();
    val myObject = s3.getObject(new GetObjectRequest(bucketName, fileName))
    val inputStream = myObject.getObjectContent()
    val reader = new BufferedReader(new InputStreamReader(inputStream))
    System.out.println("PRINT content")
    val myArray = reader.lines().toArray()
    myArray
  }

  //Class to build the response code
  case class Response(body: String, headers: Map[String,String], statusCode: Int = 200) {
    def javaHeaders: java.util.Map[String, String] = headers.asJava
  }

}

