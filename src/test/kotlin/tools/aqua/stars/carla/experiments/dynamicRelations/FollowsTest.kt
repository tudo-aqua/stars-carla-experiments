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
import tools.aqua.stars.carla.experiments.follows
import tools.aqua.stars.core.evaluation.PredicateContext
import tools.aqua.stars.data.av.dataclasses.Block
import tools.aqua.stars.data.av.dataclasses.ContactLaneInfo
import tools.aqua.stars.data.av.dataclasses.Lane
import tools.aqua.stars.data.av.dataclasses.Road
import tools.aqua.stars.data.av.dataclasses.Segment
import tools.aqua.stars.data.av.dataclasses.TickData
import tools.aqua.stars.data.av.dataclasses.TickDataUnitSeconds
import tools.aqua.stars.data.av.dataclasses.Vehicle

class FollowsTest {
  private lateinit var road1: Road
  private lateinit var road2: Road
  private lateinit var successorRoad: Road

  private lateinit var lane1: Lane
  private lateinit var lane2: Lane
  private lateinit var laneRoad2: Lane
  private lateinit var laneSuccessorRoad: Lane

  private lateinit var block: Block

  private lateinit var vehicle0: Vehicle
  private lateinit var vehicle1: Vehicle
  private lateinit var vehicle2: Vehicle

  @BeforeTest
  fun setup() {
    lane1 = Lane(laneId = 1, laneLength = 50.0)
    lane2 = Lane(laneId = 2, laneLength = 50.0)
    laneRoad2 = Lane(laneId = 1, laneLength = 50.0)
    laneSuccessorRoad = Lane(laneId = 1)

    road1 =
        Road(id = 1, lanes = listOf(lane1, lane2)).apply {
          lane1.road = this
          lane2.road = this
        }

    road2 = Road(id = 2, lanes = listOf(laneRoad2)).apply { laneRoad2.road = this }

    successorRoad =
        Road(id = 3, lanes = listOf(laneSuccessorRoad)).apply { laneSuccessorRoad.road = this }

    lane1.successorLanes = listOf(ContactLaneInfo(laneSuccessorRoad))

    block = Block(roads = listOf(road1, road2))

    vehicle0 = Vehicle(isEgo = true, id = 0, lane = lane1)
    vehicle1 = Vehicle(isEgo = false, id = 1, lane = lane1)
    vehicle2 = Vehicle(isEgo = false, id = 2, lane = lane1)
  }

  @Test
  fun testFollows() {
    val tickDataList = mutableListOf<TickData>()

    for (i in 0..30) {

      val v1 = Vehicle(isEgo = true, id = vehicle1.id, positionOnLane = i.toDouble(), lane = lane1)
      val v2 =
          Vehicle(
              isEgo = false, id = vehicle2.id, positionOnLane = i.toDouble() + 10.0, lane = lane1)

      val tickData =
          TickData(
              currentTick = TickDataUnitSeconds(i.toDouble()),
              blocks = listOf(block),
              entities = listOf(v1, v2))

      v1.tickData = tickData
      v2.tickData = tickData

      tickDataList.add(tickData)
    }

    val segment = Segment(tickDataList, segmentSource = "")
    val ctx = PredicateContext(segment)

    // Vehicle with id 1 should follow Vehicle with id 2
    assertTrue { follows.holds(ctx, TickDataUnitSeconds(0.0), vehicle1.id, vehicle2.id) }
    assertTrue {
      follows.holds(ctx, tickDataList.first().vehicles[0], tickDataList.first().vehicles[1])
    }

    // Vehicle with id 2 should not follow Vehicle with id 1
    assertFalse { follows.holds(ctx, TickDataUnitSeconds(0.0), vehicle2.id, vehicle1.id) }
    assertFalse {
      follows.holds(ctx, tickDataList.first().vehicles[1], tickDataList.first().vehicles[0])
    }
  }

