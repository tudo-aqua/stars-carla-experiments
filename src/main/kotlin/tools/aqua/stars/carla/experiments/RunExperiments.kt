/*
 * Copyright 2023 The STARS Carla Experiments Authors
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
import tools.aqua.stars.data.av.dataclasses.Actor
import tools.aqua.stars.data.av.dataclasses.Segment
import tools.aqua.stars.data.av.dataclasses.TickData
import tools.aqua.stars.data.av.metrics.AverageVehiclesInEgosBlockMetric
import tools.aqua.stars.importer.carla.CarlaDataLoader
import tools.aqua.stars.importer.carla.CarlaSimulationRunsWrapper

fun main() {
  val time = measureTime {
    downloadAndUnzipExperimentsData()

    val simulationRunsWrappers = getSimulationRuns()
    val segments =
        CarlaDataLoader(
                useEveryVehicleAsEgo = USE_EVERY_VEHICLE_AS_EGO,
                minSegmentTickCount = MIN_SEGMENT_TICK_COUNT,
                orderFilesBySeed = SORT_BY_SEED)
            .loadSegments(
                simulationRunsWrappers = simulationRunsWrappers,
            )

    val validTSCInstancesPerProjectionMetric =
        ValidTSCInstancesPerProjectionMetric<Actor, TickData, Segment>()

    TSCEvaluation(tsc = tsc(), projectionIgnoreList = PROJECTION_IGNORE_LIST, numThreads = 20)
        .apply {
          registerMetricProviders(
              SegmentDurationPerIdentifierMetric(),
              SegmentCountMetric(),
              AverageVehiclesInEgosBlockMetric(),
              TotalSegmentTimeLengthMetric(),
              validTSCInstancesPerProjectionMetric,
              InvalidTSCInstancesPerProjectionMetric(),
              MissedTSCInstancesPerProjectionMetric(),
              MissingPredicateCombinationsPerProjectionMetric(validTSCInstancesPerProjectionMetric),
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
fun downloadAndUnzipExperimentsData() {
  if (!File("stars-reproduction-source").exists()) {
    println("The experiments data is missing.")
    if (!File("stars-reproduction-source.zip").exists()) {
      println("The experiments data zip file is missing.")
      if (DOWNLOAD_EXPERIMENTS_DATA) {
        println("Start with downloading the experiments data. This may take a while.")
        downloadExperimentsData()
        println("Finished downloading.")
      } else {
        simulationDataMissing()
      }
    }
    if (!File("stars-reproduction-source.zip").exists()) {
      simulationDataMissing()
    }
    println("Extract experiments data from zip file.")
    extractZipFile(zipFile = File("stars-reproduction-source.zip"), extractTo = File("."), true)
  }
  if (!File("stars-reproduction-source").exists()) {
    simulationDataMissing()
  }
}

/**
 * Throws an exception when the experiments data is not available and when the
 * [DOWNLOAD_EXPERIMENTS_DATA] is set to false.
 */
fun simulationDataMissing() {
  error(
      "The experiments data is not available. Either download it: https://zenodo.org/record/8131947 or set " +
          "DOWNLOAD_EXPERIMENTS_DATA to 'true'")
}

fun getSimulationRuns(): List<CarlaSimulationRunsWrapper> =
    File(SIMULATION_RUN_FOLDER)
        .walk()
        .filter {
          it.isDirectory &&
              it != File(SIMULATION_RUN_FOLDER) &&
              STATIC_FILTER_REGEX.toRegex().containsMatchIn(it.name)
        }
        .toList()
        .mapNotNull { mapFolder ->
          var staticFile: Path? = null
          val dynamicFiles = mutableListOf<Path>()
          mapFolder.walk().forEach { mapFile ->
            if (mapFile.nameWithoutExtension.contains("static_data") &&
                STATIC_FILTER_REGEX.toRegex().containsMatchIn(mapFile.name)) {
              staticFile = mapFile.toPath()
            }
            if (mapFile.nameWithoutExtension.contains("dynamic_data") &&
                FILTER_REGEX.toRegex().containsMatchIn(mapFile.name)) {
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

/** Download the experiments data and saves it in the root directory of the project. */
fun downloadExperimentsData() {
  URL("https://zenodo.org/record/8131947/files/stars-reproduction-source.zip?download=1")
      .openStream()
      .use { Files.copy(it, Paths.get("stars-reproduction-source.zip")) }
}

/**
 * Extract a zip file into any directory
 *
 * @param zipFile src zip file
 * @param extractTo directory to extract into. There will be new folder with the zip's name inside
 *   [extractTo] directory.
 * @param extractHere no extra folder will be created and will be extracted directly inside
 *   [extractTo] folder.
 * @return the extracted directory i.e, [extractTo] folder if [extractHere] is `true` and
 *   [extractTo]\zipFile\ folder otherwise.
 */
private fun extractZipFile(
    zipFile: File,
    extractTo: File,
    extractHere: Boolean = false,
): File? {
  return try {
    val outputDir = if (extractHere) extractTo else File(extractTo, zipFile.nameWithoutExtension)

    ZipFile(zipFile).use { zip ->
      zip.entries().asSequence().forEach { entry ->
        zip.getInputStream(entry).use { input ->
          if (entry.isDirectory) {
            val d = File(outputDir, entry.name)
            if (!d.exists()) d.mkdirs()
          } else {
            val f = File(outputDir, entry.name)
            if (f.parentFile?.exists() != true) f.parentFile?.mkdirs()

            f.outputStream().use { output -> input.copyTo(output) }
          }
        }
      }
    }
    extractTo
  } catch (e: Exception) {
    e.printStackTrace()
    null
  }
}
