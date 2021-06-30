package object observatory {
  type Temperature = Double // °C, introduced in Week 1

  type Year = Int // Calendar year, introduced in Week 1
  type Month = Int
  type Day = Int

  type StnId = Int
  type WbanId = Int

  type StationId = (Option[StnId], Option[WbanId])
}
