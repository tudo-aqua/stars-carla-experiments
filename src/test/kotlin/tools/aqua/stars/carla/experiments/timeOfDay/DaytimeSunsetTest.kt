/*
 * Copyright 2024 The STARS Carla Experiments Authors
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

package tools.aqua.stars.carla.experiments.timeOfDay

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import tools.aqua.stars.carla.experiments.emptyTickData
import tools.aqua.stars.carla.experiments.emptyVehicle
import tools.aqua.stars.carla.experiments.sunset
import tools.aqua.stars.core.evaluation.PredicateContext
import tools.aqua.stars.data.av.dataclasses.Daytime
import tools.aqua.stars.data.av.dataclasses.Segment
import tools.aqua.stars.data.av.dataclasses.TickData
import tools.aqua.stars.data.av.dataclasses.TickDataUnitSeconds

class DaytimeSunsetTest {

  private val currentDaytime = Daytime.Sunset
  private val otherDaytime = Daytime.entries.first { it != currentDaytime }

  @Test
  fun testOtherDayTimes() {
    Daytime.entries.forEach { daytime ->
      if (daytime == currentDaytime) {
        return@forEach
      }
      val ego = emptyVehicle(egoVehicle = true)
      val tick = emptyTickData(daytime = daytime, actors = listOf(ego))
      val segment = Segment(listOf(tick), segmentSource = "")
      val context = PredicateContext(segment)

      assertFalse { context.sunset() }
    }
  }

  @Test
  fun testDaytime() {
    val ego = emptyVehicle(egoVehicle = true)
    val tick = emptyTickData(daytime = currentDaytime, actors = listOf(ego))
    val segment = Segment(listOf(tick), segmentSource = "")
    val context = PredicateContext(segment)

    assertTrue { context.sunset() }
  }

  @Test
  fun testDaytimeOver60Percent() {
    val ticks = mutableListOf<TickData>()
    for (i in 1..6) {
      val ego = emptyVehicle(egoVehicle = true)
      val tick = emptyTickData(daytime = currentDaytime, actors = listOf(ego))
      ticks.add(tick)
    }
    for (i in 1..4) {
      val ego = emptyVehicle(egoVehicle = true)
      val tick = emptyTickData(daytime = otherDaytime, actors = listOf(ego))
      ticks.add(tick)
    }
    val segment = Segment(ticks, segmentSource = "")
    val context = PredicateContext(segment)

    assertTrue { context.sunset() }
  }

  @Test
  fun testDaytimeUnder60Percent() {
    val ticks = mutableListOf<TickData>()
    for (i in 1..5) {
      val ego = emptyVehicle(egoVehicle = true)
      val tick =
          emptyTickData(
              currentTick = TickDataUnitSeconds(i.toDouble()),
              daytime = currentDaytime,
              actors = listOf(ego))
      ticks.add(tick)
    }
    for (i in 6..10) {
      val ego = emptyVehicle(egoVehicle = true)
      val tick =
          emptyTickData(
              currentTick = TickDataUnitSeconds(i.toDouble()),
              daytime = otherDaytime,
              actors = listOf(ego))
      ticks.add(tick)
    }
    val segment = Segment(ticks, segmentSource = "")
    val context = PredicateContext(segment)

    assertFalse { context.sunset() }
  }
}
