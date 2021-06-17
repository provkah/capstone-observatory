package observatory

import java.io.InputStream

import scala.io.{BufferedSource, Source}
import scala.math.{abs, sin, cos, acos, Pi}

object Utils {

  def greatCircleDistanceCentralAngle(loc1: Location, loc2: Location): Double = {

    if (loc1 == loc2) 0.0
    else if (loc1.isAntipode(loc2)) Pi
    else {
      val lat1 = angleDegreesToRadians(loc1.lat)
      val lat2 = angleDegreesToRadians(loc2.lat)
      val deltaLon = angleDegreesToRadians(abs(loc1.lon - loc2.lon))
      acos(sin(lat1) * sin(lat2) + cos(lat1) * cos(lat1) * cos(deltaLon))
    }
  }

  def angleDegreesToRadians(angleInDegrees: Double): Double = angleInDegrees / 180.0 * Pi

  def tempFahrenheitToCelcius(degreesFahrenheit: Double): Double =
    (degreesFahrenheit - 32) / 1.8

  def getLinesIteratorFromResFile(resFile: String, classObj: Class[_]): Iterator[String] = {

    val resStream: InputStream = classObj.getResourceAsStream(resFile)
    if (resStream == null) sys.error(s"Resource file not found: $resFile")

    val bufSrc: BufferedSource = Source.fromInputStream(resStream)("UTF-8")
    bufSrc.getLines()
  }
}
