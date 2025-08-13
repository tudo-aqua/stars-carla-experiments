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
import kotlin.io.path.Path
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
import tools.aqua.stars.importer.carla.CarlaSimulationRunsWrapper
import tools.aqua.stars.importer.carla.loadTicks

/**
 * The [ExperimentConfiguration] configures all [CliktCommand]s that can be used for this experiment
 * and passes them to the STARS-framework.
 */
class ExperimentConfiguration : CliktCommand() {

  // region command line options
  private val simulationRunFolder: String by
      option("--input", help = "Directory of the input files").default("data")

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
    val simulationRunsWrappers =
        listOf(
            CarlaSimulationRunsWrapper(
                mapDataFile = Path("$simulationRunFolder/static_data.json"),
                dynamicDataFiles = listOf(Path("$simulationRunFolder/dynamic_data.json")),
            ))

    println("Loading ticks...")
    val ticks =
        loadTicks(
            orderFilesBySeed = sortBySeed,
            simulationRunsWrappers = simulationRunsWrappers,
            bufferSize = 100)

    val validTSCInstancesPerProjectionMetric =
        ValidTSCInstancesPerTSCMetric<
            Actor, TickData, TickDataUnitSeconds, TickDataDifferenceSeconds>()

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
                  //                  TotalSegmentTickDifferencePerIdentifierMetric(),
                  //                  SegmentCountMetric(),
                  //                  AverageVehiclesInEgosBlockMetric(),
                  //                  TotalSegmentTickDifferenceMetric(),
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

//  /**
//   * Extract a zip file into any directory.
//   *
//   * @param zipFile src zip file
//   * @param outputDir directory to extract into. There will be new folder with the zip's name inside
//   *   [outputDir] directory.
//   * @return the extracted directory i.e.
//   */
//  private fun extractZipFile(zipFile: File, outputDir: File): File? {
//    ZipFile(zipFile).use { zip ->
//      zip.entries().asSequence().forEach { entry ->
//        zip.getInputStream(entry).use { input ->
//          if (entry.isDirectory) File(outputDir, entry.name).also { it.mkdirs() }
//          else
//              File(outputDir, entry.name)
//                  .also { it.parentFile.mkdirs() }
//                  .outputStream()
//                  .use { output -> input.copyTo(output) }
//        }
//      }
//    }
//    return outputDir
//  }
}
