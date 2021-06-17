package observatory

import org.junit.Test

import scala.math._

trait Utils2Test extends MilestoneSuite {

  private val milestoneTest = namedMilestoneTest("raw data display", 2) _

  @Test def `angleDegreesToRadians, 0 `(): Unit = {
    val angleDeg = 0.0
    val expectedResult = 0.0

    val result = Utils.angleDegreesToRadians(angleDeg)

    assert(result == expectedResult, s"Expected: $expectedResult, actual: $result")
  }

  @Test def `angleDegreesToRadians, 90 `(): Unit = {
    val angleDeg = 90.0
    val expectedResult = Pi / 2

    val result = Utils.angleDegreesToRadians(angleDeg)

    assert(result == expectedResult, s"Expected: $expectedResult, actual: $result")
  }

  @Test def `angleDegreesToRadians, 180 `(): Unit = {
    val angleDeg = 180.0
    val expectedResult = Pi

    val result = Utils.angleDegreesToRadians(angleDeg)

    assert(result == expectedResult, s"Expected: $expectedResult, actual: $result")
  }

  @Test def `greatCircleDistanceCentralAngle, same loc`(): Unit = {
    val loc1 = Location(45.0, 90.0)
    val loc2 = Location(loc1.lat, loc1.lon)

    val angleRes = Utils.greatCircleDistanceCentralAngle(loc1, loc2)

    assert(angleRes == 0.0, s"angleRes1 should be 0, actual: $angleRes")
  }

  @Test def `greatCircleDistanceCentralAngle, antipodes`(): Unit = {
    val loc1 = Location(45.0, 90.0)
    val loc2 = Location(-loc1.lat, -loc1.lon)

    val angleRes = Utils.greatCircleDistanceCentralAngle(loc1, loc2)

    assert(angleRes == Pi, s"angleRes1 should be $Pi, actual: $angleRes")
  }

  @Test def `greatCircleDistanceCentralAngle, 90 degrees lat`(): Unit = {
    val loc1 = Location(0.0, 0.0)
    val loc2 = Location(90.0, 0.0)

    val angleRes = Utils.greatCircleDistanceCentralAngle(loc1, loc2)

    assert(angleRes == Pi / 2, s"angleRes should be ${Pi / 2}, actual: $angleRes")
  }

  @Test def `greatCircleDistanceCentralAngle, 90 degrees lon`(): Unit = {
    val loc1 = Location(0.0, 0.0)
    val loc2 = Location(0.0, 90.0)

    val angleRes = Utils.greatCircleDistanceCentralAngle(loc1, loc2)

    assert(angleRes == Pi / 2, s"angleRes should be ${Pi / 2}, actual: $angleRes")
  }

  @Test def `greatCircleDistanceCentralAngle, closer to 1st`(): Unit = {
    val loc1 = Location(0.0, 0.0)
    val loc2 = Location(45.0, 90.0)
    val loc = Location(loc2.lat / 4, loc2.lon / 4)

    val angleRes1 = Utils.greatCircleDistanceCentralAngle(loc1, loc)
    val angleRes2 = Utils.greatCircleDistanceCentralAngle(loc2, loc)

    assert(angleRes1 > 0 && angleRes1 < angleRes2, s"0 < angleRes1 < angleRes2, angleRes1: $angleRes1, angleRes2: $angleRes2")
  }

  @Test def `greatCircleDistanceCentralAngle, closer to 2nd`(): Unit = {
    val loc1 = Location(0.0, 0.0)
    val loc2 = Location(45.0, 90.0)
    val loc = Location(loc2.lat / 4 * 3, loc2.lon / 4 * 3)

    val angleRes1 = Utils.greatCircleDistanceCentralAngle(loc1, loc)
    val angleRes2 = Utils.greatCircleDistanceCentralAngle(loc2, loc)

    assert(angleRes2 > 0 && angleRes2 < angleRes1, s"0 < angleRes2 < angleRes1, angleRes1: $angleRes1, angleRes2: $angleRes2")
  }
}
