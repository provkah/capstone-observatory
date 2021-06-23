package observatory

// import org.apache.log4j.{Level, Logger}
// import org.apache.spark.{SparkConf, SparkContext}
// import org.apache.spark.sql.SparkSession

import com.sksamuel.scrimage.Image
import com.sksamuel.scrimage.nio.ImageWriter

import java.io.File
import java.nio.file.{Files, Paths}
import java.time.LocalDate

object Main extends App {

  val StationsFile = "stations.csv"

  val OutputImageFolder = "target/temperatures"

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
  Console.println(s"stations size: ${stations.size}")
  val stationLocationMap: Map[(Option[StnId], Option[WbanId]), Location] = stations.toMap.seq

  val temprColors: Seq[(Temperature, Color)] =
    for (c <- 0 to 255) yield (c - 128.0, Color(c, 255 - c, c))

  val years: Range = 2014 to 2015
  for (year <- years) {
    Console.println(s"Year: $year")

    val temprRecs: Iterable[(LocalDate, Location, Temperature)] = Extraction.locateTemperatures(
      year, s"/$year.csv", stationLocationMap)
    Console.println(s"Year: $year, temprRecs size: ${temprRecs.size}")

    val locAvgTemps: Iterable[(Location, Temperature)] = Extraction.locationYearlyAverageRecords(temprRecs)
    Console.println(s"Year: $year, locAvgTemps size: ${locAvgTemps.size}")

    Interaction.generateTiles(
      year, locAvgTemps,
      (year: Int, tile: Tile, locTemprData: Iterable[(Location, Temperature)]) => {
        Console.println(s"Generating image, year: $year, tile $tile")
        val image: Image = Interaction.tile(locTemprData, temprColors, tile)
        Console.println(s"Image, year: $year, tile $tile, image $image")

        val imageFolderName = s"$OutputImageFolder/$year/${tile.zoom}"
        val folderPath = Paths.get(imageFolderName)
        Files.createDirectories(folderPath)
        Console.println(s"created, if did not exist, folder: $folderPath")

        val imageFileName = s"$imageFolderName/${tile.x}-${tile.y}.png"
        val createNewFileResult = new File(imageFolderName).createNewFile()
        Console.println(s"createNewFileResult: $createNewFileResult")

        image.output(imageFileName)(ImageWriter.default)
        Console.println(s"Created image file: $imageFileName")
    })

    // image: Image = Visualization.visualize(locAvgTemps, temprColorMap)
    // Console.println(s"Created image: $image")
  }
}
