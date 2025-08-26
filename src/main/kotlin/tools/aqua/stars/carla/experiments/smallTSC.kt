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

import tools.aqua.stars.core.tsc.TSC
import tools.aqua.stars.core.tsc.builder.*
import tools.aqua.stars.data.av.dataclasses.*

/**
 * Returns the [TSC] with the dataclasses [Actor], [TickData], [TickDataUnitSeconds], and
 * [TickDataDifferenceSeconds] that is used in this experiment.
 */
@Suppress("StringLiteralDuplication")
fun smallTSC() =
    tsc<Actor, TickData, TickDataUnitSeconds, TickDataDifferenceSeconds> {
      all("TSCRoot") {
        exclusive("Weather") {
          leaf("Clear") { condition { weatherClear.holds(it) } }
          leaf("Cloudy") { condition { weatherCloudy.holds(it) } }
          leaf("Wet") { condition { weatherWet.holds(it) } }
          leaf("Wet Cloudy") { condition { weatherWetCloudy.holds(it) } }
          leaf("Soft Rain") { condition { weatherSoftRain.holds(it) } }
          leaf("Mid Rain") { condition { weatherMidRain.holds(it) } }
          leaf("Hard Rain") { condition { weatherHardRain.holds(it) } }
        }

        exclusive("Road Type") {
          all("Junction") { condition { isInJunction.holds(it) } }
          all("Multi-Lane") { condition { isOnMultiLane.holds(it) } }
          all("Single-Lane") { condition { isOnSingleLane.holds(it) } }
        }

        exclusive("Time of Day") {
          leaf("Sunset") { condition { sunset.holds(it) } }
          leaf("Noon") { condition { noon.holds(it) } }
        }
      }
    }
