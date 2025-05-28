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
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import tools.aqua.stars.carla.experiments.pedestrianCrossed
import tools.aqua.stars.core.evaluation.PredicateContext
import tools.aqua.stars.data.av.dataclasses.Block
import tools.aqua.stars.data.av.dataclasses.Lane
import tools.aqua.stars.data.av.dataclasses.Pedestrian
import tools.aqua.stars.data.av.dataclasses.Road
import tools.aqua.stars.data.av.dataclasses.Segment
import tools.aqua.stars.data.av.dataclasses.TickData
import tools.aqua.stars.data.av.dataclasses.TickDataUnitSeconds
import tools.aqua.stars.data.av.dataclasses.Vehicle

class PedestrianCrossedTest {
  private lateinit var road0: Road
  private lateinit var road0lane1: Lane
  private lateinit var road0lane2: Lane
  private lateinit var road0lane3: Lane

  private lateinit var road1: Road
  private lateinit var road1lane1: Lane

  private lateinit var block: Block

  @BeforeTest
  fun setup() {
    road0lane1 = Lane(laneId = 1, laneLength = 50.0)
    road0lane2 = Lane(laneId = 2, laneLength = 50.0)
    road0lane3 = Lane(laneId = 3, laneLength = 50.0)

    road1lane1 = Lane(laneId = 1, laneLength = 50.0)

    road0 =
        Road(id = 0, lanes = listOf(road0lane1, road0lane2, road0lane3)).apply {
          road0lane1.road = this
          road0lane2.road = this
          road0lane3.road = this
        }

    road1 = Road(id = 1, lanes = listOf(road1lane1)).apply { road1lane1.road = this }

    block = Block(roads = listOf(road0, road1))
  }

  @Test
  fun pedestrianCrossed() {
    val tickDataList = mutableListOf<TickData>()
    for (i in 0..3) {

      val ego =
          Vehicle(
              id = 0, isEgo = true, positionOnLane = road0lane1.laneLength - 3.0, lane = road0lane1)
      val pedestrianLane =
          when (i) {
            1 -> road0lane3
            2 -> road0lane2
            else -> road0lane1
          }
      val pedestrian =
          Pedestrian(
              id = 1, lane = pedestrianLane, positionOnLane = pedestrianLane.laneLength - 1.0)

      val tickData =
          TickData(
              currentTick = TickDataUnitSeconds(i.toDouble()),
              blocks = listOf(block),
              entities = listOf(ego, pedestrian))

      ego.tickData = tickData
      pedestrian.tickData = tickData

      tickDataList.add(tickData)
    }
    val segment = Segment(tickDataList, segmentSource = "")
    val ctx = PredicateContext(segment)

    assertTrue { pedestrianCrossed.holds(ctx, TickDataUnitSeconds(0.0), 0) }
  }

  @Test
  fun pedestrianCrossedTooEarly() {
    val tickDataList = mutableListOf<TickData>()
    for (i in 0..3) {
      val ego =
          Vehicle(
              id = 0,
              isEgo = true,
              positionOnLane = road0lane1.laneLength - 20.0,
              lane = road0lane1)
      val pedestrianLane =
          when (i) {
            1 -> road0lane3
            2 -> road0lane2
            else -> road0lane1
          }
      val pedestrian =
          Pedestrian(
              id = 1, lane = pedestrianLane, positionOnLane = pedestrianLane.laneLength - 1.0)

      val tickData =
          TickData(
              currentTick = TickDataUnitSeconds(i.toDouble()),
              blocks = listOf(block),
              entities = listOf(ego, pedestrian))
      ego.tickData = tickData
      pedestrian.tickData = tickData

      tickDataList.add(tickData)
    }
    val ctx = PredicateContext(Segment(tickDataList, segmentSource = ""))
    assertFalse { pedestrianCrossed.holds(ctx, TickDataUnitSeconds(0.0), 0) }
  }

  @Test
  fun pedestrianCrossedTooLate() {
    val tickDataList = mutableListOf<TickData>()
    for (i in 0..3) {
      val ego =
          Vehicle(
              id = 0,
              isEgo = true,
              positionOnLane = road0lane1.laneLength - 20.0,
              lane = road0lane1)
      val pedestrianLane =
          when (i) {
            1 -> road0lane3
            2 -> road0lane2
            else -> road0lane1
          }
      val pedestrian =
          Pedestrian(
              id = 1, lane = pedestrianLane, positionOnLane = pedestrianLane.laneLength - 30.0)

      val tickData =
          TickData(
              currentTick = TickDataUnitSeconds(i.toDouble()),
              blocks = listOf(block),
              entities = listOf(ego, pedestrian))
      ego.tickData = tickData
      pedestrian.tickData = tickData

      tickDataList.add(tickData)
    }
    val ctx = PredicateContext(Segment(tickDataList, segmentSource = ""))
    assertFalse { pedestrianCrossed.holds(ctx, TickDataUnitSeconds(0.0), 0) }
  }

  @Test
  fun noPedestrianCrossed() {
    val tickDataList = mutableListOf<TickData>()
    for (i in 0..3) {
      val ego =
          Vehicle(
              id = 0,
              isEgo = true,
              positionOnLane = road0lane1.laneLength - 20.0,
              lane = road0lane1)

      val tickData =
          TickData(
              currentTick = TickDataUnitSeconds(i.toDouble()),
              blocks = listOf(block),
              entities = listOf(ego))
      ego.tickData = tickData

      tickDataList.add(tickData)
    }
    val ctx = PredicateContext(Segment(tickDataList, segmentSource = ""))
    assertFalse { pedestrianCrossed.holds(ctx, TickDataUnitSeconds(0.0), 0) }
  }

  @Test
  fun vehicleCrossed() {
    val tickDataList = mutableListOf<TickData>()
    for (i in 0..3) {
      val ego =
          Vehicle(
              id = 0, isEgo = true, positionOnLane = road0lane1.laneLength - 3.0, lane = road0lane1)
      val otherVehicleLane =
          when (i) {
            1 -> road0lane3
            2 -> road0lane2
            else -> road0lane1
          }
      val otherVehicle =
          Vehicle(
              id = 1, lane = otherVehicleLane, positionOnLane = otherVehicleLane.laneLength - 1.0)

      val tickData =
          TickData(
              currentTick = TickDataUnitSeconds(i.toDouble()),
              blocks = listOf(block),
              entities = listOf(ego, otherVehicle))
      ego.tickData = tickData
      otherVehicle.tickData = tickData

      tickDataList.add(tickData)
    }
    val ctx = PredicateContext(Segment(tickDataList, segmentSource = ""))
    // since it's a Vehicle, not Pedestrian, predicate should be false
    assertFalse { pedestrianCrossed.holds(ctx, TickDataUnitSeconds(0.0), 0) }
  }
}
