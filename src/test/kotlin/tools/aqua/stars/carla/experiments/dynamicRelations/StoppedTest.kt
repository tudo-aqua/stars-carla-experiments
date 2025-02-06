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
import tools.aqua.stars.carla.experiments.emptyRoad
import tools.aqua.stars.carla.experiments.emptyTickData
import tools.aqua.stars.carla.experiments.emptyVehicle
import tools.aqua.stars.carla.experiments.stopped
import tools.aqua.stars.core.evaluation.PredicateContext
import tools.aqua.stars.data.av.dataclasses.Segment
import tools.aqua.stars.data.av.dataclasses.TickDataUnitSeconds

class StoppedTest {

  private val road0 = emptyRoad(id = 0)
  private val road0lane1 = emptyLane(laneId = 1, road = road0, laneLength = 50.0)
  private val road0lane2 = emptyLane(laneId = 2, road = road0, laneLength = 50.0)

  private val road1 = emptyRoad(id = 1)
  private val road1lane1 = emptyLane(laneId = 1, road = road1, laneLength = 50.0)

  private val block = emptyBlock()

  @BeforeTest
  fun setup() {
    road0.lanes = listOf(road0lane1, road0lane2)
    road1.lanes = listOf(road1lane1)

    block.roads = listOf(road0, road1)
  }

  @Test
  fun stopped() {
    val vehicle =
        emptyVehicle(
            id = 0,
            egoVehicle = true,
            lane = road0lane1,
            positionOnLane = 0.0,
            effVelocityMPH = 0.0)
    val tickData =
        emptyTickData(
            currentTick = TickDataUnitSeconds(0.0),
            blocks = listOf(block),
            actors = listOf(vehicle))
    val segment = Segment(listOf(tickData), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(stopped.holds(ctx, vehicle))
    assert(stopped.holds(ctx, TickDataUnitSeconds(0.0), 0))
  }

  @Test
  fun speedIs1MPH() {
    val vehicle =
        emptyVehicle(
            id = 0,
            egoVehicle = true,
            lane = road0lane1,
            positionOnLane = 0.0,
            effVelocityMPH = 1.0)
    val tickData =
        emptyTickData(
            currentTick = TickDataUnitSeconds(0.0),
            blocks = listOf(block),
            actors = listOf(vehicle))
    val segment = Segment(listOf(tickData), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(stopped.holds(ctx, vehicle))
    assert(stopped.holds(ctx, TickDataUnitSeconds(0.0), 0))
  }

  @Test
  fun speedIs0Point5MPH() {
    val vehicle =
        emptyVehicle(
            id = 0,
            egoVehicle = true,
            lane = road0lane1,
            positionOnLane = 0.0,
            effVelocityMPH = 0.5)
    val tickData =
        emptyTickData(
            currentTick = TickDataUnitSeconds(0.0),
            blocks = listOf(block),
            actors = listOf(vehicle))
    val segment = Segment(listOf(tickData), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(stopped.holds(ctx, vehicle))
    assert(stopped.holds(ctx, TickDataUnitSeconds(0.0), 0))
  }

  @Test
  fun speedIs0MH() {
    val vehicle =
        emptyVehicle(
            id = 0,
            egoVehicle = true,
            lane = road0lane1,
            positionOnLane = 0.0,
            effVelocityMPH = 0.0)
    val tickData =
        emptyTickData(
            currentTick = TickDataUnitSeconds(0.0),
            blocks = listOf(block),
            actors = listOf(vehicle))
    val segment = Segment(listOf(tickData), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(stopped.holds(ctx, vehicle))
    assert(stopped.holds(ctx, TickDataUnitSeconds(0.0), 0))
  }

  @Test
  fun speedIs2MPH() {
    val vehicle =
        emptyVehicle(
            id = 0,
            egoVehicle = true,
            lane = road0lane1,
            positionOnLane = 0.0,
            effVelocityMPH = 2.0)
    val tickData =
        emptyTickData(
            currentTick = TickDataUnitSeconds(0.0),
            blocks = listOf(block),
            actors = listOf(vehicle))
    val segment = Segment(listOf(tickData), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(!stopped.holds(ctx, vehicle))
    assert(!stopped.holds(ctx, TickDataUnitSeconds(0.0), 0))
  }

  @Test
  fun speedIs30MPH() {
    val vehicle =
        emptyVehicle(
            id = 0,
            egoVehicle = true,
            lane = road0lane1,
            positionOnLane = 0.0,
            effVelocityMPH = 30.0)
    val tickData =
        emptyTickData(
            currentTick = TickDataUnitSeconds(0.0),
            blocks = listOf(block),
            actors = listOf(vehicle))
    val segment = Segment(listOf(tickData), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(!stopped.holds(ctx, vehicle))
    assert(!stopped.holds(ctx, TickDataUnitSeconds(0.0), 0))
  }
}
