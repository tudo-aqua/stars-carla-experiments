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
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import tools.aqua.stars.carla.experiments.hasYielded
import tools.aqua.stars.carla.experiments.passedContactPoint
import tools.aqua.stars.core.evaluation.PredicateContext
import tools.aqua.stars.data.av.dataclasses.Block
import tools.aqua.stars.data.av.dataclasses.ContactArea
import tools.aqua.stars.data.av.dataclasses.ContactLaneInfo
import tools.aqua.stars.data.av.dataclasses.Lane
import tools.aqua.stars.data.av.dataclasses.Location
import tools.aqua.stars.data.av.dataclasses.Road
import tools.aqua.stars.data.av.dataclasses.Segment
import tools.aqua.stars.data.av.dataclasses.TickData
import tools.aqua.stars.data.av.dataclasses.TickDataUnitSeconds
import tools.aqua.stars.data.av.dataclasses.Vehicle

class HasYieldedTest {

  private lateinit var road0: Road
  private lateinit var road1: Road
  private lateinit var road2: Road

  private lateinit var road0lane1: Lane
  private lateinit var road1lane1: Lane
  private lateinit var road1lane2: Lane
  private lateinit var road2lane1: Lane

  private lateinit var block: Block

  @BeforeTest
  fun setup() {
    // lanes
    road0lane1 = Lane(laneId = 1, laneLength = 200.0)
    road1lane1 = Lane(laneId = 1, laneLength = 200.0)
    road1lane2 = Lane(laneId = 2, laneLength = 200.0)
    road2lane1 = Lane(laneId = 1, laneLength = 200.0)

    road0 =
        Road(id = 0, isJunction = true, lanes = listOf(road0lane1)).apply { road0lane1.road = this }
    road1 =
        Road(id = 1, isJunction = true, lanes = listOf(road1lane1, road1lane2)).apply {
          road1lane1.road = this
          road1lane2.road = this
        }
    road2 =
        Road(id = 2, isJunction = false, lanes = listOf(road2lane1)).apply {
          road2lane1.road = this
        }

    block = Block(roads = listOf(road0, road1, road2))

    // contact area between road0lane1 and road1lane1
    val road0road1ContactArea =
        ContactArea(
            "",
            Location(),
            lane1 = road0lane1,
            lane1StartPos = 10.0,
            lane1EndPos = 15.0,
            lane2 = road1lane1,
            lane2StartPos = 10.0,
            lane2EndPos = 15.0)

    // assign contact areas and intersections
    road0lane1.contactAreas = listOf(road0road1ContactArea)
    road1lane1.contactAreas = listOf(road0road1ContactArea)
    road0lane1.intersectingLanes = listOf(ContactLaneInfo(road1lane1))
    road1lane1.intersectingLanes = listOf(ContactLaneInfo(road0lane1))
  }

  @Test
  fun egoAtContactPointAfterOther() {

    val ego = Vehicle(id = 0, isEgo = true, lane = road0lane1, positionOnLane = 0.0 + 2)
    val other = Vehicle(id = 1, isEgo = false, lane = road1lane1, positionOnLane = 9.0 + 2)

    // 2) build TickData list
    val tickDataList =
        (0..100).map { i ->
          val egoI = Vehicle(id = 0, isEgo = true, lane = road0lane1, positionOnLane = 0.0 + i)
          val otherI = Vehicle(id = 1, isEgo = false, lane = road1lane1, positionOnLane = 9.0 + i)
          val td =
              TickData(
                  currentTick = TickDataUnitSeconds(i.toDouble()),
                  blocks = listOf(block),
                  entities = listOf(egoI, otherI))
          egoI.tickData = td
          egoI.setVelocityFromEffVelocityMPH(0.0)
          otherI.tickData = td
          otherI.setVelocityFromEffVelocityMPH(0.0)
          td
        }

    // 3) context
    val segment = Segment(tickDataList, segmentSource = "")
    val ctx = PredicateContext(segment)

    // 4) assertions
    assertTrue { passedContactPoint.holds(ctx, TickDataUnitSeconds(2.0), 1, 0) }
    assertFalse { passedContactPoint.holds(ctx, TickDataUnitSeconds(2.0), 0, 1) }
    assertTrue { passedContactPoint.holds(ctx, TickDataUnitSeconds(11.0), 1, 0) }
    assertTrue { passedContactPoint.holds(ctx, TickDataUnitSeconds(11.0), 0, 1) }
    assertTrue { hasYielded.holds(ctx, TickDataUnitSeconds(0.0), 0, 1) }
  }

