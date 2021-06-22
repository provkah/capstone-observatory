package observatory

import java.time.LocalDate
import scala.collection.parallel.{ParIterable, ParMap, ParSeq}

object Extraction extends ExtractionInterface {

  /**
    * 1st milestone: data extraction
    */

  def locateStations(stationsFile: String): Iterable[((Option[StnId], Option[WbanId]), Location)] = {

    val stationsLines: ParSeq[String] =
      Utils.getLinesIteratorFromResFile(stationsFile, getClass).toList.par
    stationsLines
      .map(ExtractionUtils.lineToStationRec)
      .filter({ case (_, loc) => loc.isValid })
      .seq
  }

  /**
    * @param year             Year number
    * @param stationsFile     Path of the stations resource file to use (e.g. "/stations.csv")
    * @param temperaturesFile Path of the temperatures resource file to use (e.g. "/1975.csv")
    * @return A sequence containing triplets (date, location, temperature)
    */
  def locateTemperatures(
    year: Year, stationsFile: String, temperaturesFile: String): Iterable[(LocalDate, Location, Temperature)] = {

    val stations: Iterable[((Option[StnId], Option[WbanId]), Location)] = locateStations(stationsFile)
    val stationLocationMap: Map[(Option[StnId], Option[WbanId]), Location] = stations.toMap

    locateTemperatures(year, temperaturesFile, stationLocationMap)
  }

  def locateTemperatures(
    year: Year, temperaturesFile: String,
    stationLocationMap: Map[(Option[StnId], Option[WbanId]), Location]): Iterable[(LocalDate, Location, Temperature)] = {

    val tempsLines: ParSeq[String] =
      Utils.getLinesIteratorFromResFile(temperaturesFile, getClass).toList.par
    val temps: ParSeq[((Option[StnId], Option[WbanId]), (Month, Day), Temperature)] =
      tempsLines.map(ExtractionUtils.lineToTempRec)

    temps.map({ case ((stnId, wbanId), (month, day), temp) =>
        val localDate = LocalDate.of(year, month, day)
        val locOption = stationLocationMap.get((stnId, wbanId))
        (localDate, locOption, temp)
      })
      .filter({ case (_, locOption, _) => locOption.nonEmpty })
      .map({ case (date, locOption, temp) => (date, locOption.get, temp) })
      .seq
  }

  /**
    * @param records A sequence containing triplets (date, location, temperature)
    * @return A sequence containing, for each location, the average temperature over the year.
    */
  def locationYearlyAverageRecords(
    records: Iterable[(LocalDate, Location, Temperature)]): Iterable[(Location, Temperature)] = {

    val recordsByLoc: ParMap[Location, ParIterable[(LocalDate, Location, Temperature)]] = records.par
      .groupBy({ case (_, loc, _) => loc })
    val tempsByLoc: ParMap[Location, ParIterable[Temperature]] = recordsByLoc
      .map({ case (loc, recs) => (loc, recs.map({ case (_, _, temp) => temp })) })
    tempsByLoc.map({ case (loc, temps) => (loc, temps.fold(0D)(_ + _) / temps.size) }).seq
  }
}
