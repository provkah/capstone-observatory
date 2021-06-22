package observatory

// import org.apache.log4j.{Level, Logger}
// import org.apache.spark.{SparkConf, SparkContext}
// import org.apache.spark.sql.SparkSession

// import com.sksamuel.scrimage.Image

import java.time.LocalDate

object Main extends App {

  val StationsFile = "stations.csv"

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

  val stations: Iterable[((Option[StnId], Option[WbanId]), Location)] =
    Extraction.locateStations(s"/$StationsFile")
  val stationLocationMap: Map[(Option[StnId], Option[WbanId]), Location] = stations.toMap
  Console.println(s"stations size: ${stationLocationMap.size}")

  val temprColors: Seq[(Temperature, Color)] =
    for (c <- 0 to 255) yield (c - 128.0, Color(c, 255 - c, c))

  for (year <- 2015 to 2015) {
    Console.println(s"Year: $year")

    val temprRecs: Iterable[(LocalDate, Location, Temperature)] = Extraction.locateTemperatures(
      year, s"/$year.csv", stationLocationMap)
    Console.println(s"temprRecs size: ${temprRecs.size}")

    val locAvgTemps: Iterable[(Location, Temperature)] = Extraction.locationYearlyAverageRecords(temprRecs)
    Console.println(s"locAvgTemps size: ${locAvgTemps.size}")

    Interaction.generateTiles(
      year, locAvgTemps,
      (year: Int, tile: Tile, locTemprData: Iterable[(Location, Temperature)]) => {
        val image = Interaction.tile(locTemprData, temprColors, tile)
        Console.println(s"Generated image $image for tile $tile")
    })

    // image: Image = Visualization.visualize(locAvgTemps, temprColorMap)
    // Console.println(s"Created image: $image")
  }
}
