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

package tools.aqua.stars.carla.experiments.trafficDensity

import kotlin.test.BeforeTest
import kotlin.test.Test
import tools.aqua.stars.carla.experiments.emptyBlock
import tools.aqua.stars.carla.experiments.emptyLane
import tools.aqua.stars.carla.experiments.emptyRoad
import tools.aqua.stars.carla.experiments.emptyTickData
import tools.aqua.stars.carla.experiments.emptyVehicle
import tools.aqua.stars.carla.experiments.hasHighTrafficDensity
import tools.aqua.stars.carla.experiments.hasLowTrafficDensity
import tools.aqua.stars.carla.experiments.hasMidTrafficDensity
import tools.aqua.stars.core.evaluation.PredicateContext
import tools.aqua.stars.data.av.dataclasses.Actor
import tools.aqua.stars.data.av.dataclasses.Segment
import tools.aqua.stars.data.av.dataclasses.TickDataUnitSeconds

class TrafficDensityTest {

  private val block = emptyBlock(id = "1")

  private val road0 = emptyRoad(id = 0, block = block)
  private val road0lane1 = emptyLane(laneId = 1, road = road0, laneLength = 50.0)

  private val road1 = emptyRoad(id = 1, block = block)
  private val road1lane1 = emptyLane(laneId = 1, road = road1, laneLength = 50.0)

  private val block2 = emptyBlock(id = "2")

  private val road2 = emptyRoad(id = 2, block = block2)
  private val road2lane1 = emptyLane(laneId = 1, road = road2, laneLength = 50.0)

  @BeforeTest
  fun setup() {
    road0.lanes = listOf(road0lane1)
    road1.lanes = listOf(road1lane1)

    block.roads = listOf(road0, road1)

    road2.lanes = listOf(road2lane1)

    block2.roads = listOf(road2)
  }

