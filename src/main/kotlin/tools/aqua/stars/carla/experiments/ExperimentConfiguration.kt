/*
 * Copyright 2023-2025 The STARS Carla Experiments Authors
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
import kotlin.system.exitProcess
import tools.aqua.stars.carla.experiments.Experiment.EXIT_CODE_EQUAL_RESULTS
import tools.aqua.stars.carla.experiments.Experiment.EXIT_CODE_NORMAL
import tools.aqua.stars.carla.experiments.Experiment.EXIT_CODE_NO_RESULTS
import tools.aqua.stars.carla.experiments.Experiment.EXIT_CODE_UNEQUAL_RESULTS
import tools.aqua.stars.core.evaluation.TSCEvaluation
import tools.aqua.stars.core.metric.metrics.evaluation.*
import tools.aqua.stars.core.metric.metrics.postEvaluation.*
import tools.aqua.stars.core.metric.utils.ApplicationConstantsHolder
import tools.aqua.stars.core.metric.utils.ApplicationConstantsHolder.baselineDirectory
import tools.aqua.stars.data.av.dataclasses.*
import tools.aqua.stars.data.av.metrics.AverageVehiclesInEgosBlockMetric
import tools.aqua.stars.importer.carla.CarlaSimulationRunsWrapper
import tools.aqua.stars.importer.carla.loadSegments

/**
 * The [ExperimentConfiguration] configures all [CliktCommand]s that can be used for this experiment
 * and passes them to the STARS-framework.
 */
class ExperimentConfiguration : CliktCommand() {

  // region command line options
  private val simulationRunFolder: String by
      option("--input", help = "Directory of the input files")
          .default("./stars-reproduction-source/stars-experiments-data/simulation_runs")

  private val allEgo: Boolean by
      option("--allEgo", help = "Whether to treat all vehicles as ego").flag(default = false)

  private val firstEgo: Boolean by
      option("--firstEgo", help = "Whether to treat the first vehicle as ego").flag(default = false)

  private val minSegmentTickCount: Int by
      option("--minSegmentTicks", help = "Minimum ticks per segment").int().default(11)

  private val sortBySeed: Boolean by
      option("--sorted", help = "Whether to sort data by seed").flag(default = true)

  private val dynamicFilter: String by
      option("--dynamicFilter", help = "Regex to filter on dynamic data").default(".*")

  private val staticFilter: String by
      option("--staticFilter", help = "Regex to filter on static data").default(".*")

  private val projectionIgnoreList: List<String> by
      option(
              "--ignore",
              help =
                  "A list of TSC projections that should be ignored (given as a String, separated by ',')")
          .split(",")
          .default(listOf())

  private val writePlots: Boolean by
      option("--writePlots", help = "Whether to write plots").flag(default = false)

  private val writePlotDataCSV: Boolean by
      option("--writePlotData", help = "Whether to write plot data to csv").flag(default = false)

  private val writeSerializedResults: Boolean by
      option("--saveResults", help = "Whether to save serialized results").flag(default = false)

  private val compareToBaselineResults: Boolean by
      option(
              "--compareToBaselineResults",
              help = "Whether to compare the results to the baseline results")
          .flag(default = false)

  private val compareToPreviousRun: Boolean by
      option("--compare", help = "Whether to compare the results to the previous run")
          .flag(default = false)

  private val showMemoryConsumption: Boolean by
      option("--showMemoryConsumption", help = "Whether to show memory consumption")
          .flag(default = false)

  private val reproduction: String? by option("--reproduction", help = "Path to baseline results")
  // endregion

