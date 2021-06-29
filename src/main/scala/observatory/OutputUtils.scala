package observatory

import com.sksamuel.scrimage.nio.ImageWriter

import java.nio.file.{Files, Paths}

object OutputUtils extends OutputUtilsInterface {

  def generateImageFile(
    year: Int, tile: Tile, locTemperatures: Iterable[(Location, Temperature)],
    outputFolder: String): Unit = {

    Console.println(s"Generating image, year: $year, tile $tile")
    val image = Interaction.tile(locTemperatures, temperatureColors, tile)
    Console.println(s"Image, year: $year, tile $tile, image $image")

    val imageFolderName = s"$outputFolder/$year/${tile.zoom}"
    val folderPath = Paths.get(imageFolderName)
    Files.createDirectories(folderPath)
    Console.println(s"created, if did not exist, folder: $folderPath")

    val imageFileName = s"$imageFolderName/${tile.x}-${tile.y}.png"
    image.output(imageFileName)(ImageWriter.default)
    Console.println(s"Created image file: $imageFileName")
  }

  def generateTemperatureImageFile(
    year: Int, tile: Tile, locTemperatures: Iterable[(Location, Temperature)]): Unit =

    generateImageFile(year, tile, locTemperatures, TemperatureImageOutputFolder)

  def generateTemperatureDeviationImageFile(
     year: Int, tile: Tile, locTemperatures: Iterable[(Location, Temperature)]): Unit =

    generateImageFile(year, tile, locTemperatures, TemperatureDeviationImageOutputFolder)
}
