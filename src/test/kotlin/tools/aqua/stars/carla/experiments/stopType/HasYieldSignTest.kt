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

package tools.aqua.stars.carla.experiments.stopType

import kotlin.test.BeforeTest
import kotlin.test.Test
import tools.aqua.stars.carla.experiments.emptyBlock
import tools.aqua.stars.carla.experiments.emptyLane
import tools.aqua.stars.carla.experiments.emptyLocation
import tools.aqua.stars.carla.experiments.emptyRoad
import tools.aqua.stars.carla.experiments.emptyRotation
import tools.aqua.stars.carla.experiments.emptyTickData
import tools.aqua.stars.carla.experiments.emptyVehicle
import tools.aqua.stars.carla.experiments.hasYieldSign
import tools.aqua.stars.core.evaluation.PredicateContext
import tools.aqua.stars.data.av.dataclasses.ContactLaneInfo
import tools.aqua.stars.data.av.dataclasses.Landmark
import tools.aqua.stars.data.av.dataclasses.LandmarkType
import tools.aqua.stars.data.av.dataclasses.Segment
import tools.aqua.stars.data.av.dataclasses.TickDataUnitSeconds

class HasYieldSignTest {
  private val yieldSign1 =
      Landmark(
          id = 11,
          name = "",
          distance = 50.0,
          s = 50.0,
          country = "",
          type = LandmarkType.YieldSign,
          value = 0.0,
          unit = "",
          text = "",
          location = emptyLocation(),
          rotation = emptyRotation())
  private val yieldSign2 =
      Landmark(
          id = 12,
          name = "",
          distance = 50.0,
          s = 50.0,
          country = "",
          type = LandmarkType.YieldSign,
          value = 0.0,
          unit = "",
          text = "",
          location = emptyLocation(),
          rotation = emptyRotation())

  private val road0 = emptyRoad(id = 0)
  private val road0lane1 = emptyLane(laneId = 1, road = road0, laneLength = 50.0)
  private val road1 = emptyRoad(id = 1)
  private val road1lane1 =
      emptyLane(laneId = 1, road = road1, laneLength = 50.0, landmarks = listOf(yieldSign1))
  private val road1lane2 = emptyLane(laneId = 2, road = road1, laneLength = 50.0)

  private val road2 = emptyRoad(id = 2)
  private val road2lane1 =
      emptyLane(laneId = 1, road = road2, laneLength = 50.0, landmarks = listOf(yieldSign2))
  private val road3 = emptyRoad(id = 2)
  private val road3lane1 = emptyLane(laneId = 1, road = road3, laneLength = 50.0)

  private val block = emptyBlock()

  private val blocks = listOf(block)

  @BeforeTest
  fun setup() {
    road0.lanes = listOf(road0lane1)
    road1.lanes = listOf(road1lane1, road1lane2)
    road2.lanes = listOf(road2lane1)
    road3.lanes = listOf(road3lane1)

    block.roads = listOf(road0, road1, road2, road3)

    road0lane1.successorLanes = listOf(ContactLaneInfo(road1lane1))
    road1lane1.predecessorLanes = listOf(ContactLaneInfo(road0lane1))

    road2lane1.successorLanes = listOf(ContactLaneInfo(road3lane1))
    road3lane1.predecessorLanes = listOf(ContactLaneInfo(road2lane1))
  }

