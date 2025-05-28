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
import tools.aqua.stars.carla.experiments.besides
import tools.aqua.stars.core.evaluation.PredicateContext
import tools.aqua.stars.data.av.dataclasses.*

class BesidesTest {
  private lateinit var road0: Road
  private lateinit var road0Lane1: Lane
  private lateinit var road0Lane2: Lane
  private lateinit var road0Lane3: Lane
  private lateinit var road0LaneNeg1: Lane

  private lateinit var road1: Road
  private lateinit var road1Lane1: Lane

  private lateinit var block: Block

  private val vehicleId0 = 0
  private val vehicleId1 = 1
  private val vehicleId2 = 2

  @BeforeTest
  fun setup() {
    road0Lane1 = Lane(laneId = 1)
    road0Lane2 = Lane(laneId = 2)
    road0Lane3 = Lane(laneId = 3)
    road0LaneNeg1 = Lane(laneId = -1)

    road1Lane1 = Lane(laneId = 1)

    road0 = Road(id = 0, lanes = listOf(road0Lane1, road0Lane2, road0Lane3, road0LaneNeg1))
    road1 = Road(id = 1, lanes = listOf(road1Lane1))
    block = Block(roads = listOf(road0, road1))

    road0Lane1.road = road0
    road0Lane2.road = road0
    road0Lane3.road = road0
    road0LaneNeg1.road = road0

    road1Lane1.road = road1
  }

  @Test
  fun twoVehiclesBesides() {
    val vehicle0 = Vehicle(isEgo = true, id = vehicleId0, lane = road0Lane1)
    val vehicle1 = Vehicle(isEgo = false, id = vehicleId1, lane = road0Lane2)

    val tickData =
        TickData(
            TickDataUnitSeconds(0.0), blocks = listOf(block), entities = listOf(vehicle0, vehicle1))

    val segment = Segment(listOf(tickData), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(besides.holds(ctx, vehicle0, vehicle1))
    assert(besides.holds(ctx, vehicle1, vehicle0))
    assert(besides.holds(ctx, TickDataUnitSeconds(0.0), vehicleId0, vehicleId1))
    assert(besides.holds(ctx, TickDataUnitSeconds(0.0), vehicleId1, vehicleId0))
  }

  @Test
  fun twoVehiclesBesidesDifferentDirection() {
    val vehicle0 = Vehicle(isEgo = true, id = vehicleId0, lane = road0Lane1)
    val vehicle1 = Vehicle(isEgo = false, id = vehicleId1, lane = road0LaneNeg1)

    val tickData =
        TickData(
            TickDataUnitSeconds(0.0), blocks = listOf(block), entities = listOf(vehicle0, vehicle1))

    val segment = Segment(listOf(tickData), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(!besides.holds(ctx, vehicle0, vehicle1))
    assert(!besides.holds(ctx, vehicle1, vehicle0))
    assert(!besides.holds(ctx, TickDataUnitSeconds(0.0), vehicleId0, vehicleId1))
    assert(!besides.holds(ctx, TickDataUnitSeconds(0.0), vehicleId1, vehicleId0))
  }

  @Test
  fun vehicleBesidesLeft() {
    val vehicle0 = Vehicle(isEgo = true, id = vehicleId0, lane = road0Lane2)
    val vehicle1 = Vehicle(isEgo = false, id = vehicleId1, lane = road0Lane1)

    val tickData =
        TickData(
            TickDataUnitSeconds(0.0), blocks = listOf(block), entities = listOf(vehicle0, vehicle1))

    val segment = Segment(listOf(tickData), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(besides.holds(ctx, vehicle0, vehicle1))
    assert(besides.holds(ctx, vehicle1, vehicle0))
    assert(besides.holds(ctx, TickDataUnitSeconds(0.0), vehicleId0, vehicleId1))
    assert(besides.holds(ctx, TickDataUnitSeconds(0.0), vehicleId1, vehicleId0))
  }

  @Test
  fun vehicleBesidesRight() {
    val vehicle0 = Vehicle(isEgo = true, id = vehicleId0, lane = road0Lane2)
    val vehicle1 = Vehicle(isEgo = false, id = vehicleId1, lane = road0Lane3)

    val tickData =
        TickData(
            TickDataUnitSeconds(0.0), blocks = listOf(block), entities = listOf(vehicle0, vehicle1))

    val segment = Segment(listOf(tickData), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(besides.holds(ctx, vehicle0, vehicle1))
    assert(besides.holds(ctx, vehicle1, vehicle0))
    assert(besides.holds(ctx, TickDataUnitSeconds(0.0), vehicleId0, vehicleId1))
    assert(besides.holds(ctx, TickDataUnitSeconds(0.0), vehicleId1, vehicleId0))
  }

  @Test
  fun vehicleBesidesWithLaneInBetween() {
    val vehicle0 = Vehicle(isEgo = true, id = vehicleId0, lane = road0Lane1)
    val vehicle1 = Vehicle(isEgo = false, id = vehicleId1, lane = road0Lane3)

    val tickData =
        TickData(
            TickDataUnitSeconds(0.0), blocks = listOf(block), entities = listOf(vehicle0, vehicle1))

    val segment = Segment(listOf(tickData), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(besides.holds(ctx, vehicle0, vehicle1))
    assert(besides.holds(ctx, vehicle1, vehicle0))
    assert(besides.holds(ctx, TickDataUnitSeconds(0.0), vehicleId0, vehicleId1))
    assert(besides.holds(ctx, TickDataUnitSeconds(0.0), vehicleId1, vehicleId0))
  }

  @Test
  fun besidesForThreeVehiclesNextToEachOther() {
    val vehicle0 = Vehicle(isEgo = true, id = vehicleId0, lane = road0Lane1)
    val vehicle1 = Vehicle(isEgo = false, id = vehicleId1, lane = road0Lane2)
    val vehicle2 = Vehicle(isEgo = false, id = vehicleId2, lane = road0Lane3)

    val tickData =
        TickData(
            TickDataUnitSeconds(0.0),
            blocks = listOf(block),
            entities = listOf(vehicle0, vehicle1, vehicle2))

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
    val vehicle0 = Vehicle(isEgo = true, id = vehicleId0, lane = road0Lane1, positionOnLane = 10.0)
    val vehicle1 = Vehicle(isEgo = false, id = vehicleId1, lane = road0Lane2, positionOnLane = 0.0)

    val tickData =
        TickData(
            TickDataUnitSeconds(0.0), blocks = listOf(block), entities = listOf(vehicle0, vehicle1))

    val segment = Segment(listOf(tickData), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(!besides.holds(ctx, vehicle0, vehicle1))
    assert(!besides.holds(ctx, vehicle1, vehicle0))
  }

  @Test
  fun tooFarAwayBehind() {
    val vehicle0 = Vehicle(isEgo = true, id = vehicleId0, lane = road0Lane1, positionOnLane = 0.0)
    val vehicle1 = Vehicle(isEgo = false, id = vehicleId1, lane = road0Lane2, positionOnLane = 10.0)

    val tickData =
        TickData(
            TickDataUnitSeconds(0.0), blocks = listOf(block), entities = listOf(vehicle0, vehicle1))

    val segment = Segment(listOf(tickData), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(!besides.holds(ctx, vehicle0, vehicle1))
    assert(!besides.holds(ctx, vehicle1, vehicle0))
  }
}
