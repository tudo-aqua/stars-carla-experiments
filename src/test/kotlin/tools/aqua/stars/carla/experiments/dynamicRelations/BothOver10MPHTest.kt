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
import tools.aqua.stars.carla.experiments.bothOver10MPH
import tools.aqua.stars.carla.experiments.emptyBlock
import tools.aqua.stars.carla.experiments.emptyLane
import tools.aqua.stars.carla.experiments.emptyRoad
import tools.aqua.stars.carla.experiments.emptyTickData
import tools.aqua.stars.carla.experiments.emptyVehicle
import tools.aqua.stars.core.evaluation.PredicateContext
import tools.aqua.stars.data.av.dataclasses.Segment
import tools.aqua.stars.data.av.dataclasses.TickDataUnitSeconds

class BothOver10MPHTest {

  private val road0 = emptyRoad(id = 0)
  private val road0lane1 = emptyLane(laneId = 1, road = road0, laneLength = 50.0)
  private val road0lane2 = emptyLane(laneId = 1, road = road0, laneLength = 50.0)

  private val road1 = emptyRoad(id = 1)
  private val road1lane1 = emptyLane(laneId = 1, road = road1, laneLength = 50.0)

  private val block = emptyBlock()

  private val egoId = 0
  private val otherId = 1

  @BeforeTest
  fun setup() {
    road0.lanes = listOf(road0lane1, road0lane2)
    road1.lanes = listOf(road1lane1)

    block.roads = listOf(road0, road1)
  }