  @Test
  fun laneHasYieldSignIsAtStart() {
    val tickData = emptyTickData(currentTick = TickDataUnitSeconds(0.0), blocks = blocks)
    val ego =
        emptyVehicle(
            id = 0,
            egoVehicle = true,
            lane = road1lane1,
            tickData = tickData,
            positionOnLane = 0.0,
            effVelocityMPH = 11.0)
    tickData.entities = listOf(ego)

    val segment = Segment(listOf(tickData), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(hasYieldSign.holds(ctx, ego))
  }

  @Test
  fun laneHasYieldSignIsAtEnd() {
    val tickData = emptyTickData(currentTick = TickDataUnitSeconds(0.0), blocks = blocks)
    val ego =
        emptyVehicle(
            id = 0,
            egoVehicle = true,
            lane = road1lane1,
            tickData = tickData,
            positionOnLane = road0lane1.laneLength - 1.0,
            effVelocityMPH = 11.0)
    tickData.entities = listOf(ego)

    val segment = Segment(listOf(tickData), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(hasYieldSign.holds(ctx, ego))
  }

  @Test
  fun laneHasNoYieldSignIsAtStart() {
    val tickData = emptyTickData(currentTick = TickDataUnitSeconds(0.0), blocks = blocks)
    val ego =
        emptyVehicle(
            id = 0,
            egoVehicle = true,
            lane = road0lane1,
            tickData = tickData,
            positionOnLane = 0.0,
            effVelocityMPH = 11.0)
    tickData.entities = listOf(ego)

    val segment = Segment(listOf(tickData), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(!hasYieldSign.holds(ctx, ego))
  }

  @Test
  fun laneHasNoYieldSignIsAtEnd() {
    val tickData = emptyTickData(currentTick = TickDataUnitSeconds(0.0), blocks = blocks)
    val ego =
        emptyVehicle(
            id = 0,
            egoVehicle = true,
            lane = road0lane1,
            tickData = tickData,
            positionOnLane = road0lane1.laneLength - 1.0,
            effVelocityMPH = 11.0)
    tickData.entities = listOf(ego)

    val segment = Segment(listOf(tickData), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(!hasYieldSign.holds(ctx, ego))
  }

  @Test
  fun successorLaneHasYieldSign() {
    val tickData = emptyTickData(currentTick = TickDataUnitSeconds(0.0), blocks = blocks)
    val ego =
        emptyVehicle(
            id = 0,
            egoVehicle = true,
            lane = road0lane1,
            tickData = tickData,
            positionOnLane = 0.0,
            effVelocityMPH = 11.0)
    tickData.entities = listOf(ego)

    val segment = Segment(listOf(tickData), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(!hasYieldSign.holds(ctx, ego))
  }

  @Test
  fun predecessorLaneHasYieldSign() {
    val tickData = emptyTickData(currentTick = TickDataUnitSeconds(0.0), blocks = blocks)
    val ego =
        emptyVehicle(
            id = 0,
            egoVehicle = true,
            lane = road3lane1,
            tickData = tickData,
            positionOnLane = 0.0,
            effVelocityMPH = 11.0)
    tickData.entities = listOf(ego)

    val segment = Segment(listOf(tickData), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(!hasYieldSign.holds(ctx, ego))
  }

  @Test
  fun otherLaneOfOtherRoadHasYieldSign() {
    val tickData = emptyTickData(currentTick = TickDataUnitSeconds(0.0), blocks = blocks)
    val ego =
        emptyVehicle(
            id = 0,
            egoVehicle = true,
            lane = road1lane2,
            tickData = tickData,
            positionOnLane = road0lane1.laneLength - 1.0,
            effVelocityMPH = 11.0)
    tickData.entities = listOf(ego)

    val segment = Segment(listOf(tickData), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(!hasYieldSign.holds(ctx, ego))
  }

  @Test
  fun drivesIntoLaneWithYieldSign() {
    // lane without yield sign
    val tickData0 = emptyTickData(currentTick = TickDataUnitSeconds(0.0), blocks = blocks)
    val ego0 =
        emptyVehicle(
            id = 0,
            egoVehicle = true,
            lane = road0lane1,
            tickData = tickData0,
            positionOnLane = road0lane1.laneLength - 1.0,
            effVelocityMPH = 11.0)
    tickData0.entities = listOf(ego0)

    val segment0 = Segment(listOf(tickData0), segmentSource = "")
    val ctx0 = PredicateContext(segment0)

    assert(!hasYieldSign.holds(ctx0, ego0))

    // lane with yield sign
    val tickData1 = emptyTickData(currentTick = TickDataUnitSeconds(1.0), blocks = blocks)
    val ego1 =
        emptyVehicle(
            id = 0,
            egoVehicle = true,
            lane = road1lane1,
            tickData = tickData1,
            positionOnLane = 0.0,
            effVelocityMPH = 11.0)
    tickData1.entities = listOf(ego1)

    val segment1 = Segment(listOf(tickData1), segmentSource = "")
    val ctx1 = PredicateContext(segment1)

    assert(hasYieldSign.holds(ctx1, ego1))

    // Full check
    val segment = Segment(listOf(tickData0, tickData1), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(hasYieldSign.holds(ctx, TickDataUnitSeconds(0.0), 0))
  }

  @Test
  fun drivesFromLaneWithYieldSign() {
    // lane without yield sign
    val tickData0 = emptyTickData(currentTick = TickDataUnitSeconds(0.0), blocks = blocks)
    val ego0 =
        emptyVehicle(
            id = 0,
            egoVehicle = true,
            lane = road2lane1,
            tickData = tickData0,
            positionOnLane = road2lane1.laneLength - 1.0,
            effVelocityMPH = 11.0)
    tickData0.entities = listOf(ego0)

    val segment0 = Segment(listOf(tickData0), segmentSource = "")
    val ctx0 = PredicateContext(segment0)

    assert(hasYieldSign.holds(ctx0, ego0))

    // lane with yield sign
    val tickData1 = emptyTickData(currentTick = TickDataUnitSeconds(1.0), blocks = blocks)
    val ego1 =
        emptyVehicle(
            id = 0,
            egoVehicle = true,
            lane = road3lane1,
            tickData = tickData1,
            positionOnLane = 0.0,
            effVelocityMPH = 11.0)
    tickData1.entities = listOf(ego1)

    val segment1 = Segment(listOf(tickData1), segmentSource = "")
    val ctx1 = PredicateContext(segment1)

    assert(!hasYieldSign.holds(ctx1, ego1))

    // Full check
    val segment = Segment(listOf(tickData0, tickData1), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(hasYieldSign.holds(ctx, TickDataUnitSeconds(0.0), 0))
  }
}