  @Test
  fun hasLowTrafficDensity() {
    val tickData = emptyTickData(currentTick = TickDataUnitSeconds(0.0), blocks = listOf(block))
    val actors = mutableListOf<Actor>()
    for (i in 1..5) {
      val vehicle =
          emptyVehicle(
              id = i,
              egoVehicle = i == 1,
              lane = road0lane1,
              tickData = tickData,
              positionOnLane = i.toDouble(),
              effVelocityMPH = 11.0)
      actors += vehicle
    }
    tickData.entities = actors

    val segment = Segment(listOf(tickData), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(hasLowTrafficDensity.holds(ctx, TickDataUnitSeconds(0.0), 1))
    assert(!hasMidTrafficDensity.holds(ctx, TickDataUnitSeconds(0.0), 1))
    assert(!hasHighTrafficDensity.holds(ctx, TickDataUnitSeconds(0.0), 1))
  }

  @Test
  fun hasMidTrafficDensity() {
    val tickData = emptyTickData(currentTick = TickDataUnitSeconds(0.0), blocks = listOf(block))
    val actors = mutableListOf<Actor>()
    for (i in 1..10) {
      val vehicle =
          emptyVehicle(
              id = i,
              egoVehicle = i == 1,
              lane = road0lane1,
              tickData = tickData,
              positionOnLane = i.toDouble(),
              effVelocityMPH = 11.0)
      actors += vehicle
    }
    tickData.entities = actors

    val segment = Segment(listOf(tickData), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(!hasLowTrafficDensity.holds(ctx, TickDataUnitSeconds(0.0), 1))
    assert(hasMidTrafficDensity.holds(ctx, TickDataUnitSeconds(0.0), 1))
    assert(!hasHighTrafficDensity.holds(ctx, TickDataUnitSeconds(0.0), 1))
  }

  @Test
  fun hasHighTrafficDensity() {
    val tickData = emptyTickData(currentTick = TickDataUnitSeconds(0.0), blocks = listOf(block))
    val actors = mutableListOf<Actor>()
    for (i in 1..20) {
      val vehicle =
          emptyVehicle(
              id = i,
              egoVehicle = i == 1,
              lane = road0lane1,
              tickData = tickData,
              positionOnLane = i.toDouble(),
              effVelocityMPH = 11.0)
      actors += vehicle
    }
    tickData.entities = actors

    val segment = Segment(listOf(tickData), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(!hasLowTrafficDensity.holds(ctx, TickDataUnitSeconds(0.0), 1))
    assert(!hasMidTrafficDensity.holds(ctx, TickDataUnitSeconds(0.0), 1))
    assert(hasHighTrafficDensity.holds(ctx, TickDataUnitSeconds(0.0), 1))
  }

  @Test
  fun hasLowTrafficAndOtherBlockHasHighTrafficDensity() {
    val tickData =
        emptyTickData(currentTick = TickDataUnitSeconds(0.0), blocks = listOf(block, block2))
    val actors = mutableListOf<Actor>()
    for (i in 1..5) {
      val vehicle =
          emptyVehicle(
              id = i,
              egoVehicle = i == 1,
              lane = road0lane1,
              tickData = tickData,
              positionOnLane = i.toDouble(),
              effVelocityMPH = 11.0)
      actors += vehicle
    }
    for (i in 6..25) {
      val vehicle =
          emptyVehicle(
              id = i,
              egoVehicle = false,
              lane = road2lane1,
              tickData = tickData,
              positionOnLane = i.toDouble(),
              effVelocityMPH = 11.0)
      actors += vehicle
    }
    tickData.entities = actors

    val segment = Segment(listOf(tickData), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(hasLowTrafficDensity.holds(ctx, TickDataUnitSeconds(0.0), 1))
    assert(!hasMidTrafficDensity.holds(ctx, TickDataUnitSeconds(0.0), 1))
    assert(!hasHighTrafficDensity.holds(ctx, TickDataUnitSeconds(0.0), 1))
  }

  @Test
  fun hasMidTrafficAndOtherBlockHasHighTrafficDensity() {
    val tickData =
        emptyTickData(currentTick = TickDataUnitSeconds(0.0), blocks = listOf(block, block2))
    val actors = mutableListOf<Actor>()
    for (i in 1..10) {
      val vehicle =
          emptyVehicle(
              id = i,
              egoVehicle = i == 1,
              lane = road0lane1,
              tickData = tickData,
              positionOnLane = i.toDouble(),
              effVelocityMPH = 11.0)
      actors += vehicle
    }
    for (i in 11..100) {
      val vehicle =
          emptyVehicle(
              id = i,
              egoVehicle = false,
              lane = road2lane1,
              tickData = tickData,
              positionOnLane = i.toDouble(),
              effVelocityMPH = 11.0)
      actors += vehicle
    }
    tickData.entities = actors

    val segment = Segment(listOf(tickData), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(!hasLowTrafficDensity.holds(ctx, TickDataUnitSeconds(0.0), 1))
    assert(hasMidTrafficDensity.holds(ctx, TickDataUnitSeconds(0.0), 1))
    assert(!hasHighTrafficDensity.holds(ctx, TickDataUnitSeconds(0.0), 1))
  }

  @Test
  fun hasHighTrafficAndOtherBlockHasLowTrafficDensity() {
    val tickData =
        emptyTickData(currentTick = TickDataUnitSeconds(0.0), blocks = listOf(block, block2))
    val actors = mutableListOf<Actor>()
    for (i in 1..20) {
      val vehicle =
          emptyVehicle(
              id = i,
              egoVehicle = i == 1,
              lane = road0lane1,
              tickData = tickData,
              positionOnLane = i.toDouble(),
              effVelocityMPH = 11.0)
      actors += vehicle
    }
    for (i in 11..15) {
      val vehicle =
          emptyVehicle(
              id = i,
              egoVehicle = false,
              lane = road2lane1,
              tickData = tickData,
              positionOnLane = i.toDouble(),
              effVelocityMPH = 11.0)
      actors += vehicle
    }
    tickData.entities = actors

    val segment = Segment(listOf(tickData), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(!hasLowTrafficDensity.holds(ctx, TickDataUnitSeconds(0.0), 1))
    assert(!hasMidTrafficDensity.holds(ctx, TickDataUnitSeconds(0.0), 1))
    assert(hasHighTrafficDensity.holds(ctx, TickDataUnitSeconds(0.0), 1))
  }
}
