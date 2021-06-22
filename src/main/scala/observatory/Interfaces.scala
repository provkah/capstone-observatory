package observatory

import com.sksamuel.scrimage.Image

import java.time.LocalDate

// Interfaces used by the grading infrastructure. Do not change signatures
// or your submission will fail with a NoSuchMethodError.

trait ManipulationInterface {
  def makeGrid(temperatures: Iterable[(Location, Temperature)]): GridLocation => Temperature
  def average(temperatures: Iterable[Iterable[(Location, Temperature)]]): GridLocation => Temperature
  def deviation(temperatures: Iterable[(Location, Temperature)], normals: GridLocation => Temperature): GridLocation => Temperature
}

trait Visualization2Interface {
  def bilinearInterpolation(point: CellPoint, d00: Temperature, d01: Temperature, d10: Temperature, d11: Temperature): Temperature
  def visualizeGrid(grid: GridLocation => Temperature, colors: Iterable[(Temperature, Color)], tile: Tile): Image
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
}

trait VisualizationInterface {

  def predictTemperature(temperatures: Iterable[(Location, Temperature)], location: Location): Temperature

  def interpolateColor(points: Iterable[(Temperature, Color)], value: Temperature): Color

  def visualize(temperatures: Iterable[(Location, Temperature)], colors: Iterable[(Temperature, Color)]): Image
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

  def lineToTempRec(line: String): ((Option[StnId], Option[WbanId]), (Month, Day), Temperature)

  def lineToStationRec(line: String): ((Option[StnId], Option[WbanId]), Location)
}

trait UtilsInterface {

  val RgbColorMin = 0
  val RgbColorMax = 255
  val LatitudeMax = 90
  val LongitudeMax = 180
  type Point = (Double, Double)

  def linearInterpolation(p1: Point, p2: Point, x: Double): Double

  // points must be sorted
  def findPointsForLinearInterpolation(points: Array[Double], value: Double): (Int, Int)

  def greatCircleDistanceCentralAngle(loc1: Location, loc2: Location): Double

  def clipRgbColor(color: Int): Int

  def angleDegreesToRadians(angleInDegrees: Double): Double

  def tempFahrenheitToCelcius(degreesFahrenheit: Double): Double

  def getLinesIteratorFromResFile(resFile: String, classObj: Class[_]): Iterator[String]
}
