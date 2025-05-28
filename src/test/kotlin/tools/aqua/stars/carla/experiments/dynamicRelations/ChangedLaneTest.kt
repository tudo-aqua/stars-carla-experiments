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
import tools.aqua.stars.carla.experiments.changedLane
import tools.aqua.stars.core.evaluation.PredicateContext
import tools.aqua.stars.data.av.dataclasses.Block
import tools.aqua.stars.data.av.dataclasses.Lane
import tools.aqua.stars.data.av.dataclasses.Road
import tools.aqua.stars.data.av.dataclasses.Segment
import tools.aqua.stars.data.av.dataclasses.TickData
import tools.aqua.stars.data.av.dataclasses.TickDataUnitSeconds
import tools.aqua.stars.data.av.dataclasses.Vehicle

class ChangedLaneTest {
  private lateinit var block1: Block

  private lateinit var road0: Road
  private lateinit var road0lane1: Lane
  private lateinit var road0lane2: Lane

  private lateinit var road1: Road
  private lateinit var road1lane1: Lane

  private lateinit var block2: Block

  private lateinit var road2: Road
  private lateinit var road2lane1: Lane

  private lateinit var blocks: List<Block>

  private val egoId = 0

  @BeforeTest
  fun setup() {

    road0lane1 = Lane(laneId = 1, laneLength = 50.0)
    road0lane2 = Lane(laneId = 2, laneLength = 50.0)
    road1lane1 = Lane(laneId = 1, laneLength = 50.0)
    road2lane1 = Lane(laneId = 1, laneLength = 50.0)

    road0 = Road(id = 0, lanes = listOf(road0lane1, road0lane2))
    road1 = Road(id = 1, lanes = listOf(road1lane1))
    road2 = Road(id = 2, lanes = listOf(road2lane1))

    block1 = Block(roads = listOf(road0, road1))
    block2 = Block(roads = listOf(road2))

    road0.block = block1
    road1.block = block1
    road2.block = block2

    blocks = listOf(block1, block2)
  }

  @Test
  fun changedLane() {

    val ego0 = Vehicle(id = egoId, isEgo = true, lane = road0lane1, positionOnLane = 0.0)
    val tickData0 =
        TickData(currentTick = TickDataUnitSeconds(0.0), blocks = blocks, entities = listOf(ego0))
    ego0.tickData = tickData0
    ego0.setVelocityFromEffVelocityMPH(11.0)

    val ego1 = Vehicle(id = egoId, isEgo = true, lane = road0lane2, positionOnLane = 5.0)
    val tickData1 =
        TickData(currentTick = TickDataUnitSeconds(1.0), blocks = blocks, entities = listOf(ego1))
    ego1.tickData = tickData1
    ego1.setVelocityFromEffVelocityMPH(11.0)

    val segment = Segment(listOf(tickData0, tickData1), segmentSource = "")
    val ctx = PredicateContext(segment)

    assertTrue { changedLane.holds(ctx, TickDataUnitSeconds(0.0), egoId) }
  }

  @Test
  fun stayedOnLane() {

    val ego0 = Vehicle(id = egoId, isEgo = true, lane = road0lane1, positionOnLane = 0.0)
    val tickData0 =
        TickData(currentTick = TickDataUnitSeconds(0.0), blocks = blocks, entities = listOf(ego0))
    ego0.tickData = tickData0
    ego0.setVelocityFromEffVelocityMPH(11.0)

    val ego1 = Vehicle(id = egoId, isEgo = true, lane = road0lane1, positionOnLane = 5.0)
    val tickData1 =
        TickData(currentTick = TickDataUnitSeconds(1.0), blocks = blocks, entities = listOf(ego1))
    ego1.tickData = tickData1
    ego1.setVelocityFromEffVelocityMPH(11.0)

    val segment = Segment(listOf(tickData0, tickData1), segmentSource = "")
    val ctx = PredicateContext(segment)

    assertFalse { changedLane.holds(ctx, TickDataUnitSeconds(0.0), egoId) }
  }

  @Test
  fun changedRoadButToSameLaneId() {

    val ego0 = Vehicle(id = egoId, isEgo = true, lane = road0lane1, positionOnLane = 0.0)
    val tickData0 =
        TickData(currentTick = TickDataUnitSeconds(0.0), blocks = blocks, entities = listOf(ego0))
    ego0.tickData = tickData0
    ego0.setVelocityFromEffVelocityMPH(11.0)

    val ego1 = Vehicle(id = egoId, isEgo = true, lane = road1lane1, positionOnLane = 5.0)
    val tickData1 =
        TickData(currentTick = TickDataUnitSeconds(1.0), blocks = blocks, entities = listOf(ego1))
    ego1.tickData = tickData1
    ego1.setVelocityFromEffVelocityMPH(11.0)

    val segment = Segment(listOf(tickData0, tickData1), segmentSource = "")
    val ctx = PredicateContext(segment)

    assertFalse { changedLane.holds(ctx, TickDataUnitSeconds(0.0), egoId) }
  }

  @Test
  fun changedRoadButToDifferentLaneId() {

    val ego0 = Vehicle(id = egoId, isEgo = true, lane = road0lane2, positionOnLane = 0.0)
    val tickData0 =
        TickData(currentTick = TickDataUnitSeconds(0.0), blocks = blocks, entities = listOf(ego0))
    ego0.tickData = tickData0
    ego0.setVelocityFromEffVelocityMPH(11.0)

    val ego1 = Vehicle(id = egoId, isEgo = true, lane = road1lane1, positionOnLane = 5.0)
    val tickData1 =
        TickData(currentTick = TickDataUnitSeconds(1.0), blocks = blocks, entities = listOf(ego1))
    ego1.tickData = tickData1
    ego1.setVelocityFromEffVelocityMPH(11.0)

    val segment = Segment(listOf(tickData0, tickData1), segmentSource = "")
    val ctx = PredicateContext(segment)

    assertFalse { changedLane.holds(ctx, TickDataUnitSeconds(0.0), egoId) }
  }
}
