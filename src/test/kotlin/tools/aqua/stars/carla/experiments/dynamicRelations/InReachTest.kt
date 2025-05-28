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
import tools.aqua.stars.carla.experiments.inReach
import tools.aqua.stars.core.evaluation.PredicateContext
import tools.aqua.stars.data.av.dataclasses.Block
import tools.aqua.stars.data.av.dataclasses.Lane
import tools.aqua.stars.data.av.dataclasses.Pedestrian
import tools.aqua.stars.data.av.dataclasses.Road
import tools.aqua.stars.data.av.dataclasses.Segment
import tools.aqua.stars.data.av.dataclasses.TickData
import tools.aqua.stars.data.av.dataclasses.Vehicle

class InReachTest {
  private lateinit var road0: Road
  private lateinit var road0lane1: Lane

  private lateinit var road1: Road
  private lateinit var road1lane1: Lane

  private lateinit var block: Block

  @BeforeTest
  fun setup() {
    road0lane1 = Lane(laneId = 1, laneLength = 50.0)
    road1lane1 = Lane(laneId = 1, laneLength = 50.0)

    road0 = Road(id = 0, lanes = listOf(road0lane1)).apply { road0lane1.road = this }
    road1 = Road(id = 1, lanes = listOf(road1lane1)).apply { road1lane1.road = this }

    block = Block(roads = listOf(road0, road1))
  }

  @Test
  fun sameLane9Meters() {
    val vehicle = Vehicle(isEgo = true, id = 0, lane = road0lane1, positionOnLane = 10.0)
    val pedestrian = Pedestrian(id = 1, lane = road0lane1, positionOnLane = 19.0)

    val tickData = TickData(entities = listOf(vehicle, pedestrian))
    val segment = Segment(listOf(tickData), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(inReach.holds(ctx, pedestrian, vehicle))
  }

  @Test
  fun sameLane10Meters() {
    val vehicle = Vehicle(isEgo = true, id = 0, lane = road0lane1, positionOnLane = 10.0)
    val pedestrian = Pedestrian(id = 1, lane = road0lane1, positionOnLane = 20.0)

    val tickData = TickData(entities = listOf(vehicle, pedestrian))
    val segment = Segment(listOf(tickData), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(inReach.holds(ctx, pedestrian, vehicle))
  }

  @Test
  fun sameLane11Meters() {
    val vehicle = Vehicle(isEgo = true, id = 0, lane = road0lane1, positionOnLane = 10.0)
    val pedestrian = Pedestrian(id = 1, lane = road0lane1, positionOnLane = 21.0)

    val tickData = TickData(entities = listOf(vehicle, pedestrian))
    val segment = Segment(listOf(tickData), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(!inReach.holds(ctx, pedestrian, vehicle))
  }

  @Test
  fun sameLane0Meters() {
    val vehicle = Vehicle(isEgo = true, id = 0, lane = road0lane1, positionOnLane = 10.0)
    val pedestrian = Pedestrian(id = 1, lane = road0lane1, positionOnLane = 10.0)

    val tickData = TickData(entities = listOf(vehicle, pedestrian))
    val segment = Segment(listOf(tickData), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(inReach.holds(ctx, pedestrian, vehicle))
  }

  @Test
  fun sameLaneMinus1Meters() {
    val vehicle = Vehicle(isEgo = true, id = 0, lane = road0lane1, positionOnLane = 10.0)
    val pedestrian = Pedestrian(id = 1, lane = road0lane1, positionOnLane = 9.0)

    val tickData = TickData(entities = listOf(vehicle, pedestrian))
    val segment = Segment(listOf(tickData), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(!inReach.holds(ctx, pedestrian, vehicle))
  }

  @Test
  fun differentLane9Meters() {
    val vehicle = Vehicle(isEgo = true, id = 0, lane = road0lane1, positionOnLane = 10.0)
    val pedestrian = Pedestrian(id = 1, lane = road1lane1, positionOnLane = 19.0)

    val tickData = TickData(entities = listOf(vehicle, pedestrian))
    val segment = Segment(listOf(tickData), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(!inReach.holds(ctx, pedestrian, vehicle))
  }

  @Test
  fun differentLane10Meters() {
    val vehicle = Vehicle(isEgo = true, id = 0, lane = road0lane1, positionOnLane = 10.0)
    val pedestrian = Pedestrian(id = 1, lane = road1lane1, positionOnLane = 20.0)

    val tickData = TickData(entities = listOf(vehicle, pedestrian))
    val segment = Segment(listOf(tickData), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(!inReach.holds(ctx, pedestrian, vehicle))
  }

  @Test
  fun sameLane9MetersButBothVehicles() {
    val vehicle = Vehicle(isEgo = true, id = 0, lane = road0lane1, positionOnLane = 10.0)
    val vehicle1 = Vehicle(isEgo = false, id = 1, lane = road0lane1, positionOnLane = 10.0)

    val tickData = TickData(entities = listOf(vehicle, vehicle1))
    val segment = Segment(listOf(tickData), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(!inReach.holds(ctx, tickData.currentTick, vehicle1.id, vehicle.id))
  }
}