  @Test
  fun testFollowsWithTooShortSegment() {
    val tickDataList = mutableListOf<TickData>()

    // minimum duration is 30.0, so only 0..29
    for (i in 0..29) {
      val v1 = Vehicle(isEgo = true, id = vehicle1.id, positionOnLane = i.toDouble(), lane = lane1)
      val v2 =
          Vehicle(
              isEgo = false, id = vehicle2.id, positionOnLane = i.toDouble() + 10.0, lane = lane1)

      val tickData =
          TickData(
              currentTick = TickDataUnitSeconds(i.toDouble()),
              blocks = listOf(block),
              entities = listOf(v1, v2))
      v1.tickData = tickData
      v2.tickData = tickData

      tickDataList.add(tickData)
    }

    val segment = Segment(tickDataList, segmentSource = "")
    val ctx = PredicateContext(segment)

    assertFalse { follows.holds(ctx, TickDataUnitSeconds(0.0), vehicle1.id, vehicle2.id) }
    assertFalse {
      follows.holds(ctx, tickDataList.first().vehicles[0], tickDataList.first().vehicles[1])
    }
    assertFalse { follows.holds(ctx, TickDataUnitSeconds(0.0), vehicle2.id, vehicle1.id) }
  }

  @Test
  fun testFollowsWithSomeoneBetween() {
    val tickDataList = mutableListOf<TickData>()

    for (i in 0..30) {
      val v1 = Vehicle(isEgo = true, id = vehicle0.id, positionOnLane = i.toDouble(), lane = lane1)
      val v2 =
          Vehicle(
              isEgo = false, id = vehicle1.id, positionOnLane = i.toDouble() + 10.0, lane = lane1)
      val v3 =
          Vehicle(
              isEgo = false, id = vehicle2.id, positionOnLane = i.toDouble() + 5.0, lane = lane1)

      val tickData =
          TickData(
              currentTick = TickDataUnitSeconds(i.toDouble()),
              blocks = listOf(block),
              entities = listOf(v1, v2, v3))
      v1.tickData = tickData
      v2.tickData = tickData
      v3.tickData = tickData

      tickDataList.add(tickData)
    }

    val segment = Segment(tickDataList, segmentSource = "")
    val ctx = PredicateContext(segment)

    // v0 should not follow v1 (v3 is between)
    assertFalse { follows.holds(ctx, TickDataUnitSeconds(0.0), vehicle0.id, vehicle1.id) }
    // v2 should follow v1
    assertTrue { follows.holds(ctx, TickDataUnitSeconds(0.0), vehicle2.id, vehicle1.id) }
    // v0 should follow v2
    assertTrue { follows.holds(ctx, TickDataUnitSeconds(0.0), vehicle0.id, vehicle2.id) }
    // all other combos false
    assertFalse { follows.holds(ctx, TickDataUnitSeconds(0.0), vehicle1.id, vehicle0.id) }
    assertFalse { follows.holds(ctx, TickDataUnitSeconds(0.0), vehicle1.id, vehicle2.id) }
  }

  @Test
  fun testFollowsWithDifferentLanes() {
    val tickDataList = mutableListOf<TickData>()

    for (i in 0..30) {

      val v1 = Vehicle(isEgo = true, id = vehicle0.id, positionOnLane = i.toDouble(), lane = lane1)
      val v2 =
          Vehicle(
              isEgo = false, id = vehicle1.id, positionOnLane = i.toDouble() + 10.0, lane = lane2)

      val tickData =
          TickData(
              currentTick = TickDataUnitSeconds(i.toDouble()),
              blocks = listOf(block),
              entities = listOf(v1, v2))

      v1.tickData = tickData
      v2.tickData = tickData

      tickDataList.add(tickData)
    }

    val segment = Segment(tickDataList, segmentSource = "")
    val ctx = PredicateContext(segment)

    // neither follows the other across different lanes
    assertFalse { follows.holds(ctx, TickDataUnitSeconds(0.0), vehicle0.id, vehicle1.id) }
    assertFalse {
      follows.holds(ctx, tickDataList.first().vehicles[0], tickDataList.first().vehicles[1])
    }
    assertFalse { follows.holds(ctx, TickDataUnitSeconds(0.0), vehicle1.id, vehicle0.id) }
    assertFalse {
      follows.holds(ctx, tickDataList.first().vehicles[1], tickDataList.first().vehicles[0])
    }
  }

