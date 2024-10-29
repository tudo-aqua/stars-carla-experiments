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
import tools.aqua.stars.carla.experiments.emptyBlock
import tools.aqua.stars.carla.experiments.emptyLane
import tools.aqua.stars.carla.experiments.emptyLocation
import tools.aqua.stars.carla.experiments.emptyRoad
import tools.aqua.stars.carla.experiments.emptyTickData
import tools.aqua.stars.carla.experiments.emptyVehicle
import tools.aqua.stars.carla.experiments.passedContactPoint
import tools.aqua.stars.core.evaluation.PredicateContext
import tools.aqua.stars.data.av.dataclasses.ContactArea
import tools.aqua.stars.data.av.dataclasses.ContactLaneInfo
import tools.aqua.stars.data.av.dataclasses.Segment
import tools.aqua.stars.data.av.dataclasses.TickDataUnitSeconds

class ReachedContactPointTest {

  private val road0 = emptyRoad(id = 0, isJunction = true)
  private val road0lane1 = emptyLane(laneId = 1, road = road0, laneLength = 200.0)

  private val road1 = emptyRoad(id = 1, isJunction = true)
  private val road1lane1 = emptyLane(laneId = 1, road = road1, laneLength = 200.0)
  private val road1lane2 = emptyLane(laneId = 2, road = road1, laneLength = 200.0)

  private val block = emptyBlock()

  @BeforeTest
  fun setup() {
    road0.lanes = listOf(road0lane1)
    road1.lanes = listOf(road1lane1, road1lane2)

    block.roads = listOf(road0, road1)
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
  fun egoIsInContactArea() {
    val tickData = emptyTickData(currentTick = TickDataUnitSeconds(0.0), blocks = listOf(block))
    val ego =
        emptyVehicle(
            id = 0,
            egoVehicle = true,
            lane = road0lane1,
            positionOnLane = 11.0,
            tickData = tickData)
    val other =
        emptyVehicle(
            id = 1,
            egoVehicle = false,
            lane = road1lane1,
            positionOnLane = 19.0,
            tickData = tickData)
    tickData.entities = listOf(ego, other)
    val segment = Segment(listOf(tickData), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(passedContactPoint.holds(ctx, ego, other))
    assert(passedContactPoint.holds(ctx, other, ego))
  }

  @Test
  fun egoIsAtEdgeOfContactArea() {
    val tickData = emptyTickData(currentTick = TickDataUnitSeconds(0.0), blocks = listOf(block))
    val ego =
        emptyVehicle(
            id = 0,
            egoVehicle = true,
            lane = road0lane1,
            positionOnLane = 10.0,
            tickData = tickData)
    val other =
        emptyVehicle(
            id = 1,
            egoVehicle = false,
            lane = road1lane1,
            positionOnLane = 19.0,
            tickData = tickData)
    tickData.entities = listOf(ego, other)
    val segment = Segment(listOf(tickData), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(!passedContactPoint.holds(ctx, ego, other))
    assert(passedContactPoint.holds(ctx, other, ego))
  }

  @Test
  fun egoIsInFrontOfContactArea() {
    val tickData = emptyTickData(currentTick = TickDataUnitSeconds(0.0), blocks = listOf(block))
    val ego =
        emptyVehicle(
            id = 0, egoVehicle = true, lane = road0lane1, positionOnLane = 9.0, tickData = tickData)
    val other =
        emptyVehicle(
            id = 1,
            egoVehicle = false,
            lane = road1lane1,
            positionOnLane = 19.0,
            tickData = tickData)
    tickData.entities = listOf(ego, other)
    val segment = Segment(listOf(tickData), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(!passedContactPoint.holds(ctx, ego, other))
    assert(passedContactPoint.holds(ctx, other, ego))
  }

  @Test
  fun egoIsBehindContactArea() {
    val tickData = emptyTickData(currentTick = TickDataUnitSeconds(0.0), blocks = listOf(block))
    val ego =
        emptyVehicle(
            id = 0,
            egoVehicle = true,
            lane = road0lane1,
            positionOnLane = 16.0,
            tickData = tickData)
    val other =
        emptyVehicle(
            id = 1,
            egoVehicle = false,
            lane = road1lane1,
            positionOnLane = 19.0,
            tickData = tickData)
    tickData.entities = listOf(ego, other)
    val segment = Segment(listOf(tickData), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(passedContactPoint.holds(ctx, ego, other))
    assert(passedContactPoint.holds(ctx, other, ego))
  }

  @Test
  fun lanesHaveNoContactArea() {
    val tickData = emptyTickData(currentTick = TickDataUnitSeconds(0.0), blocks = listOf(block))
    val ego =
        emptyVehicle(
            id = 0,
            egoVehicle = true,
            lane = road0lane1,
            positionOnLane = 11.0,
            tickData = tickData)
    val other =
        emptyVehicle(
            id = 1,
            egoVehicle = false,
            lane = road1lane2,
            positionOnLane = 19.0,
            tickData = tickData)
    tickData.entities = listOf(ego, other)
    val segment = Segment(listOf(tickData), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(!passedContactPoint.holds(ctx, ego, other))
    assert(!passedContactPoint.holds(ctx, other, ego))
  }
}
