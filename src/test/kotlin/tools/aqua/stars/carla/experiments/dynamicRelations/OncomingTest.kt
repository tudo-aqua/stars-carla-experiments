/*
 * Copyright 2024 The STARS Carla Experiments Authors
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
import tools.aqua.stars.carla.experiments.oncoming
import tools.aqua.stars.core.evaluation.PredicateContext
import tools.aqua.stars.data.av.dataclasses.Segment
import tools.aqua.stars.data.av.dataclasses.TickDataUnitSeconds

class OncomingTest {

  private val block = emptyBlock(id = "1")

  private val road0 = emptyRoad(id = 0, block = block)
  private val road0lane1 = emptyLane(laneId = 1, road = road0, laneLength = 50.0)
  private val road0lane2 = emptyLane(laneId = 2, road = road0, laneLength = 50.0)
  private val road0laneMinus1 = emptyLane(laneId = -1, road = road0, laneLength = 50.0)
  private val road0laneMinus2 = emptyLane(laneId = -2, road = road0, laneLength = 50.0)

  private val road1 = emptyRoad(id = 1, block = block)
  private val road1lane1 = emptyLane(laneId = 1, road = road1, laneLength = 50.0)

  private val block2 = emptyBlock(id = "2")

  private val road2 = emptyRoad(id = 2, block = block2)
  private val road2lane1 = emptyLane(laneId = 1, road = road2, laneLength = 50.0)

  private val blocks = listOf(block, block2)

  private val egoId = 0
  private val otherId = 1

  @BeforeTest
  fun setup() {
    road0.lanes = listOf(road0lane1, road0lane2, road0laneMinus1, road0laneMinus2)
    road1.lanes = listOf(road1lane1)

    block.roads = listOf(road0, road1)

    road2.lanes = listOf(road2lane1)

    block2.roads = listOf(road2)
  }

  @Test
  fun egoAndOtherAreOncoming() {
    val tickData = emptyTickData(currentTick = TickDataUnitSeconds(0.0), blocks = blocks)
    val ego =
        emptyVehicle(
            id = egoId,
            egoVehicle = true,
            lane = road0lane1,
            positionOnLane = 10.0,
            tickData = tickData,
            effVelocityMPH = 11.0)
    val other =
        emptyVehicle(
            id = otherId,
            egoVehicle = false,
            lane = road0laneMinus1,
            positionOnLane = 10.0,
            tickData = tickData,
            effVelocityMPH = 11.0)
    tickData.entities = listOf(ego, other)

    val segment = Segment(listOf(tickData), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(oncoming.holds(ctx, ego, other))
    assert(oncoming.holds(ctx, other, ego))
  }

  @Test
  fun egoAndOtherAreOncomingWithDifferentLaneIds() {
    val tickData = emptyTickData(currentTick = TickDataUnitSeconds(0.0), blocks = blocks)
    val ego =
        emptyVehicle(
            id = egoId,
            egoVehicle = true,
            lane = road0lane1,
            positionOnLane = 10.0,
            tickData = tickData,
            effVelocityMPH = 11.0)
    val other =
        emptyVehicle(
            id = otherId,
            egoVehicle = false,
            lane = road0laneMinus2,
            positionOnLane = 10.0,
            tickData = tickData,
            effVelocityMPH = 11.0)
    tickData.entities = listOf(ego, other)

    val segment = Segment(listOf(tickData), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(oncoming.holds(ctx, ego, other))
    assert(oncoming.holds(ctx, other, ego))
  }

  @Test
  fun egoAndOtherAreOnSameDirection() {
    val tickData = emptyTickData(currentTick = TickDataUnitSeconds(0.0), blocks = blocks)
    val ego =
        emptyVehicle(
            id = egoId,
            egoVehicle = true,
            lane = road0lane1,
            positionOnLane = 10.0,
            tickData = tickData,
            effVelocityMPH = 11.0)
    val other =
        emptyVehicle(
            id = otherId,
            egoVehicle = false,
            lane = road0lane2,
            positionOnLane = 10.0,
            tickData = tickData,
            effVelocityMPH = 11.0)
    tickData.entities = listOf(ego, other)

    val segment = Segment(listOf(tickData), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(!oncoming.holds(ctx, ego, other))
    assert(!oncoming.holds(ctx, other, ego))
  }

  @Test
  fun egoAndOtherAreOnDifferentRoads() {
    val tickData = emptyTickData(currentTick = TickDataUnitSeconds(0.0), blocks = blocks)
    val ego =
        emptyVehicle(
            id = egoId,
            egoVehicle = true,
            lane = road0lane1,
            positionOnLane = 10.0,
            tickData = tickData,
            effVelocityMPH = 11.0)
    val other =
        emptyVehicle(
            id = otherId,
            egoVehicle = false,
            lane = road2lane1,
            positionOnLane = 10.0,
            tickData = tickData,
            effVelocityMPH = 11.0)
    tickData.entities = listOf(ego, other)

    val segment = Segment(listOf(tickData), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(!oncoming.holds(ctx, ego, other))
    assert(!oncoming.holds(ctx, other, ego))
  }

  @Test
  fun egoAndOtherAreOnSameLane() {
    val tickData = emptyTickData(currentTick = TickDataUnitSeconds(0.0), blocks = blocks)
    val ego =
        emptyVehicle(
            id = egoId,
            egoVehicle = true,
            lane = road0lane1,
            positionOnLane = 10.0,
            tickData = tickData,
            effVelocityMPH = 11.0)
    val other =
        emptyVehicle(
            id = otherId,
            egoVehicle = false,
            lane = road0lane1,
            positionOnLane = 10.0,
            tickData = tickData,
            effVelocityMPH = 11.0)
    tickData.entities = listOf(ego, other)

    val segment = Segment(listOf(tickData), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(!oncoming.holds(ctx, ego, other))
    assert(!oncoming.holds(ctx, other, ego))
  }
}
