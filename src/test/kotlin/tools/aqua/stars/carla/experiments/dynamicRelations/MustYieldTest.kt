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
import tools.aqua.stars.carla.experiments.mustYield
import tools.aqua.stars.core.evaluation.PredicateContext
import tools.aqua.stars.data.av.dataclasses.Block
import tools.aqua.stars.data.av.dataclasses.ContactLaneInfo
import tools.aqua.stars.data.av.dataclasses.Lane
import tools.aqua.stars.data.av.dataclasses.Road
import tools.aqua.stars.data.av.dataclasses.Segment
import tools.aqua.stars.data.av.dataclasses.TickData
import tools.aqua.stars.data.av.dataclasses.TickDataUnitSeconds
import tools.aqua.stars.data.av.dataclasses.Vehicle

class MustYieldTest {
  private lateinit var road0: Road
  private lateinit var road1: Road
  private lateinit var road2: Road

  private lateinit var road0lane1: Lane
  private lateinit var road1lane1: Lane
  private lateinit var road2lane1: Lane

  private lateinit var block: Block
  private lateinit var block2: Block

  private lateinit var blocks: List<Block>

  private val egoId = 0
  private val otherId = 1

  @BeforeTest
  fun setup() {
    road0lane1 = Lane(laneId = 1)
    road1lane1 = Lane(laneId = 1)
    road2lane1 = Lane(laneId = 1)

    road0 = Road(id = 0, isJunction = true, lanes = listOf(road0lane1))
    road1 = Road(id = 1, isJunction = true, lanes = listOf(road1lane1))
    road2 = Road(id = 2, isJunction = false, lanes = listOf(road2lane1))

    road0lane1.road = road0
    road1lane1.road = road1
    road2lane1.road = road2

    road0lane1.yieldLanes = listOf(ContactLaneInfo(road1lane1))

    block = Block(roads = listOf(road0, road1))
    block2 = Block(roads = listOf(road2))

    blocks = listOf(block, block2)
  }

  @Test
  fun mustYieldCheckTest() {

    val ego = Vehicle(id = egoId, isEgo = true, lane = road0lane1, positionOnLane = 0.0)
    val other = Vehicle(id = otherId, isEgo = false, lane = road1lane1, positionOnLane = 0.0)

    val tickData =
        TickData(
            currentTick = TickDataUnitSeconds(0.0), blocks = blocks, entities = listOf(ego, other))

    ego.tickData = tickData
    ego.setVelocityFromEffVelocityMPH(11.0)
    other.tickData = tickData
    other.setVelocityFromEffVelocityMPH(11.0)

    val ctx = PredicateContext(Segment(listOf(tickData), segmentSource = ""))
    assertTrue { mustYield.holds(ctx, ego, other) }
    assertFalse { mustYield.holds(ctx, other, ego) }
  }

  @Test
  fun mustNotYield() {
    val ego = Vehicle(id = egoId, isEgo = true, lane = road1lane1, positionOnLane = 0.0)
    val other = Vehicle(id = otherId, isEgo = false, lane = road0lane1, positionOnLane = 0.0)

    val tickData =
        TickData(
            currentTick = TickDataUnitSeconds(0.0), blocks = blocks, entities = listOf(ego, other))
    ego.tickData = tickData
    ego.setVelocityFromEffVelocityMPH(11.0)
    other.tickData = tickData
    other.setVelocityFromEffVelocityMPH(11.0)

    val ctx = PredicateContext(Segment(listOf(tickData), segmentSource = ""))
    assertFalse { mustYield.holds(ctx, ego, other) }
    assertTrue { mustYield.holds(ctx, other, ego) }
  }

  @Test
  fun mustNotYieldOtherRoad() {
    val ego = Vehicle(id = egoId, isEgo = true, lane = road0lane1, positionOnLane = 0.0)
    val other = Vehicle(id = otherId, isEgo = false, lane = road2lane1, positionOnLane = 0.0)

    val tickData =
        TickData(
            currentTick = TickDataUnitSeconds(0.0), blocks = blocks, entities = listOf(ego, other))
    ego.tickData = tickData
    ego.setVelocityFromEffVelocityMPH(11.0)
    other.tickData = tickData
    other.setVelocityFromEffVelocityMPH(11.0)

    val ctx = PredicateContext(Segment(listOf(tickData), segmentSource = ""))
    assertFalse { mustYield.holds(ctx, ego, other) }
    assertFalse { mustYield.holds(ctx, other, ego) }
  }
}
