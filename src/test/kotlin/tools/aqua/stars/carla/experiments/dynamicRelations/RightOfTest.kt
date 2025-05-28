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
import tools.aqua.stars.carla.experiments.rightOf
import tools.aqua.stars.core.evaluation.PredicateContext
import tools.aqua.stars.data.av.dataclasses.Block
import tools.aqua.stars.data.av.dataclasses.Lane
import tools.aqua.stars.data.av.dataclasses.Road
import tools.aqua.stars.data.av.dataclasses.Segment
import tools.aqua.stars.data.av.dataclasses.TickData
import tools.aqua.stars.data.av.dataclasses.TickDataUnitSeconds
import tools.aqua.stars.data.av.dataclasses.Vehicle

class RightOfTest {
  private lateinit var road0: Road
  private lateinit var road0lane1: Lane
  private lateinit var road0lane2: Lane
  private lateinit var road0lane3: Lane
  private lateinit var road0laneMinus1: Lane

  private lateinit var road1: Road
  private lateinit var road1lane1: Lane

  private lateinit var block: Block

  @BeforeTest
  fun setup() {
    road0lane1 = Lane(laneId = 1, laneLength = 50.0)
    road0lane2 = Lane(laneId = 2, laneLength = 50.0)
    road0lane3 = Lane(laneId = 3, laneLength = 50.0)
    road0laneMinus1 = Lane(laneId = -1, laneLength = 50.0)

    road1lane1 = Lane(laneId = 1, laneLength = 50.0)

    road0 =
        Road(id = 0, lanes = listOf(road0lane1, road0lane2, road0lane3, road0laneMinus1)).apply {
          road0lane1.road = this
          road0lane2.road = this
          road0lane3.road = this
          road0laneMinus1.road = this
        }

    road1 = Road(id = 1, lanes = listOf(road1lane1)).apply { road1lane1.road = this }

    block = Block(roads = listOf(road0, road1))
  }

  @Test
  fun vehicleIsLeftOfEgo() {
    val ego = Vehicle(id = 0, isEgo = true, lane = road0lane2, positionOnLane = 0.0)
    val otherVehicle = Vehicle(id = 1, isEgo = false, lane = road0lane1, positionOnLane = 0.0)

    val tickData =
        TickData(
            currentTick = TickDataUnitSeconds(0.0),
            blocks = listOf(block),
            entities = listOf(ego, otherVehicle))
    val segment = Segment(listOf(tickData), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(rightOf.holds(ctx, ego, otherVehicle))
    assert(!rightOf.holds(ctx, otherVehicle, ego))
  }

  @Test
  fun vehicleIsLeftWithLaneInBetweenOfEgo() {
    val ego = Vehicle(id = 0, isEgo = true, lane = road0lane3, positionOnLane = 0.0)
    val otherVehicle = Vehicle(id = 1, isEgo = false, lane = road0lane1, positionOnLane = 0.0)

    val tickData =
        TickData(
            currentTick = TickDataUnitSeconds(0.0),
            blocks = listOf(block),
            entities = listOf(ego, otherVehicle))
    val segment = Segment(listOf(tickData), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(rightOf.holds(ctx, ego, otherVehicle))
    assert(!rightOf.holds(ctx, otherVehicle, ego))
  }

  @Test
  fun vehicleIsLeftOfEgoOnDifferentDirection() {
    val ego = Vehicle(id = 0, isEgo = true, lane = road0lane3, positionOnLane = 0.0)
    val otherVehicle = Vehicle(id = 1, isEgo = false, lane = road0laneMinus1, positionOnLane = 0.0)

    val tickData =
        TickData(
            currentTick = TickDataUnitSeconds(0.0),
            blocks = listOf(block),
            entities = listOf(ego, otherVehicle))
    val segment = Segment(listOf(tickData), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(!rightOf.holds(ctx, ego, otherVehicle))
    assert(!rightOf.holds(ctx, otherVehicle, ego))
  }

  @Test
  fun vehicleIsLeftOfEgoButBehind() {
    val ego = Vehicle(id = 0, isEgo = true, lane = road0lane3, positionOnLane = 10.0)
    val otherVehicle = Vehicle(id = 1, isEgo = false, lane = road0lane1, positionOnLane = 0.0)

    val tickData =
        TickData(
            currentTick = TickDataUnitSeconds(0.0),
            blocks = listOf(block),
            entities = listOf(ego, otherVehicle))
    val segment = Segment(listOf(tickData), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(!rightOf.holds(ctx, ego, otherVehicle))
    assert(!rightOf.holds(ctx, otherVehicle, ego))
  }

  @Test
  fun vehicleIsLeftOfEgoButInFront() {
    val ego = Vehicle(id = 0, isEgo = true, lane = road0lane3, positionOnLane = 0.0)
    val otherVehicle = Vehicle(id = 1, isEgo = false, lane = road0lane1, positionOnLane = 10.0)

    val tickData =
        TickData(
            currentTick = TickDataUnitSeconds(0.0),
            blocks = listOf(block),
            entities = listOf(ego, otherVehicle))
    val segment = Segment(listOf(tickData), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(!rightOf.holds(ctx, ego, otherVehicle))
    assert(!rightOf.holds(ctx, otherVehicle, ego))
  }

  @Test
  fun vehicleIsOnSameLaneAndBehind() {
    val ego = Vehicle(id = 0, isEgo = true, lane = road0lane1, positionOnLane = 10.0)
    val otherVehicle = Vehicle(id = 1, isEgo = false, lane = road0lane1, positionOnLane = 0.0)

    val tickData =
        TickData(
            currentTick = TickDataUnitSeconds(0.0),
            blocks = listOf(block),
            entities = listOf(ego, otherVehicle))
    val segment = Segment(listOf(tickData), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(!rightOf.holds(ctx, ego, otherVehicle))
    assert(!rightOf.holds(ctx, otherVehicle, ego))
  }

  @Test
  fun vehicleIsOnSameLaneAndInFront() {
    val ego = Vehicle(id = 0, isEgo = true, lane = road0lane1, positionOnLane = 0.0)
    val otherVehicle = Vehicle(id = 1, isEgo = false, lane = road0lane1, positionOnLane = 10.0)

    val tickData =
        TickData(
            currentTick = TickDataUnitSeconds(0.0),
            blocks = listOf(block),
            entities = listOf(ego, otherVehicle))
    val segment = Segment(listOf(tickData), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(!rightOf.holds(ctx, ego, otherVehicle))
    assert(!rightOf.holds(ctx, otherVehicle, ego))
  }

  @Test
  fun vehicleIsOnDifferentRoadAndInLeftOfLaneId() {
    val ego = Vehicle(id = 0, isEgo = true, lane = road0lane2, positionOnLane = 0.0)
    val otherVehicle = Vehicle(id = 1, isEgo = false, lane = road1lane1, positionOnLane = 0.0)

    val tickData =
        TickData(
            currentTick = TickDataUnitSeconds(0.0),
            blocks = listOf(block),
            entities = listOf(ego, otherVehicle))
    val segment = Segment(listOf(tickData), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(!rightOf.holds(ctx, ego, otherVehicle))
    assert(!rightOf.holds(ctx, otherVehicle, ego))
  }
}
