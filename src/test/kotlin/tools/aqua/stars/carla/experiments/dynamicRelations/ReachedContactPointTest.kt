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

class ReachedContactPointTest {
  private lateinit var road0: Road
  private lateinit var road0lane1: Lane

  private lateinit var road1: Road
  private lateinit var road1lane1: Lane
  private lateinit var road1lane2: Lane

  private lateinit var block: Block

  @BeforeTest
  fun setup() {
    road0lane1 = Lane(laneId = 1, laneLength = 200.0)
    road1lane1 = Lane(laneId = 1, laneLength = 200.0)
    road1lane2 = Lane(laneId = 2, laneLength = 200.0)

    road0 =
        Road(id = 0, isJunction = true, lanes = listOf(road0lane1)).apply { road0lane1.road = this }
    road1 =
        Road(id = 1, isJunction = true, lanes = listOf(road1lane1, road1lane2)).apply {
          road1lane1.road = this
          road1lane2.road = this
        }

    block = Block(roads = listOf(road0, road1))

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

    road0lane1.contactAreas = listOf(road0road1ContactArea)
    road1lane1.contactAreas = listOf(road0road1ContactArea)
    road0lane1.intersectingLanes = listOf(ContactLaneInfo(road1lane1))
    road1lane1.intersectingLanes = listOf(ContactLaneInfo(road0lane1))
  }

  @Test
  fun egoIsInContactArea() {
    // create vehicles
    val ego = Vehicle(id = 0, isEgo = true, lane = road0lane1, positionOnLane = 11.0)
    val other = Vehicle(id = 1, isEgo = false, lane = road1lane1, positionOnLane = 19.0)

    // build TickData
    val tickData =
        TickData(
            currentTick = TickDataUnitSeconds(0.0),
            blocks = listOf(block),
            entities = listOf(ego, other))
    ego.tickData = tickData
    other.tickData = tickData

    val ctx = PredicateContext(Segment(listOf(tickData), segmentSource = ""))
    assertTrue { passedContactPoint.holds(ctx, ego, other) }
    assertTrue { passedContactPoint.holds(ctx, other, ego) }
  }

  @Test
  fun egoIsAtEdgeOfContactArea() {
    val ego = Vehicle(id = 0, isEgo = true, lane = road0lane1, positionOnLane = 10.0)
    val other = Vehicle(id = 1, isEgo = false, lane = road1lane1, positionOnLane = 19.0)

    val tickData =
        TickData(
            currentTick = TickDataUnitSeconds(0.0),
            blocks = listOf(block),
            entities = listOf(ego, other))
    ego.tickData = tickData
    other.tickData = tickData

    val ctx = PredicateContext(Segment(listOf(tickData), segmentSource = ""))
    assertFalse { passedContactPoint.holds(ctx, ego, other) }
    assertTrue { passedContactPoint.holds(ctx, other, ego) }
  }

  @Test
  fun egoIsInFrontOfContactArea() {
    val ego = Vehicle(id = 0, isEgo = true, lane = road0lane1, positionOnLane = 9.0)
    val other = Vehicle(id = 1, isEgo = false, lane = road1lane1, positionOnLane = 19.0)

    val tickData =
        TickData(
            currentTick = TickDataUnitSeconds(0.0),
            blocks = listOf(block),
            entities = listOf(ego, other))
    ego.tickData = tickData
    other.tickData = tickData

    val ctx = PredicateContext(Segment(listOf(tickData), segmentSource = ""))
    assertFalse { passedContactPoint.holds(ctx, ego, other) }
    assertTrue { passedContactPoint.holds(ctx, other, ego) }
  }

  @Test
  fun egoIsBehindContactArea() {
    val ego = Vehicle(id = 0, isEgo = true, lane = road0lane1, positionOnLane = 16.0)
    val other = Vehicle(id = 1, isEgo = false, lane = road1lane1, positionOnLane = 19.0)

    val tickData =
        TickData(
            currentTick = TickDataUnitSeconds(0.0),
            blocks = listOf(block),
            entities = listOf(ego, other))
    ego.tickData = tickData
    other.tickData = tickData

    val ctx = PredicateContext(Segment(listOf(tickData), segmentSource = ""))
    assertTrue { passedContactPoint.holds(ctx, ego, other) }
    assertTrue { passedContactPoint.holds(ctx, other, ego) }
  }

  @Test
  fun lanesHaveNoContactArea() {
    val ego = Vehicle(id = 0, isEgo = true, lane = road0lane1, positionOnLane = 11.0)
    val other = Vehicle(id = 1, isEgo = false, lane = road1lane2, positionOnLane = 19.0)

    val tickData =
        TickData(
            currentTick = TickDataUnitSeconds(0.0),
            blocks = listOf(block),
            entities = listOf(ego, other))
    ego.tickData = tickData
    other.tickData = tickData

    val ctx = PredicateContext(Segment(listOf(tickData), segmentSource = ""))
    assertFalse { passedContactPoint.holds(ctx, ego, other) }
    assertFalse { passedContactPoint.holds(ctx, other, ego) }
  }
}
