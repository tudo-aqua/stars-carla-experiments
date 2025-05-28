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

package tools.aqua.stars.carla.experiments.maneuver

import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import tools.aqua.stars.carla.experiments.makesLeftTurn
import tools.aqua.stars.carla.experiments.makesNoTurn
import tools.aqua.stars.carla.experiments.makesRightTurn
import tools.aqua.stars.core.evaluation.PredicateContext
import tools.aqua.stars.data.av.dataclasses.Block
import tools.aqua.stars.data.av.dataclasses.Lane
import tools.aqua.stars.data.av.dataclasses.LaneDirection
import tools.aqua.stars.data.av.dataclasses.Road
import tools.aqua.stars.data.av.dataclasses.Segment
import tools.aqua.stars.data.av.dataclasses.TickData
import tools.aqua.stars.data.av.dataclasses.TickDataUnitSeconds
import tools.aqua.stars.data.av.dataclasses.Vehicle

class MakesTurnTest {
  private lateinit var road0: Road

  private lateinit var laneStraight: Lane
  private lateinit var laneLeftTurn: Lane
  private lateinit var laneRightTurn: Lane
  private lateinit var laneUnknown: Lane

  private lateinit var block: Block

  private lateinit var blocks: List<Block>

  private val egoId = 0

  @BeforeTest
  fun setup() {
    laneStraight = Lane(laneId = 1, laneDirection = LaneDirection.STRAIGHT)
    laneLeftTurn = Lane(laneId = 2, laneDirection = LaneDirection.LEFT_TURN)
    laneRightTurn = Lane(laneId = 3, laneDirection = LaneDirection.RIGHT_TURN)
    laneUnknown = Lane(laneId = 4, laneDirection = LaneDirection.UNKNOWN)

    road0 =
        Road(id = 0, lanes = listOf(laneLeftTurn, laneStraight, laneRightTurn, laneUnknown)).apply {
          laneStraight.road = this
          laneLeftTurn.road = this
          laneRightTurn.road = this
          laneUnknown.road = this
        }

    block = Block(roads = listOf(road0))
    blocks = listOf(block)
  }

  @Test
  fun makesLeftTurnTest() {

    val ego = Vehicle(id = egoId, isEgo = true, lane = laneLeftTurn, positionOnLane = 0.0)

    // 2) build TickData with ego
    val td =
        TickData(currentTick = TickDataUnitSeconds(0.0), blocks = blocks, entities = listOf(ego))

    // 3) wire and set speed
    ego.tickData = td
    ego.setVelocityFromEffVelocityMPH(11.0)

    // 4) assert
    val ctx = PredicateContext(Segment(listOf(td), segmentSource = ""))
    assertTrue { makesLeftTurn.holds(ctx, ego) }
    assertFalse { makesRightTurn.holds(ctx, ego) }
    assertFalse { makesNoTurn.holds(ctx, ego) }
  }

  @Test
  fun atLeast80TicksLeftTurn() {
    val tds = mutableListOf<TickData>()

    // first 0–90 ticks on left-turn lane
    for (i in 0..90) {
      val ego =
          Vehicle(id = egoId, isEgo = true, lane = laneLeftTurn, positionOnLane = i.toDouble())
      val td =
          TickData(
              currentTick = TickDataUnitSeconds(i.toDouble()),
              blocks = blocks,
              entities = listOf(ego))
      ego.tickData = td
      ego.setVelocityFromEffVelocityMPH(11.0)
      tds += td
    }

    // ticks 91–100 on straight lane
    for (i in 91..100) {
      val ego =
          Vehicle(id = egoId, isEgo = true, lane = laneStraight, positionOnLane = i.toDouble())
      val td =
          TickData(
              currentTick = TickDataUnitSeconds(i.toDouble()),
              blocks = blocks,
              entities = listOf(ego))
      ego.tickData = td
      ego.setVelocityFromEffVelocityMPH(11.0)
      tds += td
    }

    val ctx = PredicateContext(Segment(tds, segmentSource = ""))
    assertTrue { makesLeftTurn.holds(ctx, TickDataUnitSeconds(0.0), egoId) }
    assertFalse { makesRightTurn.holds(ctx, TickDataUnitSeconds(0.0), egoId) }
    assertFalse { makesNoTurn.holds(ctx, TickDataUnitSeconds(0.0), egoId) }
  }

