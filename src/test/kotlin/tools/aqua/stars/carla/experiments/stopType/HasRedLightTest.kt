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
import tools.aqua.stars.carla.experiments.hasRedLight
import tools.aqua.stars.core.evaluation.PredicateContext
import tools.aqua.stars.data.av.dataclasses.Block
import tools.aqua.stars.data.av.dataclasses.ContactLaneInfo
import tools.aqua.stars.data.av.dataclasses.Lane
import tools.aqua.stars.data.av.dataclasses.Location
import tools.aqua.stars.data.av.dataclasses.Road
import tools.aqua.stars.data.av.dataclasses.Rotation
import tools.aqua.stars.data.av.dataclasses.Segment
import tools.aqua.stars.data.av.dataclasses.StaticTrafficLight
import tools.aqua.stars.data.av.dataclasses.TickData
import tools.aqua.stars.data.av.dataclasses.TickDataUnitSeconds
import tools.aqua.stars.data.av.dataclasses.TrafficLight
import tools.aqua.stars.data.av.dataclasses.TrafficLightState
import tools.aqua.stars.data.av.dataclasses.Vehicle

class HasRedLightTest {

  private val staticTrafficLight01 =
      StaticTrafficLight(
          id = 10, location = Location(), rotation = Rotation(), stopLocations = listOf())
  private val staticTrafficLight02 =
      StaticTrafficLight(
          id = 11, location = Location(), rotation = Rotation(), stopLocations = listOf())
  private val staticTrafficLight11 =
      StaticTrafficLight(
          id = 12, location = Location(), rotation = Rotation(), stopLocations = listOf())

  private lateinit var block0: Block

  private lateinit var road0: Road
  private lateinit var road0lane1: Lane
  private lateinit var road0lane2: Lane

  private lateinit var roadSingle: Road
  private lateinit var contactLaneInfo: ContactLaneInfo
  private lateinit var roadLane1: Lane

  private lateinit var road1: Road
  private lateinit var road1lane1: Lane

  private lateinit var blocks: List<Block>

  private val egoId = 0

  @BeforeTest
  fun setup() {
    // lanes for road0
    road0lane1 = Lane(laneId = 1, laneLength = 50.0, trafficLights = listOf(staticTrafficLight01))
    road0lane2 = Lane(laneId = 2, laneLength = 50.0, trafficLights = listOf(staticTrafficLight02))

    // lanes for roadSingle
    contactLaneInfo = ContactLaneInfo(lane = road0lane1)
    roadLane1 = Lane(laneId = 1, successorLanes = listOf(contactLaneInfo))

    // lane for road1
    road1lane1 = Lane(laneId = 1, laneLength = 50.0, trafficLights = listOf(staticTrafficLight11))

    roadSingle = Road(id = -1, lanes = listOf(roadLane1))
    roadLane1.road = roadSingle

    road0 = Road(id = 0, lanes = listOf(road0lane1, road0lane2))
    road0lane1.road = road0
    road0lane2.road = road0

    road1 = Road(id = 1, lanes = listOf(road1lane1))
    road1lane1.road = road1

    block0 = Block(id = "1", roads = listOf(road0, road1))

    road0.block = block0
    road1.block = block0

    blocks = listOf(block0)
  }

  @Test
  fun laneHasRedLight() {
    val trafficLight =
        TrafficLight(
            id = 1, relatedOpenDriveId = staticTrafficLight01.id, state = TrafficLightState.Red)

    val ego = Vehicle(id = egoId, isEgo = true, lane = road0lane1, positionOnLane = 0.0)

    // 2) create the TickData with that vehicle
    val tickData =
        TickData(
            currentTick = TickDataUnitSeconds(0.0),
            blocks = blocks,
            trafficLights = listOf(trafficLight),
            entities = listOf(ego))

    // 3) wire back–reference & set speed
    ego.tickData = tickData
    ego.setVelocityFromEffVelocityMPH(0.0)

    // 4) assert
    val ctx = PredicateContext(Segment(listOf(tickData), segmentSource = ""))
    assertFalse { hasRedLight.holds(ctx, ego) }
  }

  @Test
  fun laneHasGreenLight() {
    val trafficLight =
        TrafficLight(
            id = 1, relatedOpenDriveId = staticTrafficLight01.id, state = TrafficLightState.Green)

    val ego = Vehicle(id = egoId, isEgo = true, lane = road0lane1, positionOnLane = 0.0)
    val tickData =
        TickData(
            currentTick = TickDataUnitSeconds(0.0),
            blocks = blocks,
            trafficLights = listOf(trafficLight),
            entities = listOf(ego))
    ego.tickData = tickData
    ego.setVelocityFromEffVelocityMPH(0.0)

    val ctx = PredicateContext(Segment(listOf(tickData), segmentSource = ""))
    assertFalse { hasRedLight.holds(ctx, ego) }
  }

  @Test
  fun successorLaneHasRedLight() {
    val trafficLight =
        TrafficLight(
            id = 1, relatedOpenDriveId = staticTrafficLight01.id, state = TrafficLightState.Red)

    val ego = Vehicle(id = egoId, isEgo = true, lane = roadLane1, positionOnLane = 0.0)
    val tickData =
        TickData(
            currentTick = TickDataUnitSeconds(0.0),
            blocks = blocks,
            trafficLights = listOf(trafficLight),
            entities = listOf(ego))
    ego.tickData = tickData
    ego.setVelocityFromEffVelocityMPH(0.0)

    val ctx = PredicateContext(Segment(listOf(tickData), segmentSource = ""))
    assertTrue { hasRedLight.holds(ctx, ego) }
  }

  @Test
  fun successorLaneHasGreenLight() {
    val trafficLight =
        TrafficLight(
            id = 1, relatedOpenDriveId = staticTrafficLight01.id, state = TrafficLightState.Green)

    val ego = Vehicle(id = egoId, isEgo = true, lane = roadLane1, positionOnLane = 0.0)
    val tickData =
        TickData(
            currentTick = TickDataUnitSeconds(0.0),
            blocks = blocks,
            trafficLights = listOf(trafficLight),
            entities = listOf(ego))
    ego.tickData = tickData
    ego.setVelocityFromEffVelocityMPH(0.0)

    val ctx = PredicateContext(Segment(listOf(tickData), segmentSource = ""))
    assertFalse { hasRedLight.holds(ctx, ego) }
  }

  @Test
  fun differentLaneHasRedLight() {
    val trafficLight =
        TrafficLight(
            id = 2, relatedOpenDriveId = staticTrafficLight11.id, state = TrafficLightState.Green)

    val ego = Vehicle(id = egoId, isEgo = true, lane = roadLane1, positionOnLane = 0.0)
    val tickData =
        TickData(
            currentTick = TickDataUnitSeconds(0.0),
            blocks = blocks,
            trafficLights = listOf(trafficLight),
            entities = listOf(ego))
    ego.tickData = tickData
    ego.setVelocityFromEffVelocityMPH(0.0)

    val ctx = PredicateContext(Segment(listOf(tickData), segmentSource = ""))
    assertFalse { hasRedLight.holds(ctx, ego) }
  }
}
