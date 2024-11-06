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

package tools.aqua.stars.carla.experiments.maneuver

import kotlin.test.BeforeTest
import kotlin.test.Test
import tools.aqua.stars.carla.experiments.emptyBlock
import tools.aqua.stars.carla.experiments.emptyLane
import tools.aqua.stars.carla.experiments.emptyRoad
import tools.aqua.stars.carla.experiments.emptyTickData
import tools.aqua.stars.carla.experiments.emptyVehicle
import tools.aqua.stars.carla.experiments.makesLeftTurn
import tools.aqua.stars.carla.experiments.makesNoTurn
import tools.aqua.stars.carla.experiments.makesRightTurn
import tools.aqua.stars.core.evaluation.PredicateContext
import tools.aqua.stars.data.av.dataclasses.LaneDirection
import tools.aqua.stars.data.av.dataclasses.Segment
import tools.aqua.stars.data.av.dataclasses.TickData
import tools.aqua.stars.data.av.dataclasses.TickDataUnitSeconds

class MakesTurnTest {

  private val road0 = emptyRoad(id = 0)
  private val laneStraight =
      emptyLane(laneId = 1, road = road0, laneDirection = LaneDirection.STRAIGHT)
  private val laneLeftTurn =
      emptyLane(laneId = 2, road = road0, laneDirection = LaneDirection.LEFT_TURN)
  private val laneRightTurn =
      emptyLane(laneId = 3, road = road0, laneDirection = LaneDirection.RIGHT_TURN)
  private val laneUnknown =
      emptyLane(laneId = 4, road = road0, laneDirection = LaneDirection.UNKNOWN)

  private val block = emptyBlock()

  private val blocks = listOf(block)

  private val egoId = 0

  @BeforeTest
  fun setup() {
    road0.lanes = listOf(laneLeftTurn, laneStraight, laneRightTurn, laneUnknown)

    block.roads = listOf(road0)
  }

