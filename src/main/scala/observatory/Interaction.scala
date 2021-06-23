package observatory

import com.sksamuel.scrimage.{Image, Pixel}

import scala.collection.parallel.{ParIterable, ParSeq}
import scala.math.{Pi, atan, pow, sinh}

/**
  * 3rd milestone: interactive visualization
  */
object Interaction extends InteractionInterface {

  val TileWidth = 256
  val TileHeight = 256

  val TileRgbaAlpha = 0.5

  val ZoomLevels: Range.Inclusive = 0 to 3

  /**
    * @param tile Tile coordinates
    * @return The latitude and longitude of the top-left corner of the tile, as per http://wiki.openstreetmap.org/wiki/Slippy_map_tilenames
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

    // number of subtiles in both axis: 2 ** 8 == 256
    val subtileRelativeZoom = 8
    val coordFactor = pow(2, subtileRelativeZoom).toInt

    val subtileZoom = tile.zoom + subtileRelativeZoom

    val xStart = tile.x * coordFactor
    val yStart = tile.y * coordFactor
    val subtileCoords: Seq[(Int, Int)] = for {
      y <- yStart until yStart + TileHeight
      x <- xStart until xStart + TileWidth
    } yield (x, y)

    val pixelLocations: ParSeq[Location] = subtileCoords.par
      .map({ case (x, y) => tileLocation(Tile(x, y, subtileZoom)) })

    val alpha = (TileRgbaAlpha * 256 - 1).toInt
    val pixels: ParIterable[Pixel] = Visualization.locationsToPixels(pixelLocations, alpha, temperatures, colors)

    Image(TileWidth, TileHeight, pixels.toArray)
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

    yearlyData.par
      .foreach({ case (year, yearData) => generateTiles(year, yearData, generateImage) })
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
    val tiles: Seq[Tile] = for {
      xTile <- 0 until numTiles
      yTile <- 0 until numTiles
    } yield Tile(xTile, yTile, zoom)

    tiles.par.foreach(generateImage(year, _, data))
  }
}