  @Test
  fun under80TicksLeftTurn() {
    val tds = mutableListOf<TickData>()

    // 0–90 ticks all on straight, then 91–100 on left-turn
    for (i in 0..90) {
      val ego =
          Vehicle(id = egoId, isEgo = true, lane = laneStraight, positionOnLane = i.toDouble())
      val td =
          TickData(
              currentTick = TickDataUnitSeconds(i.toDouble()),
              blocks = blocks,
              entities = listOf(ego))
      ego.tickData = td
      ego.setVelocityFromEffVelocityMPH(11.0)
      tds += td
    }
    for (i in 91..100) {
      val ego =
          Vehicle(id = egoId, isEgo = true, lane = laneLeftTurn, positionOnLane = i.toDouble())
      val td =
          TickData(
              currentTick = TickDataUnitSeconds(i.toDouble()),
              blocks = blocks,
              entities = listOf(ego))
      ego.tickData = td
      ego.setVelocityFromEffVelocityMPH(11.0)
      tds += td
    }

    val ctx = PredicateContext(Segment(tds, segmentSource = ""))
    assertFalse { makesLeftTurn.holds(ctx, TickDataUnitSeconds(0.0), egoId) }
    assertFalse { makesRightTurn.holds(ctx, TickDataUnitSeconds(0.0), egoId) }
    assertTrue { makesNoTurn.holds(ctx, TickDataUnitSeconds(0.0), egoId) }
  }

  @Test
  fun makesRightTurnTest() {
    val ego = Vehicle(id = egoId, isEgo = true, lane = laneRightTurn, positionOnLane = 0.0)
    val td =
        TickData(currentTick = TickDataUnitSeconds(0.0), blocks = blocks, entities = listOf(ego))
    ego.tickData = td
    ego.setVelocityFromEffVelocityMPH(11.0)

    val ctx = PredicateContext(Segment(listOf(td), segmentSource = ""))
    assertFalse { makesLeftTurn.holds(ctx, ego) }
    assertTrue { makesRightTurn.holds(ctx, ego) }
    assertFalse { makesNoTurn.holds(ctx, ego) }
  }

  @Test
  fun atLeast80TicksRightTurn() {
    val tds = mutableListOf<TickData>()

    // 0–90 ticks on right-turn lane
    for (i in 0..90) {
      val ego =
          Vehicle(id = egoId, isEgo = true, lane = laneRightTurn, positionOnLane = i.toDouble())
      val td =
          TickData(
              currentTick = TickDataUnitSeconds(i.toDouble()),
              blocks = blocks,
              entities = listOf(ego))
      ego.tickData = td
      ego.setVelocityFromEffVelocityMPH(11.0)
      tds += td
    }

    // 91–100 on straight
    for (i in 91..100) {
      val ego =
          Vehicle(id = egoId, isEgo = true, lane = laneStraight, positionOnLane = i.toDouble())
      val td =
          TickData(
              currentTick = TickDataUnitSeconds(i.toDouble()),
              blocks = blocks,
              entities = listOf(ego))
      ego.tickData = td
      ego.setVelocityFromEffVelocityMPH(11.0)
      tds += td
    }

    val ctx = PredicateContext(Segment(tds, segmentSource = ""))
    assertFalse { makesLeftTurn.holds(ctx, TickDataUnitSeconds(0.0), egoId) }
    assertTrue { makesRightTurn.holds(ctx, TickDataUnitSeconds(0.0), egoId) }
    assertFalse { makesNoTurn.holds(ctx, TickDataUnitSeconds(0.0), egoId) }
  }

  @Test
  fun under80TicksRightTurn() {
    val tds = mutableListOf<TickData>()

    // 0–90 straight
    for (i in 0..90) {
      val ego =
          Vehicle(id = egoId, isEgo = true, lane = laneStraight, positionOnLane = i.toDouble())
      val td =
          TickData(
              currentTick = TickDataUnitSeconds(i.toDouble()),
              blocks = blocks,
              entities = listOf(ego))
      ego.tickData = td
      ego.setVelocityFromEffVelocityMPH(11.0)
      tds += td
    }

    // 91–100 right-turn
    for (i in 91..100) {
      val ego =
          Vehicle(id = egoId, isEgo = true, lane = laneRightTurn, positionOnLane = i.toDouble())
      val td =
          TickData(
              currentTick = TickDataUnitSeconds(i.toDouble()),
              blocks = blocks,
              entities = listOf(ego))
      ego.tickData = td
      ego.setVelocityFromEffVelocityMPH(11.0)
      tds += td
    }

    val ctx = PredicateContext(Segment(tds, segmentSource = ""))
    assertFalse { makesLeftTurn.holds(ctx, TickDataUnitSeconds(0.0), egoId) }
    assertFalse { makesRightTurn.holds(ctx, TickDataUnitSeconds(0.0), egoId) }
    assertTrue { makesNoTurn.holds(ctx, TickDataUnitSeconds(0.0), egoId) }
  }

