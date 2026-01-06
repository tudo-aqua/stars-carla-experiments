/*
 * Copyright 2024-2026 The STARS Carla Experiments Authors
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
import tools.aqua.stars.carla.experiments.besides
import tools.aqua.stars.carla.experiments.emptyBlock
import tools.aqua.stars.carla.experiments.emptyLane
import tools.aqua.stars.carla.experiments.emptyRoad
import tools.aqua.stars.carla.experiments.emptyTickData
import tools.aqua.stars.carla.experiments.emptyVehicle
import tools.aqua.stars.core.evaluation.PredicateContext
import tools.aqua.stars.data.av.dataclasses.*

class BesidesTest {

  private val road0 = emptyRoad(id = 0)
  private val road0Lane1 = emptyLane(laneId = 1, road = road0)
  private val road0Lane2 = emptyLane(laneId = 2, road = road0)
  private val road0Lane3 = emptyLane(laneId = 3, road = road0)
  private val road0LaneNeg1 = emptyLane(laneId = -1, road = road0)

  private val road1 = emptyRoad(id = 1)
  private val road1Lane1 = emptyLane(laneId = 1, road = road1)

  private val block = emptyBlock()

  private val vehicleId0 = 0
  private val vehicleId1 = 1
  private val vehicleId2 = 2

  @BeforeTest
  fun setup() {
    road0.lanes = listOf(road0Lane1, road0Lane2, road0Lane3, road0LaneNeg1)
    road1.lanes = listOf(road1Lane1)
    block.roads = listOf(road0, road1)
  }

  @Test
  fun twoVehiclesBesides() {
    val vehicle0 = emptyVehicle(egoVehicle = true, id = vehicleId0, lane = road0Lane1)
    val vehicle1 = emptyVehicle(egoVehicle = false, id = vehicleId1, lane = road0Lane2)

    val tickData =
        emptyTickData(TickDataUnitSeconds(0.0), listOf(block), actors = listOf(vehicle0, vehicle1))

    val segment = Segment(listOf(tickData), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(besides.holds(ctx, vehicle0, vehicle1))
    assert(besides.holds(ctx, vehicle1, vehicle0))
    assert(besides.holds(ctx, TickDataUnitSeconds(0.0), vehicleId0, vehicleId1))
    assert(besides.holds(ctx, TickDataUnitSeconds(0.0), vehicleId1, vehicleId0))
  }

  @Test
  fun twoVehiclesBesidesDifferentDirection() {
    val vehicle0 = emptyVehicle(egoVehicle = true, id = vehicleId0, lane = road0Lane1)
    val vehicle1 = emptyVehicle(egoVehicle = false, id = vehicleId1, lane = road0LaneNeg1)

    val tickData =
        emptyTickData(TickDataUnitSeconds(0.0), listOf(block), actors = listOf(vehicle0, vehicle1))

    val segment = Segment(listOf(tickData), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(!besides.holds(ctx, vehicle0, vehicle1))
    assert(!besides.holds(ctx, vehicle1, vehicle0))
    assert(!besides.holds(ctx, TickDataUnitSeconds(0.0), vehicleId0, vehicleId1))
    assert(!besides.holds(ctx, TickDataUnitSeconds(0.0), vehicleId1, vehicleId0))
  }

  @Test
  fun vehicleBesidesLeft() {
    val vehicle0 = emptyVehicle(egoVehicle = true, id = vehicleId0, lane = road0Lane2)
    val vehicle1 = emptyVehicle(egoVehicle = false, id = vehicleId1, lane = road0Lane1)

    val tickData =
        emptyTickData(TickDataUnitSeconds(0.0), listOf(block), actors = listOf(vehicle0, vehicle1))

    val segment = Segment(listOf(tickData), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(besides.holds(ctx, vehicle0, vehicle1))
    assert(besides.holds(ctx, vehicle1, vehicle0))
    assert(besides.holds(ctx, TickDataUnitSeconds(0.0), vehicleId0, vehicleId1))
    assert(besides.holds(ctx, TickDataUnitSeconds(0.0), vehicleId1, vehicleId0))
  }

  @Test
  fun vehicleBesidesRight() {
    val vehicle0 = emptyVehicle(egoVehicle = true, id = vehicleId0, lane = road0Lane2)
    val vehicle1 = emptyVehicle(egoVehicle = false, id = vehicleId1, lane = road0Lane3)

    val tickData =
        emptyTickData(TickDataUnitSeconds(0.0), listOf(block), actors = listOf(vehicle0, vehicle1))

    val segment = Segment(listOf(tickData), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(besides.holds(ctx, vehicle0, vehicle1))
    assert(besides.holds(ctx, vehicle1, vehicle0))
    assert(besides.holds(ctx, TickDataUnitSeconds(0.0), vehicleId0, vehicleId1))
    assert(besides.holds(ctx, TickDataUnitSeconds(0.0), vehicleId1, vehicleId0))
  }

  @Test
  fun vehicleBesidesWithLaneInBetween() {
    val vehicle0 = emptyVehicle(egoVehicle = true, id = vehicleId0, lane = road0Lane1)
    val vehicle1 = emptyVehicle(egoVehicle = false, id = vehicleId1, lane = road0Lane3)

    val tickData =
        emptyTickData(TickDataUnitSeconds(0.0), listOf(block), actors = listOf(vehicle0, vehicle1))

    val segment = Segment(listOf(tickData), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(besides.holds(ctx, vehicle0, vehicle1))
    assert(besides.holds(ctx, vehicle1, vehicle0))
    assert(besides.holds(ctx, TickDataUnitSeconds(0.0), vehicleId0, vehicleId1))
    assert(besides.holds(ctx, TickDataUnitSeconds(0.0), vehicleId1, vehicleId0))
  }

  @Test
  fun besidesForThreeVehiclesNextToEachOther() {
    val vehicle0 = emptyVehicle(egoVehicle = true, id = vehicleId0, lane = road0Lane1)
    val vehicle1 = emptyVehicle(egoVehicle = false, id = vehicleId1, lane = road0Lane2)
    val vehicle2 = emptyVehicle(egoVehicle = false, id = vehicleId2, lane = road0Lane3)

    val tickData =
        emptyTickData(
            TickDataUnitSeconds(0.0),
            listOf(block),
            actors = listOf(vehicle0, vehicle1, vehicle2),
        )

    val segment = Segment(listOf(tickData), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(besides.holds(ctx, vehicle0, vehicle1))
    assert(besides.holds(ctx, vehicle1, vehicle0))
    assert(besides.holds(ctx, vehicle0, vehicle2))
    assert(besides.holds(ctx, vehicle2, vehicle0))
    assert(besides.holds(ctx, vehicle1, vehicle2))
    assert(besides.holds(ctx, vehicle2, vehicle1))
  }

  @Test
  fun tooFarAwayInFront() {
    val vehicle0 =
        emptyVehicle(egoVehicle = true, id = vehicleId0, lane = road0Lane1, positionOnLane = 10.0)
    val vehicle1 =
        emptyVehicle(egoVehicle = false, id = vehicleId1, lane = road0Lane2, positionOnLane = 0.0)

    val tickData =
        emptyTickData(TickDataUnitSeconds(0.0), listOf(block), actors = listOf(vehicle0, vehicle1))

    val segment = Segment(listOf(tickData), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(!besides.holds(ctx, vehicle0, vehicle1))
    assert(!besides.holds(ctx, vehicle1, vehicle0))
  }

  @Test
  fun tooFarAwayBehind() {
    val vehicle0 =
        emptyVehicle(egoVehicle = true, id = vehicleId0, lane = road0Lane1, positionOnLane = 0.0)
    val vehicle1 =
        emptyVehicle(egoVehicle = false, id = vehicleId1, lane = road0Lane2, positionOnLane = 10.0)

    val tickData =
        emptyTickData(TickDataUnitSeconds(0.0), listOf(block), actors = listOf(vehicle0, vehicle1))

    val segment = Segment(listOf(tickData), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(!besides.holds(ctx, vehicle0, vehicle1))
    assert(!besides.holds(ctx, vehicle1, vehicle0))
  }
}
