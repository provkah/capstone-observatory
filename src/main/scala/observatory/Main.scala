package observatory

// import org.apache.log4j.{Level, Logger}
// import org.apache.spark.{SparkConf, SparkContext}
// import org.apache.spark.sql.SparkSession

import com.sksamuel.scrimage.Image
import com.sksamuel.scrimage.nio.ImageWriter

import java.nio.file.{Files, Paths}
import java.time.LocalDate

object Main extends App {

  val StationsFile = "stations.csv"

  val OutputImageFolder = "target/temperatures"

  val temperatureColors: Iterable[(Temperature, Color)] =
    for (c <- 0 to 255) yield (c - 128.0, Color(c, 255 - c, c))

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

  def generateImageFile(
    year: Int, tile: Tile,
    locTemperatureData: Iterable[(Location, Temperature)]): Unit = {

    Console.println(s"Generating image, year: $year, tile $tile")
    val image: Image = Interaction.tile(locTemperatureData, temperatureColors, tile)
    Console.println(s"Image, year: $year, tile $tile, image $image")

    val imageFolderName = s"$OutputImageFolder/$year/${tile.zoom}"
    val folderPath = Paths.get(imageFolderName)
    Files.createDirectories(folderPath)
    Console.println(s"created, if did not exist, folder: $folderPath")

    val imageFileName = s"$imageFolderName/${tile.x}-${tile.y}.png"
    image.output(imageFileName)(ImageWriter.default)
    Console.println(s"Created image file: $imageFileName")
  }

  val stations: Iterable[((Option[StnId], Option[WbanId]), Location)] =
    Extraction.locateStations(s"/$StationsFile")
  Console.println(s"stations size: ${stations.size}")
  val stationLocationMap: Map[(Option[StnId], Option[WbanId]), Location] = stations.toMap.seq

  val years: Range = 2014 to 2015
  for (year <- years) {
    Console.println(s"Year: $year")

    val temperatureRecs: Iterable[(LocalDate, Location, Temperature)] = Extraction.locateTemperatures(
      year, s"/$year.csv", stationLocationMap)
    Console.println(s"Year: $year, temperatureRecs size: ${temperatureRecs.size}")

    val locAvgTemps: Iterable[(Location, Temperature)] =
      Extraction.locationYearlyAverageRecords(temperatureRecs)
    Console.println(s"Year: $year, locAvgTemps size: ${locAvgTemps.size}")

    // val image: Image = Visualization.visualize(locAvgTemps, temperatureColors)
    // Console.println(s"Created image: $image")

    // Interaction.generateTiles(year, locAvgTemps, generateImageFile)

    val gridLocTemperatureMap: GridLocation => Temperature = Manipulation.makeGrid(locAvgTemps)
    Console.println(s"makeGrid gridLocTemperatureMap")

    val gridLocs: Seq[GridLocation] = for {
      lat <- Utils.GridLocLatitudeMin to Utils.GridLocLatitudeMax
      lon <- Utils.GridLocLongitudeMin to Utils.GridLocLongitudeMax
    } yield GridLocation(lat, lon)
    Console.println(s"gridLocs: ${gridLocs.size}")

    gridLocs.foreach(gridLocTemperatureMap)
    Console.println("Completed gridLocTemperatureMap")

    val temperatures = for (gridLock <- gridLocs) yield gridLocTemperatureMap(gridLock)
    Console.println(s"temperatures: ${temperatures.size}")
  }
}