  @Test
  fun makesLeftTurnTest() {
    val tickData = emptyTickData(currentTick = TickDataUnitSeconds(0.0), blocks)
    val ego =
        emptyVehicle(
            id = egoId,
            egoVehicle = true,
            lane = laneLeftTurn,
            effVelocityMPH = 11.0,
            positionOnLane = 0.0,
            tickData = tickData)
    tickData.entities = listOf(ego)

    val segment = Segment(listOf(tickData), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(makesLeftTurn.holds(ctx, ego))
    assert(!makesRightTurn.holds(ctx, ego))
    assert(!makesNoTurn.holds(ctx, ego))
  }

  @Test
  fun atLeast80TicksLeftTurn() {
    val tickDataList = mutableListOf<TickData>()
    for (i in 0..90) {
      val tickData = emptyTickData(currentTick = TickDataUnitSeconds(i.toDouble()), blocks)
      val ego =
          emptyVehicle(
              id = egoId,
              egoVehicle = true,
              lane = laneLeftTurn,
              tickData = tickData,
              positionOnLane = i.toDouble(),
              effVelocityMPH = 11.0)
      tickData.entities = listOf(ego)
      tickDataList += tickData
    }
    for (i in 91..100) {
      val tickData = emptyTickData(currentTick = TickDataUnitSeconds(i.toDouble()), blocks)
      val ego =
          emptyVehicle(
              id = egoId,
              egoVehicle = true,
              lane = laneStraight,
              tickData = tickData,
              positionOnLane = i.toDouble(),
              effVelocityMPH = 11.0)
      tickData.entities = listOf(ego)
      tickDataList += tickData
    }
    val segment = Segment(tickDataList, segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(makesLeftTurn.holds(ctx, TickDataUnitSeconds(0.0), egoId))
    assert(!makesRightTurn.holds(ctx, TickDataUnitSeconds(0.0), egoId))
    assert(!makesNoTurn.holds(ctx, TickDataUnitSeconds(0.0), egoId))
  }

  @Test
  fun under80TicksLeftTurn() {
    val tickDataList = mutableListOf<TickData>()
    for (i in 0..90) {
      val tickData = emptyTickData(currentTick = TickDataUnitSeconds(i.toDouble()), blocks)
      val ego =
          emptyVehicle(
              id = egoId,
              egoVehicle = true,
              lane = laneStraight,
              tickData = tickData,
              positionOnLane = i.toDouble(),
              effVelocityMPH = 11.0)
      tickData.entities = listOf(ego)
      tickDataList += tickData
    }
    for (i in 91..100) {
      val tickData = emptyTickData(currentTick = TickDataUnitSeconds(i.toDouble()), blocks)
      val ego =
          emptyVehicle(
              id = egoId,
              egoVehicle = true,
              lane = laneLeftTurn,
              tickData = tickData,
              positionOnLane = i.toDouble(),
              effVelocityMPH = 11.0)
      tickData.entities = listOf(ego)
      tickDataList += tickData
    }
    val segment = Segment(tickDataList, segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(!makesLeftTurn.holds(ctx, TickDataUnitSeconds(0.0), egoId))
    assert(!makesRightTurn.holds(ctx, TickDataUnitSeconds(0.0), egoId))
    assert(makesNoTurn.holds(ctx, TickDataUnitSeconds(0.0), egoId))
  }

  @Test
  fun makesRightTurnTest() {
    val tickData = emptyTickData(currentTick = TickDataUnitSeconds(0.0), blocks)
    val ego =
        emptyVehicle(
            id = egoId,
            egoVehicle = true,
            lane = laneRightTurn,
            effVelocityMPH = 11.0,
            positionOnLane = 0.0,
            tickData = tickData)
    tickData.entities = listOf(ego)

    val segment = Segment(listOf(tickData), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(!makesLeftTurn.holds(ctx, ego))
    assert(makesRightTurn.holds(ctx, ego))
    assert(!makesNoTurn.holds(ctx, ego))
  }

  @Test
  fun atLeast80TicksRightTurn() {
    val tickDataList = mutableListOf<TickData>()
    for (i in 0..90) {
      val tickData = emptyTickData(currentTick = TickDataUnitSeconds(i.toDouble()), blocks)
      val ego =
          emptyVehicle(
              id = egoId,
              egoVehicle = true,
              lane = laneRightTurn,
              tickData = tickData,
              positionOnLane = i.toDouble(),
              effVelocityMPH = 11.0)
      tickData.entities = listOf(ego)
      tickDataList += tickData
    }
    for (i in 91..100) {
      val tickData = emptyTickData(currentTick = TickDataUnitSeconds(i.toDouble()), blocks)
      val ego =
          emptyVehicle(
              id = egoId,
              egoVehicle = true,
              lane = laneStraight,
              tickData = tickData,
              positionOnLane = i.toDouble(),
              effVelocityMPH = 11.0)
      tickData.entities = listOf(ego)
      tickDataList += tickData
    }
    val segment = Segment(tickDataList, segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(!makesLeftTurn.holds(ctx, TickDataUnitSeconds(0.0), egoId))
    assert(makesRightTurn.holds(ctx, TickDataUnitSeconds(0.0), egoId))
    assert(!makesNoTurn.holds(ctx, TickDataUnitSeconds(0.0), egoId))
  }

  @Test
  fun under80TicksRightTurn() {
    val tickDataList = mutableListOf<TickData>()
    for (i in 0..90) {
      val tickData = emptyTickData(currentTick = TickDataUnitSeconds(i.toDouble()), blocks)
      val ego =
          emptyVehicle(
              id = egoId,
              egoVehicle = true,
              lane = laneStraight,
              tickData = tickData,
              positionOnLane = i.toDouble(),
              effVelocityMPH = 11.0)
      tickData.entities = listOf(ego)
      tickDataList += tickData
    }
    for (i in 91..100) {
      val tickData = emptyTickData(currentTick = TickDataUnitSeconds(i.toDouble()), blocks)
      val ego =
          emptyVehicle(
              id = egoId,
              egoVehicle = true,
              lane = laneRightTurn,
              tickData = tickData,
              positionOnLane = i.toDouble(),
              effVelocityMPH = 11.0)
      tickData.entities = listOf(ego)
      tickDataList += tickData
    }
    val segment = Segment(tickDataList, segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(!makesLeftTurn.holds(ctx, TickDataUnitSeconds(0.0), egoId))
    assert(!makesRightTurn.holds(ctx, TickDataUnitSeconds(0.0), egoId))
    assert(makesNoTurn.holds(ctx, TickDataUnitSeconds(0.0), egoId))
  }

  @Test
  fun drivesStraightTest() {
    val tickData = emptyTickData(currentTick = TickDataUnitSeconds(0.0), blocks)
    val ego =
        emptyVehicle(
            id = egoId,
            egoVehicle = true,
            lane = laneStraight,
            effVelocityMPH = 11.0,
            positionOnLane = 0.0,
            tickData = tickData)
    tickData.entities = listOf(ego)

    val segment = Segment(listOf(tickData), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(!makesLeftTurn.holds(ctx, ego))
    assert(!makesRightTurn.holds(ctx, ego))
    assert(makesNoTurn.holds(ctx, ego))
  }

  @Test
  fun atLeast80TicksStraight() {
    val tickDataList = mutableListOf<TickData>()
    for (i in 0..90) {
      val tickData = emptyTickData(currentTick = TickDataUnitSeconds(i.toDouble()), blocks)
      val ego =
          emptyVehicle(
              id = egoId,
              egoVehicle = true,
              lane = laneStraight,
              tickData = tickData,
              positionOnLane = i.toDouble(),
              effVelocityMPH = 11.0)
      tickData.entities = listOf(ego)
      tickDataList += tickData
    }
    for (i in 91..100) {
      val tickData = emptyTickData(currentTick = TickDataUnitSeconds(i.toDouble()), blocks)
      val ego =
          emptyVehicle(
              id = egoId,
              egoVehicle = true,
              lane = laneLeftTurn,
              tickData = tickData,
              positionOnLane = i.toDouble(),
              effVelocityMPH = 11.0)
      tickData.entities = listOf(ego)
      tickDataList += tickData
    }
    val segment = Segment(tickDataList, segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(!makesLeftTurn.holds(ctx, TickDataUnitSeconds(0.0), egoId))
    assert(!makesRightTurn.holds(ctx, TickDataUnitSeconds(0.0), egoId))
    assert(makesNoTurn.holds(ctx, TickDataUnitSeconds(0.0), egoId))
  }

  @Test
  fun under80TicksStraight() {
    val tickDataList = mutableListOf<TickData>()
    for (i in 0..90) {
      val tickData = emptyTickData(currentTick = TickDataUnitSeconds(i.toDouble()), blocks)
      val ego =
          emptyVehicle(
              id = egoId,
              egoVehicle = true,
              lane = laneLeftTurn,
              tickData = tickData,
              positionOnLane = i.toDouble(),
              effVelocityMPH = 11.0)
      tickData.entities = listOf(ego)
      tickDataList += tickData
    }
    for (i in 91..100) {
      val tickData = emptyTickData(currentTick = TickDataUnitSeconds(i.toDouble()), blocks)
      val ego =
          emptyVehicle(
              id = egoId,
              egoVehicle = true,
              lane = laneStraight,
              tickData = tickData,
              positionOnLane = i.toDouble(),
              effVelocityMPH = 11.0)
      tickData.entities = listOf(ego)
      tickDataList += tickData
    }
    val segment = Segment(tickDataList, segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(makesLeftTurn.holds(ctx, TickDataUnitSeconds(0.0), egoId))
    assert(!makesRightTurn.holds(ctx, TickDataUnitSeconds(0.0), egoId))
    assert(!makesNoTurn.holds(ctx, TickDataUnitSeconds(0.0), egoId))
  }

  @Test
  fun drivingDirectionUnknown() {
    val tickData = emptyTickData(currentTick = TickDataUnitSeconds(0.0), blocks)
    val ego =
        emptyVehicle(
            id = egoId,
            egoVehicle = true,
            lane = laneUnknown,
            effVelocityMPH = 11.0,
            positionOnLane = 0.0,
            tickData = tickData)
    tickData.entities = listOf(ego)

    val segment = Segment(listOf(tickData), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(!makesLeftTurn.holds(ctx, ego))
    assert(!makesRightTurn.holds(ctx, ego))
    assert(!makesNoTurn.holds(ctx, ego))
  }
}
