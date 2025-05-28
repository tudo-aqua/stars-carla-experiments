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

package tools.aqua.stars.carla.experiments.dynamicRelations

import kotlin.test.BeforeTest
import kotlin.test.Test
import tools.aqua.stars.carla.experiments.emptyBlock
import tools.aqua.stars.carla.experiments.emptyLane
import tools.aqua.stars.carla.experiments.emptyPedestrian
import tools.aqua.stars.carla.experiments.emptyRoad
import tools.aqua.stars.carla.experiments.emptyTickData
import tools.aqua.stars.carla.experiments.emptyVehicle
import tools.aqua.stars.carla.experiments.pedestrianCrossed
import tools.aqua.stars.core.evaluation.PredicateContext
import tools.aqua.stars.data.av.dataclasses.Segment
import tools.aqua.stars.data.av.dataclasses.TickData
import tools.aqua.stars.data.av.dataclasses.TickDataUnitSeconds

class PedestrianCrossedTest {

  private val road0 = emptyRoad(id = 0)
  private val road0lane1 = emptyLane(laneId = 1, road = road0, laneLength = 50.0)
  private val road0lane2 = emptyLane(laneId = 2, road = road0, laneLength = 50.0)
  private val road0lane3 = emptyLane(laneId = 3, road = road0, laneLength = 50.0)

  private val road1 = emptyRoad(id = 1)
  private val road1lane1 = emptyLane(laneId = 1, road = road1, laneLength = 50.0)

  private val block = emptyBlock()

  @BeforeTest
  fun setup() {
    road0.lanes = listOf(road0lane1, road0lane2, road0lane3)
    road1.lanes = listOf(road1lane1)

    block.roads = listOf(road0, road1)
  }

  @Test
  fun pedestrianCrossed() {
    val tickDataList = mutableListOf<TickData>()
    for (i in 0..3) {
      val tickData =
          emptyTickData(
              currentTick = TickDataUnitSeconds(i.toDouble()),
              blocks = listOf(block),
              actors = listOf())
      val ego =
          emptyVehicle(
              id = 0,
              egoVehicle = true,
              positionOnLane = road0lane1.laneLength - 3.0,
              lane = road0lane1,
              tickData = tickData)
      val pedestrianLane = if (i == 1) road0lane3 else if (i == 2) road0lane2 else road0lane1
      val pedestrian =
          emptyPedestrian(
              id = 1,
              lane = pedestrianLane,
              positionOnLane = pedestrianLane.laneLength - 1.0,
              tickData = tickData)
      tickData.entities = listOf(ego, pedestrian)
      tickDataList.add(tickData)
    }
    val segment = Segment(tickDataList, segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(pedestrianCrossed.holds(ctx, TickDataUnitSeconds(0.0), 0))
  }

  @Test
  fun pedestrianCrossedTooEarly() {
    val tickDataList = mutableListOf<TickData>()
    for (i in 0..3) {
      val tickData =
          emptyTickData(
              currentTick = TickDataUnitSeconds(i.toDouble()),
              blocks = listOf(block),
              actors = listOf())
      val ego =
          emptyVehicle(
              id = 0,
              egoVehicle = true,
              positionOnLane = road0lane1.laneLength - 20.0,
              lane = road0lane1,
              tickData = tickData)
      val pedestrianLane = if (i == 1) road0lane3 else if (i == 2) road0lane2 else road0lane1
      val pedestrian =
          emptyPedestrian(
              id = 1,
              lane = pedestrianLane,
              positionOnLane = pedestrianLane.laneLength - 1.0,
              tickData = tickData)
      tickData.entities = listOf(ego, pedestrian)
      tickDataList.add(tickData)
    }
    val segment = Segment(tickDataList, segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(!pedestrianCrossed.holds(ctx, TickDataUnitSeconds(0.0), 0))
  }

  @Test
  fun pedestrianCrossedTooLate() {
    val tickDataList = mutableListOf<TickData>()
    for (i in 0..3) {
      val tickData =
          emptyTickData(
              currentTick = TickDataUnitSeconds(i.toDouble()),
              blocks = listOf(block),
              actors = listOf())
      val ego =
          emptyVehicle(
              id = 0,
              egoVehicle = true,
              positionOnLane = road0lane1.laneLength - 20.0,
              lane = road0lane1,
              tickData = tickData)
      val pedestrianLane = if (i == 1) road0lane3 else if (i == 2) road0lane2 else road0lane1
      val pedestrian =
          emptyPedestrian(
              id = 1,
              lane = pedestrianLane,
              positionOnLane = pedestrianLane.laneLength - 30.0,
              tickData = tickData)
      tickData.entities = listOf(ego, pedestrian)
      tickDataList.add(tickData)
    }
    val segment = Segment(tickDataList, segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(!pedestrianCrossed.holds(ctx, TickDataUnitSeconds(0.0), 0))
  }

  @Test
  fun noPedestrianCrossed() {
    val tickDataList = mutableListOf<TickData>()
    for (i in 0..3) {
      val tickData =
          emptyTickData(
              currentTick = TickDataUnitSeconds(i.toDouble()),
              blocks = listOf(block),
              actors = listOf())
      val ego =
          emptyVehicle(
              id = 0,
              egoVehicle = true,
              positionOnLane = road0lane1.laneLength - 20.0,
              lane = road0lane1,
              tickData = tickData)
      tickData.entities = listOf(ego)
      tickDataList.add(tickData)
    }
    val segment = Segment(tickDataList, segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(!pedestrianCrossed.holds(ctx, TickDataUnitSeconds(0.0), 0))
  }

  @Test
  fun vehicleCrossed() {
    val tickDataList = mutableListOf<TickData>()
    for (i in 0..3) {
      val tickData =
          emptyTickData(
              currentTick = TickDataUnitSeconds(i.toDouble()),
              blocks = listOf(block),
              actors = listOf())
      val ego =
          emptyVehicle(
              id = 0,
              egoVehicle = true,
              positionOnLane = road0lane1.laneLength - 3.0,
              lane = road0lane1,
              tickData = tickData)
      val otherVehicleLane = if (i == 1) road0lane3 else if (i == 2) road0lane2 else road0lane1
      val otherVehicle =
          emptyVehicle(
              id = 1,
              lane = otherVehicleLane,
              positionOnLane = otherVehicleLane.laneLength - 1.0,
              tickData = tickData)
      tickData.entities = listOf(ego, otherVehicle)
      tickDataList.add(tickData)
    }
    val segment = Segment(tickDataList, segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(!pedestrianCrossed.holds(ctx, TickDataUnitSeconds(0.0), 0))
  }
}
