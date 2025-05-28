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
import tools.aqua.stars.carla.experiments.hasStopSign
import tools.aqua.stars.core.evaluation.PredicateContext
import tools.aqua.stars.data.av.dataclasses.Block
import tools.aqua.stars.data.av.dataclasses.ContactLaneInfo
import tools.aqua.stars.data.av.dataclasses.Landmark
import tools.aqua.stars.data.av.dataclasses.LandmarkType
import tools.aqua.stars.data.av.dataclasses.Lane
import tools.aqua.stars.data.av.dataclasses.Location
import tools.aqua.stars.data.av.dataclasses.Road
import tools.aqua.stars.data.av.dataclasses.Rotation
import tools.aqua.stars.data.av.dataclasses.Segment
import tools.aqua.stars.data.av.dataclasses.TickData
import tools.aqua.stars.data.av.dataclasses.TickDataUnitSeconds
import tools.aqua.stars.data.av.dataclasses.Vehicle

class HasStopSignTest {
  private val stopSign1 =
      Landmark(
          id = 11,
          name = "",
          distance = 50.0,
          s = 50.0,
          country = "",
          type = LandmarkType.StopSign,
          value = 0.0,
          unit = "",
          text = "",
          location = Location(),
          rotation = Rotation())
  private val stopSign2 =
      Landmark(
          id = 12,
          name = "",
          distance = 50.0,
          s = 50.0,
          country = "",
          type = LandmarkType.StopSign,
          value = 0.0,
          unit = "",
          text = "",
          location = Location(),
          rotation = Rotation())

  private lateinit var road0: Road
  private lateinit var road0lane1: Lane

  private lateinit var road1: Road
  private lateinit var road1lane1: Lane
  private lateinit var road1lane2: Lane

  private lateinit var road2: Road
  private lateinit var road2lane1: Lane

  private lateinit var road3: Road
  private lateinit var road3lane1: Lane

  private lateinit var block: Block
  private lateinit var blocks: List<Block>

  @BeforeTest
  fun setup() {
    road0lane1 = Lane(laneId = 1, laneLength = 50.0)
    road1lane1 = Lane(laneId = 1, laneLength = 50.0, landmarks = listOf(stopSign1))
    road1lane2 = Lane(laneId = 2, laneLength = 50.0)
    road2lane1 = Lane(laneId = 1, laneLength = 50.0, landmarks = listOf(stopSign2))
    road3lane1 = Lane(laneId = 1, laneLength = 50.0)

    road0 = Road(id = 0, lanes = listOf(road0lane1)).apply { road0lane1.road = this }
    road1 =
        Road(id = 1, lanes = listOf(road1lane1, road1lane2)).apply {
          road1lane1.road = this
          road1lane2.road = this
        }
    road2 = Road(id = 2, lanes = listOf(road2lane1)).apply { road2lane1.road = this }
    road3 = Road(id = 3, lanes = listOf(road3lane1)).apply { road3lane1.road = this }

    block = Block(roads = listOf(road0, road1, road2, road3))
    blocks = listOf(block)

    road0lane1.successorLanes = listOf(ContactLaneInfo(road1lane1))
    road1lane1.predecessorLanes = listOf(ContactLaneInfo(road0lane1))
    road2lane1.successorLanes = listOf(ContactLaneInfo(road3lane1))
    road3lane1.predecessorLanes = listOf(ContactLaneInfo(road2lane1))
  }

  @Test
  fun laneHasStopSignIsAtStart() {

    val ego = Vehicle(id = 0, isEgo = true, lane = road1lane1, positionOnLane = 0.0)

    // 2) tickData with ego
    val tickData =
        TickData(currentTick = TickDataUnitSeconds(0.0), blocks = blocks, entities = listOf(ego))

    // 3) back‐ref + speed
    ego.tickData = tickData
    ego.setVelocityFromEffVelocityMPH(11.0)

    // 4) assert
    val ctx = PredicateContext(Segment(listOf(tickData), segmentSource = ""))
    assertTrue { hasStopSign.holds(ctx, ego) }
  }

  @Test
  fun laneHasStopSignIsAtEnd() {
    val ego =
        Vehicle(
            id = 0, isEgo = true, lane = road1lane1, positionOnLane = road0lane1.laneLength - 1.0)
    val tickData =
        TickData(currentTick = TickDataUnitSeconds(0.0), blocks = blocks, entities = listOf(ego))
    ego.tickData = tickData
    ego.setVelocityFromEffVelocityMPH(11.0)

    val ctx = PredicateContext(Segment(listOf(tickData), segmentSource = ""))
    assertTrue { hasStopSign.holds(ctx, ego) }
  }

  @Test
  fun laneHasNoStopSignIsAtStart() {
    val ego = Vehicle(id = 0, isEgo = true, lane = road0lane1, positionOnLane = 0.0)
    val tickData =
        TickData(currentTick = TickDataUnitSeconds(0.0), blocks = blocks, entities = listOf(ego))
    ego.tickData = tickData
    ego.setVelocityFromEffVelocityMPH(11.0)

    val ctx = PredicateContext(Segment(listOf(tickData), segmentSource = ""))
    assertFalse { hasStopSign.holds(ctx, ego) }
  }

