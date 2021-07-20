package observatory

// import org.apache.log4j.{Level, Logger}
// import org.apache.spark.{SparkConf, SparkContext}
// import org.apache.spark.sql.SparkSession

object Main extends App {

  val StationsFile = "stations.csv"

  // Logger.getLogger("org.apache.spark").setLevel(Level.WARN)

  /*val spark: SparkSession =
    SparkSession.builder().appName("Observatory").master("local").getOrCreate()

  spark.close()*/

  /*val conf: SparkConf = new SparkConf().setMaster("local").setAppName("Observatory")
  val sc: SparkContext = new SparkContext(conf)*/

  // 1975 to 1990
  val YearsForNormals = 1975 to 1990
  Console.println(s"yearsForNormals: $YearsForNormals")

  // 1991 to 2015
  val YearsForTemperatureDeviations = 1991 to 2015
  Console.println(s"yearsForTemperatureDeviations: $YearsForTemperatureDeviations")

  val overwriteFiles = false
  Console.println(s"overwriteFiles: $overwriteFiles")

  private def yearLocationAvgTemperatures(
    years: Iterable[Year],
    stationLocationMap: Map[StationId, Location]): Iterable[(Year, Iterable[(Location, Temperature)])] = {

    years.map(year => {
      val temperatureRecs = Extraction.locateTemperatures(year, s"/$year.csv", stationLocationMap)
      Console.println(s"Year: $year, temperatureRecs size: ${temperatureRecs.size}")

      val locAvgTemperatures = Extraction.locationYearlyAverageRecords(temperatureRecs)
      Console.println(s"Year: $year, locAvgTemps size: ${locAvgTemperatures.size}")

      (year, locAvgTemperatures)
    }).toList
  }

  val stations = Extraction.locateStations(s"/$StationsFile")
  Console.println(s"stations size: ${stations.size}")

  val allYears = YearsForNormals ++ YearsForTemperatureDeviations
  Console.println(s"allYears: $allYears")

  val allYearLocationAvgTemperatures = yearLocationAvgTemperatures(allYears, stations.toMap)
  Console.println(s"allYearLocationAvgTemperatures: ${allYearLocationAvgTemperatures.size}")

  for ((year, locAvgTemperatures) <- allYearLocationAvgTemperatures.par) {
    Console.println(s"Year: $year, locAvgTemps size: ${locAvgTemperatures.size}")

    // val image = Visualization.visualize(locAvgTemperatures, OutputUtils.temperatureColors)
    // Console.println(s"Created image: $image")

    Interaction.generateTiles(year, locAvgTemperatures, Interaction.generateTemperatureImageFile, overwriteFiles)

    val gridLocTemperatureGrid = Manipulation.makeGrid(locAvgTemperatures)
    Console.println(s"Year: $year, created gridLocTemperatureGrid")

    Utils.GridLocations.par.foreach(gridLocTemperatureGrid)
    Console.println(s"Year: $year, Completed gridLocTemperatureGrid")

    val temperaturesInAllGridLocs = Utils.GridLocations.par.map(gridLocTemperatureGrid)
    Console.println(s"Year: $year, temperaturesInAllGridLocs: ${temperaturesInAllGridLocs.size}")
  }

  val yearLocAvgTemperaturesForNormals = allYearLocationAvgTemperatures
    .filter({ case (year, _) => YearsForNormals.contains(year) })
  Console.println(s"yearsForNormals: $YearsForNormals, yearLocAvgTemperaturesForNormals: ${yearLocAvgTemperaturesForNormals.size}")

  val yearsLocAvgTemperaturesForNormals = yearLocAvgTemperaturesForNormals
    .map({ case (_, locAvgTemperatures) => locAvgTemperatures })
  var avgTemperatureNormalsGrid = Manipulation.average(yearsLocAvgTemperaturesForNormals)
  Console.println(s"yearsForNormals: $YearsForNormals, created avgTemperatureNormalsGrid")

  val yearLocAvgTemperaturesDeviations = allYearLocationAvgTemperatures
    .filter({ case (year, _) => YearsForTemperatureDeviations.contains(year) })
  Console.println(s"yearsForTemperatureDeviations: $YearsForTemperatureDeviations, yearLocAvgTemperaturesDeviations: ${yearLocAvgTemperaturesDeviations.size}")
  for ((year, locAvgTemperatures) <- yearLocAvgTemperaturesDeviations.par) {
    Console.println(s"Deviations, year: $year, locAvgTemps size: ${locAvgTemperatures.size}")

    val temperatureDeviationGrid = Manipulation.deviation(locAvgTemperatures, avgTemperatureNormalsGrid)
    Console.println(s"Deviations, year: $year, created temperatureDeviationGrid")

    val temperatureDeviations = Utils.LocationsForGrid.par
      .zip(Utils.GridLocations.par.map(l => temperatureDeviationGrid(l)))
      .toList
    Console.println(s"Deviations, year: $year, temperatureDeviations: ${temperatureDeviations.size}")

    Interaction.generateTiles(year, temperatureDeviations, Interaction.generateTemperatureDeviationImageFile, overwriteFiles)
  }
}
