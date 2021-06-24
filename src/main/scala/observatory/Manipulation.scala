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

    val gridLocTemperatures: Iterable[(GridLocation, Temperature)] = temperatures.map({
      case (loc, tempr) => (Utils.locationToGridLocation(loc), tempr)
    })
    // val gridLocTemperMap: Map[GridLocation, Temperature] = gridLocTemperatures.toMap
    val gridLocTemperMap: mutable.Map[GridLocation, Temperature] =
      new mutable.HashMap[GridLocation, Temperature] ++ gridLocTemperatures

    (gridLocation: GridLocation) => {
      gridLocTemperMap.get(gridLocation) match {
        case Some(t) => t
        case None =>
          val t = Visualization.predictTemperature(temperatures, Location(gridLocation.lat, gridLocation.lon))
          gridLocTemperMap.+((gridLocation, t))
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

