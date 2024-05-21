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
import tools.aqua.stars.carla.experiments.*
import tools.aqua.stars.importer.carla.CarlaSimulationRunsWrapper
import tools.aqua.stars.importer.carla.loadSegments

/** The output directory of the exported files. */
private const val OUTPUT_DIR = "./stars-carla-export/"

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
  println("Exporting Simulation Run ${wrapper.mapDataFile}")
  val segments =
      loadSegments(
          wrapper.mapDataFile,
          wrapper.dynamicDataFiles,
          USE_EVERY_VEHICLE_AS_EGO,
          MIN_SEGMENT_TICK_COUNT,
          SORT_BY_SEED)
  println("Loaded ${segments.count()} segments.")

  // TODO: Export static data
  // TODO: Export dynamic data
}
