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

package tools.aqua.stars.carla.experiments.roadType

import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import tools.aqua.stars.carla.experiments.isInJunction
import tools.aqua.stars.core.evaluation.PredicateContext
import tools.aqua.stars.data.av.dataclasses.Block
import tools.aqua.stars.data.av.dataclasses.Lane
import tools.aqua.stars.data.av.dataclasses.Road
import tools.aqua.stars.data.av.dataclasses.Segment
import tools.aqua.stars.data.av.dataclasses.TickData
import tools.aqua.stars.data.av.dataclasses.TickDataUnitSeconds
import tools.aqua.stars.data.av.dataclasses.Vehicle

class IsInJunctionTest {

  private lateinit var singleLaneRoad: Road
  private lateinit var singleLane: Lane

  private lateinit var junctionRoad: Road
  private lateinit var junctionLane: Lane

  private lateinit var block: Block

  private val vehicleId: Int = 0

  @BeforeTest
  fun setup() {
    singleLane = Lane(laneId = 1, laneLength = 50.0)
    singleLaneRoad =
        Road(id = 0, isJunction = false, lanes = listOf(singleLane)).apply {
          singleLane.road = this
        }

    junctionLane = Lane(laneId = 1, laneLength = 50.0)
    junctionRoad =
        Road(id = 1, isJunction = true, lanes = listOf(junctionLane)).apply {
          junctionLane.road = this
        }

    block = Block(roads = listOf(singleLaneRoad, junctionRoad))
  }

  @Test
  fun testIsInJunctionAllTheTime() {
    val tds = mutableListOf<TickData>()

    for (i in 0..80) {

      val veh =
          Vehicle(id = vehicleId, lane = junctionLane, positionOnLane = i.toDouble(), isEgo = true)
      // 2) create the TickData with that vehicle in its entities
      val td =
          TickData(
              currentTick = TickDataUnitSeconds(i.toDouble()),
              blocks = listOf(block),
              entities = listOf(veh))
      // 3) wire back‐reference
      veh.tickData = td

      tds += td
    }

    val ctx = PredicateContext(Segment(tds, segmentSource = ""))
    assertTrue { isInJunction.holds(ctx, TickDataUnitSeconds(0.0), vehicleId) }
  }

  @Test
  fun testIsInJunction80Percent() {
    val tds = mutableListOf<TickData>()

    // ticks 1–80 in junction
    for (i in 1..80) {
      val veh =
          Vehicle(id = vehicleId, lane = junctionLane, positionOnLane = i.toDouble(), isEgo = true)
      val td =
          TickData(
              currentTick = TickDataUnitSeconds(i.toDouble()),
              blocks = listOf(block),
              entities = listOf(veh))
      veh.tickData = td
      tds += td
    }

    // ticks 81–100 off junction
    for (i in 81..100) {
      val veh =
          Vehicle(id = vehicleId, lane = singleLane, positionOnLane = i.toDouble(), isEgo = true)
      val td =
          TickData(
              currentTick = TickDataUnitSeconds(i.toDouble()),
              blocks = listOf(block),
              entities = listOf(veh))
      veh.tickData = td
      tds += td
    }

    val segment = Segment(tds, segmentSource = "")
    val ctx = PredicateContext(segment)

    // exactly 80/100 in junction → holds at t=1
    assertTrue { isInJunction.holds(ctx, TickDataUnitSeconds(1.0), vehicleId) }
    // only 79/100 from t=2 → no longer holds
    assertFalse { isInJunction.holds(ctx, TickDataUnitSeconds(2.0), vehicleId) }
  }

  @Test
  fun testIsInJunctionForMultiLaneRoad() {
    val tds = mutableListOf<TickData>()

    // never in junction
    for (i in 0..100) {
      val veh =
          Vehicle(id = vehicleId, lane = singleLane, positionOnLane = i.toDouble(), isEgo = true)
      val td =
          TickData(
              currentTick = TickDataUnitSeconds(i.toDouble()),
              blocks = listOf(block),
              entities = listOf(veh))
      veh.tickData = td
      tds += td
    }

    val ctx = PredicateContext(Segment(tds, segmentSource = ""))
    assertFalse { isInJunction.holds(ctx, TickDataUnitSeconds(0.0), vehicleId) }
  }
}
