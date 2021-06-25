package observatory

import java.time.LocalDate

object Extraction extends ExtractionInterface {

  /**
    * 1st milestone: data extraction
    */

  def locateStations(stationsFile: String): Iterable[((Option[StnId], Option[WbanId]), Location)] = {

    val lines = Utils.getLinesIteratorFromResFile(stationsFile, getClass).toList
    lines.par
      .map(ExtractionUtils.lineToStationRec)
      .filter({ case (_, loc) => loc.isValid })
      .toList
  }

  /**
    * @param year             Year number
    * @param stationsFile     Path of the stations resource file to use (e.g. "/stations.csv")
    * @param temperaturesFile Path of the temperatures resource file to use (e.g. "/1975.csv")
    * @return A sequence containing triplets (date, location, temperature)
    */
  def locateTemperatures(
    year: Year, stationsFile: String, temperaturesFile: String): Iterable[(LocalDate, Location, Temperature)] = {

    val stationLocationMap = locateStations(stationsFile).toMap
    locateTemperatures(year, temperaturesFile, stationLocationMap)
  }

  def locateTemperatures(
    year: Year, temperaturesFile: String,
    stationLocationMap: Map[(Option[StnId], Option[WbanId]), Location]): Iterable[(LocalDate, Location, Temperature)] = {

    val lines = Utils.getLinesIteratorFromResFile(temperaturesFile, getClass).toList
    val temperatureRecs = lines.par
      .map(ExtractionUtils.lineToTemperatureRec)

    temperatureRecs.map({ case ((stnId, wbanId), (month, day), temp) =>
        val localDate = LocalDate.of(year, month, day)
        val locOption = stationLocationMap.get((stnId, wbanId))
        (localDate, locOption, temp)
      })
      .filter({ case (_, locOption, _) => locOption.nonEmpty })
      .map({ case (date, locOption, temp) => (date, locOption.get, temp) })
      .toList
  }

  /**
    * @param records A sequence containing triplets (date, location, temperature)
    * @return A sequence containing, for each location, the average temperature over the year.
    */
  def locationYearlyAverageRecords(
    records: Iterable[(LocalDate, Location, Temperature)]): Iterable[(Location, Temperature)] = {

    val recordsByLoc = records.par.groupBy({ case (_, loc, _) => loc })
    val tempsByLoc = recordsByLoc.map({ case (loc, recs) => (loc, recs.map({ case (_, _, temp) => temp })) })
    tempsByLoc
      .map({ case (loc, temperatures) => (loc, Utils.average(temperatures)) })
      .toList
  }
}
