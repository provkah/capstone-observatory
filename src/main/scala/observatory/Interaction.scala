package observatory

import com.sksamuel.scrimage.Image
import com.sksamuel.scrimage.nio.ImageWriter

import java.nio.file.{Files, Paths}
import scala.math.{Pi, atan, pow, sinh}

/**
  * 3rd milestone: interactive visualization
  */
object Interaction extends InteractionInterface {

  val TileSize = 256

  val GenScaleZoom = 0 // 0 - no scaling, 1 - scaling factor 2
  private val GenScaleFactor = pow(2, GenScaleZoom).toInt
  private val GenTileSize = TileSize / GenScaleFactor

  // in unscaled case - number of subtiles in both axis: 2 ** 8 == 256
  private val TileRelativeZoom = 8 - GenScaleZoom
  private val TileCoordFactor = pow(2, TileRelativeZoom).toInt

  val TileRgbaAlpha = 0.5

  val ZoomLevels: Range.Inclusive = 0 to 3

  val TemperatureColors = List(
    (60.0, Color(255, 255, 255)), (32.0, Color(255, 0, 0)), (12.0, Color(255, 0, 0)), (0.0, Color(0, 255, 255)),
    (-15.0, Color(0, 0, 255)), (-27.0, Color(255, 0, 0)), (-50.0, Color(33, 0, 107)), (60.0, Color(0, 0, 0))
  )

  val TemperatureDeviationColors = List(
    (7.0, Color(0, 0, 0)), (4.0, Color(255, 0, 0)), (2.0, Color(255, 255, 0)),
    (0.0, Color(255, 255, 255)), (-2.0, Color(0, 255, 255)), (-7.0, Color(0, 0, 255)))

  val TemperatureImageOutputFolder = "target/temperatures"
  val TemperatureDeviationImageOutputFolder = "target/deviations"


  /**
    * @param tile Tile coordinates
    * @return The latitude and longitude of the top-left corner of the tile,
    *         as per http://wiki.openstreetmap.org/wiki/Slippy_map_tilenames
    */
  def tileLocation(tile: Tile): Location = {

    val twoToPowerOfZoom: Double = pow(2.0, tile.zoom)

    val lon: Double = tile.x / twoToPowerOfZoom * 360.0  - 180.0
    val lat = atan(sinh(Pi * (1.0 - 2.0 * tile.y / twoToPowerOfZoom))) * 180.0 / Pi

    Location(lat, lon)
  }

  /**
    * @param temperatures Known temperatures
    * @param colors Color scale
    * @param tile Tile coordinates
    * @return A 256×256 image showing the contents of the given tile
    */
  def tile(
    temperatures: Iterable[(Location, Temperature)], colors: Iterable[(Temperature, Color)],
    tile: Tile): Image = {

    val tileZoom = tile.zoom + TileRelativeZoom

    val xStart = tile.x * TileCoordFactor
    val yStart = tile.y * TileCoordFactor
    val tileCoords = for {
      y <- yStart until yStart + GenTileSize
      x <- xStart until xStart + GenTileSize
    } yield (x, y)

    val pixelLocations = tileCoords.par.map({ case (x, y) => tileLocation(Tile(x, y, tileZoom)) })

    Console.println(s"Constructing pixels, tile $tile")
    val alpha = (TileRgbaAlpha * 256 - 1).toInt
    val pixels = Visualization.locationsToPixels(pixelLocations, alpha, temperatures, colors)
    Console.println(s"Done constructing pixels, tile $tile, pixels: ${pixels.size}")

    Console.println(s"Generating image, tile $tile")
    val image = Image(GenTileSize, GenTileSize, pixels.toArray)
    Console.println(s"Done generating image, tile $tile, image $image")

    Console.println(s"GenTileSize: $GenTileSize, GenScaleFactor: $GenScaleFactor")
    if (GenScaleFactor == 1) image
    else {
      val scaledImage = image.scale(GenScaleFactor)
      Console.println(s"Scaled image, tile $tile, GenScaleFactor: $GenScaleFactor, scaledImage $scaledImage")

      scaledImage
    }
  }

  /**
    * Generates all the tiles for zoom levels 0 to 3 (included), for all the given years.
    * @param yearlyData Sequence of (year, data), where `data` is some data associated with
    *                   `year`. The type of `data` can be anything.
    * @param generateImage Function that generates an image given a year, a zoom level, the x and
    *                      y coordinates of the tile and the data to build the image from
    */
  def generateTiles[Data](
    yearlyData: Iterable[(Year, Data)],
    generateImage: (Year, Tile, Data) => Unit): Unit = {

    yearlyData.par.foreach({ case (year, yearData) => generateTiles(year, yearData, generateImage) })
  }

  def generateTiles[Data](
    year: Int, data: Data,
    generateImage: (Year, Tile, Data) => Unit): Unit = {

    ZoomLevels.par.foreach(generateTiles(year, data, _, generateImage))
  }

  private def generateTiles[Data](
    year: Int, data: Data, zoom: Int,
    generateImage: (Year, Tile, Data) => Unit): Unit = {

    val numTiles = pow(2, zoom).toInt
    val tiles = for {
      xTile <- 0 until numTiles
      yTile <- 0 until numTiles
    } yield Tile(xTile, yTile, zoom)

    tiles.par.foreach(generateImage(year, _, data))
  }

  private def generateImageFile(
    year: Int, tile: Tile, locTemperatures: Iterable[(Location, Temperature)],
    temperatureColors: Iterable[(Temperature, Color)],
    outputFolder: String): Unit = {

    Console.println(s"Generating image, year: $year, tile $tile")
    val image = Interaction.tile(locTemperatures, temperatureColors, tile)
    Console.println(s"Image, year: $year, tile $tile, image $image")

    val imageFolderName = s"$outputFolder/$year/${tile.zoom}"
    val folderPath = Paths.get(imageFolderName)
    Files.createDirectories(folderPath)
    Console.println(s"created, if did not exist, folder: $folderPath")

    val imageFileName = s"$imageFolderName/${tile.x}-${tile.y}.png"
    image.output(imageFileName)(ImageWriter.default)
    Console.println(s"Created image file: $imageFileName")
  }

  def generateTemperatureImageFile(
    year: Int, tile: Tile, locTemperatures: Iterable[(Location, Temperature)]): Unit =

    generateImageFile(
      year, tile, locTemperatures,
      TemperatureColors,
      TemperatureImageOutputFolder)

  def generateTemperatureDeviationImageFile(
    year: Int, tile: Tile, locTemperatures: Iterable[(Location, Temperature)]): Unit =

    generateImageFile(
      year, tile, locTemperatures,
      TemperatureDeviationColors,
      TemperatureDeviationImageOutputFolder)
}
