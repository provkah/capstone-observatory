package observatory

object ExtractionUtils {

  val StationsNumFields = 4

  val TempsNumFields = 5

  val MonthMin = 1
  val MonthMax = 12
  val DayMin = 1
  val DayMax = 31

  val LatitudeMax = 90
  val LongitudeMax = 180

  val NoTempStr = "9999.9"

  def lineToTempRec(line: String): ((Option[StnId], Option[WbanId]), (Month, Day), Temperature) = {

    val fields = line.split(",")
    if (fields.length != TempsNumFields) sys.error(s"Temperatures file line must have $TempsNumFields fields. Found line with ${fields.length}.")

    val stnId = if (fields(0).nonEmpty) Some(fields(0).toInt) else None
    val wbanId = if (fields(1).nonEmpty) Some(fields(1).toInt) else None

    val month = fields(2).toInt
    if (month < MonthMin || month > MonthMax) sys.error(s"Month value must be between $MonthMin and $MonthMax, found: $month")
    val day = fields(3).toInt
    if (day < DayMin || day > DayMax) sys.error(s"Day value must be between $DayMin and $DayMax, found: $day")

    val temp =
      if (fields(4).nonEmpty && fields(4) != NoTempStr) Utils.tempFahrenheitToCelcius(fields(4).toDouble)
      else Double.NaN

    ((stnId, wbanId), (month, day), temp)
  }

  def lineToStationRec(line: String): ((Option[StnId], Option[WbanId]), Location) = {

    def getLatitude(field: String): Double = {
      val v = field.toDouble
      if (v < -LatitudeMax || v > LatitudeMax) sys.error(s"Latitude value must be between -$LatitudeMax and $LatitudeMax. Found: $v")
      v
    }

    def getLongitude(field: String): Double = {
      val v = field.toDouble
      if (v < -LongitudeMax || v > LongitudeMax) sys.error(s"Longitude value must be between -$LongitudeMax and $LongitudeMax. Found: $v")
      v
    }

    val fields = line.split(",")
    if (fields.isEmpty || fields.length > StationsNumFields) sys.error(s"Stations file line must have between 1 and $StationsNumFields fields. Found line with ${fields.length}.")

    val stnId =
      if (fields(0).nonEmpty) Some(fields(0).toInt)
      else None
    val wbanId =
      if (fields.length > 1 && fields(1).nonEmpty) Some(fields(1).toInt)
      else None

    val lat =
      if (fields.length > 2 && fields(2).nonEmpty) getLatitude(fields(2))
      else Double.NaN
    val lon =
      if (fields.length > 3 && fields(3).nonEmpty) getLongitude(fields(3))
      else Double.NaN

    ((stnId, wbanId), Location(lat, lon))
  }
}