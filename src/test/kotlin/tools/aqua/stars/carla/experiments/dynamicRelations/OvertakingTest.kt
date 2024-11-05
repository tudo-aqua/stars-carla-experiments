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
import tools.aqua.stars.carla.experiments.besides
import tools.aqua.stars.carla.experiments.bothOver10MPH
import tools.aqua.stars.carla.experiments.emptyBlock
import tools.aqua.stars.carla.experiments.emptyLane
import tools.aqua.stars.carla.experiments.emptyRoad
import tools.aqua.stars.carla.experiments.emptyTickData
import tools.aqua.stars.carla.experiments.emptyVehicle
import tools.aqua.stars.carla.experiments.hasOvertaken
import tools.aqua.stars.carla.experiments.isBehind
import tools.aqua.stars.carla.experiments.overtaking
import tools.aqua.stars.core.evaluation.PredicateContext
import tools.aqua.stars.data.av.dataclasses.Segment
import tools.aqua.stars.data.av.dataclasses.TickDataUnitSeconds

class OvertakingTest {

  private val road0 = emptyRoad(id = 0)
  private val road0lane1 = emptyLane(laneId = 1, road = road0, laneLength = 50.0)
  private val road0lane2 = emptyLane(laneId = 2, road = road0, laneLength = 50.0)
  private val road0lane3 = emptyLane(laneId = 3, road = road0, laneLength = 50.0)
  private val road0laneMinus1 = emptyLane(laneId = -1, road = road0, laneLength = 50.0)

  private val road1 = emptyRoad(id = 1)
  private val road1lane1 = emptyLane(laneId = 1, road = road1, laneLength = 50.0)

  private val block = emptyBlock()

  private val egoId = 0
  private val otherId = 1

  @BeforeTest
  fun setup() {
    road0.lanes = listOf(road0lane1, road0lane2, road0lane3, road0laneMinus1)
    road1.lanes = listOf(road1lane1)

    block.roads = listOf(road0, road1)
  }