  override fun run() {
    ApplicationConstantsHolder.executionCommand =
        """
        --input=$simulationRunFolder
        --allEgo=$allEgo
        --firstEgo=$firstEgo
        --minSegmentTicks=$minSegmentTickCount
        --sorted=$sortBySeed
        --dynamicFilter=$dynamicFilter
        --staticFilter=$staticFilter
        --ignore=$projectionIgnoreList
        --writePlots=$writePlots
        --writePlotData=$writePlotDataCSV
        --saveResults=$writeSerializedResults
        --compareToBaselineResults=$compareToBaselineResults
        --compare=$compareToPreviousRun
        --reproduction=$reproduction
        --showMemoryConsumption=$showMemoryConsumption
    """
            .trimIndent()
    println("Executing with the following settings:")
    println(ApplicationConstantsHolder.executionCommand)

    reproduction?.let { baselineDirectory = it }

    downloadAndUnzipExperimentsData()

    if (showMemoryConsumption) {
      Thread {
            while (true) {
              val runtime = Runtime.getRuntime()
              val usedMemory = runtime.totalMemory() - runtime.freeMemory()
              val freeMemory = runtime.freeMemory()
              println(
                  "Used Memory: ${usedMemory / (1024 * 1024)} MB          Free Memory: ${freeMemory / (1024 * 1024)} MB")
              Thread.sleep(5000)
            }
          }
          .apply {
            isDaemon = true
            start()
          }
    }

    val tsc = tsc()

    println("Projections:")
    tsc.buildProjections(projectionIgnoreList.map { it.trim() }).forEach {
      println("TSC for Projection $it:")
      println(tsc)
      println("All possible instances:")
      println(it.possibleTSCInstances.size)
      println()
    }
    println("-----------------")

    println("Loading simulation runs...")
    val simulationRunsWrappers = getSimulationRuns()

    println("Loading segments...")
    val segments =
        loadSegments(
            useEveryVehicleAsEgo = allEgo,
            useFirstVehicleAsEgo = firstEgo,
            minSegmentTickCount = minSegmentTickCount,
            orderFilesBySeed = sortBySeed,
            simulationRunsWrappers = simulationRunsWrappers,
        )

    val validTSCInstancesPerProjectionMetric =
        ValidTSCInstancesPerTSCMetric<
            Actor, TickData, Segment, TickDataUnitSeconds, TickDataDifferenceSeconds>()

    println("Creating TSC...")
    val evaluation =
        TSCEvaluation(
                tscList = tsc().buildProjections(projectionIgnoreList = projectionIgnoreList),
                writePlots = writePlots,
                writePlotDataCSV = writePlotDataCSV,
                writeSerializedResults = writeSerializedResults,
                compareToBaselineResults = compareToBaselineResults || reproduction != null,
                compareToPreviousRun = compareToPreviousRun)
            .apply {
              registerMetricProviders(
                  TotalSegmentTickDifferencePerIdentifierMetric(),
                  SegmentCountMetric(),
                  AverageVehiclesInEgosBlockMetric(),
                  TotalSegmentTickDifferenceMetric(),
                  validTSCInstancesPerProjectionMetric,
                  InvalidTSCInstancesPerTSCMetric(),
                  MissedTSCInstancesPerTSCMetric(),
                  MissedPredicateCombinationsPerTSCMetric(validTSCInstancesPerProjectionMetric),
                  FailedMonitorsMetric(validTSCInstancesPerProjectionMetric),
              )
              println("Run Evaluation")
              runEvaluation(segments = segments)
            }

    exitProcess(
        status =
            if (reproduction != null) {
              when (evaluation.resultsReproducedFromBaseline) {
                null -> EXIT_CODE_NO_RESULTS
                false -> EXIT_CODE_UNEQUAL_RESULTS
                true -> EXIT_CODE_EQUAL_RESULTS
              }
            } else {
              EXIT_CODE_NORMAL
            })
  }

  /**
   * Checks if the experiments data is available. Otherwise, it is downloaded and extracted to the
   * correct folder.
   */
  private fun downloadAndUnzipExperimentsData() {
    val reproductionSourceFolderName = "stars-reproduction-source"
    val reproductionSourceZipFile = "$reproductionSourceFolderName.zip"

    if (File(reproductionSourceFolderName).exists()) {
      println("The 'stars-reproduction-source' already exists")
      return
    }

    if (!File(reproductionSourceZipFile).exists()) {
      println("Start with downloading the experiments data. This may take a while.")
      URL("https://zenodo.org/record/8131947/files/stars-reproduction-source.zip?download=1")
          .openStream()
          .use { Files.copy(it, Paths.get(reproductionSourceZipFile)) }
    }

    check(File(reproductionSourceZipFile).exists()) {
      "After downloading the file '$reproductionSourceZipFile' does not exist."
    }

    println("Extracting experiments data from zip file.")
    extractZipFile(zipFile = File(reproductionSourceZipFile), outputDir = File("."))

    check(File(reproductionSourceFolderName).exists()) { "Error unzipping simulation data." }
    check(File("./$reproductionSourceFolderName").totalSpace > 0) {
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

              if (staticFile == null || dynamicFiles.isEmpty()) {
                return@mapNotNull null
              }

              dynamicFiles.sortBy {
                "_seed([0-9]{1,4})".toRegex().find(it.fileName.name)?.groups?.get(1)?.value?.toInt()
                    ?: 0
              }
              return@mapNotNull CarlaSimulationRunsWrapper(staticFile, dynamicFiles)
            }
      }

  /**
   * Extract a zip file into any directory.
   *
   * @param zipFile src zip file
   * @param outputDir directory to extract into. There will be new folder with the zip's name inside
   *   [outputDir] directory.
   * @return the extracted directory i.e.
   */
  private fun extractZipFile(zipFile: File, outputDir: File): File? {
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
    return outputDir
  }
}
