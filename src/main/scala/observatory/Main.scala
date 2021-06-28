package observatory

// import org.apache.log4j.{Level, Logger}
// import org.apache.spark.{SparkConf, SparkContext}
// import org.apache.spark.sql.SparkSession

import com.sksamuel.scrimage.nio.ImageWriter

import java.nio.file.{Files, Paths}

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
    val image = Interaction.tile(locTemperatureData, temperatureColors, tile)
    Console.println(s"Image, year: $year, tile $tile, image $image")

    val imageFolderName = s"$OutputImageFolder/$year/${tile.zoom}"
    val folderPath = Paths.get(imageFolderName)
    Files.createDirectories(folderPath)
    Console.println(s"created, if did not exist, folder: $folderPath")

    val imageFileName = s"$imageFolderName/${tile.x}-${tile.y}.png"
    image.output(imageFileName)(ImageWriter.default)
    Console.println(s"Created image file: $imageFileName")
  }

  val stations = Extraction.locateStations(s"/$StationsFile")
  Console.println(s"stations size: ${stations.size}")

  val years: Range = 2015 to 2015

  val stationLocationMap = stations.toMap
  val yearLocAvgTemperatures: Seq[(Year, Iterable[(Location, Temperature)])] = for {
    year <- years
    p1 = Console.println(s"Year: $year")

    temperatureRecs = Extraction.locateTemperatures(year, s"/$year.csv", stationLocationMap)
    p2 = Console.println(s"Year: $year, temperatureRecs size: ${temperatureRecs.size}")

    locAvgTemperatures = Extraction.locationYearlyAverageRecords(temperatureRecs)
    p3 = Console.println(s"Year: $year, locAvgTemps size: ${locAvgTemperatures.size}")
  } yield (year, locAvgTemperatures)
  Console.println(s"yearLocAvgTemperatures: ${yearLocAvgTemperatures.size}")

  val yearsLocAvgTemperatures = yearLocAvgTemperatures.map({ case (_, locAvgTemperatures) => locAvgTemperatures })
  var temperatureAverages = Manipulation.average(yearsLocAvgTemperatures)
  Console.println(s"Created temperatureAverages grid")

  for ((year, locAvgTemperatures) <- yearLocAvgTemperatures) {
    Console.println(s"Year: $year, locAvgTemps size: ${locAvgTemperatures.size}")

    /* val image: Image = Visualization.visualize(locAvgTemperatures, temperatureColors)
    Console.println(s"Created image: $image") */

    // Interaction.generateTiles(year, locAvgTemperatures, generateImageFile)

    val gridLocTemperatureMap: GridLocation => Temperature = Manipulation.makeGrid(locAvgTemperatures)
    Console.println(s"Year: $year, makeGrid gridLocTemperatureMap")

    val gridLocs: Seq[GridLocation] = for {
      lat <- Utils.GridLocLatitudeMin to Utils.GridLocLatitudeMax
      lon <- Utils.GridLocLongitudeMin to Utils.GridLocLongitudeMax
    } yield GridLocation(lat, lon)
    Console.println(s"Year: $year, gridLocs: ${gridLocs.size}")

    gridLocs.par.foreach(gridLocTemperatureMap)
    Console.println(s"Year: $year, Completed gridLocTemperatureMap")

    val temperatures = for (gridLock <- gridLocs) yield gridLocTemperatureMap(gridLock)
    Console.println(s"Year: $year, temperatures: ${temperatures.size}")

    val deviations = Manipulation.deviation(locAvgTemperatures, temperatureAverages)
    Console.println(s"Year: $year, created deviations grid")
  }
}