  @Test
  fun testFollowsWithDifferentRoads() {
    val tickDataList = mutableListOf<TickData>()

    for (i in 0..30) {
      val v0 = Vehicle(isEgo = true, id = vehicle0.id, positionOnLane = i.toDouble(), lane = lane1)
      val v1 =
          Vehicle(isEgo = false, id = vehicle1.id, positionOnLane = i.toDouble(), lane = laneRoad2)
      val v2 =
          Vehicle(
              isEgo = false,
              id = vehicle2.id,
              positionOnLane = i.toDouble() + 10.0,
              lane = laneRoad2)

      val tickData =
          TickData(
              currentTick = TickDataUnitSeconds(i.toDouble()),
              blocks = listOf(block),
              entities = listOf(v0, v1, v2))
      v0.tickData = tickData
      v1.tickData = tickData
      v2.tickData = tickData

      tickDataList.add(tickData)
    }

    val segment = Segment(tickDataList, segmentSource = "")
    val ctx = PredicateContext(segment)

    // v0 does not follow v1 or v2 across roads
    assertFalse { follows.holds(ctx, TickDataUnitSeconds(0.0), vehicle0.id, vehicle1.id) }
    assertFalse { follows.holds(ctx, TickDataUnitSeconds(0.0), vehicle0.id, vehicle2.id) }
    // v1 follows v2 on the same road
    assertTrue { follows.holds(ctx, TickDataUnitSeconds(0.0), vehicle1.id, vehicle2.id) }
  }

  @Test
  fun testFollowsSuccessorLane() {
    val tickDataList = mutableListOf<TickData>()

    for (i in 0..30) {
      val v1 = Vehicle(isEgo = true, id = vehicle1.id, positionOnLane = i.toDouble(), lane = lane1)
      val v2 =
          Vehicle(
              isEgo = false,
              id = vehicle2.id,
              positionOnLane = i.toDouble(),
              lane = laneSuccessorRoad)

      val tickData =
          TickData(
              currentTick = TickDataUnitSeconds(i.toDouble()),
              blocks = listOf(block),
              entities = listOf(v1, v2))
      v1.tickData = tickData
      v2.tickData = tickData

      tickDataList.add(tickData)
    }

    val segment = Segment(tickDataList, segmentSource = "")
    val ctx = PredicateContext(segment)

    // v1 follows v2 via successor lane relation
    assertTrue { follows.holds(ctx, TickDataUnitSeconds(0.0), vehicle1.id, vehicle2.id) }
    assertTrue {
      follows.holds(ctx, tickDataList.first().vehicles[0], tickDataList.first().vehicles[1])
    }
    // v2 does not follow v1
    assertFalse { follows.holds(ctx, TickDataUnitSeconds(0.0), vehicle2.id, vehicle1.id) }
  }

  @Test
  fun testFollowsSuccessorLaneWithSomeoneBetween() {
    val tickDataList = mutableListOf<TickData>()

    for (i in 0..30) {
      val v0 = Vehicle(isEgo = true, id = vehicle0.id, positionOnLane = i.toDouble(), lane = lane1)
      val v1 =
          Vehicle(
              isEgo = false, id = vehicle1.id, positionOnLane = i.toDouble() + 10.0, lane = lane1)
      val v2 =
          Vehicle(
              isEgo = false,
              id = vehicle2.id,
              positionOnLane = i.toDouble(),
              lane = laneSuccessorRoad)

      val tickData =
          TickData(
              currentTick = TickDataUnitSeconds(i.toDouble()),
              blocks = listOf(block),
              entities = listOf(v0, v1, v2))
      v0.tickData = tickData
      v1.tickData = tickData
      v2.tickData = tickData

      tickDataList.add(tickData)
    }

    val segment = Segment(tickDataList, segmentSource = "")
    val ctx = PredicateContext(segment)

    // v0 follows v1 (same lane, v1 ahead)
    assertTrue { follows.holds(ctx, TickDataUnitSeconds(0.0), vehicle0.id, vehicle1.id) }
    // v1 follows v2 (successor lane)
    assertTrue { follows.holds(ctx, TickDataUnitSeconds(0.0), vehicle1.id, vehicle2.id) }
    // v0 does NOT follow v2 directly
    assertFalse { follows.holds(ctx, TickDataUnitSeconds(0.0), vehicle0.id, vehicle2.id) }
  }
}