  @Test
  fun behindBesidesInFront() {
    // Behind
    val tickData0 = emptyTickData(currentTick = TickDataUnitSeconds(0.0), blocks = listOf(block))
    val ego0 =
        emptyVehicle(
            id = egoId,
            egoVehicle = true,
            lane = road0lane2,
            positionOnLane = 0.0,
            tickData = tickData0,
            effVelocityMPH = 20.0)
    val other0 =
        emptyVehicle(
            id = otherId,
            egoVehicle = false,
            lane = road0lane2,
            positionOnLane = 5.0,
            tickData = tickData0,
            effVelocityMPH = 11.0)
    tickData0.entities = listOf(ego0, other0)

    val segment0 = Segment(listOf(tickData0), segmentSource = "")
    val ctx0 = PredicateContext(segment0)

    assert(isBehind.holds(ctx0, ego0, other0))
    assert(!isBehind.holds(ctx0, other0, ego0))

    assert(!besides.holds(ctx0, ego0, other0))
    assert(!besides.holds(ctx0, other0, ego0))

    assert(bothOver10MPH.holds(ctx0, ego0, other0))
    assert(bothOver10MPH.holds(ctx0, other0, ego0))

    // Left of
    val tickData1 = emptyTickData(currentTick = TickDataUnitSeconds(1.0), blocks = listOf(block))
    val ego1 =
        emptyVehicle(
            id = egoId,
            egoVehicle = true,
            lane = road0lane1,
            positionOnLane = 7.0,
            tickData = tickData1,
            effVelocityMPH = 30.0)
    val other1 =
        emptyVehicle(
            id = otherId,
            egoVehicle = false,
            lane = road0lane2,
            positionOnLane = 7.0,
            tickData = tickData1,
            effVelocityMPH = 11.0)
    tickData1.entities = listOf(ego1, other1)

    val segment1 = Segment(listOf(tickData1), segmentSource = "")
    val ctx1 = PredicateContext(segment1)

    assert(besides.holds(ctx1, ego1, other1))
    assert(besides.holds(ctx1, other1, ego1))

    assert(!isBehind.holds(ctx1, ego1, other1))
    assert(!isBehind.holds(ctx1, other1, ego1))

    assert(bothOver10MPH.holds(ctx1, ego1, other1))
    assert(bothOver10MPH.holds(ctx1, other1, ego1))

    // In front of
    val tickData2 = emptyTickData(currentTick = TickDataUnitSeconds(2.0), blocks = listOf(block))
    val ego2 =
        emptyVehicle(
            id = egoId,
            egoVehicle = true,
            lane = road0lane2,
            positionOnLane = 20.0,
            tickData = tickData2,
            effVelocityMPH = 30.0)
    val other2 =
        emptyVehicle(
            id = otherId,
            egoVehicle = false,
            lane = road0lane2,
            positionOnLane = 10.0,
            tickData = tickData2,
            effVelocityMPH = 11.0)
    tickData2.entities = listOf(ego2, other2)

    val segment2 = Segment(listOf(tickData2), segmentSource = "")
    val ctx2 = PredicateContext(segment2)

    assert(isBehind.holds(ctx2, other2, ego2))
    assert(!isBehind.holds(ctx2, ego2, other2))

    assert(!besides.holds(ctx2, ego2, other2))
    assert(!besides.holds(ctx2, other2, ego2))

    assert(bothOver10MPH.holds(ctx2, ego2, other2))
    assert(bothOver10MPH.holds(ctx2, other2, ego2))

    val segment = Segment(listOf(tickData0, tickData1, tickData2), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(overtaking.holds(ctx, TickDataUnitSeconds(0.0), egoId, otherId))
    assert(hasOvertaken.holds(ctx, TickDataUnitSeconds(0.0), egoId))
  }

  @Test
  fun behindBesidesBehindBesidesInFront() {
    // behind
    val tickData0 = emptyTickData(currentTick = TickDataUnitSeconds(0.0), blocks = listOf(block))
    val ego0 =
        emptyVehicle(
            id = egoId,
            egoVehicle = true,
            lane = road0lane2,
            tickData = tickData0,
            positionOnLane = 0.0,
            effVelocityMPH = 30.0)
    val other0 =
        emptyVehicle(
            id = otherId,
            egoVehicle = false,
            lane = road0lane2,
            tickData = tickData0,
            positionOnLane = 5.0,
            effVelocityMPH = 11.0)
    tickData0.entities = listOf(ego0, other0)

    val segment0 = Segment(listOf(tickData0), segmentSource = "")
    val ctx0 = PredicateContext(segment0)

    assert(isBehind.holds(ctx0, ego0, other0))
    assert(!isBehind.holds(ctx0, other0, ego0))

    assert(!besides.holds(ctx0, ego0, other0))
    assert(!besides.holds(ctx0, other0, ego0))

    assert(bothOver10MPH.holds(ctx0, ego0, other0))
    assert(bothOver10MPH.holds(ctx0, other0, ego0))

    // besides
    val tickData1 = emptyTickData(currentTick = TickDataUnitSeconds(1.0), blocks = listOf(block))
    val ego1 =
        emptyVehicle(
            id = egoId,
            egoVehicle = true,
            lane = road0lane1,
            tickData = tickData1,
            positionOnLane = 7.0,
            effVelocityMPH = 50.0)
    val other1 =
        emptyVehicle(
            id = otherId,
            egoVehicle = false,
            lane = road0lane2,
            tickData = tickData1,
            positionOnLane = 7.0,
            effVelocityMPH = 11.0)
    tickData1.entities = listOf(ego1, other1)

    val segment1 = Segment(listOf(tickData1), segmentSource = "")
    val ctx1 = PredicateContext(segment1)

    assert(besides.holds(ctx1, ego1, other1))
    assert(besides.holds(ctx1, other1, ego1))

    assert(!isBehind.holds(ctx1, ego1, other1))
    assert(!isBehind.holds(ctx1, other1, ego1))

    assert(bothOver10MPH.holds(ctx1, ego1, other1))
    assert(bothOver10MPH.holds(ctx1, other1, ego1))

    // behind
    val tickData2 = emptyTickData(currentTick = TickDataUnitSeconds(2.0), blocks = listOf(block))
    val ego2 =
        emptyVehicle(
            id = egoId,
            egoVehicle = true,
            lane = road0lane1,
            tickData = tickData2,
            positionOnLane = 8.0,
            effVelocityMPH = 11.0)
    val other2 =
        emptyVehicle(
            id = otherId,
            egoVehicle = false,
            lane = road0lane2,
            tickData = tickData2,
            positionOnLane = 12.0,
            effVelocityMPH = 11.0)
    tickData2.entities = listOf(ego2, other2)

    val segment2 = Segment(listOf(tickData2), segmentSource = "")
    val ctx2 = PredicateContext(segment2)

    assert(isBehind.holds(ctx2, ego2, other2))
    assert(!isBehind.holds(ctx2, other2, ego2))

    assert(!besides.holds(ctx2, ego2, other2))
    assert(!besides.holds(ctx2, other2, ego2))

    assert(bothOver10MPH.holds(ctx2, ego2, other2))
    assert(bothOver10MPH.holds(ctx2, other2, ego2))

    // besides
    val tickData3 = emptyTickData(currentTick = TickDataUnitSeconds(3.0), blocks = listOf(block))
    val ego3 =
        emptyVehicle(
            id = egoId,
            egoVehicle = true,
            lane = road0lane1,
            tickData = tickData3,
            positionOnLane = 15.0,
            effVelocityMPH = 30.0)
    val other3 =
        emptyVehicle(
            id = otherId,
            egoVehicle = false,
            lane = road0lane2,
            tickData = tickData3,
            positionOnLane = 15.0,
            effVelocityMPH = 11.0)
    tickData3.entities = listOf(ego3, other3)

    val segment3 = Segment(listOf(tickData3), segmentSource = "")
    val ctx3 = PredicateContext(segment3)

    assert(besides.holds(ctx3, ego3, other3))
    assert(besides.holds(ctx3, other3, ego3))

    assert(!isBehind.holds(ctx3, ego3, other3))
    assert(!isBehind.holds(ctx3, other3, ego3))

    assert(bothOver10MPH.holds(ctx3, ego3, other3))
    assert(bothOver10MPH.holds(ctx3, other3, ego3))

    // in front
    val tickData4 = emptyTickData(currentTick = TickDataUnitSeconds(4.0), blocks = listOf(block))
    val ego4 =
        emptyVehicle(
            id = egoId,
            egoVehicle = true,
            lane = road0lane1,
            tickData = tickData4,
            positionOnLane = 30.0,
            effVelocityMPH = 60.0)
    val other4 =
        emptyVehicle(
            id = otherId,
            egoVehicle = false,
            lane = road0lane2,
            tickData = tickData4,
            positionOnLane = 18.0,
            effVelocityMPH = 11.0)
    tickData4.entities = listOf(ego4, other4)

    val segment4 = Segment(listOf(tickData4), segmentSource = "")
    val ctx4 = PredicateContext(segment4)

    assert(isBehind.holds(ctx4, other4, ego4))
    assert(!isBehind.holds(ctx4, ego4, other4))

    assert(!besides.holds(ctx4, ego4, other4))
    assert(!besides.holds(ctx4, other4, ego4))

    assert(bothOver10MPH.holds(ctx4, ego4, other4))
    assert(bothOver10MPH.holds(ctx4, other4, ego4))

    val segment =
        Segment(listOf(tickData0, tickData1, tickData2, tickData3, tickData4), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(overtaking.holds(ctx, TickDataUnitSeconds(0.0), egoId, otherId))
    assert(hasOvertaken.holds(ctx, TickDataUnitSeconds(0.0), egoId))
  }

  @Test
  fun behindBesidesBehind() {
    // behind
    val tickData0 = emptyTickData(currentTick = TickDataUnitSeconds(0.0), blocks = listOf(block))
    val ego0 =
        emptyVehicle(
            id = egoId,
            egoVehicle = true,
            lane = road0lane2,
            tickData = tickData0,
            positionOnLane = 0.0,
            effVelocityMPH = 30.0)
    val other0 =
        emptyVehicle(
            id = otherId,
            egoVehicle = false,
            lane = road0lane2,
            tickData = tickData0,
            positionOnLane = 5.0,
            effVelocityMPH = 30.0)
    tickData0.entities = listOf(ego0, other0)

    val segment0 = Segment(listOf(tickData0), segmentSource = "")
    val ctx0 = PredicateContext(segment0)

    assert(isBehind.holds(ctx0, ego0, other0))
    assert(!isBehind.holds(ctx0, other0, ego0))

    assert(!besides.holds(ctx0, ego0, other0))
    assert(!besides.holds(ctx0, other0, ego0))

    assert(bothOver10MPH.holds(ctx0, ego0, other0))
    assert(bothOver10MPH.holds(ctx0, other0, ego0))

    // besides
    val tickData1 = emptyTickData(currentTick = TickDataUnitSeconds(1.0), blocks = listOf(block))
    val ego1 =
        emptyVehicle(
            id = egoId,
            egoVehicle = true,
            lane = road0lane1,
            tickData = tickData1,
            positionOnLane = 7.0,
            effVelocityMPH = 60.0)
    val other1 =
        emptyVehicle(
            id = otherId,
            egoVehicle = false,
            lane = road0lane2,
            tickData = tickData1,
            positionOnLane = 7.0,
            effVelocityMPH = 30.0)
    tickData1.entities = listOf(ego1, other1)

    val segment1 = Segment(listOf(tickData1), segmentSource = "")
    val ctx1 = PredicateContext(segment1)

    assert(besides.holds(ctx1, ego1, other1))
    assert(besides.holds(ctx1, other1, ego1))

    assert(!isBehind.holds(ctx1, ego1, other1))
    assert(!isBehind.holds(ctx1, other1, ego1))

    assert(bothOver10MPH.holds(ctx1, ego1, other1))
    assert(bothOver10MPH.holds(ctx1, other1, ego1))

    // behind
    val tickData2 = emptyTickData(currentTick = TickDataUnitSeconds(2.0), blocks = listOf(block))
    val ego2 =
        emptyVehicle(
            id = egoId,
            egoVehicle = true,
            lane = road0lane1,
            tickData = tickData2,
            positionOnLane = 8.0,
            effVelocityMPH = 30.0)
    val other2 =
        emptyVehicle(
            id = otherId,
            egoVehicle = false,
            lane = road0lane2,
            tickData = tickData2,
            positionOnLane = 14.0,
            effVelocityMPH = 60.0)
    tickData2.entities = listOf(ego2, other2)

    val segment2 = Segment(listOf(tickData2), segmentSource = "")
    val ctx2 = PredicateContext(segment2)

    assert(isBehind.holds(ctx2, ego2, other2))
    assert(!isBehind.holds(ctx2, other2, ego2))

    assert(!besides.holds(ctx2, ego2, other2))
    assert(!besides.holds(ctx2, other2, ego2))

    assert(bothOver10MPH.holds(ctx2, ego2, other2))
    assert(bothOver10MPH.holds(ctx2, other2, ego2))

    // full check
    val segment = Segment(listOf(tickData0, tickData1, tickData2), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(!overtaking.holds(ctx, TickDataUnitSeconds(0.0), egoId, otherId))
    assert(!hasOvertaken.holds(ctx, TickDataUnitSeconds(0.0), egoId))
    assert(!hasOvertaken.holds(ctx, TickDataUnitSeconds(0.0), otherId))
  }

  @Test
  fun behindBesides() {
    // behind
    val tickData0 = emptyTickData(currentTick = TickDataUnitSeconds(0.0), blocks = listOf(block))
    val ego0 =
        emptyVehicle(
            id = egoId,
            egoVehicle = true,
            lane = road0lane2,
            tickData = tickData0,
            positionOnLane = 0.0,
            effVelocityMPH = 30.0)
    val other0 =
        emptyVehicle(
            id = otherId,
            egoVehicle = false,
            lane = road0lane2,
            tickData = tickData0,
            positionOnLane = 5.0,
            effVelocityMPH = 30.0)
    tickData0.entities = listOf(ego0, other0)

    val segment0 = Segment(listOf(tickData0), segmentSource = "")
    val ctx0 = PredicateContext(segment0)

    assert(isBehind.holds(ctx0, ego0, other0))
    assert(!isBehind.holds(ctx0, other0, ego0))

    assert(!besides.holds(ctx0, ego0, other0))
    assert(!besides.holds(ctx0, other0, ego0))

    assert(bothOver10MPH.holds(ctx0, ego0, other0))
    assert(bothOver10MPH.holds(ctx0, other0, ego0))

    // besides
    val tickData1 = emptyTickData(currentTick = TickDataUnitSeconds(1.0), blocks = listOf(block))
    val ego1 =
        emptyVehicle(
            id = egoId,
            egoVehicle = true,
            lane = road0lane1,
            tickData = tickData1,
            positionOnLane = 7.0,
            effVelocityMPH = 60.0)
    val other1 =
        emptyVehicle(
            id = otherId,
            egoVehicle = false,
            lane = road0lane2,
            tickData = tickData1,
            positionOnLane = 7.0,
            effVelocityMPH = 30.0)
    tickData1.entities = listOf(ego1, other1)

    val segment1 = Segment(listOf(tickData1), segmentSource = "")
    val ctx1 = PredicateContext(segment1)

    assert(besides.holds(ctx1, ego1, other1))
    assert(besides.holds(ctx1, other1, ego1))

    assert(!isBehind.holds(ctx1, ego1, other1))
    assert(!isBehind.holds(ctx1, other1, ego1))

    assert(bothOver10MPH.holds(ctx1, ego1, other1))
    assert(bothOver10MPH.holds(ctx1, other1, ego1))

    // full check
    val segment = Segment(listOf(tickData0, tickData1), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(!overtaking.holds(ctx, TickDataUnitSeconds(0.0), egoId, otherId))
    assert(!hasOvertaken.holds(ctx, TickDataUnitSeconds(0.0), egoId))
    assert(!hasOvertaken.holds(ctx, TickDataUnitSeconds(0.0), otherId))
  }

  @Test
  fun besidesInFront() {
    // besides
    val tickData0 = emptyTickData(currentTick = TickDataUnitSeconds(0.0), blocks = listOf(block))
    val ego0 =
        emptyVehicle(
            id = egoId,
            egoVehicle = true,
            lane = road0lane1,
            tickData = tickData0,
            positionOnLane = 0.0,
            effVelocityMPH = 11.0)
    val other0 =
        emptyVehicle(
            id = otherId,
            egoVehicle = false,
            lane = road0lane2,
            tickData = tickData0,
            positionOnLane = 0.0,
            effVelocityMPH = 11.0)
    tickData0.entities = listOf(ego0, other0)

    val segment0 = Segment(listOf(tickData0), segmentSource = "")
    val ctx0 = PredicateContext(segment0)

    assert(besides.holds(ctx0, ego0, other0))
    assert(besides.holds(ctx0, other0, ego0))

    assert(!isBehind.holds(ctx0, ego0, other0))
    assert(!isBehind.holds(ctx0, other0, ego0))

    assert(bothOver10MPH.holds(ctx0, ego0, other0))
    assert(bothOver10MPH.holds(ctx0, other0, ego0))

    // in front
    val tickData1 = emptyTickData(currentTick = TickDataUnitSeconds(1.0), blocks = listOf(block))
    val ego1 =
        emptyVehicle(
            id = egoId,
            egoVehicle = true,
            lane = road0lane1,
            tickData = tickData1,
            positionOnLane = 5.0,
            effVelocityMPH = 11.0)
    val other1 =
        emptyVehicle(
            id = otherId,
            egoVehicle = false,
            lane = road0lane2,
            tickData = tickData1,
            positionOnLane = 1.0,
            effVelocityMPH = 11.0)
    tickData1.entities = listOf(ego1, other1)

    val segment1 = Segment(listOf(tickData1), segmentSource = "")
    val ctx1 = PredicateContext(segment1)

    assert(!isBehind.holds(ctx1, ego1, other1))
    assert(isBehind.holds(ctx1, other1, ego1))

    assert(!besides.holds(ctx1, ego1, other1))
    assert(!besides.holds(ctx1, other1, ego1))

    assert(bothOver10MPH.holds(ctx1, ego1, other1))
    assert(bothOver10MPH.holds(ctx1, other1, ego1))

    // full check
    val segment = Segment(listOf(tickData0, tickData1), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(!overtaking.holds(ctx, TickDataUnitSeconds(0.0), egoId, otherId))
    assert(!hasOvertaken.holds(ctx, TickDataUnitSeconds(0.0), egoId))
    assert(!hasOvertaken.holds(ctx, TickDataUnitSeconds(0.0), otherId))
  }

  @Test
  fun inFrontBesidesBehind() {
    // In front of
    val tickData0 = emptyTickData(currentTick = TickDataUnitSeconds(0.0), blocks = listOf(block))
    val ego0 =
        emptyVehicle(
            id = egoId,
            egoVehicle = true,
            lane = road0lane2,
            tickData = tickData0,
            positionOnLane = 15.0,
            effVelocityMPH = 11.0)
    val other0 =
        emptyVehicle(
            id = otherId,
            egoVehicle = false,
            lane = road0lane1,
            tickData = tickData0,
            positionOnLane = 10.0,
            effVelocityMPH = 40.0)
    tickData0.entities = listOf(ego0, other0)

    val segment0 = Segment(listOf(tickData0), segmentSource = "")
    val ctx0 = PredicateContext(segment0)

    assert(!isBehind.holds(ctx0, ego0, other0))
    assert(isBehind.holds(ctx0, other0, ego0))

    assert(!besides.holds(ctx0, ego0, other0))
    assert(!besides.holds(ctx0, other0, ego0))

    assert(bothOver10MPH.holds(ctx0, ego0, other0))
    assert(bothOver10MPH.holds(ctx0, other0, ego0))

    // Besides
    val tickData1 = emptyTickData(currentTick = TickDataUnitSeconds(1.0), blocks = listOf(block))
    val ego1 =
        emptyVehicle(
            id = egoId,
            egoVehicle = true,
            lane = road0lane2,
            tickData = tickData1,
            positionOnLane = 16.0,
            effVelocityMPH = 11.0)
    val other1 =
        emptyVehicle(
            id = otherId,
            egoVehicle = false,
            lane = road0lane1,
            tickData = tickData1,
            positionOnLane = 16.0,
            effVelocityMPH = 60.0)
    tickData1.entities = listOf(ego1, other1)

    val segment1 = Segment(listOf(tickData1), segmentSource = "")
    val ctx1 = PredicateContext(segment1)

    assert(besides.holds(ctx1, ego1, other1))
    assert(besides.holds(ctx1, other1, ego1))

    assert(!isBehind.holds(ctx1, ego1, other1))
    assert(!isBehind.holds(ctx1, other1, ego1))

    assert(bothOver10MPH.holds(ctx1, ego1, other1))
    assert(bothOver10MPH.holds(ctx1, other1, ego1))

    // Behind
    val tickData2 = emptyTickData(currentTick = TickDataUnitSeconds(2.0), blocks = listOf(block))
    val ego2 =
        emptyVehicle(
            id = egoId,
            egoVehicle = true,
            lane = road0lane2,
            tickData = tickData2,
            positionOnLane = 18.0,
            effVelocityMPH = 11.0)
    val other2 =
        emptyVehicle(
            id = otherId,
            egoVehicle = false,
            lane = road0lane1,
            tickData = tickData2,
            positionOnLane = 30.0,
            effVelocityMPH = 60.0)
    tickData2.entities = listOf(ego2, other2)

    val segment2 = Segment(listOf(tickData2), segmentSource = "")
    val ctx2 = PredicateContext(segment2)

    assert(isBehind.holds(ctx2, ego2, other2))
    assert(!isBehind.holds(ctx2, other2, ego2))

    assert(!besides.holds(ctx2, ego2, other2))
    assert(!besides.holds(ctx2, other2, ego2))

    assert(bothOver10MPH.holds(ctx2, ego2, other2))
    assert(bothOver10MPH.holds(ctx2, other2, ego2))

    // Full check
    val segment = Segment(listOf(tickData0, tickData1, tickData2), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(!overtaking.holds(ctx, TickDataUnitSeconds(0.0), egoId, otherId))
    assert(!hasOvertaken.holds(ctx, TickDataUnitSeconds(0.0), egoId))

    assert(overtaking.holds(ctx, TickDataUnitSeconds(0.0), otherId, egoId))
    assert(hasOvertaken.holds(ctx, TickDataUnitSeconds(0.0), otherId))
  }

  @Test
  fun overtakingOfStandingVehicle() {
    // Behind
    val tickData0 = emptyTickData(currentTick = TickDataUnitSeconds(0.0), blocks = listOf(block))
    val ego0 =
        emptyVehicle(
            id = egoId,
            egoVehicle = true,
            lane = road0lane2,
            positionOnLane = 0.0,
            tickData = tickData0,
            effVelocityMPH = 20.0)
    val other0 =
        emptyVehicle(
            id = otherId,
            egoVehicle = false,
            lane = road0lane2,
            positionOnLane = 5.0,
            tickData = tickData0,
            effVelocityMPH = 0.0)
    tickData0.entities = listOf(ego0, other0)

    val segment0 = Segment(listOf(tickData0), segmentSource = "")
    val ctx0 = PredicateContext(segment0)

    assert(isBehind.holds(ctx0, ego0, other0))
    assert(!isBehind.holds(ctx0, other0, ego0))

    assert(!besides.holds(ctx0, ego0, other0))
    assert(!besides.holds(ctx0, other0, ego0))

    assert(!bothOver10MPH.holds(ctx0, ego0, other0))
    assert(!bothOver10MPH.holds(ctx0, other0, ego0))

    // Left of
    val tickData1 = emptyTickData(currentTick = TickDataUnitSeconds(1.0), blocks = listOf(block))
    val ego1 =
        emptyVehicle(
            id = egoId,
            egoVehicle = true,
            lane = road0lane1,
            positionOnLane = 7.0,
            tickData = tickData1,
            effVelocityMPH = 30.0)
    val other1 =
        emptyVehicle(
            id = otherId,
            egoVehicle = false,
            lane = road0lane2,
            positionOnLane = 7.0,
            tickData = tickData1,
            effVelocityMPH = 0.0)
    tickData1.entities = listOf(ego1, other1)

    val segment1 = Segment(listOf(tickData1), segmentSource = "")
    val ctx1 = PredicateContext(segment1)

    assert(besides.holds(ctx1, ego1, other1))
    assert(besides.holds(ctx1, other1, ego1))

    assert(!isBehind.holds(ctx1, ego1, other1))
    assert(!isBehind.holds(ctx1, other1, ego1))

    assert(!bothOver10MPH.holds(ctx1, ego1, other1))
    assert(!bothOver10MPH.holds(ctx1, other1, ego1))

    // In front of
    val tickData2 = emptyTickData(currentTick = TickDataUnitSeconds(2.0), blocks = listOf(block))
    val ego2 =
        emptyVehicle(
            id = egoId,
            egoVehicle = true,
            lane = road0lane2,
            positionOnLane = 20.0,
            tickData = tickData2,
            effVelocityMPH = 30.0)
    val other2 =
        emptyVehicle(
            id = otherId,
            egoVehicle = false,
            lane = road0lane2,
            positionOnLane = 10.0,
            tickData = tickData2,
            effVelocityMPH = 0.0)
    tickData2.entities = listOf(ego2, other2)

    val segment2 = Segment(listOf(tickData2), segmentSource = "")
    val ctx2 = PredicateContext(segment2)

    assert(isBehind.holds(ctx2, other2, ego2))
    assert(!isBehind.holds(ctx2, ego2, other2))

    assert(!besides.holds(ctx2, ego2, other2))
    assert(!besides.holds(ctx2, other2, ego2))

    assert(!bothOver10MPH.holds(ctx2, ego2, other2))
    assert(!bothOver10MPH.holds(ctx2, other2, ego2))

    val segment = Segment(listOf(tickData0, tickData1, tickData2), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(!overtaking.holds(ctx, TickDataUnitSeconds(0.0), egoId, otherId))
    assert(!hasOvertaken.holds(ctx, TickDataUnitSeconds(0.0), egoId))
  }

  @Test
  fun overtakingOfSlowVehicle() {
    // Behind
    val tickData0 = emptyTickData(currentTick = TickDataUnitSeconds(0.0), blocks = listOf(block))
    val ego0 =
        emptyVehicle(
            id = egoId,
            egoVehicle = true,
            lane = road0lane2,
            positionOnLane = 0.0,
            tickData = tickData0,
            effVelocityMPH = 20.0)
    val other0 =
        emptyVehicle(
            id = otherId,
            egoVehicle = false,
            lane = road0lane2,
            positionOnLane = 5.0,
            tickData = tickData0,
            effVelocityMPH = 5.0)
    tickData0.entities = listOf(ego0, other0)

    val segment0 = Segment(listOf(tickData0), segmentSource = "")
    val ctx0 = PredicateContext(segment0)

    assert(isBehind.holds(ctx0, ego0, other0))
    assert(!isBehind.holds(ctx0, other0, ego0))

    assert(!besides.holds(ctx0, ego0, other0))
    assert(!besides.holds(ctx0, other0, ego0))

    assert(!bothOver10MPH.holds(ctx0, ego0, other0))
    assert(!bothOver10MPH.holds(ctx0, other0, ego0))

    // Left of
    val tickData1 = emptyTickData(currentTick = TickDataUnitSeconds(1.0), blocks = listOf(block))
    val ego1 =
        emptyVehicle(
            id = egoId,
            egoVehicle = true,
            lane = road0lane1,
            positionOnLane = 7.0,
            tickData = tickData1,
            effVelocityMPH = 30.0)
    val other1 =
        emptyVehicle(
            id = otherId,
            egoVehicle = false,
            lane = road0lane2,
            positionOnLane = 7.0,
            tickData = tickData1,
            effVelocityMPH = 5.0)
    tickData1.entities = listOf(ego1, other1)

    val segment1 = Segment(listOf(tickData1), segmentSource = "")
    val ctx1 = PredicateContext(segment1)

    assert(besides.holds(ctx1, ego1, other1))
    assert(besides.holds(ctx1, other1, ego1))

    assert(!isBehind.holds(ctx1, ego1, other1))
    assert(!isBehind.holds(ctx1, other1, ego1))

    assert(!bothOver10MPH.holds(ctx1, ego1, other1))
    assert(!bothOver10MPH.holds(ctx1, other1, ego1))

    // In front of
    val tickData2 = emptyTickData(currentTick = TickDataUnitSeconds(2.0), blocks = listOf(block))
    val ego2 =
        emptyVehicle(
            id = egoId,
            egoVehicle = true,
            lane = road0lane2,
            positionOnLane = 20.0,
            tickData = tickData2,
            effVelocityMPH = 30.0)
    val other2 =
        emptyVehicle(
            id = otherId,
            egoVehicle = false,
            lane = road0lane2,
            positionOnLane = 10.0,
            tickData = tickData2,
            effVelocityMPH = 5.0)
    tickData2.entities = listOf(ego2, other2)

    val segment2 = Segment(listOf(tickData2), segmentSource = "")
    val ctx2 = PredicateContext(segment2)

    assert(isBehind.holds(ctx2, other2, ego2))
    assert(!isBehind.holds(ctx2, ego2, other2))

    assert(!besides.holds(ctx2, ego2, other2))
    assert(!besides.holds(ctx2, other2, ego2))

    assert(!bothOver10MPH.holds(ctx2, ego2, other2))
    assert(!bothOver10MPH.holds(ctx2, other2, ego2))

    val segment = Segment(listOf(tickData0, tickData1, tickData2), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(!overtaking.holds(ctx, TickDataUnitSeconds(0.0), egoId, otherId))
    assert(!hasOvertaken.holds(ctx, TickDataUnitSeconds(0.0), egoId))
  }

  @Test
  fun slowlyOvertakingStandingVehicle() {
    // Behind
    val tickData0 = emptyTickData(currentTick = TickDataUnitSeconds(0.0), blocks = listOf(block))
    val ego0 =
        emptyVehicle(
            id = egoId,
            egoVehicle = true,
            lane = road0lane2,
            positionOnLane = 0.0,
            tickData = tickData0,
            effVelocityMPH = 5.0)
    val other0 =
        emptyVehicle(
            id = otherId,
            egoVehicle = false,
            lane = road0lane2,
            positionOnLane = 5.0,
            tickData = tickData0,
            effVelocityMPH = 0.0)
    tickData0.entities = listOf(ego0, other0)

    val segment0 = Segment(listOf(tickData0), segmentSource = "")
    val ctx0 = PredicateContext(segment0)

    assert(isBehind.holds(ctx0, ego0, other0))
    assert(!isBehind.holds(ctx0, other0, ego0))

    assert(!besides.holds(ctx0, ego0, other0))
    assert(!besides.holds(ctx0, other0, ego0))

    assert(!bothOver10MPH.holds(ctx0, ego0, other0))
    assert(!bothOver10MPH.holds(ctx0, other0, ego0))

    // Left of
    val tickData1 = emptyTickData(currentTick = TickDataUnitSeconds(1.0), blocks = listOf(block))
    val ego1 =
        emptyVehicle(
            id = egoId,
            egoVehicle = true,
            lane = road0lane1,
            positionOnLane = 7.0,
            tickData = tickData1,
            effVelocityMPH = 5.0)
    val other1 =
        emptyVehicle(
            id = otherId,
            egoVehicle = false,
            lane = road0lane2,
            positionOnLane = 7.0,
            tickData = tickData1,
            effVelocityMPH = 0.0)
    tickData1.entities = listOf(ego1, other1)

    val segment1 = Segment(listOf(tickData1), segmentSource = "")
    val ctx1 = PredicateContext(segment1)

    assert(besides.holds(ctx1, ego1, other1))
    assert(besides.holds(ctx1, other1, ego1))

    assert(!isBehind.holds(ctx1, ego1, other1))
    assert(!isBehind.holds(ctx1, other1, ego1))

    assert(!bothOver10MPH.holds(ctx1, ego1, other1))
    assert(!bothOver10MPH.holds(ctx1, other1, ego1))

    // In front of
    val tickData2 = emptyTickData(currentTick = TickDataUnitSeconds(2.0), blocks = listOf(block))
    val ego2 =
        emptyVehicle(
            id = egoId,
            egoVehicle = true,
            lane = road0lane2,
            positionOnLane = 20.0,
            tickData = tickData2,
            effVelocityMPH = 5.0)
    val other2 =
        emptyVehicle(
            id = otherId,
            egoVehicle = false,
            lane = road0lane2,
            positionOnLane = 10.0,
            tickData = tickData2,
            effVelocityMPH = 0.0)
    tickData2.entities = listOf(ego2, other2)

    val segment2 = Segment(listOf(tickData2), segmentSource = "")
    val ctx2 = PredicateContext(segment2)

    assert(isBehind.holds(ctx2, other2, ego2))
    assert(!isBehind.holds(ctx2, ego2, other2))

    assert(!besides.holds(ctx2, ego2, other2))
    assert(!besides.holds(ctx2, other2, ego2))

    assert(!bothOver10MPH.holds(ctx2, ego2, other2))
    assert(!bothOver10MPH.holds(ctx2, other2, ego2))

    val segment = Segment(listOf(tickData0, tickData1, tickData2), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(!overtaking.holds(ctx, TickDataUnitSeconds(0.0), egoId, otherId))
    assert(!hasOvertaken.holds(ctx, TickDataUnitSeconds(0.0), egoId))
  }

  @Test
  fun otherVehicleIsSlowingDownWhileBesides() {
    // Behind
    val tickData0 = emptyTickData(currentTick = TickDataUnitSeconds(0.0), blocks = listOf(block))
    val ego0 =
        emptyVehicle(
            id = egoId,
            egoVehicle = true,
            lane = road0lane2,
            positionOnLane = 0.0,
            tickData = tickData0,
            effVelocityMPH = 20.0)
    val other0 =
        emptyVehicle(
            id = otherId,
            egoVehicle = false,
            lane = road0lane2,
            positionOnLane = 5.0,
            tickData = tickData0,
            effVelocityMPH = 20.0)
    tickData0.entities = listOf(ego0, other0)

    val segment0 = Segment(listOf(tickData0), segmentSource = "")
    val ctx0 = PredicateContext(segment0)

    assert(isBehind.holds(ctx0, ego0, other0))
    assert(!isBehind.holds(ctx0, other0, ego0))

    assert(!besides.holds(ctx0, ego0, other0))
    assert(!besides.holds(ctx0, other0, ego0))

    assert(bothOver10MPH.holds(ctx0, ego0, other0))
    assert(bothOver10MPH.holds(ctx0, other0, ego0))

    // Left of
    val tickData1 = emptyTickData(currentTick = TickDataUnitSeconds(1.0), blocks = listOf(block))
    val ego1 =
        emptyVehicle(
            id = egoId,
            egoVehicle = true,
            lane = road0lane1,
            positionOnLane = 7.0,
            tickData = tickData1,
            effVelocityMPH = 30.0)
    val other1 =
        emptyVehicle(
            id = otherId,
            egoVehicle = false,
            lane = road0lane2,
            positionOnLane = 7.0,
            tickData = tickData1,
            effVelocityMPH = 10.0)
    tickData1.entities = listOf(ego1, other1)

    val segment1 = Segment(listOf(tickData1), segmentSource = "")
    val ctx1 = PredicateContext(segment1)

    assert(besides.holds(ctx1, ego1, other1))
    assert(besides.holds(ctx1, other1, ego1))

    assert(!isBehind.holds(ctx1, ego1, other1))
    assert(!isBehind.holds(ctx1, other1, ego1))

    assert(!bothOver10MPH.holds(ctx1, ego1, other1))
    assert(!bothOver10MPH.holds(ctx1, other1, ego1))

    // In front of
    val tickData2 = emptyTickData(currentTick = TickDataUnitSeconds(2.0), blocks = listOf(block))
    val ego2 =
        emptyVehicle(
            id = egoId,
            egoVehicle = true,
            lane = road0lane2,
            positionOnLane = 20.0,
            tickData = tickData2,
            effVelocityMPH = 30.0)
    val other2 =
        emptyVehicle(
            id = otherId,
            egoVehicle = false,
            lane = road0lane2,
            positionOnLane = 10.0,
            tickData = tickData2,
            effVelocityMPH = 0.0)
    tickData2.entities = listOf(ego2, other2)

    val segment2 = Segment(listOf(tickData2), segmentSource = "")
    val ctx2 = PredicateContext(segment2)

    assert(isBehind.holds(ctx2, other2, ego2))
    assert(!isBehind.holds(ctx2, ego2, other2))

    assert(!besides.holds(ctx2, ego2, other2))
    assert(!besides.holds(ctx2, other2, ego2))

    assert(!bothOver10MPH.holds(ctx2, ego2, other2))
    assert(!bothOver10MPH.holds(ctx2, other2, ego2))

    val segment = Segment(listOf(tickData0, tickData1, tickData2), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(!overtaking.holds(ctx, TickDataUnitSeconds(0.0), egoId, otherId))
    assert(!hasOvertaken.holds(ctx, TickDataUnitSeconds(0.0), egoId))
  }

  @Test
  fun egoIsSlowingDownWhileBesides() {
    // Behind
    val tickData0 = emptyTickData(currentTick = TickDataUnitSeconds(0.0), blocks = listOf(block))
    val ego0 =
        emptyVehicle(
            id = egoId,
            egoVehicle = true,
            lane = road0lane2,
            positionOnLane = 0.0,
            tickData = tickData0,
            effVelocityMPH = 20.0)
    val other0 =
        emptyVehicle(
            id = otherId,
            egoVehicle = false,
            lane = road0lane2,
            positionOnLane = 5.0,
            tickData = tickData0,
            effVelocityMPH = 10.0)
    tickData0.entities = listOf(ego0, other0)

    val segment0 = Segment(listOf(tickData0), segmentSource = "")
    val ctx0 = PredicateContext(segment0)

    assert(isBehind.holds(ctx0, ego0, other0))
    assert(!isBehind.holds(ctx0, other0, ego0))

    assert(!besides.holds(ctx0, ego0, other0))
    assert(!besides.holds(ctx0, other0, ego0))

    // Left of
    val tickData1 = emptyTickData(currentTick = TickDataUnitSeconds(1.0), blocks = listOf(block))
    val ego1 =
        emptyVehicle(
            id = egoId,
            egoVehicle = true,
            lane = road0lane1,
            positionOnLane = 7.0,
            tickData = tickData1,
            effVelocityMPH = 10.0)
    val other1 =
        emptyVehicle(
            id = otherId,
            egoVehicle = false,
            lane = road0lane2,
            positionOnLane = 7.0,
            tickData = tickData1,
            effVelocityMPH = 10.0)
    tickData1.entities = listOf(ego1, other1)

    val segment1 = Segment(listOf(tickData1), segmentSource = "")
    val ctx1 = PredicateContext(segment1)

    assert(besides.holds(ctx1, ego1, other1))
    assert(besides.holds(ctx1, other1, ego1))

    assert(!isBehind.holds(ctx1, ego1, other1))
    assert(!isBehind.holds(ctx1, other1, ego1))

    // In front of
    val tickData2 = emptyTickData(currentTick = TickDataUnitSeconds(2.0), blocks = listOf(block))
    val ego2 =
        emptyVehicle(
            id = egoId,
            egoVehicle = true,
            lane = road0lane2,
            positionOnLane = 20.0,
            tickData = tickData2,
            effVelocityMPH = 00.0)
    val other2 =
        emptyVehicle(
            id = otherId,
            egoVehicle = false,
            lane = road0lane2,
            positionOnLane = 10.0,
            tickData = tickData2,
            effVelocityMPH = 10.0)
    tickData2.entities = listOf(ego2, other2)

    val segment2 = Segment(listOf(tickData2), segmentSource = "")
    val ctx2 = PredicateContext(segment2)

    assert(isBehind.holds(ctx2, other2, ego2))
    assert(!isBehind.holds(ctx2, ego2, other2))

    assert(!besides.holds(ctx2, ego2, other2))
    assert(!besides.holds(ctx2, other2, ego2))

    val segment = Segment(listOf(tickData0, tickData1, tickData2), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(!overtaking.holds(ctx, TickDataUnitSeconds(0.0), egoId, otherId))
    assert(!hasOvertaken.holds(ctx, TickDataUnitSeconds(0.0), egoId))
  }
}