  @Test
  fun egoAtContactPointBeforeOther() {

    val ego = Vehicle(id = 0, isEgo = true, lane = road0lane1, positionOnLane = 9.0 + 2)
    val other = Vehicle(id = 1, isEgo = false, lane = road1lane1, positionOnLane = 0.0 + 2)

    // 2) build TickData list
    val tickDataList =
        (0..100).map { i ->
          val egoI = Vehicle(id = 0, isEgo = true, lane = road0lane1, positionOnLane = 9.0 + i)
          val otherI = Vehicle(id = 1, isEgo = false, lane = road1lane1, positionOnLane = 0.0 + i)
          val td =
              TickData(
                  currentTick = TickDataUnitSeconds(i.toDouble()),
                  blocks = listOf(block),
                  entities = listOf(egoI, otherI))
          egoI.tickData = td
          egoI.setVelocityFromEffVelocityMPH(0.0)
          otherI.tickData = td
          otherI.setVelocityFromEffVelocityMPH(0.0)
          td
        }

    // 3) context
    val segment = Segment(tickDataList, segmentSource = "")
    val ctx = PredicateContext(segment)

    // 4) assertions
    assertFalse { hasYielded.holds(ctx, TickDataUnitSeconds(0.0), 0, 1) }
    assertTrue { hasYielded.holds(ctx, TickDataUnitSeconds(0.0), 1, 0) }
  }

  @Test
  fun egoAndOtherHaveNoContactPoint() {

    val ego = Vehicle(id = 0, isEgo = true, lane = road0lane1, positionOnLane = 0.0 + 2)
    val other = Vehicle(id = 1, isEgo = false, lane = road1lane2, positionOnLane = 9.0 + 2)

    // 2) build TickData list
    val tickDataList =
        (0..100).map { i ->
          val egoI = Vehicle(id = 0, isEgo = true, lane = road0lane1, positionOnLane = 0.0 + i)
          val otherI = Vehicle(id = 1, isEgo = false, lane = road1lane2, positionOnLane = 9.0 + i)
          val td =
              TickData(
                  currentTick = TickDataUnitSeconds(i.toDouble()),
                  blocks = listOf(block),
                  entities = listOf(egoI, otherI))
          egoI.tickData = td
          egoI.setVelocityFromEffVelocityMPH(0.0)
          otherI.tickData = td
          otherI.setVelocityFromEffVelocityMPH(0.0)
          td
        }

    // 3) context
    val segment = Segment(tickDataList, segmentSource = "")
    val ctx = PredicateContext(segment)

    // 4) assertions
    assertFalse { hasYielded.holds(ctx, TickDataUnitSeconds(0.0), 0, 1) }
    assertFalse { hasYielded.holds(ctx, TickDataUnitSeconds(0.0), 1, 0) }
  }

  @Test
  fun egoAndOtherAreOnDifferentJunctions() {

    val ego = Vehicle(id = 0, isEgo = true, lane = road0lane1, positionOnLane = 0.0 + 2)
    val other = Vehicle(id = 1, isEgo = false, lane = road2lane1, positionOnLane = 9.0 + 2)

    // 2) build TickData list
    val tickDataList =
        (0..100).map { i ->
          val egoI = Vehicle(id = 0, isEgo = true, lane = road0lane1, positionOnLane = 0.0 + i)
          val otherI = Vehicle(id = 1, isEgo = false, lane = road2lane1, positionOnLane = 9.0 + i)
          val td =
              TickData(
                  currentTick = TickDataUnitSeconds(i.toDouble()),
                  blocks = listOf(block),
                  entities = listOf(egoI, otherI))
          egoI.tickData = td
          egoI.setVelocityFromEffVelocityMPH(0.0)
          otherI.tickData = td
          otherI.setVelocityFromEffVelocityMPH(0.0)
          td
        }

    // 3) context
    val segment = Segment(tickDataList, segmentSource = "")
    val ctx = PredicateContext(segment)

    // 4) assertions
    assertFalse { hasYielded.holds(ctx, TickDataUnitSeconds(0.0), 0, 1) }
    assertFalse { hasYielded.holds(ctx, TickDataUnitSeconds(0.0), 1, 0) }
  }
}
