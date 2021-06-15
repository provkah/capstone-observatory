package observatory

import java.io.InputStream
import scala.io.{BufferedSource, Source}

object Utils {

  def tempFahrenheitToCelcius(degreesFahrenheit: Double): Double =
    (degreesFahrenheit - 32) / 1.8

  def getLinesIteratorFromResFile(resFile: String, classObj: Class[_]): Iterator[String] = {

    val resStream: InputStream = classObj.getResourceAsStream(resFile)
    if (resStream == null) sys.error(s"Resource file not found: $resFile")

    val bufSrc: BufferedSource = Source.fromInputStream(resStream)("UTF-8")
    bufSrc.getLines()
  }
}
