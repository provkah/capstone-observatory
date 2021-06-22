package observatory

import java.time.LocalDate

object Extraction extends ExtractionInterface {

  /**
    * 1st milestone: data extraction
    */

  def locateStations(stationsFile: String): Iterable[((Option[StnId], Option[WbanId]), Location)] = {

    val stationsLines: List[String] = Utils.getLinesIteratorFromResFile(stationsFile, getClass).toList
    stationsLines
      .map(ExtractionUtils.lineToStationRec)
      .filter({ case (_, loc) => loc.isValid })
  }

  /**
    * @param year             Year number
    * @param stationsFile     Path of the stations resource file to use (e.g. "/stations.csv")
    * @param temperaturesFile Path of the temperatures resource file to use (e.g. "/1975.csv")
    * @return A sequence containing triplets (date, location, temperature)
    */
  def locateTemperatures(
    year: Year, stationsFile: String, temperaturesFile: String): Iterable[(LocalDate, Location, Temperature)] = {

    val tempsLines: List[String] = Utils.getLinesIteratorFromResFile(temperaturesFile, getClass).toList
    val temps: List[((Option[StnId], Option[WbanId]), (Month, Day), Temperature)] =
      tempsLines.map(ExtractionUtils.lineToTempRec)

    val stations: Iterable[((Option[StnId], Option[WbanId]), Location)] = locateStations(stationsFile)

    val stationLocations: Map[(Option[StnId], Option[WbanId]), Location] = stations.toMap
    temps.map({
      case ((stnId, wbanId), (month, day), temp) =>
        val localDate = LocalDate.of(year, month, day)
        val locOption = stationLocations.get((stnId, wbanId))
        (localDate, locOption, temp)
      })
      .filter({ case (_, locOption, _) => locOption.nonEmpty })
      .map({ case (date, locOption, temp) => (date, locOption.get, temp) })
  }

  def locateTemperatures(
    year: Year, temperaturesFile: String,
    stationLocations: Map[(Option[StnId], Option[WbanId]), Location]): Iterable[(LocalDate, Location, Temperature)] = {

    val tempsLines: List[String] = Utils.getLinesIteratorFromResFile(temperaturesFile, getClass).toList
    val temps: List[((Option[StnId], Option[WbanId]), (Month, Day), Temperature)] =
      tempsLines.map(ExtractionUtils.lineToTempRec)

    temps.map({
      case ((stnId, wbanId), (month, day), temp) =>
        val localDate = LocalDate.of(year, month, day)
        val locOption = stationLocations.get((stnId, wbanId))
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
}
