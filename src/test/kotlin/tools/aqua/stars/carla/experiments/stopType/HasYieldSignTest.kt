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
import tools.aqua.stars.carla.experiments.hasYieldSign
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
          location = Location(),
          rotation = Rotation())
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
    road1lane1 = Lane(laneId = 1, laneLength = 50.0, landmarks = listOf(yieldSign1))
    road1lane2 = Lane(laneId = 2, laneLength = 50.0)
    road2lane1 = Lane(laneId = 1, laneLength = 50.0, landmarks = listOf(yieldSign2))
    road3lane1 = Lane(laneId = 1, laneLength = 50.0)

    road0 = Road(id = 0, lanes = listOf(road0lane1)).apply { road0lane1.road = this }
    road1 =
        Road(id = 1, lanes = listOf(road1lane1, road1lane2)).apply {
          road1lane1.road = this
          road1lane2.road = this
        }
    road2 = Road(id = 2, lanes = listOf(road2lane1)).apply { road2lane1.road = this }
    road3 = Road(id = 2, lanes = listOf(road3lane1)).apply { road3lane1.road = this }

    block = Block(roads = listOf(road0, road1, road2, road3))
    blocks = listOf(block)

    road0lane1.successorLanes = listOf(ContactLaneInfo(road1lane1))
    road1lane1.predecessorLanes = listOf(ContactLaneInfo(road0lane1))
    road2lane1.successorLanes = listOf(ContactLaneInfo(road3lane1))
    road3lane1.predecessorLanes = listOf(ContactLaneInfo(road2lane1))
  }

  @Test
  fun laneHasYieldSignIsAtStart() {

    val ego = Vehicle(id = 0, isEgo = true, lane = road1lane1, positionOnLane = 0.0)

    // 2) single TickData
    val td =
        TickData(currentTick = TickDataUnitSeconds(0.0), blocks = blocks, entities = listOf(ego))

    // 3) wire back references & velocity
    ego.tickData = td
    ego.setVelocityFromEffVelocityMPH(11.0)

    // 4) assert
    val ctx = PredicateContext(Segment(listOf(td), segmentSource = ""))
    assertTrue { hasYieldSign.holds(ctx, ego) }
  }

  @Test
  fun laneHasYieldSignIsAtEnd() {

    val ego =
        Vehicle(
            id = 0, isEgo = true, lane = road1lane1, positionOnLane = road0lane1.laneLength - 1.0)

    // 2) single TickData
    val td =
        TickData(currentTick = TickDataUnitSeconds(0.0), blocks = blocks, entities = listOf(ego))

    // 3) wire back references & velocity
    ego.tickData = td
    ego.setVelocityFromEffVelocityMPH(11.0)

    // 4) assert
    val ctx = PredicateContext(Segment(listOf(td), segmentSource = ""))
    assertTrue { hasYieldSign.holds(ctx, ego) }
  }

  @Test
  fun laneHasNoYieldSignIsAtStart() {

    val ego = Vehicle(id = 0, isEgo = true, lane = road0lane1, positionOnLane = 0.0)

    // 2) single TickData
    val td =
        TickData(currentTick = TickDataUnitSeconds(0.0), blocks = blocks, entities = listOf(ego))

    // 3) wire back references & velocity
    ego.tickData = td
    ego.setVelocityFromEffVelocityMPH(11.0)

    // 4) assert
    val ctx = PredicateContext(Segment(listOf(td), segmentSource = ""))
    assertFalse { hasYieldSign.holds(ctx, ego) }
  }

  @Test
  fun laneHasNoYieldSignIsAtEnd() {

    val ego =
        Vehicle(
            id = 0, isEgo = true, lane = road0lane1, positionOnLane = road0lane1.laneLength - 1.0)

    // 2) single TickData
    val td =
        TickData(currentTick = TickDataUnitSeconds(0.0), blocks = blocks, entities = listOf(ego))

    // 3) wire back references & velocity
    ego.tickData = td
    ego.setVelocityFromEffVelocityMPH(11.0)

    // 4) assert
    val ctx = PredicateContext(Segment(listOf(td), segmentSource = ""))
    assertFalse { hasYieldSign.holds(ctx, ego) }
  }

  @Test
  fun drivesIntoLaneWithYieldSign() {

    val ego0 =
        Vehicle(
            id = 0, isEgo = true, lane = road0lane1, positionOnLane = road0lane1.laneLength - 1.0)
    val ego1 = Vehicle(id = 0, isEgo = true, lane = road1lane1, positionOnLane = 0.0)

    val td0 =
        TickData(currentTick = TickDataUnitSeconds(0.0), blocks = blocks, entities = listOf(ego0))
    ego0.tickData = td0
    ego0.setVelocityFromEffVelocityMPH(11.0)

    val segment0 = Segment(listOf(td0), segmentSource = "")
    val ctx0 = PredicateContext(segment0)
    assertFalse { hasYieldSign.holds(ctx0, TickDataUnitSeconds(0.0), 0) }

    val td1 =
        TickData(currentTick = TickDataUnitSeconds(1.0), blocks = blocks, entities = listOf(ego1))
    ego1.tickData = td1
    ego1.setVelocityFromEffVelocityMPH(11.0)

    val segment = Segment(listOf(td0, td1), segmentSource = "")
    val ctx = PredicateContext(segment)

    assertTrue { hasYieldSign.holds(ctx, TickDataUnitSeconds(1.0), 0) }
    // and overall:
    assertTrue { hasYieldSign.holds(ctx, TickDataUnitSeconds(0.0), 0) }
  }

  @Test
  fun drivesFromLaneWithYieldSign() {

    val ego0 =
        Vehicle(
            id = 0, isEgo = true, lane = road2lane1, positionOnLane = road2lane1.laneLength - 1.0)
    val ego1 = Vehicle(id = 0, isEgo = true, lane = road3lane1, positionOnLane = 0.0)

    // 2) first TickData (start in yield-sign lane)
    val td0 =
        TickData(currentTick = TickDataUnitSeconds(0.0), blocks = blocks, entities = listOf(ego0))
    ego0.tickData = td0
    ego0.setVelocityFromEffVelocityMPH(11.0)

    // 3) second TickData (move out of yield-sign lane)
    val td1 =
        TickData(currentTick = TickDataUnitSeconds(1.0), blocks = blocks, entities = listOf(ego1))
    ego1.tickData = td1
    ego1.setVelocityFromEffVelocityMPH(11.0)

    // 4) full segment & assertions
    val segment = Segment(listOf(td0, td1), segmentSource = "")
    val ctx = PredicateContext(segment)

    assertTrue { hasYieldSign.holds(ctx, TickDataUnitSeconds(0.0), 0) }
  }
}