  @Test
  fun bothOver10MPHSameLane() {
    val tickData = emptyTickData(currentTick = TickDataUnitSeconds(0.0), blocks = listOf(block))
    val ego =
        emptyVehicle(
            id = egoId,
            egoVehicle = true,
            lane = road0lane1,
            positionOnLane = 0.0,
            tickData = tickData,
            effVelocityMPH = 11.0)
    val other =
        emptyVehicle(
            id = otherId,
            egoVehicle = false,
            lane = road0lane1,
            positionOnLane = 0.0,
            tickData = tickData,
            effVelocityMPH = 11.0)
    tickData.entities = listOf(ego, other)

    val segment = Segment(listOf(tickData), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(bothOver10MPH.holds(ctx, ego, other))
    assert(bothOver10MPH.holds(ctx, other, ego))
  }

  @Test
  fun bothOver10MPHDifferentLane() {
    val tickData = emptyTickData(currentTick = TickDataUnitSeconds(0.0), blocks = listOf(block))
    val ego =
        emptyVehicle(
            id = egoId,
            egoVehicle = true,
            lane = road0lane1,
            positionOnLane = 0.0,
            tickData = tickData,
            effVelocityMPH = 11.0)
    val other =
        emptyVehicle(
            id = otherId,
            egoVehicle = false,
            lane = road0lane2,
            positionOnLane = 0.0,
            tickData = tickData,
            effVelocityMPH = 11.0)
    tickData.entities = listOf(ego, other)

    val segment = Segment(listOf(tickData), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(bothOver10MPH.holds(ctx, ego, other))
    assert(bothOver10MPH.holds(ctx, other, ego))
  }

  @Test
  fun bothOver10MPHDifferentRoad() {
    val tickData = emptyTickData(currentTick = TickDataUnitSeconds(0.0), blocks = listOf(block))
    val ego =
        emptyVehicle(
            id = egoId,
            egoVehicle = true,
            lane = road0lane1,
            positionOnLane = 0.0,
            tickData = tickData,
            effVelocityMPH = 11.0)
    val other =
        emptyVehicle(
            id = otherId,
            egoVehicle = false,
            lane = road1lane1,
            positionOnLane = 0.0,
            tickData = tickData,
            effVelocityMPH = 11.0)
    tickData.entities = listOf(ego, other)

    val segment = Segment(listOf(tickData), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(bothOver10MPH.holds(ctx, ego, other))
    assert(bothOver10MPH.holds(ctx, other, ego))
  }

  @Test
  fun egoUnder10MPHSameLane() {
    val tickData = emptyTickData(currentTick = TickDataUnitSeconds(0.0), blocks = listOf(block))
    val ego =
        emptyVehicle(
            id = egoId,
            egoVehicle = true,
            lane = road0lane1,
            positionOnLane = 0.0,
            tickData = tickData,
            effVelocityMPH = 9.0)
    val other =
        emptyVehicle(
            id = otherId,
            egoVehicle = false,
            lane = road0lane1,
            positionOnLane = 0.0,
            tickData = tickData,
            effVelocityMPH = 11.0)
    tickData.entities = listOf(ego, other)

    val segment = Segment(listOf(tickData), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(!bothOver10MPH.holds(ctx, ego, other))
    assert(!bothOver10MPH.holds(ctx, other, ego))
  }

  @Test
  fun egoUnder10MPHDifferentLane() {
    val tickData = emptyTickData(currentTick = TickDataUnitSeconds(0.0), blocks = listOf(block))
    val ego =
        emptyVehicle(
            id = egoId,
            egoVehicle = true,
            lane = road0lane1,
            positionOnLane = 0.0,
            tickData = tickData,
            effVelocityMPH = 9.0)
    val other =
        emptyVehicle(
            id = otherId,
            egoVehicle = false,
            lane = road0lane2,
            positionOnLane = 0.0,
            tickData = tickData,
            effVelocityMPH = 11.0)
    tickData.entities = listOf(ego, other)

    val segment = Segment(listOf(tickData), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(!bothOver10MPH.holds(ctx, ego, other))
    assert(!bothOver10MPH.holds(ctx, other, ego))
  }

  @Test
  fun egoUnder10MPHDifferentRoad() {
    val tickData = emptyTickData(currentTick = TickDataUnitSeconds(0.0), blocks = listOf(block))
    val ego =
        emptyVehicle(
            id = egoId,
            egoVehicle = true,
            lane = road0lane1,
            positionOnLane = 0.0,
            tickData = tickData,
            effVelocityMPH = 9.0)
    val other =
        emptyVehicle(
            id = otherId,
            egoVehicle = false,
            lane = road1lane1,
            positionOnLane = 0.0,
            tickData = tickData,
            effVelocityMPH = 11.0)
    tickData.entities = listOf(ego, other)

    val segment = Segment(listOf(tickData), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(!bothOver10MPH.holds(ctx, ego, other))
    assert(!bothOver10MPH.holds(ctx, other, ego))
  }

  @Test
  fun otherUnder10MPHSameLane() {
    val tickData = emptyTickData(currentTick = TickDataUnitSeconds(0.0), blocks = listOf(block))
    val ego =
        emptyVehicle(
            id = egoId,
            egoVehicle = true,
            lane = road0lane1,
            positionOnLane = 0.0,
            tickData = tickData,
            effVelocityMPH = 11.0)
    val other =
        emptyVehicle(
            id = otherId,
            egoVehicle = false,
            lane = road0lane1,
            positionOnLane = 0.0,
            tickData = tickData,
            effVelocityMPH = 9.0)
    tickData.entities = listOf(ego, other)

    val segment = Segment(listOf(tickData), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(!bothOver10MPH.holds(ctx, ego, other))
    assert(!bothOver10MPH.holds(ctx, other, ego))
  }

  @Test
  fun otherUnder10MPHDifferentLane() {
    val tickData = emptyTickData(currentTick = TickDataUnitSeconds(0.0), blocks = listOf(block))
    val ego =
        emptyVehicle(
            id = egoId,
            egoVehicle = true,
            lane = road0lane1,
            positionOnLane = 0.0,
            tickData = tickData,
            effVelocityMPH = 11.0)
    val other =
        emptyVehicle(
            id = otherId,
            egoVehicle = false,
            lane = road0lane2,
            positionOnLane = 0.0,
            tickData = tickData,
            effVelocityMPH = 9.0)
    tickData.entities = listOf(ego, other)

    val segment = Segment(listOf(tickData), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(!bothOver10MPH.holds(ctx, ego, other))
    assert(!bothOver10MPH.holds(ctx, other, ego))
  }

  @Test
  fun otherUnder10MPHDifferentRoad() {
    val tickData = emptyTickData(currentTick = TickDataUnitSeconds(0.0), blocks = listOf(block))
    val ego =
        emptyVehicle(
            id = egoId,
            egoVehicle = true,
            lane = road0lane1,
            positionOnLane = 0.0,
            tickData = tickData,
            effVelocityMPH = 11.0)
    val other =
        emptyVehicle(
            id = otherId,
            egoVehicle = false,
            lane = road1lane1,
            positionOnLane = 0.0,
            tickData = tickData,
            effVelocityMPH = 9.0)
    tickData.entities = listOf(ego, other)

    val segment = Segment(listOf(tickData), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(!bothOver10MPH.holds(ctx, ego, other))
    assert(!bothOver10MPH.holds(ctx, other, ego))
  }

  @Test
  fun bothUnder10MPHSameLane() {
    val tickData = emptyTickData(currentTick = TickDataUnitSeconds(0.0), blocks = listOf(block))
    val ego =
        emptyVehicle(
            id = egoId,
            egoVehicle = true,
            lane = road0lane1,
            positionOnLane = 0.0,
            tickData = tickData,
            effVelocityMPH = 9.0)
    val other =
        emptyVehicle(
            id = otherId,
            egoVehicle = false,
            lane = road0lane1,
            positionOnLane = 0.0,
            tickData = tickData,
            effVelocityMPH = 9.0)
    tickData.entities = listOf(ego, other)

    val segment = Segment(listOf(tickData), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(!bothOver10MPH.holds(ctx, ego, other))
    assert(!bothOver10MPH.holds(ctx, other, ego))
  }

  @Test
  fun bothUnder10MPHDifferentLane() {
    val tickData = emptyTickData(currentTick = TickDataUnitSeconds(0.0), blocks = listOf(block))
    val ego =
        emptyVehicle(
            id = egoId,
            egoVehicle = true,
            lane = road0lane1,
            positionOnLane = 0.0,
            tickData = tickData,
            effVelocityMPH = 9.0)
    val other =
        emptyVehicle(
            id = otherId,
            egoVehicle = false,
            lane = road0lane2,
            positionOnLane = 0.0,
            tickData = tickData,
            effVelocityMPH = 9.0)
    tickData.entities = listOf(ego, other)

    val segment = Segment(listOf(tickData), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(!bothOver10MPH.holds(ctx, ego, other))
    assert(!bothOver10MPH.holds(ctx, other, ego))
  }

  @Test
  fun bothUnder10MPHDifferentRoad() {
    val tickData = emptyTickData(currentTick = TickDataUnitSeconds(0.0), blocks = listOf(block))
    val ego =
        emptyVehicle(
            id = egoId,
            egoVehicle = true,
            lane = road0lane1,
            positionOnLane = 0.0,
            tickData = tickData,
            effVelocityMPH = 9.0)
    val other =
        emptyVehicle(
            id = otherId,
            egoVehicle = false,
            lane = road1lane1,
            positionOnLane = 0.0,
            tickData = tickData,
            effVelocityMPH = 9.0)
    tickData.entities = listOf(ego, other)

    val segment = Segment(listOf(tickData), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(!bothOver10MPH.holds(ctx, ego, other))
    assert(!bothOver10MPH.holds(ctx, other, ego))
  }

  @Test
  fun bothEqual10MPHSameLane() {
    val tickData = emptyTickData(currentTick = TickDataUnitSeconds(0.0), blocks = listOf(block))
    val ego =
        emptyVehicle(
            id = egoId,
            egoVehicle = true,
            lane = road0lane1,
            positionOnLane = 0.0,
            tickData = tickData,
            effVelocityMPH = 10.0)
    val other =
        emptyVehicle(
            id = otherId,
            egoVehicle = false,
            lane = road0lane1,
            positionOnLane = 0.0,
            tickData = tickData,
            effVelocityMPH = 10.0)
    tickData.entities = listOf(ego, other)

    val segment = Segment(listOf(tickData), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(!bothOver10MPH.holds(ctx, ego, other))
    assert(!bothOver10MPH.holds(ctx, other, ego))
  }

  @Test
  fun bothEqual10MPHDifferentLane() {
    val tickData = emptyTickData(currentTick = TickDataUnitSeconds(0.0), blocks = listOf(block))
    val ego =
        emptyVehicle(
            id = egoId,
            egoVehicle = true,
            lane = road0lane1,
            positionOnLane = 0.0,
            tickData = tickData,
            effVelocityMPH = 10.0)
    val other =
        emptyVehicle(
            id = otherId,
            egoVehicle = false,
            lane = road0lane2,
            positionOnLane = 0.0,
            tickData = tickData,
            effVelocityMPH = 10.0)
    tickData.entities = listOf(ego, other)

    val segment = Segment(listOf(tickData), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(!bothOver10MPH.holds(ctx, ego, other))
    assert(!bothOver10MPH.holds(ctx, other, ego))
  }

  @Test
  fun bothEqual10MPHDifferentRoad() {
    val tickData = emptyTickData(currentTick = TickDataUnitSeconds(0.0), blocks = listOf(block))
    val ego =
        emptyVehicle(
            id = egoId,
            egoVehicle = true,
            lane = road0lane1,
            positionOnLane = 0.0,
            tickData = tickData,
            effVelocityMPH = 10.0)
    val other =
        emptyVehicle(
            id = otherId,
            egoVehicle = false,
            lane = road1lane1,
            positionOnLane = 0.0,
            tickData = tickData,
            effVelocityMPH = 10.0)
    tickData.entities = listOf(ego, other)

    val segment = Segment(listOf(tickData), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(!bothOver10MPH.holds(ctx, ego, other))
    assert(!bothOver10MPH.holds(ctx, other, ego))
  }

  @Test
  fun egoEqual10MPHSameLane() {
    val tickData = emptyTickData(currentTick = TickDataUnitSeconds(0.0), blocks = listOf(block))
    val ego =
        emptyVehicle(
            id = egoId,
            egoVehicle = true,
            lane = road0lane1,
            positionOnLane = 0.0,
            tickData = tickData,
            effVelocityMPH = 10.0)
    val other =
        emptyVehicle(
            id = otherId,
            egoVehicle = false,
            lane = road0lane1,
            positionOnLane = 0.0,
            tickData = tickData,
            effVelocityMPH = 11.0)
    tickData.entities = listOf(ego, other)

    val segment = Segment(listOf(tickData), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(!bothOver10MPH.holds(ctx, ego, other))
    assert(!bothOver10MPH.holds(ctx, other, ego))
  }

  @Test
  fun egoEqual10MPHDifferentLane() {
    val tickData = emptyTickData(currentTick = TickDataUnitSeconds(0.0), blocks = listOf(block))
    val ego =
        emptyVehicle(
            id = egoId,
            egoVehicle = true,
            lane = road0lane1,
            positionOnLane = 0.0,
            tickData = tickData,
            effVelocityMPH = 10.0)
    val other =
        emptyVehicle(
            id = otherId,
            egoVehicle = false,
            lane = road0lane2,
            positionOnLane = 0.0,
            tickData = tickData,
            effVelocityMPH = 11.0)
    tickData.entities = listOf(ego, other)

    val segment = Segment(listOf(tickData), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(!bothOver10MPH.holds(ctx, ego, other))
    assert(!bothOver10MPH.holds(ctx, other, ego))
  }

  @Test
  fun egoEqual10MPHDifferentRoad() {
    val tickData = emptyTickData(currentTick = TickDataUnitSeconds(0.0), blocks = listOf(block))
    val ego =
        emptyVehicle(
            id = egoId,
            egoVehicle = true,
            lane = road0lane1,
            positionOnLane = 0.0,
            tickData = tickData,
            effVelocityMPH = 10.0)
    val other =
        emptyVehicle(
            id = otherId,
            egoVehicle = false,
            lane = road1lane1,
            positionOnLane = 0.0,
            tickData = tickData,
            effVelocityMPH = 11.0)
    tickData.entities = listOf(ego, other)

    val segment = Segment(listOf(tickData), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(!bothOver10MPH.holds(ctx, ego, other))
    assert(!bothOver10MPH.holds(ctx, other, ego))
  }

  @Test
  fun otherEqual10MPHSameLane() {
    val tickData = emptyTickData(currentTick = TickDataUnitSeconds(0.0), blocks = listOf(block))
    val ego =
        emptyVehicle(
            id = egoId,
            egoVehicle = true,
            lane = road0lane1,
            positionOnLane = 0.0,
            tickData = tickData,
            effVelocityMPH = 11.0)
    val other =
        emptyVehicle(
            id = otherId,
            egoVehicle = false,
            lane = road0lane1,
            positionOnLane = 0.0,
            tickData = tickData,
            effVelocityMPH = 10.0)
    tickData.entities = listOf(ego, other)

    val segment = Segment(listOf(tickData), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(!bothOver10MPH.holds(ctx, ego, other))
    assert(!bothOver10MPH.holds(ctx, other, ego))
  }

  @Test
  fun otherEqual10MPHDifferentLane() {
    val tickData = emptyTickData(currentTick = TickDataUnitSeconds(0.0), blocks = listOf(block))
    val ego =
        emptyVehicle(
            id = egoId,
            egoVehicle = true,
            lane = road0lane1,
            positionOnLane = 0.0,
            tickData = tickData,
            effVelocityMPH = 11.0)
    val other =
        emptyVehicle(
            id = otherId,
            egoVehicle = false,
            lane = road0lane2,
            positionOnLane = 0.0,
            tickData = tickData,
            effVelocityMPH = 10.0)
    tickData.entities = listOf(ego, other)

    val segment = Segment(listOf(tickData), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(!bothOver10MPH.holds(ctx, ego, other))
    assert(!bothOver10MPH.holds(ctx, other, ego))
  }

  @Test
  fun otherEqual10MPHDifferentRoad() {
    val tickData = emptyTickData(currentTick = TickDataUnitSeconds(0.0), blocks = listOf(block))
    val ego =
        emptyVehicle(
            id = egoId,
            egoVehicle = true,
            lane = road0lane1,
            positionOnLane = 0.0,
            tickData = tickData,
            effVelocityMPH = 11.0)
    val other =
        emptyVehicle(
            id = otherId,
            egoVehicle = false,
            lane = road1lane1,
            positionOnLane = 0.0,
            tickData = tickData,
            effVelocityMPH = 10.0)
    tickData.entities = listOf(ego, other)

    val segment = Segment(listOf(tickData), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(!bothOver10MPH.holds(ctx, ego, other))
    assert(!bothOver10MPH.holds(ctx, other, ego))
  }

  @Test
  fun testWithNoOtherVehicle() {
    val tickData = emptyTickData(currentTick = TickDataUnitSeconds(0.0), blocks = listOf(block))
    val ego =
        emptyVehicle(
            id = egoId,
            egoVehicle = true,
            lane = road0lane1,
            positionOnLane = 0.0,
            tickData = tickData,
            effVelocityMPH = 11.0)
    tickData.entities = listOf(ego)

    val segment = Segment(listOf(tickData), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(!bothOver10MPH.holds(ctx, TickDataUnitSeconds(0.0), egoId, 1))
  }

  @Test
  fun testWithItselfVehicle() {
    val tickData = emptyTickData(currentTick = TickDataUnitSeconds(0.0), blocks = listOf(block))
    val ego =
        emptyVehicle(
            id = egoId,
            egoVehicle = true,
            lane = road0lane1,
            positionOnLane = 0.0,
            tickData = tickData,
            effVelocityMPH = 11.0)
    tickData.entities = listOf(ego)

    val segment = Segment(listOf(tickData), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(!bothOver10MPH.holds(ctx, TickDataUnitSeconds(0.0), egoId, egoId))
  }
}
