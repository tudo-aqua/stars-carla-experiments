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

package tools.aqua.stars.carla.experiments.stopType

import kotlin.test.BeforeTest
import kotlin.test.Test
import tools.aqua.stars.carla.experiments.emptyBlock
import tools.aqua.stars.carla.experiments.emptyLane
import tools.aqua.stars.carla.experiments.emptyLocation
import tools.aqua.stars.carla.experiments.emptyRoad
import tools.aqua.stars.carla.experiments.emptyTickData
import tools.aqua.stars.carla.experiments.emptyVehicle
import tools.aqua.stars.carla.experiments.hasYielded
import tools.aqua.stars.carla.experiments.passedContactPoint
import tools.aqua.stars.core.evaluation.PredicateContext
import tools.aqua.stars.data.av.dataclasses.ContactArea
import tools.aqua.stars.data.av.dataclasses.ContactLaneInfo
import tools.aqua.stars.data.av.dataclasses.Segment
import tools.aqua.stars.data.av.dataclasses.TickData
import tools.aqua.stars.data.av.dataclasses.TickDataUnitSeconds

class HasYieldedTest {

  private val road0 = emptyRoad(id = 0, isJunction = true)
  private val road0lane1 = emptyLane(laneId = 1, road = road0, laneLength = 200.0)

  private val road1 = emptyRoad(id = 1, isJunction = true)
  private val road1lane1 = emptyLane(laneId = 1, road = road1, laneLength = 200.0)
  private val road1lane2 = emptyLane(laneId = 2, road = road1, laneLength = 200.0)

  private val road2 = emptyRoad(id = 2, isJunction = false)
  private val road2lane1 = emptyLane(laneId = 1, road = road2, laneLength = 200.0)

  private val block = emptyBlock()

  @BeforeTest
  fun setup() {
    road0.lanes = listOf(road0lane1)
    road1.lanes = listOf(road1lane1, road1lane2)
    road2.lanes = listOf(road2lane1)

    block.roads = listOf(road0, road1, road2)
    val road0road1ContactArea =
        ContactArea(
            "",
            emptyLocation(),
            lane1 = road0lane1,
            lane1StartPos = 10.0,
            lane1EndPos = 15.0,
            lane2 = road1lane1,
            lane2StartPos = 10.0,
            lane2EndPos = 15.0)
    road0lane1.contactAreas = listOf(road0road1ContactArea)
    road1lane1.contactAreas = listOf(road0road1ContactArea)
    road0lane1.intersectingLanes = listOf(ContactLaneInfo(road1lane1))
    road1lane1.intersectingLanes = listOf(ContactLaneInfo(road0lane1))
  }

  @Test
  fun egoAtContactPointAfterOther() {
    val tickDataList = mutableListOf<TickData>()
    for (i in 0..100) {
      val tickData =
          emptyTickData(currentTick = TickDataUnitSeconds(i.toDouble()), blocks = listOf(block))
      val ego =
          emptyVehicle(
              id = 0,
              egoVehicle = true,
              lane = road0lane1,
              positionOnLane = 0.0 + i,
              tickData = tickData)
      val other =
          emptyVehicle(
              id = 1,
              egoVehicle = false,
              lane = road1lane1,
              positionOnLane = 9.0 + i,
              tickData = tickData)
      tickData.entities = listOf(ego, other)
      tickDataList.add(tickData)
    }
    val segment = Segment(tickDataList, segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(passedContactPoint.holds(ctx, TickDataUnitSeconds(2.0), 1, 0))
    assert(!passedContactPoint.holds(ctx, TickDataUnitSeconds(2.0), 0, 1))

    assert(passedContactPoint.holds(ctx, TickDataUnitSeconds(11.0), 1, 0))
    assert(passedContactPoint.holds(ctx, TickDataUnitSeconds(11.0), 0, 1))

    assert(hasYielded.holds(ctx, TickDataUnitSeconds(0.0), 0, 1))
  }

  @Test
  fun egoAtContactPointBeforeOther() {
    val tickDataList = mutableListOf<TickData>()
    for (i in 0..100) {
      val tickData =
          emptyTickData(currentTick = TickDataUnitSeconds(i.toDouble()), blocks = listOf(block))
      val ego =
          emptyVehicle(
              id = 0,
              egoVehicle = true,
              lane = road0lane1,
              positionOnLane = 9.0 + i,
              tickData = tickData)
      val other =
          emptyVehicle(
              id = 1,
              egoVehicle = false,
              lane = road1lane1,
              positionOnLane = 0.0 + i,
              tickData = tickData)
      tickData.entities = listOf(ego, other)
      tickDataList.add(tickData)
    }
    val segment = Segment(tickDataList, segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(!hasYielded.holds(ctx, TickDataUnitSeconds(0.0), 0, 1))
    assert(hasYielded.holds(ctx, TickDataUnitSeconds(0.0), 1, 0))
  }

  @Test
  fun egoAndOtherHaveNoContactPoint() {
    val tickDataList = mutableListOf<TickData>()
    for (i in 0..100) {
      val tickData =
          emptyTickData(currentTick = TickDataUnitSeconds(i.toDouble()), blocks = listOf(block))
      val ego =
          emptyVehicle(
              id = 0,
              egoVehicle = true,
              lane = road0lane1,
              positionOnLane = 0.0 + i,
              tickData = tickData)
      val other =
          emptyVehicle(
              id = 1,
              egoVehicle = false,
              lane = road1lane2,
              positionOnLane = 9.0 + i,
              tickData = tickData)
      tickData.entities = listOf(ego, other)
      tickDataList.add(tickData)
    }
    val segment = Segment(tickDataList, segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(!hasYielded.holds(ctx, TickDataUnitSeconds(0.0), 0, 1))
    assert(!hasYielded.holds(ctx, TickDataUnitSeconds(0.0), 1, 0))
  }

  @Test
  fun egoAndOtherAreOnDifferentJunctions() {
    val tickDataList = mutableListOf<TickData>()
    for (i in 0..100) {
      val tickData =
          emptyTickData(currentTick = TickDataUnitSeconds(i.toDouble()), blocks = listOf(block))
      val ego =
          emptyVehicle(
              id = 0,
              egoVehicle = true,
              lane = road0lane1,
              positionOnLane = 0.0 + i,
              tickData = tickData)
      val other =
          emptyVehicle(
              id = 1,
              egoVehicle = false,
              lane = road2lane1,
              positionOnLane = 9.0 + i,
              tickData = tickData)
      tickData.entities = listOf(ego, other)
      tickDataList.add(tickData)
    }
    val segment = Segment(tickDataList, segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(!hasYielded.holds(ctx, TickDataUnitSeconds(0.0), 0, 1))
    assert(!hasYielded.holds(ctx, TickDataUnitSeconds(0.0), 1, 0))
  }
}
