package observatory

// import org.apache.log4j.{Level, Logger}
// import org.apache.spark.{SparkConf, SparkContext}
// import org.apache.spark.sql.SparkSession

import java.time.LocalDate

object Main extends App {

  val StationsFile = "stations.csv"

  Console.println("Hello, Capstone!")

  // Logger.getLogger("org.apache.spark").setLevel(Level.WARN)

  /*val spark: SparkSession =
    SparkSession
      .builder()
      .appName("Observatory")
      .master("local")
      .getOrCreate()

  spark.close()*/

  /*val conf: SparkConf = new SparkConf().setMaster("local").setAppName("Observatory")
  val sc: SparkContext = new SparkContext(conf)*/

  /*val dir = new File("./src/main/resources")
  Console.println(s"current dir: ${dir.getAbsolutePath}")
  for (file <- dir.listFiles) Console.println(file.getPath)*/

  for (year <- 1975 to 2015) {
    Console.println(s"Year: $year")

    val temprRecs: Iterable[(LocalDate, Location, Temperature)] = Extraction.locateTemperatures(
      year, s"/$StationsFile", s"/$year.csv")
    Console.println(s"temprRecs size: ${temprRecs.size}")

    val locAvgTemps: Iterable[(Location, Temperature)] = Extraction.locationYearlyAverageRecords(temprRecs)
    Console.println(s"locAvgTemps size: ${locAvgTemps.size}")
  }
}
