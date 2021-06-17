package observatory

import org.junit.Test

import scala.math.abs

trait VisualizationTest extends MilestoneSuite {

  private val milestoneTest = namedMilestoneTest("raw data display", 2) _

  @Test def `Empty temperatures results in RuntimeException`(): Unit = {
    val locTemps: Iterable[(Location, Temperature)] = List()
    val loc = Location(0.0, 0.0)

    val temp = try {
      Visualization.predictTemperature(locTemps, loc)
    } catch {
      case ex: RuntimeException =>
        Double.NaN
    }

    assert(temp.isNaN, s"temp result should be $Double.NaN")
  }

  @Test def `1 entry, location doesn't matter`(): Unit = {
    val (loc1, temp1) = (Location(0.0, 0.0), 0.0)
    val locTemps = List((loc1, temp1))
    val loc = Location(45.0, 90.0)

    val tempRes = Visualization.predictTemperature(locTemps, loc)

    assert(tempRes == temp1, s"tempRes should be $temp1, actual: $tempRes")
  }

  @Test def `Exact location, 2 entries, 1st match`(): Unit = {
    val (loc1, temp1) = (Location(0.0, 0.0), 1.0)
    val (loc2, temp2) = (Location(45.0, 90.0), 2.0)
    val locTemps = List((loc1, temp1), (loc2, temp2))
    val loc = Location(loc1.lat, loc1.lon)

    val tempRes = Visualization.predictTemperature(locTemps, loc)

    assert(tempRes == temp1, s"tempRes should be $temp1, actual: $tempRes")
  }

  @Test def `Exact location, 2 entries, 2nd match`(): Unit = {
    val (loc1, temp1) = (Location(0.0, 0.0), 1.0)
    val (loc2, temp2) = (Location(45.0, 90.0), 2.0)
    val locTemps = List((loc1, temp1), (loc2, temp2))
    val loc = Location(loc2.lat, loc2.lon)

    val tempRes = Visualization.predictTemperature(locTemps, loc)

    assert(tempRes == temp2, s"tempRes should be $temp2, actual: $tempRes")
  }

  @Test def `Exact location, 3 entries, 2nd match`(): Unit = {
    val (loc1, temp1) = (Location(0.0, 0.0), 1.0)
    val (loc2, temp2) = (Location(45.0, 90.0), 2.0)
    val (loc3, temp3) = (Location(-45.0, -90.0), 3.0)
    val locTemps = List((loc1, temp1), (loc2, temp2), (loc3, temp3))
    val loc = Location(loc2.lat, loc2.lon)

    val tempRes = Visualization.predictTemperature(locTemps, loc)

    assert(tempRes == temp2, s"tempRes should be $temp2, actual: $tempRes")
  }

  @Test def `2 entries, close to 1st, 10, 20`(): Unit = {
    val (loc1, temp1) = (Location(0.0, 0.0), 10.0)
    val (loc2, temp2) = (Location(45.0, 90.0), 20.0)
    val locTemps = List((loc1, temp1), (loc2, temp2))
    val loc = Location(loc2.lat / 4, loc2.lon / 4)

    val tempRes = Visualization.predictTemperature(locTemps, loc)

    assert(abs(tempRes - temp1) < abs(tempRes - temp2), s"tempRes should be closer to $temp1 than to $temp2, value: $tempRes")
  }

  @Test def `2 entries, close to 1st, 20, 10`(): Unit = {
    val (loc1, temp1) = (Location(0.0, 0.0), 20.0)
    val (loc2, temp2) = (Location(45.0, 90.0), 10.0)
    val locTemps = List((loc1, temp1), (loc2, temp2))
    val loc = Location(loc2.lat / 4, loc2.lon / 4)

    val tempRes = Visualization.predictTemperature(locTemps, loc)

    assert(abs(tempRes - temp1) < abs(tempRes - temp2), s"tempRes should be closer to $temp1 than to $temp2, value: $tempRes")
  }

  @Test def `2 entries, close to 2nd, 10, 20`(): Unit = {
    val (loc1, temp1) = (Location(0.0, 0.0), 10.0)
    val (loc2, temp2) = (Location(45.0, 90.0), 20.0)
    val locTemps = List((loc1, temp1), (loc2, temp2))
    val loc = Location(loc2.lat / 4 * 3, loc2.lon / 4 * 3)

    val tempRes = Visualization.predictTemperature(locTemps, loc)

    assert(abs(tempRes - temp2) < abs(tempRes - temp1), s"tempRes should be closer to $temp2 than to $temp1, value: $tempRes")
  }

  @Test def `2 entries, close to 2nd, 20, 10`(): Unit = {
    val (loc1, temp1) = (Location(0.0, 0.0), 20.0)
    val (loc2, temp2) = (Location(45.0, 90.0), 10.0)
    val locTemps = List((loc1, temp1), (loc2, temp2))
    val loc = Location(loc2.lat / 4 * 3, loc2.lon / 4 * 3)

    val tempRes = Visualization.predictTemperature(locTemps, loc)

    assert(abs(tempRes - temp2) < abs(tempRes - temp1), s"tempRes should be closer to $temp2 than to $temp1, value: $tempRes")
  }
}