  @Test
  fun drivesStraightTest() {
    val ego = Vehicle(id = egoId, isEgo = true, lane = laneStraight, positionOnLane = 0.0)
    val td =
        TickData(currentTick = TickDataUnitSeconds(0.0), blocks = blocks, entities = listOf(ego))
    ego.tickData = td
    ego.setVelocityFromEffVelocityMPH(11.0)

    val ctx = PredicateContext(Segment(listOf(td), segmentSource = ""))
    assertFalse { makesLeftTurn.holds(ctx, ego) }
    assertFalse { makesRightTurn.holds(ctx, ego) }
    assertTrue { makesNoTurn.holds(ctx, ego) }
  }

  @Test
  fun atLeast80TicksStraight() {
    val tds = mutableListOf<TickData>()

    // 0–90 straight
    for (i in 0..90) {
      val ego =
          Vehicle(id = egoId, isEgo = true, lane = laneStraight, positionOnLane = i.toDouble())
      val td =
          TickData(
              currentTick = TickDataUnitSeconds(i.toDouble()),
              blocks = blocks,
              entities = listOf(ego))
      ego.tickData = td
      ego.setVelocityFromEffVelocityMPH(11.0)
      tds += td
    }

    // 91–100 left-turn
    for (i in 91..100) {
      val ego =
          Vehicle(id = egoId, isEgo = true, lane = laneLeftTurn, positionOnLane = i.toDouble())
      val td =
          TickData(
              currentTick = TickDataUnitSeconds(i.toDouble()),
              blocks = blocks,
              entities = listOf(ego))
      ego.tickData = td
      ego.setVelocityFromEffVelocityMPH(11.0)
      tds += td
    }

    val ctx = PredicateContext(Segment(tds, segmentSource = ""))
    assertFalse { makesLeftTurn.holds(ctx, TickDataUnitSeconds(0.0), egoId) }
    assertFalse { makesRightTurn.holds(ctx, TickDataUnitSeconds(0.0), egoId) }
    assertTrue { makesNoTurn.holds(ctx, TickDataUnitSeconds(0.0), egoId) }
  }

  @Test
  fun under80TicksStraight() {
    val tds = mutableListOf<TickData>()

    // 0–90 left-turn
    for (i in 0..90) {
      val ego =
          Vehicle(id = egoId, isEgo = true, lane = laneLeftTurn, positionOnLane = i.toDouble())
      val td =
          TickData(
              currentTick = TickDataUnitSeconds(i.toDouble()),
              blocks = blocks,
              entities = listOf(ego))
      ego.tickData = td
      ego.setVelocityFromEffVelocityMPH(11.0)
      tds += td
    }

    // 91–100 straight
    for (i in 91..100) {
      val ego =
          Vehicle(id = egoId, isEgo = true, lane = laneStraight, positionOnLane = i.toDouble())
      val td =
          TickData(
              currentTick = TickDataUnitSeconds(i.toDouble()),
              blocks = blocks,
              entities = listOf(ego))
      ego.tickData = td
      ego.setVelocityFromEffVelocityMPH(11.0)
      tds += td
    }

    val ctx = PredicateContext(Segment(tds, segmentSource = ""))
    assertTrue { makesLeftTurn.holds(ctx, TickDataUnitSeconds(0.0), egoId) }
    assertFalse { makesRightTurn.holds(ctx, TickDataUnitSeconds(0.0), egoId) }
    assertFalse { makesNoTurn.holds(ctx, TickDataUnitSeconds(0.0), egoId) }
  }

  @Test
  fun drivingDirectionUnknown() {
    val ego = Vehicle(id = egoId, isEgo = true, lane = laneUnknown, positionOnLane = 0.0)
    val td =
        TickData(currentTick = TickDataUnitSeconds(0.0), blocks = blocks, entities = listOf(ego))
    ego.tickData = td
    ego.setVelocityFromEffVelocityMPH(11.0)

    val ctx = PredicateContext(Segment(listOf(td), segmentSource = ""))
    assertFalse { makesLeftTurn.holds(ctx, ego) }
    assertFalse { makesRightTurn.holds(ctx, ego) }
    assertFalse { makesNoTurn.holds(ctx, ego) }
  }
}
