/*
 * Copyright 2025 The STARS Carla Experiments Authors
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

package tools.aqua.stars.carla.experiments.validation

import tools.aqua.stars.carla.experiments.*
import tools.aqua.stars.core.validation.manuallyLabelledFile
import tools.aqua.stars.data.av.dataclasses.TickDataUnitSeconds
import tools.aqua.stars.importer.carla.loadSegments

val simulationRuns = ExperimentConfiguration.getSimulationRuns("manual_tests")
val segments = loadSegments(simulationRuns).toList()

val manualTests =
    manuallyLabelledFile(segments) {
      predicate(isOnMultiLane) { interval(TickDataUnitSeconds(0.0), TickDataUnitSeconds(7.0)) }
    }
