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
import tools.aqua.stars.carla.experiments.oncoming
import tools.aqua.stars.core.evaluation.PredicateContext
import tools.aqua.stars.data.av.dataclasses.Block
import tools.aqua.stars.data.av.dataclasses.Lane
import tools.aqua.stars.data.av.dataclasses.Road
import tools.aqua.stars.data.av.dataclasses.Segment
import tools.aqua.stars.data.av.dataclasses.TickData
import tools.aqua.stars.data.av.dataclasses.TickDataUnitSeconds
import tools.aqua.stars.data.av.dataclasses.Vehicle

class OncomingTest {
  private lateinit var block0: Block

  private lateinit var road0: Road
  private lateinit var road0lane1: Lane
  private lateinit var road0lane2: Lane
  private lateinit var road0laneMinus1: Lane
  private lateinit var road0laneMinus2: Lane

  private lateinit var road1: Road
  private lateinit var road1lane1: Lane

  private lateinit var block2: Block

  private lateinit var road2: Road
  private lateinit var road2lane1: Lane

  private lateinit var blocks: List<Block>

  private val egoId = 0
  private val otherId = 1

  @BeforeTest
  fun setup() {
    road0lane1 = Lane(laneId = 1, laneLength = 50.0)
    road0lane2 = Lane(laneId = 2, laneLength = 50.0)
    road0laneMinus1 = Lane(laneId = -1, laneLength = 50.0)
    road0laneMinus2 = Lane(laneId = -2, laneLength = 50.0)

    road1lane1 = Lane(laneId = 1, laneLength = 50.0)
    road2lane1 = Lane(laneId = 1, laneLength = 50.0)

    road0 = Road(id = 0, lanes = listOf(road0lane1, road0lane2, road0laneMinus1, road0laneMinus2))
    road0lane1.road = road0
    road0lane2.road = road0
    road0laneMinus1.road = road0
    road0laneMinus2.road = road0

    road1 = Road(id = 1, lanes = listOf(road1lane1))
    road1lane1.road = road1

    block0 = Block(id = "1", roads = listOf(road0, road1))
    road0.block = block0
    road1.block = block0

    road2 = Road(id = 2, lanes = listOf(road2lane1))
    road2lane1.road = road2

    block2 = Block(id = "2", roads = listOf(road2))
    road2.block = block2

    blocks = listOf(block0, block2)
  }

  @Test
  fun egoAndOtherAreOncoming() {

    val ego = Vehicle(id = egoId, isEgo = true, lane = road0lane1, positionOnLane = 10.0)
    val other = Vehicle(id = otherId, isEgo = false, lane = road0laneMinus1, positionOnLane = 10.0)

    val tickData =
        TickData(
            currentTick = TickDataUnitSeconds(0.0), blocks = blocks, entities = listOf(ego, other))

    ego.tickData = tickData
    ego.setVelocityFromEffVelocityMPH(11.0)
    other.tickData = tickData
    other.setVelocityFromEffVelocityMPH(11.0)

    val ctx = PredicateContext(Segment(listOf(tickData), segmentSource = ""))
    assertTrue { oncoming.holds(ctx, ego, other) }
    assertTrue { oncoming.holds(ctx, other, ego) }
  }

  @Test
  fun egoAndOtherAreOncomingWithDifferentLaneIds() {
    val ego = Vehicle(id = egoId, isEgo = true, lane = road0lane1, positionOnLane = 10.0)
    val other = Vehicle(id = otherId, isEgo = false, lane = road0laneMinus2, positionOnLane = 10.0)

    val tickData =
        TickData(
            currentTick = TickDataUnitSeconds(0.0), blocks = blocks, entities = listOf(ego, other))
    ego.tickData = tickData
    ego.setVelocityFromEffVelocityMPH(11.0)
    other.tickData = tickData
    other.setVelocityFromEffVelocityMPH(11.0)

    val ctx = PredicateContext(Segment(listOf(tickData), segmentSource = ""))
    assertTrue { oncoming.holds(ctx, ego, other) }
    assertTrue { oncoming.holds(ctx, other, ego) }
  }

  @Test
  fun egoAndOtherAreOnSameDirection() {
    val ego = Vehicle(id = egoId, isEgo = true, lane = road0lane1, positionOnLane = 10.0)
    val other = Vehicle(id = otherId, isEgo = false, lane = road0lane2, positionOnLane = 10.0)

    val tickData =
        TickData(
            currentTick = TickDataUnitSeconds(0.0), blocks = blocks, entities = listOf(ego, other))
    ego.tickData = tickData
    ego.setVelocityFromEffVelocityMPH(11.0)
    other.tickData = tickData
    other.setVelocityFromEffVelocityMPH(11.0)

    val ctx = PredicateContext(Segment(listOf(tickData), segmentSource = ""))
    assertFalse { oncoming.holds(ctx, ego, other) }
    assertFalse { oncoming.holds(ctx, other, ego) }
  }

  @Test
  fun egoAndOtherAreOnDifferentRoads() {
    val ego = Vehicle(id = egoId, isEgo = true, lane = road0lane1, positionOnLane = 10.0)
    val other = Vehicle(id = otherId, isEgo = false, lane = road2lane1, positionOnLane = 10.0)

    val tickData =
        TickData(
            currentTick = TickDataUnitSeconds(0.0), blocks = blocks, entities = listOf(ego, other))
    ego.tickData = tickData
    ego.setVelocityFromEffVelocityMPH(11.0)
    other.tickData = tickData
    other.setVelocityFromEffVelocityMPH(11.0)

    val ctx = PredicateContext(Segment(listOf(tickData), segmentSource = ""))
    assertFalse { oncoming.holds(ctx, ego, other) }
    assertFalse { oncoming.holds(ctx, other, ego) }
  }

  @Test
  fun egoAndOtherAreOnSameLane() {
    val ego = Vehicle(id = egoId, isEgo = true, lane = road0lane1, positionOnLane = 10.0)
    val other = Vehicle(id = otherId, isEgo = false, lane = road0lane1, positionOnLane = 10.0)

    val tickData =
        TickData(
            currentTick = TickDataUnitSeconds(0.0), blocks = blocks, entities = listOf(ego, other))
    ego.tickData = tickData
    ego.setVelocityFromEffVelocityMPH(11.0)
    other.tickData = tickData
    other.setVelocityFromEffVelocityMPH(11.0)

    val ctx = PredicateContext(Segment(listOf(tickData), segmentSource = ""))
    assertFalse { oncoming.holds(ctx, ego, other) }
    assertFalse { oncoming.holds(ctx, other, ego) }
  }
}