  @Test
  fun laneHasNoStopSignIsAtEnd() {
    val ego =
        Vehicle(
            id = 0, isEgo = true, lane = road0lane1, positionOnLane = road0lane1.laneLength - 1.0)
    val tickData =
        TickData(currentTick = TickDataUnitSeconds(0.0), blocks = blocks, entities = listOf(ego))
    ego.tickData = tickData
    ego.setVelocityFromEffVelocityMPH(11.0)

    val ctx = PredicateContext(Segment(listOf(tickData), segmentSource = ""))
    assertFalse { hasStopSign.holds(ctx, ego) }
  }

  @Test
  fun successorLaneHasStopSign() {
    val ego = Vehicle(id = 0, isEgo = true, lane = road0lane1, positionOnLane = 0.0)
    val tickData =
        TickData(currentTick = TickDataUnitSeconds(0.0), blocks = blocks, entities = listOf(ego))
    ego.tickData = tickData
    ego.setVelocityFromEffVelocityMPH(11.0)

    val ctx = PredicateContext(Segment(listOf(tickData), segmentSource = ""))
    assertFalse { hasStopSign.holds(ctx, ego) }
  }

  @Test
  fun predecessorLaneHasStopSign() {
    val ego = Vehicle(id = 0, isEgo = true, lane = road3lane1, positionOnLane = 0.0)
    val tickData =
        TickData(currentTick = TickDataUnitSeconds(0.0), blocks = blocks, entities = listOf(ego))
    ego.tickData = tickData
    ego.setVelocityFromEffVelocityMPH(11.0)

    val ctx = PredicateContext(Segment(listOf(tickData), segmentSource = ""))
    assertFalse { hasStopSign.holds(ctx, ego) }
  }

  @Test
  fun otherLaneOfOtherRoadHasStopSign() {

    val ego =
        Vehicle(
            id = 0, isEgo = true, lane = road1lane2, positionOnLane = road0lane1.laneLength - 1.0)

    // 2) tickData with that vehicle
    val tickData =
        TickData(currentTick = TickDataUnitSeconds(0.0), blocks = blocks, entities = listOf(ego))

    // 3) wire back-ref and set speed
    ego.tickData = tickData
    ego.setVelocityFromEffVelocityMPH(11.0)

    // 4) assertion
    val ctx = PredicateContext(Segment(listOf(tickData), segmentSource = ""))
    assertFalse { hasStopSign.holds(ctx, ego) }
  }

  @Test
  fun drivesIntoLaneWithStopSign() {
    // tick 0: on a lane without a stop sign
    val ego0 =
        Vehicle(
            id = 0, isEgo = true, lane = road0lane1, positionOnLane = road0lane1.laneLength - 1.0)
    val tickData0 =
        TickData(currentTick = TickDataUnitSeconds(0.0), blocks = blocks, entities = listOf(ego0))
    ego0.tickData = tickData0
    ego0.setVelocityFromEffVelocityMPH(11.0)
    val ctx0 = PredicateContext(Segment(listOf(tickData0), segmentSource = ""))
    assertFalse { hasStopSign.holds(ctx0, ego0) }

    // tick 1: on a lane with a stop sign
    val ego1 = Vehicle(id = 0, isEgo = true, lane = road1lane1, positionOnLane = 0.0)
    val tickData1 =
        TickData(currentTick = TickDataUnitSeconds(1.0), blocks = blocks, entities = listOf(ego1))
    ego1.tickData = tickData1
    ego1.setVelocityFromEffVelocityMPH(11.0)
    val ctx1 = PredicateContext(Segment(listOf(tickData1), segmentSource = ""))
    assertTrue { hasStopSign.holds(ctx1, ego1) }

    // full segment check
    val segment = Segment(listOf(tickData0, tickData1), segmentSource = "")
    val ctx = PredicateContext(segment)
    assertTrue { hasStopSign.holds(ctx, TickDataUnitSeconds(0.0), 0) }
  }

  @Test
  fun drivesFromLaneWithStopSign() {
    // tick 0: start on lane WITH a stop sign
    val ego0 =
        Vehicle(
            id = 0, isEgo = true, lane = road2lane1, positionOnLane = road2lane1.laneLength - 1.0)
    val tickData0 =
        TickData(currentTick = TickDataUnitSeconds(0.0), blocks = blocks, entities = listOf(ego0))
    ego0.tickData = tickData0
    ego0.setVelocityFromEffVelocityMPH(11.0)
    val ctx0 = PredicateContext(Segment(listOf(tickData0), segmentSource = ""))
    assertTrue { hasStopSign.holds(ctx0, ego0) }

    // tick 1: then move onto a lane without a stop sign
    val ego1 = Vehicle(id = 0, isEgo = true, lane = road3lane1, positionOnLane = 0.0)
    val tickData1 =
        TickData(currentTick = TickDataUnitSeconds(1.0), blocks = blocks, entities = listOf(ego1))
    ego1.tickData = tickData1
    ego1.setVelocityFromEffVelocityMPH(11.0)
    val ctx1 = PredicateContext(Segment(listOf(tickData1), segmentSource = ""))
    assertFalse { hasStopSign.holds(ctx1, ego1) }

    // full segment check
    val segment = Segment(listOf(tickData0, tickData1), segmentSource = "")
    val ctx = PredicateContext(segment)
    assertTrue { hasStopSign.holds(ctx, TickDataUnitSeconds(0.0), 0) }
  }
}
