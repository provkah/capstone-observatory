package observatory

import com.sksamuel.scrimage.{Image, Pixel}

import scala.collection.parallel.ParIterable
import scala.math.{pow, round}

/**
  * 2nd milestone: basic visualization
  */
object Visualization extends VisualizationInterface {

  val InverseDistanceWeighingPower = 2

  val DistanceThresholdKm = 1.0

  val ImageWidth = 360
  val ImageHeight = 180

  val RgbaAlpha = 0.5

  /**
    * @param temperatures Known temperatures: pairs containing a location and the temperature at this location
    * @param location Location where to predict the temperature
    * @return The predicted temperature at `location`
    */
  def predictTemperature(temperatures: Iterable[(Location, Temperature)], location: Location): Temperature = {

    if (temperatures.isEmpty) sys.error("temperatures collection is empty")

    if (temperatures.size == 1) temperatures.head match { case (_, t) => t }
    else {
      val locTemperatures: ParIterable[(Location, Temperature)] = temperatures.par

      val locTempreratureExactLocOpt: Option[(Location, Temperature)] = locTemperatures
        .find({ case (loc, _) => loc == location })
      if (locTempreratureExactLocOpt.nonEmpty) locTempreratureExactLocOpt.get match { case (_, t ) => t }
      else {
        val distTemperatures: ParIterable[(Double, Temperature)] = locTemperatures
          .map({ case (loc, t) =>
            val centralAngle =  Utils.greatCircleDistanceCentralAngle(location, loc)
            (EarthRadiusKm * centralAngle, t)
          })

        val closeDistTemperature: ParIterable[(Double, Temperature)] =
          distTemperatures.filter({ case (d, _) => d <= DistanceThresholdKm })
        if (closeDistTemperature.nonEmpty) closeDistTemperature.minBy({ case (d, _) => d }) match { case (_, t) => t }
        else {
          val distWeights: ParIterable[Double] = distTemperatures
            .map({ case (dist, _) => 1.0D / pow(dist, InverseDistanceWeighingPower) })

          val sumOfWeights = distWeights.fold(0.0D)(_ + _)

          locTemperatures
            .zip(distWeights).map({ case ((_, t), w) => w * t })
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
      val (temperature, color) = points.head
      if (temperature == value) color
      else sys.error("points collection must have at least 2 items for interpolation")
    } else {

      val temperatureColorMap: Map[Temperature, Color] = points.toMap

      val colorOpt: Option[Color] = temperatureColorMap.get(value)
      if (colorOpt.nonEmpty) colorOpt.get
      else {
        val temperaturesSorted: Array[Temperature] = temperatureColorMap.keys.toArray.sorted

        val (idx1, idx2) = Utils.findPointsForLinearInterpolation(temperaturesSorted, value)

        val t1 = temperaturesSorted(idx1)
        val t2 = temperaturesSorted(idx2)
        val c1 = temperatureColorMap(t1)
        val c2 = temperatureColorMap(t2)

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

    val pixelLocations: Seq[Location] = for {
      y <- 0 until ImageHeight
      x <- 0 until ImageWidth
      lat = Utils.LatitudeMax - y
      lon = x - Utils.LongitudeMax
    } yield Location(lat, lon)

    val alpha = (RgbaAlpha * 256 - 1).toInt
    val pixels: ParIterable[Pixel] = locationsToPixels(pixelLocations.par, alpha, temperatures, colors)

    Image(ImageWidth, ImageHeight, pixels.toArray)
  }

  def locationsToPixels(
           pixelLocations: ParIterable[Location],
           alpha: Int,
           temperatures: Iterable[(Location, Temperature)],
           colors: Iterable[(Temperature, Color)]): ParIterable[Pixel] = {

    val pixelTemperatures: ParIterable[Temperature] = pixelLocations
      .map(predictTemperature(temperatures, _))

    val pixelColors: ParIterable[Color] = pixelTemperatures.map(interpolateColor(colors, _))

    pixelColors.map(c => Pixel(c.red, c.green, c.blue, alpha))
  }
}

