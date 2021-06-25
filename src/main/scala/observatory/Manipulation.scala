package observatory

import scala.collection.parallel
import scala.collection.parallel.ParIterable
import scala.collection.parallel.immutable.ParMap
import scala.collection.parallel.mutable.ParHashMap

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

    def createGridLocTemperMap(
      temperatures: Iterable[(Location, Temperature)]): ParHashMap[GridLocation, Temperature] = {

      val gridLocTemperatures: ParIterable[(GridLocation, Temperature)] = temperatures.par.map({
        case (loc, tempr) => (Utils.locationToGridLocation(loc), tempr)
      })

      val gridLocTemperaturesMap: ParMap[GridLocation, ParIterable[(GridLocation, Temperature)]] =
        gridLocTemperatures.groupBy({ case (loc, _) => loc })
      val gridLocAvgTemperatureMap: parallel.ParMap[GridLocation, Temperature] =
        gridLocTemperaturesMap.mapValues(s => s.map({ case (_, t) => t }).fold(0.0)(_ + _) / s.size)

      val map: ParHashMap[GridLocation, Temperature] = new collection.mutable.HashMap[GridLocation, Temperature].par
      map ++= gridLocAvgTemperatureMap.toList
    }

    def createTemperaturesForPredictions(
      gridLocTemperMap: ParHashMap[GridLocation, Temperature]): Iterable[(Location, Temperature)] = {

      val locTemperatureMap: ParHashMap[Location, Temperature] =
        gridLocTemperMap.map({ case (gridLoc, t) => (Location(gridLoc.lat, gridLoc.lon), t) } )
      locTemperatureMap.toList
    }

    Console.println(s"temperatures: ${temperatures.size}")

    val gridLocTemperMap: ParHashMap[GridLocation, Temperature] = createGridLocTemperMap(temperatures)
    Console.println(s"gridLocTemperMap: ${gridLocTemperMap.size}")

    val temperaturesForPredictions: Iterable[(Location, Temperature)] = createTemperaturesForPredictions(gridLocTemperMap)
    Console.println(s"temperaturesForPredictions: ${temperaturesForPredictions.size}")

    (gridLoc: GridLocation) => {
      gridLocTemperMap.get(gridLoc) match {
        case Some(t) => t
        case None =>
          val loc = Location(gridLoc.lat, gridLoc.lon)
          val t = Visualization.predictTemperature(temperaturesForPredictions, loc)
          gridLocTemperMap += ((gridLoc, t))
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

