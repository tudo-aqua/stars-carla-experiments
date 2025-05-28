/*
 * Copyright 2024-2025 The STARS Carla Experiments Authors
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
import tools.aqua.stars.carla.experiments.noon
import tools.aqua.stars.core.evaluation.PredicateContext
import tools.aqua.stars.data.av.dataclasses.Daytime
import tools.aqua.stars.data.av.dataclasses.Lane
import tools.aqua.stars.data.av.dataclasses.Segment
import tools.aqua.stars.data.av.dataclasses.TickData
import tools.aqua.stars.data.av.dataclasses.TickDataUnitSeconds
import tools.aqua.stars.data.av.dataclasses.Vehicle

class DaytimeNoonTest {

  private val currentDaytime = Daytime.Noon
  private val otherDaytime = Daytime.entries.first { it != currentDaytime }

  @Test
  fun testOtherDayTimes() {
    Daytime.entries.forEach { daytime ->
      if (daytime == currentDaytime) {
        return@forEach
      }
      val ego = Vehicle(isEgo = true, lane = Lane())
      val tick = TickData(daytime = daytime, entities = listOf(ego))
      val segment = Segment(listOf(tick), segmentSource = "")
      val context = PredicateContext(segment)

      assertFalse { context.noon() }
    }
  }

  @Test
  fun testDaytime() {
    val ego = Vehicle(isEgo = true, lane = Lane())
    val tick = TickData(daytime = currentDaytime, entities = listOf(ego))
    val segment = Segment(listOf(tick), segmentSource = "")
    val context = PredicateContext(segment)

    assertTrue { context.noon() }
  }

  @Test
  fun testDaytimeOver60Percent() {
    val ticks = mutableListOf<TickData>()
    for (i in 1..6) {
      val ego = Vehicle(isEgo = true, lane = Lane())
      val tick = TickData(daytime = currentDaytime, entities = listOf(ego))
      ticks.add(tick)
    }
    for (i in 1..4) {
      val ego = Vehicle(isEgo = true, lane = Lane())
      val tick = TickData(daytime = otherDaytime, entities = listOf(ego))
      ticks.add(tick)
    }
    val segment = Segment(ticks, segmentSource = "")
    val context = PredicateContext(segment)

    assertTrue { context.noon() }
  }

  @Test
  fun testDaytimeUnder60Percent() {
    val ticks = mutableListOf<TickData>()
    for (i in 1..5) {
      val ego = Vehicle(isEgo = true, lane = Lane())
      val tick =
          TickData(
              currentTick = TickDataUnitSeconds(i.toDouble()),
              daytime = currentDaytime,
              entities = listOf(ego))
      ticks.add(tick)
    }
    for (i in 6..10) {
      val ego = Vehicle(isEgo = true, lane = Lane())
      val tick =
          TickData(
              currentTick = TickDataUnitSeconds(i.toDouble()),
              daytime = otherDaytime,
              entities = listOf(ego))
      ticks.add(tick)
    }
    val segment = Segment(ticks, segmentSource = "")
    val context = PredicateContext(segment)

    assertFalse { context.noon() }
  }
}
