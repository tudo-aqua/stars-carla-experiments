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

package tools.aqua.stars.carla.exporter

import java.io.File
import java.io.FileOutputStream
import kotlin.io.path.name
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToStream
import tools.aqua.stars.carla.experiments.*
import tools.aqua.stars.data.av.dataclasses.Block
import tools.aqua.stars.importer.carla.CarlaSimulationRunsWrapper
import tools.aqua.stars.importer.carla.loadBlocks
import tools.aqua.stars.importer.carla.loadSegments

/** The output directory of the exported files. */
private const val OUTPUT_DIR = "./stars-carla-export/"

/** The [Json] instance used for serialization with domain specific configuration. */
val json = Json { encodeDefaults = true }

/**
 * Exports calculated [Segment]s to the import format used by the STARS-Visualizer tool. Each
 * simulation run will be handled individually and stored in separate files.
 */
fun main() {
  println("Export Experiments Data")

  println("Create Export Directory")
  File(OUTPUT_DIR).mkdirs()

  downloadAndUnzipExperimentsData()
  val simulationRunsWrappers = getSimulationRuns()
  simulationRunsWrappers.forEach { exportSimulationRun(it) }
}

/**
 * Exports a single simulation run.
 *
 * @param wrapper The [CarlaSimulationRunsWrapper] containing all references to the simulation run
 *   data to export.
 */
private fun exportSimulationRun(wrapper: CarlaSimulationRunsWrapper) {
  val simulationRunName = wrapper.mapDataFile.name.split("__").last().split(".").first()

  println("Exporting Simulation Run ${wrapper.mapDataFile}")
  val segments =
      loadSegments(
          wrapper.mapDataFile,
          wrapper.dynamicDataFiles,
          USE_EVERY_VEHICLE_AS_EGO,
          MIN_SEGMENT_TICK_COUNT,
          SORT_BY_SEED)
  println("Loaded ${segments.count()} segments.")

  val blocks = loadBlocks(wrapper.mapDataFile).toList()
  exportStaticData(blocks, simulationRunName)
  // TODO: Export dynamic data
}

/**
 * Exports static data to directory specified in [OUTPUT_DIR].
 *
 * @param blocks experiment data as [List] of [Block]s.
 * @param simulationRunName name of the simulation run. Will be used for naming of the export.
 */
@OptIn(ExperimentalSerializationApi::class)
private fun exportStaticData(blocks: List<Block>, simulationRunName: String) {
  println("Static Data: Parse Blocks")
  val staticData =
      StaticData(
          lines =
              blocks
                  .flatMap { it.roads }
                  .flatMap { it.lanes }
                  .map { lane ->
                    Line(
                        width = lane.laneWidth.toFloat(),
                        coordinates =
                            lane.laneMidpoints.map { midpoint ->
                              Location(
                                  midpoint.location.x, midpoint.location.y, midpoint.location.z)
                            })
                  })
  println("Static Data: Export Lines")

  val staticDataFilePath = "$OUTPUT_DIR${simulationRunName}_static.json"
  FileOutputStream(staticDataFilePath).use { fos ->
    json.encodeToStream(StaticData.serializer(), staticData, fos)
  }

  println("Static Data: Export to file $staticDataFilePath finished successfully!")
}
