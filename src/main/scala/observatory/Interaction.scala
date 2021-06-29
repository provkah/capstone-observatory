package observatory

import com.sksamuel.scrimage.Image

import scala.math.{Pi, atan, pow, sinh}

/**
  * 3rd milestone: interactive visualization
  */
object Interaction extends InteractionInterface {

  val GenTileSize = 128
  val TileSize = 256

  // number of subtiles in both axis: 2 ** 8 == 256
  val TileRelativeZoom = 8
  val TileCoordFactor: Int = pow(2, TileRelativeZoom).toInt

  val TileRgbaAlpha = 0.5

  val ZoomLevels: Range.Inclusive = 0 to 3

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

    val scaledImage = image.scale(TileSize / GenTileSize)
    Console.println(s"Scaled image, tile $tile, scaledImage $scaledImage")

    scaledImage
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

  def generateTiles[Data](
    year: Int, data: Data, zoom: Int,
    generateImage: (Year, Tile, Data) => Unit): Unit = {

    val numTiles = pow(2, zoom).toInt
    val tiles = for {
      xTile <- 0 until numTiles
      yTile <- 0 until numTiles
    } yield Tile(xTile, yTile, zoom)

    tiles.par.foreach(generateImage(year, _, data))
  }
}
