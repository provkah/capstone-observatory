package observatory

import java.io.InputStream
import java.time.LocalDate
import scala.io.{BufferedSource, Source}

object Extraction extends ExtractionInterface {

  val StationsNumFields = 4
  val StationsNumFields2 = 2

  val TempsNumFields = 5

  val MonthMin = 1
  val MonthMax = 12
  val DayMin = 1
  val DayMax = 31

  val LatitudeMax = 90
  val LongitudeMax = 180

  val NoTempStr = "9999.9"

  type StnId = Int
  type WbanId = Int

  type Month = Int
  type Day = Int

  /**
    * 1st milestone: data extraction
    */

  /**
    * @param year             Year number
    * @param stationsFile     Path of the stations resource file to use (e.g. "/stations.csv")
    * @param temperaturesFile Path of the temperatures resource file to use (e.g. "/1975.csv")
    * @return A sequence containing triplets (date, location, temperature)
    */
  def locateTemperatures(
    year: Year, stationsFile: String, temperaturesFile: String): Iterable[(LocalDate, Location, Temperature)] = {

    val tempsLines: List[String] = getLinesIteratorFromResFile(temperaturesFile).toList
    val temps: List[((Option[StnId], Option[WbanId]), (Month, Day), Temperature)] = tempsLines.map(lineToTempRec)

    val stationsLines: List[String] = getLinesIteratorFromResFile(stationsFile).toList
    val stations: List[((Option[StnId], Option[WbanId]), Location)] = stationsLines
      .map(lineToStationRec)
      .filter({ case (_, loc) => !loc.lon.isNaN & !loc.lat.isNaN })

    val stationMap: Map[(Option[StnId], Option[WbanId]), Location] = stations.toMap
    temps.map({
      case ((stnId, wbanId), (month, day), temp) =>
        val localDate = LocalDate.of(year, month, day)
        val locOption = stationMap.get((stnId, wbanId))
        (localDate, locOption, temp)
      })
      .filter({ case (_, locOption, _) => locOption.nonEmpty })
      .map({ case (date, locOption, temp) => (date, locOption.get, temp) })
  }

  /**
    * @param records A sequence containing triplets (date, location, temperature)
    * @return A sequence containing, for each location, the average temperature over the year.
    */
  def locationYearlyAverageRecords(
    records: Iterable[(LocalDate, Location, Temperature)]): Iterable[(Location, Temperature)] = {

    val recordsByLoc: Map[Location, Iterable[(LocalDate, Location, Temperature)]] = records
      .groupBy({ case (_, loc, _) => loc })
    val tempsByLoc: Map[Location, Iterable[Temperature]] = recordsByLoc
      .map({ case (loc, recs) => (loc, recs.map({ case (_, _, temp) => temp })) })
    tempsByLoc.map({ case (loc, temps) => (loc, temps.fold(0D)(_ + _) / temps.size) })
  }

  def lineToTempRec(line: String): ((Option[StnId], Option[WbanId]), (Month, Day), Temperature) = {

    val fields = line.split(",")
    if (fields.length != TempsNumFields) sys.error(s"Temperatures file line must have $TempsNumFields fields. Found line with ${fields.length}.")

    val stnId = if (fields(0).nonEmpty) Some(fields(0).toInt) else None
    val wbanId = if (fields(1).nonEmpty) Some(fields(1).toInt) else None

    val month = fields(2).toInt
    if (month < MonthMin || month > MonthMax) sys.error(s"Month value must be between $MonthMin and $MonthMax, found: $month")
    val day = fields(3).toInt
    if (day < DayMin || day > DayMax) sys.error(s"Day value must be between $DayMin and $DayMax, found: $day")

    val temp =
      if (fields(4).nonEmpty && fields(4) != NoTempStr) tempFahrenheitToCelcius(fields(4).toDouble)
      else Double.NaN

    ((stnId, wbanId), (month, day), temp)
  }

  def lineToStationRec(line: String): ((Option[StnId], Option[WbanId]), Location) = {

    val fields = line.split(",")
    if (fields.length != StationsNumFields && fields.length != StationsNumFields2) sys.error(s"Stations file line must have $StationsNumFields or $StationsNumFields2 fields. Found line with ${fields.length}.")

    val stnId = if (fields(0).nonEmpty) Some(fields(0).toInt) else None
    val wbanId = if (fields(1).nonEmpty) Some(fields(1).toInt) else None

    val lat =
      if (fields.length == StationsNumFields && fields(2).nonEmpty) {
        val v = fields(2).toDouble
        if (v < -LatitudeMax || v > LatitudeMax) sys.error(s"Latitude value must be between -$LatitudeMax and $LatitudeMax. Found: $v")
        v
      }
      else Double.NaN
    val lon =
      if (fields.length == StationsNumFields && fields(3).nonEmpty) {
        val v = fields(3).toDouble
        if (v < -LongitudeMax || v > LongitudeMax) sys.error(s"Longitude value must be between -$LongitudeMax and $LongitudeMax. Found: $v")
        v
      }
      else Double.NaN

    ((stnId, wbanId), Location(lat, lon))
  }

  def tempFahrenheitToCelcius(degreesFahrenheit: Double): Double =
    (degreesFahrenheit - 32) / 1.8

  def getLinesIteratorFromResFile(resFile: String): Iterator[String] = {

    val resStream: InputStream = getClass.getResourceAsStream(resFile)
    if (resStream == null) sys.error(s"Resource file not found: $resFile")

    val bufSrc: BufferedSource = Source.fromInputStream(resStream)("UTF-8")
    bufSrc.getLines()
  }
}
