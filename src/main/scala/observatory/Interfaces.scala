package observatory

import com.sksamuel.scrimage.{Image, Pixel}

import java.time.LocalDate
import scala.collection.parallel.ParIterable

// Interfaces used by the grading infrastructure. Do not change signatures
// or your submission will fail with a NoSuchMethodError.

trait Visualization2Interface {
  def bilinearInterpolation(
    point: CellPoint,
    d00: Temperature, d01: Temperature, d10: Temperature, d11: Temperature): Temperature

  def visualizeGrid(
    grid: GridLocation => Temperature,
    colors: Iterable[(Temperature, Color)],
    tile: Tile): Image
}

trait ManipulationInterface {

  def makeGrid(temperatures: Iterable[(Location, Temperature)]): GridLocation => Temperature

  def average(temperatures: Iterable[Iterable[(Location, Temperature)]]): GridLocation => Temperature

  def deviation(
    temperatures: Iterable[(Location, Temperature)],
    normals: GridLocation => Temperature): GridLocation => Temperature
}

trait InteractionInterface {

  def tileLocation(tile: Tile): Location

  def tile(
    temperatures: Iterable[(Location, Temperature)],
    colors: Iterable[(Temperature, Color)],
    tile: Tile): Image

  def generateTiles[Data](
    yearlyData: Iterable[(Year, Data)],
    generateImage: (Year, Tile, Data) => Unit): Unit

  def generateTiles[Data](
    year: Int, data: Data,
    generateImage: (Year, Tile, Data) => Unit): Unit

  def generateTiles[Data](
    year: Int, data: Data, zoom: Int,
    generateImage: (Year, Tile, Data) => Unit): Unit
}

trait VisualizationInterface {

  val EarthRadiusKm = 6378.0

  def predictTemperature(temperatures: Iterable[(Location, Temperature)], location: Location): Temperature

  def interpolateColor(points: Iterable[(Temperature, Color)], value: Temperature): Color

  def visualize(temperatures: Iterable[(Location, Temperature)], colors: Iterable[(Temperature, Color)]): Image

  def locationsToPixels(
    pixelLocations: ParIterable[Location],
    alpha: Int,
    temperatures: Iterable[(Location, Temperature)],
    colors: Iterable[(Temperature, Color)]): ParIterable[Pixel]
}

trait ExtractionInterface {

  def locateStations(stationsFile: String): Iterable[((Option[StnId], Option[WbanId]), Location)]

  def locateTemperatures(
    year: Year, stationsFile: String, temperaturesFile: String): Iterable[(LocalDate, Location, Temperature)]

  def locateTemperatures(
    year: Year, temperaturesFile: String,
    stationLocations: Map[(Option[StnId], Option[WbanId]), Location]): Iterable[(LocalDate, Location, Temperature)]

  def locationYearlyAverageRecords(
    records: Iterable[(LocalDate, Location, Temperature)]): Iterable[(Location, Temperature)]
}

trait ExtractionUtilsInterface {

  val StationsNumFields = 4

  val TempsNumFields = 5

  val MonthMin = 1
  val MonthMax = 12
  val DayMin = 1
  val DayMax = 31

  val NoTempStr = "9999.9"

  def lineToTemperatureRec(line: String): ((Option[StnId], Option[WbanId]), (Month, Day), Temperature)

  def lineToStationRec(line: String): ((Option[StnId], Option[WbanId]), Location)
}

trait OutputUtilsInterface {
  val OutputImageFolder = "target/temperatures"

  val temperatureColors: Iterable[(Temperature, Color)] =
    for (c <- 0 to 255) yield (c - 128.0, Color(c, 255 - c, c))

  def generateImageFile(year: Int, tile: Tile, locTemperatures: Iterable[(Location, Temperature)]): Unit
}

trait UtilsInterface {

  val RgbColorMin = 0
  val RgbColorMax = 255

  val LatitudeMax = 90.0
  val LongitudeMax = 180.0

  val GridLocLatitudeMin = -89
  val GridLocLatitudeMax = 90
  val GridLocLongitudeMin = -180
  val GridLocLongitudeMax = 179

  val GridLocLatRange: Seq[Int] = GridLocLatitudeMin to GridLocLatitudeMax
  val GridLocLonRange: Seq[Int] = GridLocLongitudeMin to GridLocLongitudeMax

  lazy val GridLocations: Seq[GridLocation] = for {
    lat <- Utils.GridLocLatRange
    lon <- Utils.GridLocLonRange
  } yield GridLocation(lat, lon)

  lazy val LocationsForGrid: Seq[Location] = GridLocations.map(l => Location(l.lat, l.lon))

  type Point = (Double, Double)

  def bilinearInterpolation(
    x: Double, y: Double,
    d00: Double, d01: Double, d10: Double, d11: Double): Double

  def locationToGridLocation(l: Location): GridLocation

  def linearInterpolation(p1: Point, p2: Point, x: Double): Double

  // points must be sorted
  def findPointsForLinearInterpolation(points: Array[Double], value: Double): (Int, Int)

  def greatCircleDistanceCentralAngle(loc1: Location, loc2: Location): Double

  def clipRgbColor(color: Int): Int

  def predictUsingInverseDistanceWeighting(
    distValues: ParIterable[(Double, Double)],
    distanceThreshold: Double,
    inverseDistanceWeightingPower: Int): Double

  def angleDegreesToRadians(angleInDegrees: Double): Double

  def average(values: Iterable[Double]): Double

  def average(values: ParIterable[Double]): Double

  def tempFahrenheitToCelcius(degreesFahrenheit: Double): Double

  def getLinesIteratorFromResFile(resFile: String, classObj: Class[_]): Iterator[String]
}
