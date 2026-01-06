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
import tools.aqua.stars.carla.experiments.emptyBlock
import tools.aqua.stars.carla.experiments.emptyLane
import tools.aqua.stars.carla.experiments.emptyRoad
import tools.aqua.stars.carla.experiments.emptyTickData
import tools.aqua.stars.carla.experiments.emptyVehicle
import tools.aqua.stars.carla.experiments.mustYield
import tools.aqua.stars.core.evaluation.PredicateContext
import tools.aqua.stars.data.av.dataclasses.ContactLaneInfo
import tools.aqua.stars.data.av.dataclasses.Segment
import tools.aqua.stars.data.av.dataclasses.TickDataUnitSeconds

class MustYieldTest {

  private val road0 = emptyRoad(id = 0, isJunction = true)
  private val road0lane1 = emptyLane(laneId = 1, road = road0)

  private val road1 = emptyRoad(id = 1, isJunction = true)
  private val road1lane1 = emptyLane(laneId = 1, road1)

  private val road2 = emptyRoad(id = 2, isJunction = false)
  private val road2lane1 = emptyLane(laneId = 1, road2)

  private val block = emptyBlock()
  private val block2 = emptyBlock()

  private val blocks = listOf(block, block2)

  private val egoId = 0
  private val otherId = 1

  @BeforeTest
  fun setup() {
    road0.lanes = listOf(road0lane1)
    road1.lanes = listOf(road1lane1)
    road2.lanes = listOf(road2lane1)

    road0lane1.yieldLanes = listOf(ContactLaneInfo(road1lane1))

    block.roads = listOf(road0, road1)
    block2.roads = listOf(road2)
  }

  @Test
  fun mustYieldCheckTest() {
    val tickData = emptyTickData(currentTick = TickDataUnitSeconds(0.0), blocks)
    val ego =
        emptyVehicle(
            id = egoId,
            egoVehicle = true,
            lane = road0lane1,
            effVelocityMPH = 11.0,
            positionOnLane = 0.0,
            tickData = tickData,
        )
    val other =
        emptyVehicle(
            id = otherId,
            egoVehicle = false,
            lane = road1lane1,
            effVelocityMPH = 11.0,
            positionOnLane = 0.0,
            tickData = tickData,
        )
    tickData.entities = listOf(ego, other)

    val segment = Segment(listOf(tickData), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(mustYield.holds(ctx, ego, other))
    assert(!mustYield.holds(ctx, other, ego))
  }

  @Test
  fun mustNotYield() {
    val tickData = emptyTickData(currentTick = TickDataUnitSeconds(0.0), blocks)
    val ego =
        emptyVehicle(
            id = egoId,
            egoVehicle = true,
            lane = road1lane1,
            effVelocityMPH = 11.0,
            positionOnLane = 0.0,
            tickData = tickData,
        )
    val other =
        emptyVehicle(
            id = otherId,
            egoVehicle = false,
            lane = road0lane1,
            effVelocityMPH = 11.0,
            positionOnLane = 0.0,
            tickData = tickData,
        )
    tickData.entities = listOf(ego, other)

    val segment = Segment(listOf(tickData), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(!mustYield.holds(ctx, ego, other))
    assert(mustYield.holds(ctx, other, ego))
  }

  @Test
  fun mustNotYieldOtherRoad() {
    val tickData = emptyTickData(currentTick = TickDataUnitSeconds(0.0), blocks)
    val ego =
        emptyVehicle(
            id = egoId,
            egoVehicle = true,
            lane = road0lane1,
            effVelocityMPH = 11.0,
            positionOnLane = 0.0,
            tickData = tickData,
        )
    val other =
        emptyVehicle(
            id = otherId,
            egoVehicle = false,
            lane = road2lane1,
            effVelocityMPH = 11.0,
            positionOnLane = 0.0,
            tickData = tickData,
        )
    tickData.entities = listOf(ego, other)

    val segment = Segment(listOf(tickData), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(!mustYield.holds(ctx, ego, other))
    assert(!mustYield.holds(ctx, other, ego))
  }
}
