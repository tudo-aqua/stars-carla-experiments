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
import tools.aqua.stars.core.validation.labelFile
import tools.aqua.stars.data.av.dataclasses.Actor
import tools.aqua.stars.data.av.dataclasses.Segment
import tools.aqua.stars.data.av.dataclasses.TickData
import tools.aqua.stars.data.av.dataclasses.TickDataDifferenceSeconds
import tools.aqua.stars.data.av.dataclasses.TickDataUnitSeconds

val manualTests =
    labelFile<Actor, TickData, Segment, TickDataUnitSeconds, TickDataDifferenceSeconds>(
        "data/session1.csv") {
          predicate(isOnSingleLane) {
            interval(0, 5)
            interval(20, 27)
          }
          predicate(isOnMultiLane) {
            interval(13, 15)
            interval(20, 27)
          }
          predicate(hasMidTrafficDensity) { interval(2, 8) }
          predicate(soBetween) { interval(10, 12) }
          predicate(isAtEndOfRoad) { interval(10, 12) }
        }
