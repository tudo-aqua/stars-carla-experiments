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
import tools.aqua.stars.carla.experiments.emptyBlock
import tools.aqua.stars.carla.experiments.emptyLane
import tools.aqua.stars.carla.experiments.emptyRoad
import tools.aqua.stars.carla.experiments.emptyTickData
import tools.aqua.stars.carla.experiments.emptyVehicle
import tools.aqua.stars.carla.experiments.isInJunction
import tools.aqua.stars.core.evaluation.PredicateContext
import tools.aqua.stars.data.av.dataclasses.Block
import tools.aqua.stars.data.av.dataclasses.Lane
import tools.aqua.stars.data.av.dataclasses.Road
import tools.aqua.stars.data.av.dataclasses.Segment
import tools.aqua.stars.data.av.dataclasses.TickData
import tools.aqua.stars.data.av.dataclasses.TickDataUnitSeconds

class IsInJunctionTest {

  private val singeLaneRoad: Road = emptyRoad(id = 0, isJunction = false)
  private val singleLane: Lane = emptyLane(laneId = 1, road = singeLaneRoad, laneLength = 50.0)
  private val junctionRoad: Road = emptyRoad(id = 1, isJunction = true)
  private val junctionLane: Lane = emptyLane(laneId = 1, road = junctionRoad, laneLength = 50.0)
  private val block: Block = emptyBlock()

  private val vehicleId: Int = 0

  @BeforeTest
  fun setup() {
    singeLaneRoad.lanes = listOf(singleLane)
    junctionRoad.lanes = listOf(junctionLane)
    block.roads = listOf(singeLaneRoad, junctionRoad)
  }

  @Test
  fun testIsInJunctionAllTheTime() {
    val tickDataList = mutableListOf<TickData>()
    for (i in 0..80) {
      val tickData =
          emptyTickData(currentTick = TickDataUnitSeconds(i.toDouble()), blocks = listOf(block))
      val vehicle =
          emptyVehicle(
              id = vehicleId,
              lane = junctionLane,
              positionOnLane = i.toDouble(),
              egoVehicle = true,
              tickData = tickData)
      tickData.entities = listOf(vehicle)
      tickDataList.add(tickData)
    }

    val segment = Segment(tickDataList, segmentSource = "")
    val ctx = PredicateContext(segment)
    assertTrue(isInJunction.holds(ctx, TickDataUnitSeconds(0.0), vehicleId))
  }

  @Test
  fun testIsInJunction80Percent() {
    val tickDataList = mutableListOf<TickData>()
    for (i in 1..80) {
      val tickData =
          emptyTickData(currentTick = TickDataUnitSeconds(i.toDouble()), blocks = listOf(block))
      val vehicle =
          emptyVehicle(
              id = vehicleId,
              lane = junctionLane,
              positionOnLane = i.toDouble(),
              egoVehicle = true,
              tickData = tickData)
      tickData.entities = listOf(vehicle)
      tickDataList.add(tickData)
    }
    for (i in 81..100) {
      val tickData =
          emptyTickData(currentTick = TickDataUnitSeconds(i.toDouble()), blocks = listOf(block))
      val vehicle =
          emptyVehicle(
              id = vehicleId,
              lane = singleLane,
              positionOnLane = i.toDouble(),
              egoVehicle = true,
              tickData = tickData)
      tickData.entities = listOf(vehicle)
      tickDataList.add(tickData)
    }

    val segment = Segment(tickDataList, segmentSource = "")
    val ctx = PredicateContext(segment)

    // The vehicle is in the junction for exactly 80/100 ticks
    assertTrue(isInJunction.holds(ctx, TickDataUnitSeconds(1.0), vehicleId))

    // The vehicle is in the junction for exactly 79/100 ticks
    assertFalse(isInJunction.holds(ctx, TickDataUnitSeconds(2.0), vehicleId))
  }

  @Test
  fun testIsInJunctionForMultiLaneRoad() {
    val tickDataList = mutableListOf<TickData>()
    for (i in 0..100) {
      val tickData =
          emptyTickData(currentTick = TickDataUnitSeconds(i.toDouble()), blocks = listOf(block))
      val vehicle =
          emptyVehicle(
              id = vehicleId,
              lane = singleLane,
              positionOnLane = i.toDouble(),
              egoVehicle = true,
              tickData = tickData)
      tickData.entities = listOf(vehicle)
      tickDataList.add(tickData)
    }

    val segment = Segment(tickDataList, segmentSource = "")
    val ctx = PredicateContext(segment)
    assertFalse(isInJunction.holds(ctx, TickDataUnitSeconds(0.0), vehicleId))
  }
}
