package observatory

// import org.apache.log4j.{Level, Logger}
// import org.apache.spark.{SparkConf, SparkContext}
// import org.apache.spark.sql.SparkSession

object Main extends App {

  val StationsFile = "stations.csv"

  // Logger.getLogger("org.apache.spark").setLevel(Level.WARN)

  /*val spark: SparkSession =
    SparkSession
      .builder()
      .appName("Observatory")
      .master("local")
      .getOrCreate()

  spark.close()*/

  /*val conf: SparkConf = new SparkConf().setMaster("local").setAppName("Observatory")
  val sc: SparkContext = new SparkContext(conf)*/

  val stations = Extraction.locateStations(s"/$StationsFile")
  Console.println(s"stations size: ${stations.size}")

  // 1975 to 1990
  val yearsForNormals: Range = 1975 to 1990
  val stationLocationMap = stations.toMap
  val yearLocAvgTemperatures: Seq[(Year, Iterable[(Location, Temperature)])] = for {
    year <- yearsForNormals
    p1 = Console.println(s"Year: $year")

    temperatureRecs = Extraction.locateTemperatures(year, s"/$year.csv", stationLocationMap)
    p2 = Console.println(s"Year: $year, temperatureRecs size: ${temperatureRecs.size}")

    locAvgTemperatures = Extraction.locationYearlyAverageRecords(temperatureRecs)
    p3 = Console.println(s"Year: $year, locAvgTemps size: ${locAvgTemperatures.size}")
  } yield (year, locAvgTemperatures)
  Console.println(s"From ${yearsForNormals.start} to ${yearsForNormals.end}, yearLocAvgTemperatures: ${yearLocAvgTemperatures.size}")

  val yearsLocAvgTemperatures = yearLocAvgTemperatures.map({ case (_, locAvgTemperatures) => locAvgTemperatures })
  var temperatureAverages = Manipulation.average(yearsLocAvgTemperatures)
  Console.println(s"From ${yearsForNormals.start} to ${yearsForNormals.end}, created temperatureAverages grid")

  /*for ((year, locAvgTemperatures) <- yearLocAvgTemperatures) {
    Console.println(s"Year: $year, locAvgTemps size: ${locAvgTemperatures.size}")

    // val image: Image = Visualization.visualize(locAvgTemperatures, temperatureColors)
    // Console.println(s"Created image: $image")

    // Interaction.generateTiles(year, locAvgTemperatures, OutputUtils.generateTemperatureImageFile)

    val gridLocTemperatureMap: GridLocation => Temperature = Manipulation.makeGrid(locAvgTemperatures)
    Console.println(s"Year: $year, makeGrid gridLocTemperatureMap")

    Utils.GridLocations.par.foreach(gridLocTemperatureMap)
    Console.println(s"Year: $year, Completed gridLocTemperatureMap")

    val temperaturesInGridLocs = Utils.GridLocations.par.map(gridLocTemperatureMap)
    Console.println(s"Year: $year, temperaturesInGridLocs: ${temperaturesInGridLocs.size}")

    val temperatureDeviationGrid = Manipulation.deviation(locAvgTemperatures, temperatureAverages)
    Console.println(s"Year: $year, created temperatureDeviationGrid")
  }*/

  // 1991 to 2015
  val yearsForTemperatureDeviations: Range = 1991 to 2015
  for (year <- yearsForTemperatureDeviations) {
    Console.println(s"Year: $year")

    val temperatureRecs = Extraction.locateTemperatures(year, s"/$year.csv", stationLocationMap)
    Console.println(s"Year: $year, temperatureRecs size: ${temperatureRecs.size}")

    val locAvgTemperatures = Extraction.locationYearlyAverageRecords(temperatureRecs)
    Console.println(s"Year: $year, locAvgTemps size: ${locAvgTemperatures.size}")

    val temperatureDeviationGrid = Manipulation.deviation(locAvgTemperatures, temperatureAverages)
    Console.println(s"Year: $year, created temperatureDeviationGrid")

    val temperatureDeviations = Utils.LocationsForGrid.par
      .zip(Utils.GridLocations.par.map(l => temperatureDeviationGrid(l)))
      .toList
    Console.println(s"Year: $year, temperatureDeviations: ${temperatureDeviations.size}")

    Interaction.generateTiles(year, temperatureDeviations, OutputUtils.generateTemperatureDeviationImageFile)
  }
}
