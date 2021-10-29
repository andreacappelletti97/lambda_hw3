import HelperUtils.{CreateLogger, ObtainConfigReference}
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory

object MainDriver:
  val logger = CreateLogger(classOf[MainDriver])

  val config = ObtainConfigReference("config") match {
    case Some(value) => value
    case None => throw new RuntimeException("Cannot obtain a reference to the config data.")
  }

  def main(args: Array[String])  = {
    logger.info("Running mapreduce jobs")
    logger.info("Finished mapreduce jobs...")
  }

class MainDriver