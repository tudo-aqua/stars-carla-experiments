/*
 * Copyright 2023-2024 The STARS Carla Experiments Authors
 * SPDX-License-Identifier: Apache-2.0
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tools.aqua.stars.carla.experiments

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.split
import com.github.ajalt.clikt.parameters.types.int
import java.io.File
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.zip.ZipFile
import kotlin.io.path.name
import kotlin.time.measureTime
import tools.aqua.stars.core.evaluation.TSCEvaluation
import tools.aqua.stars.core.metric.metrics.evaluation.*
import tools.aqua.stars.core.metric.metrics.postEvaluation.*
import tools.aqua.stars.core.metric.utils.ApplicationConstantsHolder
import tools.aqua.stars.data.av.dataclasses.Actor
import tools.aqua.stars.data.av.dataclasses.Segment
import tools.aqua.stars.data.av.dataclasses.TickData
import tools.aqua.stars.data.av.metrics.AverageVehiclesInEgosBlockMetric
import tools.aqua.stars.importer.carla.CarlaDataLoader
import tools.aqua.stars.importer.carla.CarlaSimulationRunsWrapper

class ExperimentConfiguration : CliktCommand() {

  // region command line options
  private val simulationRunFolder: String by
      option("--input", help = "Directory of the input files")
          .default("./stars-reproduction-source/stars-experiments-data/simulation_runs")

  private val allEgo: Boolean by
      option("--allEgo", help = "Whether to treat all vehicles as ego").flag(default = false)

  private val minSegmentTickCount: Int by
      option("--minSegmentTicks", help = "Minimum ticks per segment").int().default(10)

  private val sortBySeed: Boolean by
      option("--sorted", help = "Whether to sort data by seed").flag(default = true)

  private val dynamicFilter: String by
      option("--dynamicFilter", help = "Regex to filter on dynamic data").default(".*")

  private val staticFilter: String by
      option("--staticFilter", help = "Regex to filter on static data").default(".*")

  private val projectionIgnoreList: List<String> by option("--ignore").split(",").default(listOf())

  private val noLogging: Boolean by
      option("--noLogging", help = "Whether to disable log and plot output").flag(default = false)
  // endregion

  override fun run() {
    ApplicationConstantsHolder.logging = !noLogging
    downloadAndUnzipExperimentsData()

    val time = measureTime {
      val simulationRunsWrappers = getSimulationRuns()
      val segments =
          CarlaDataLoader(
                  useEveryVehicleAsEgo = allEgo,
                  minSegmentTickCount = minSegmentTickCount,
                  orderFilesBySeed = sortBySeed)
              .loadSegments(
                  simulationRunsWrappers = simulationRunsWrappers,
              )

      val validTSCInstancesPerProjectionMetric =
          ValidTSCInstancesPerProjectionMetric<Actor, TickData, Segment>()

      TSCEvaluation(tsc = tsc(), projectionIgnoreList = projectionIgnoreList, numThreads = 20)
          .apply {
            registerMetricProviders(
                SegmentDurationPerIdentifierMetric(),
                SegmentCountMetric(),
                AverageVehiclesInEgosBlockMetric(),
                TotalSegmentTimeLengthMetric(),
                validTSCInstancesPerProjectionMetric,
                InvalidTSCInstancesPerProjectionMetric(),
                MissedTSCInstancesPerProjectionMetric(),
                MissingPredicateCombinationsPerProjectionMetric(
                    validTSCInstancesPerProjectionMetric),
                FailedMonitorsMetric(validTSCInstancesPerProjectionMetric),
            )

            prepare()
            segments.forEach { presentSegment(it) }
            close()
          }
    }
    println("Evaluation took $time.")
  }

  /**
   * Checks if the experiments data is available. Otherwise, it is downloaded and extracted to the
   * correct folder.
   */
  private fun downloadAndUnzipExperimentsData() {
    if (File("stars-reproduction-source").exists()) {
      println("The 'stars-reproduction-source' already exists")
      return
    }

    if (!File("stars-reproduction-source.zip").exists()) {
      println("Start with downloading the experiments data. This may take a while.")
      URL("https://zenodo.org/record/8131947/files/stars-reproduction-source.zip?download=1")
          .openStream()
          .use { Files.copy(it, Paths.get("stars-reproduction-source.zip")) }
    }

    check(File("stars-reproduction-source.zip").exists()) {
      "After downloading the file 'stars-reproduction-source.zip' does not exist."
    }

    println("Extracting experiments data from zip file.")
    extractZipFile(zipFile = File("stars-reproduction-source.zip"), outputDir = File("."))

    check(File("stars-reproduction-source").exists()) { "Error unzipping simulation data." }
    check(File("./stars-reproduction-source").totalSpace > 0) {
      "There was an error while downloading/extracting the simulation data. The test zip file is missing."
    }
  }

  private fun getSimulationRuns(): List<CarlaSimulationRunsWrapper> =
      File(simulationRunFolder).let { file ->
        file
            .walk()
            .filter {
              it.isDirectory && it != file && staticFilter.toRegex().containsMatchIn(it.name)
            }
            .toList()
            .mapNotNull { mapFolder ->
              var staticFile: Path? = null
              val dynamicFiles = mutableListOf<Path>()
              mapFolder.walk().forEach { mapFile ->
                if (mapFile.nameWithoutExtension.contains("static_data") &&
                    staticFilter.toRegex().containsMatchIn(mapFile.name)) {
                  staticFile = mapFile.toPath()
                }
                if (mapFile.nameWithoutExtension.contains("dynamic_data") &&
                    dynamicFilter.toRegex().containsMatchIn(mapFile.name)) {
                  dynamicFiles.add(mapFile.toPath())
                }
              }
              if (dynamicFiles.isEmpty()) {
                return@mapNotNull null
              }

              dynamicFiles.sortBy {
                "_seed([0-9]{1,4})".toRegex().find(it.fileName.name)?.groups?.get(1)?.value?.toInt()
                    ?: 0
              }
              return@mapNotNull CarlaSimulationRunsWrapper(staticFile!!, dynamicFiles)
            }
      }

  /**
   * Extract a zip file into any directory
   *
   * @param zipFile src zip file
   * @param outputDir directory to extract into. There will be new folder with the zip's name inside
   *   [outputDir] directory.
   * @return the extracted directory i.e.
   */
  private fun extractZipFile(zipFile: File, outputDir: File): File? =
      try {
        ZipFile(zipFile).use { zip ->
          zip.entries().asSequence().forEach { entry ->
            zip.getInputStream(entry).use { input ->
              if (entry.isDirectory) File(outputDir, entry.name).also { it.mkdirs() }
              else
                  File(outputDir, entry.name)
                      .also { it.parentFile.mkdirs() }
                      .outputStream()
                      .use { output -> input.copyTo(output) }
            }
          }
        }
        outputDir
      } catch (e: Exception) {
        e.printStackTrace()
        null
      }
}
