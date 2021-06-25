package observatory

import scala.collection.mutable

/**
  * 4th milestone: value-added information
  */
object Manipulation extends ManipulationInterface {

  /**
    * @param temperatures Known temperatures
    * @return A function that, given a latitude in [-89, 90] and a longitude in [-180, 179],
    *         returns the predicted temperature at this location
    */
  def makeGrid(temperatures: Iterable[(Location, Temperature)]): GridLocation => Temperature = {

    Console.println(s"temperatures: ${temperatures.size}")

    val gridLockSet = temperatures
      .map({ case (l, _) => Utils.locationToGridLocation(l) })
      .toSet
    Console.println(s"gridLockSet: ${gridLockSet.size}")

    val gridLockTemperatureSet = gridLockSet
      .par
      .map(l => (l, Visualization.predictTemperature(temperatures, Location(l.lat, l.lon))))
    Console.println(s"gridLockTemperatureSet: ${gridLockTemperatureSet.size}")

    val gridLocTemperatureMap = new mutable.HashMap[GridLocation, Temperature]
    gridLocTemperatureMap ++= gridLockTemperatureSet.toList
    Console.println(s"gridLocTemperatureMap: ${gridLocTemperatureMap.size}")

    val temperaturesForPredictions = gridLockTemperatureSet
      .map({ case (gl, t) => (Location(gl.lat, gl.lon), t )})
      .toList
    Console.println(s"temperaturesForPredictions: ${temperaturesForPredictions.size}")

    (gridLoc: GridLocation) => {
      val temperatureOpt = synchronized {
        gridLocTemperatureMap.get(gridLoc)
      }
      temperatureOpt match {
        case Some(t) => t
        case None =>
          val loc = Location(gridLoc.lat, gridLoc.lon)
          val t = Visualization.predictTemperature(temperaturesForPredictions, loc)
          synchronized {
            gridLocTemperatureMap += ((gridLoc, t))
          }
          t
      }
    }
  }

  /**
    * @param temperatures Sequence of known temperatures over the years (each element of the collection
    *                      is a collection of pairs of location and temperature)
    * @return A function that, given a latitude and a longitude, returns the average temperature at this location
    */
  def average(temperatures: Iterable[Iterable[(Location, Temperature)]]): GridLocation => Temperature = {
    ???
  }

  /**
    * @param temperatures Known temperatures
    * @param normals A grid containing the “normal” temperatures
    * @return A grid containing the deviations compared to the normal temperatures
    */
  def deviation(temperatures: Iterable[(Location, Temperature)], normals: GridLocation => Temperature): GridLocation => Temperature = {
    ???
  }
}

