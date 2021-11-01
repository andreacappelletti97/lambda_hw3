/*
 *
 *  Copyright (c) 2021. Mark Grechanik and Lone Star Consulting, Inc. All rights reserved.
 *
 *   Unless required by applicable law or agreed to in writing, software distributed under
 *   the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 *   either express or implied.  See the License for the specific language governing permissions and limitations under the License.
 *
 */
import Generation.{LogMsgSimulator, RandomStringGenerator}
import HelperUtils.Parameters.config
import HelperUtils.{CreateLogger, Parameters}
import com.amazonaws.AmazonServiceException
import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.model.GetObjectRequest
import com.amazonaws.auth.BasicAWSCredentials
import java.io.{BufferedReader, File, InputStreamReader}
import collection.JavaConverters.*
import scala.concurrent.{Await, Future, duration}
import concurrent.ExecutionContext.Implicits.global
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success, Try}
import scala.language.postfixOps
import com.amazonaws.AmazonServiceException

object GenerateLogData:
  val logger = CreateLogger(classOf[GenerateLogData.type])

  def getListOfFiles(dir: String):List[File] = {
    logger.info("GetListOfFiles function has been called...")
    val d = new File(dir)
    if (d.exists && d.isDirectory) {
      logger.info("Directory exists, return list of files")
      d.listFiles.filter(_.isFile).toList
    } else {
      List[File]()
    }
  }

  def getFileLines(files: List[File]): Array[String] = {
    logger.info("GetFileLines has been called...")
    val linesArray = ArrayBuffer[String]()
    files.foreach{
      file =>
        val source = scala.io.Source.fromFile("log/" + file.getName)
        val lines = source.getLines()
        lines.foreach(line =>
          linesArray += line
        )
    }
    logger.info("Return all lines of code")
    return linesArray.toArray
  }

  def  uploadS3Logs(): Unit ={
    logger.info("Starting uploadS3Logs function")
    //Get list of log files generated
    val myFileList =  getListOfFiles("log/")
    //Get all logs lines to write them to S3 bucket
    val logLines = getFileLines(myFileList)
    logger.info("Writing content to S3 bucket...")
    val bucketName = config.getString("randomLogGenerator.s3.bucketName")
    val fileName = config.getString("randomLogGenerator.s3.file")
    val region = config.getString("randomLogGenerator.s3.region")
    //Init s3 amazon
    val s3 = AmazonS3ClientBuilder.standard()
     // .withCredentials(new AWSStaticCredentialsProvider(creds))
      .withRegion(region) // The first region to try your request against
      .build();

    logger.info("***WRITING OBJECT TO S3")
    val content = logLines.mkString(config.getString("randomLogGenerator.s3.newLine"))
    try  s3.putObject(bucketName, fileName, content)
    catch {
      case e: AmazonServiceException =>
        logger.error("ERROR S3")
        logger.error(e.getErrorMessage)
      //System.exit(1)
    }
    Thread.sleep(config.getLong("randomLogGenerator.s3.timePeriod")) // wait for timePeriod millisecond
    //Recursive call
    uploadS3Logs()

  }


//this is the main starting point for the log generator
@main def runLogGenerator =
  import Generation.RSGStateMachine.*
  import Generation.*
  import HelperUtils.Parameters.*
  import GenerateLogData.*

  logger.info("Log data generator started...")
  val INITSTRING = "Starting the string generation"
  val init = unit(INITSTRING)

  //Continuosly updating log on S3 bucket
  val uploadFuture = Future {
    uploadS3Logs()
  }
  val logFuture = Future {
    LogMsgSimulator(init(RandomStringGenerator((Parameters.minStringLength, Parameters.maxStringLength), Parameters.randomSeed)), Parameters.maxCount)
  }
  Try(Await.result(logFuture, Parameters.runDurationInMinutes)) match {
    case Success(value) => logger.info(s"Log data generation has completed after generating ${Parameters.maxCount} records.")
    case Failure(exception) => logger.info(s"Log data generation has completed within the allocated time, ${Parameters.runDurationInMinutes}")
  }

