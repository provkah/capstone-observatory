package observatory

import com.sksamuel.scrimage.{Image, Pixel}

import scala.math.{pow, round}

/**
  * 2nd milestone: basic visualization
  */
object Visualization extends VisualizationInterface {

  val InverseDistanceWeighingPower = 2

  val EarthRadiusKm = 6378.0

  val DistanceThresholdKm = 1.0

  /**
    * @param temperatures Known temperatures: pairs containing a location and the temperature at this location
    * @param location Location where to predict the temperature
    * @return The predicted temperature at `location`
    */
  def predictTemperature(temperatures: Iterable[(Location, Temperature)], location: Location): Temperature = {

    if (temperatures.isEmpty) sys.error("temperatures collection is empty")

    if (temperatures.size == 1) temperatures.head match { case (_, tempr) => tempr }
    else {
      val tempExactLocOpt: Option[(Location, Temperature)] = temperatures
        .find({ case (loc, _) => loc == location })
      if (tempExactLocOpt.nonEmpty) tempExactLocOpt.get match { case (_, tempr ) => tempr }
      else {
        val distTemprs: Iterable[(Double, Temperature)] = temperatures
          .map({ case (loc, tempr) =>
            val centralAngle =  Utils.greatCircleDistanceCentralAngle(location, loc)
            val dist = EarthRadiusKm * centralAngle
            (dist, tempr)
          })

        val distTemprThresholdOpt: Option[(Double, Temperature)] = distTemprs
          .find({ case (dist, _) => dist <= DistanceThresholdKm } )
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
  }

  /**
    * @param points Pairs containing a value and its associated color
    * @param value The value to interpolate
    * @return The color that corresponds to `value`, according to the color scale defined by `points`
    */
  def interpolateColor(points: Iterable[(Temperature, Color)], value: Temperature): Color = {

    if (points.isEmpty) sys.error("points collection must not be empty")

    if (points.size == 1) {
      val (tempr, color) = points.head
      if (tempr == value) color
      else sys.error("points collection must have at least 2 items for interpolation")
    } else {

      val temprColorMap: Map[Temperature, Color] = points.toMap

      val colorOpt: Option[Color] = temprColorMap.get(value)
      if (colorOpt.nonEmpty) colorOpt.get
      else {
        val temprSorted: Array[Temperature] = temprColorMap.keys.toArray.sorted

        val (idx1, idx2) = Utils.findPointsForLinearInterpolation(temprSorted, value)

        val t1 = temprSorted(idx1)
        val t2 = temprSorted(idx2)
        val c1 = temprColorMap(t1)
        val c2 = temprColorMap(t2)

        val red = Utils.linearInterpolation((t1, c1.red.toDouble), (t2, c2.red.toDouble), value)
        val green = Utils.linearInterpolation((t1, c1.green.toDouble), (t2, c2.green.toDouble), value)
        val blue = Utils.linearInterpolation((t1, c1.blue.toDouble), (t2, c2.blue.toDouble), value)

        Color(
          Utils.clipRgbColor(round(red).toInt),
          Utils.clipRgbColor(round(green).toInt),
          Utils.clipRgbColor(round(blue).toInt))
      }
    }
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

