package observatory

import java.io.InputStream
import scala.annotation.tailrec
import scala.collection.parallel.ParIterable
import scala.io.{BufferedSource, Source}
import scala.math.{Pi, abs, acos, cos, pow, sin}

object Utils extends UtilsInterface {

  def locationToGridLocation(l: Location): GridLocation = {
    val gridLat = Math.round(l.lat).toInt.max(GridLocLatitudeMin)
    val gridLon = Math.round(l.lon).toInt.min(GridLocLongitudeMax)
    GridLocation(gridLat, gridLon)
  }

  def linearInterpolation(p1: Point, p2: Point, x: Double): Double =
    (p1, p2) match {
      case ((x1, y1), (x2, y2)) => y1 + (y2 - y1) / (x2 - x1) * (x - x1)
    }

  // points must be sorted
  def findPointsForLinearInterpolation(points: Array[Double], value: Double): (Int, Int) = {

    // length (endIdx - startIdx + 1) is >= 2
    @tailrec
    def findPoints(startIdx: Int, endIdx: Int): (Int, Int) = {

      val length = endIdx - startIdx + 1
      if (length == 2) (startIdx, endIdx)
      else {
        val rightStartIdx = (startIdx + endIdx + 1) / 2
        if (length == 3)
          if (value <= points(rightStartIdx)) (startIdx, rightStartIdx)
          else (rightStartIdx, endIdx)
        else {
          val leftEndIdx = rightStartIdx - 1
          if (value <= points(leftEndIdx)) findPoints(startIdx, leftEndIdx)
          else if (value < points(rightStartIdx)) (leftEndIdx, rightStartIdx)
          else findPoints(rightStartIdx, endIdx)
        }
      }
    }

    if (points.length < 2) sys.error("points collection must have at least 2 items for interpolation")

    if (value <= points.head) (0, 1)
    else if (value >= points.last) (points.length - 2, points.length - 1)
    else findPoints(0, points.length - 1)
  }

  def greatCircleDistanceCentralAngle(loc1: Location, loc2: Location): Double = {

    if (loc1 == loc2) 0.0
    else if (loc1.isAntipode(loc2)) Pi
    else {
      val lat1 = angleDegreesToRadians(loc1.lat)
      val lat2 = angleDegreesToRadians(loc2.lat)
      val deltaLon = angleDegreesToRadians(abs(loc1.lon - loc2.lon))
      acos(sin(lat1) * sin(lat2) + cos(lat1) * cos(lat2) * cos(deltaLon))
    }
  }

  def clipRgbColor(color: Int): Int = color.min(RgbColorMax).max(RgbColorMin)

  def predictUsingInverseDistanceWeighting(
    distValues: ParIterable[(Double, Double)],
    distanceThreshold: Double,
    inverseDistanceWeightingPower: Int): Double = {

    // closeDistValues covers the case of exact match, when distance is 0
    val closeDistValues = distValues.filter({ case (d, _) => d <= distanceThreshold })
    if (closeDistValues.nonEmpty) closeDistValues
      .minBy({ case (d, _) => d }) match { case (_, v) => v }
    else {
      val weightsValues = distValues
        .map({ case (d, v) => (1.0D / pow(d, inverseDistanceWeightingPower), v) })

      val sumOfWeights = weightsValues.map({ case (w, _) => w }).fold(0.0D)(_ + _)

      weightsValues
        .map({ case (w, v) => w * v })
        .fold(0.0)(_ + _) / sumOfWeights
    }
  }

  def angleDegreesToRadians(angleInDegrees: Double): Double = angleInDegrees / 180.0 * Pi

  def average(values: Iterable[Double]): Double = values.fold(0.0)(_ + _) / values.size

  def average(values: ParIterable[Double]): Double = values.fold(0.0)(_ + _) / values.size

  def tempFahrenheitToCelcius(degreesFahrenheit: Double): Double =
    (degreesFahrenheit - 32) / 1.8

  def getLinesIteratorFromResFile(resFile: String, classObj: Class[_]): Iterator[String] = {

    val resStream: InputStream = classObj.getResourceAsStream(resFile)
    if (resStream == null) sys.error(s"Resource file not found: $resFile")

    val bufSrc: BufferedSource = Source.fromInputStream(resStream)("UTF-8")
    bufSrc.getLines()
  }
}
