package observatory

import com.sksamuel.scrimage.{Image, Pixel}

import scala.math.pow

/**
  * 2nd milestone: basic visualization
  */
object Visualization extends VisualizationInterface {

  val InverseDistanceWeighingPower = 2

  val earthRadiusKm = 6378.0

  val distanceThresholdKm = 1.0

  /**
    * @param temperatures Known temperatures: pairs containing a location and the temperature at this location
    * @param location Location where to predict the temperature
    * @return The predicted temperature at `location`
    */
  def predictTemperature(temperatures: Iterable[(Location, Temperature)], location: Location): Temperature = {

    if (temperatures.isEmpty) sys.error("temperatures collection is empty")

    val tempExactLocOpt: Option[(Location, Temperature)] = temperatures
      .find({ case (loc, _) => loc == location })
    if (tempExactLocOpt.nonEmpty) tempExactLocOpt.get match { case (_, tempr ) => tempr }
    else {
      val distTemprs: Iterable[(Double, Temperature)] = temperatures
        .map({ case (loc, tempr) =>
          val centralAngle =  Utils.greatCircleDistanceCentralAngle(location, loc)
          val dist = earthRadiusKm * centralAngle
          (dist, tempr)
        })

      val distTemprThresholdOpt: Option[(Double, Temperature)] = distTemprs
        .find({ case (dist, _) => dist <= distanceThresholdKm } )
      if (distTemprThresholdOpt.nonEmpty) distTemprThresholdOpt.get match { case (_, tempr ) => tempr }
      else {
        val weights: Iterable[Double] = distTemprs
          .map({ case (dist, _) => 1.0D / pow(dist, InverseDistanceWeighingPower) })

        val sumOfWeights = weights.fold(0.0D)(_ + _)

        temperatures
          .zip(weights).map({ case ((_, tempr), weight) => weight * tempr })
          .fold(0.0D)(_ + _) / sumOfWeights
      }
    }
  }

  /**
    * @param points Pairs containing a value and its associated color
    * @param value The value to interpolate
    * @return The color that corresponds to `value`, according to the color scale defined by `points`
    */
  def interpolateColor(points: Iterable[(Temperature, Color)], value: Temperature): Color = {
    ???
  }

  /**
    * @param temperatures Known temperatures
    * @param colors Color scale
    * @return A 360×180 image where each pixel shows the predicted temperature at its location
    */
  def visualize(temperatures: Iterable[(Location, Temperature)], colors: Iterable[(Temperature, Color)]): Image = {
    ???
  }
}

