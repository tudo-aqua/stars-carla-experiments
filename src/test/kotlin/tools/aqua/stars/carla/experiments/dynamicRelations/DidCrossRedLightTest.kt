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

import kotlin.collections.listOf
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import tools.aqua.stars.carla.experiments.didCrossRedLight
import tools.aqua.stars.carla.experiments.hasRedLight
import tools.aqua.stars.carla.experiments.hasRelevantRedLight
import tools.aqua.stars.carla.experiments.isAtEndOfRoad
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
import tools.aqua.stars.logic.kcmftbl.next

class DidCrossRedLightTest {
  private lateinit var block0: Block

  private val staticTrafficLight01 =
      StaticTrafficLight(
          id = 10, location = Location(), rotation = Rotation(), stopLocations = listOf())
  private val staticTrafficLight02 =
      StaticTrafficLight(
          id = 11, location = Location(), rotation = Rotation(), stopLocations = listOf())
  private val staticTrafficLight11 =
      StaticTrafficLight(
          id = 12, location = Location(), rotation = Rotation(), stopLocations = listOf())

  private lateinit var road0: Road
  private lateinit var road0lane1: Lane
  private lateinit var road0lane2: Lane

  private lateinit var singleRoad: Road
  private lateinit var contactLaneInfo: ContactLaneInfo
  private lateinit var roadLane1: Lane

  private lateinit var road1: Road
  private lateinit var road1lane1: Lane

  private lateinit var blocks: List<Block>
  private val egoId = 0

  @BeforeTest
  fun setup() {
    road0lane1 = Lane(laneId = 1, laneLength = 50.0, trafficLights = listOf(staticTrafficLight01))
    road0lane2 = Lane(laneId = 2, laneLength = 50.0, trafficLights = listOf(staticTrafficLight02))
    road1lane1 = Lane(laneId = 1, laneLength = 50.0, trafficLights = listOf(staticTrafficLight11))

    contactLaneInfo = ContactLaneInfo(lane = road0lane1)
    roadLane1 = Lane(laneId = 1, laneLength = 50.0, successorLanes = listOf(contactLaneInfo))

    road0 = Road(id = 0, lanes = listOf(road0lane1, road0lane2))
    singleRoad = Road(id = -1, lanes = listOf(roadLane1))
    road1 = Road(id = 1, lanes = listOf(road1lane1))

    block0 = Block(id = "1", roads = listOf(singleRoad, road1))

    road0.block = block0
    singleRoad.block = block0
    road1.block = block0

    blocks = listOf(block0)
  }

  @Test
  fun crossedRedLight() {

    val trafficLight =
        TrafficLight(
            id = 1, relatedOpenDriveId = staticTrafficLight01.id, state = TrafficLightState.Red)

    val ego0 =
        Vehicle(
            id = egoId,
            isEgo = true,
            lane = roadLane1,
            positionOnLane = road0lane1.laneLength - 1.0)
    val tickData0 =
        TickData(
            currentTick = TickDataUnitSeconds(0.0),
            blocks = blocks,
            trafficLights = listOf(trafficLight),
            entities = listOf(ego0))
    ego0.tickData = tickData0
    ego0.setVelocityFromEffVelocityMPH(0.0)

    val segment0 = Segment(listOf(tickData0), segmentSource = "")
    val ctx0 = PredicateContext(segment0)

    assertTrue { hasRedLight.holds(ctx0, ego0) }
    assertTrue { isAtEndOfRoad.holds(ctx0, ego0) }
    assertTrue { hasRelevantRedLight.holds(ctx0, ego0) }
    assertFalse { next(ego0) { v2 -> ego0.lane.road == v2.lane.road } }
    assertFalse { didCrossRedLight.holds(ctx0, ego0) }

    val ego1 = Vehicle(id = egoId, isEgo = true, lane = road0lane1, positionOnLane = 0.0)
    val tickData1 =
        TickData(
            currentTick = TickDataUnitSeconds(1.0),
            blocks = blocks,
            trafficLights = listOf(trafficLight),
            entities = listOf(ego1))
    ego1.tickData = tickData1
    ego1.setVelocityFromEffVelocityMPH(0.0)

    val segment = Segment(listOf(tickData0, tickData1), segmentSource = "")
    val ctx = PredicateContext(segment)

    assertTrue { hasRedLight.holds(ctx0, ego0) }
    assertTrue { isAtEndOfRoad.holds(ctx0, ego0) }
    assertTrue { hasRelevantRedLight.holds(ctx0, ego0) }
    assertTrue { next(ego0) { v2 -> ego0.lane.road != v2.lane.road } }
    assertTrue { didCrossRedLight.holds(ctx, TickDataUnitSeconds(0.0), egoId) }
  }

  @Test
  fun didNotCrossRedLight() {
    val trafficLight =
        TrafficLight(
            id = 1, relatedOpenDriveId = staticTrafficLight01.id, state = TrafficLightState.Red)

    val ego0 =
        Vehicle(
            id = egoId,
            isEgo = true,
            lane = roadLane1,
            positionOnLane = road0lane1.laneLength - 1.0)
    val tickData0 =
        TickData(
            currentTick = TickDataUnitSeconds(0.0),
            blocks = blocks,
            trafficLights = listOf(trafficLight),
            entities = listOf(ego0))
    ego0.tickData = tickData0
    ego0.setVelocityFromEffVelocityMPH(0.0)

    val segment0 = Segment(listOf(tickData0), segmentSource = "")
    val ctx0 = PredicateContext(segment0)

    assertTrue { hasRedLight.holds(ctx0, ego0) }
    assertTrue { isAtEndOfRoad.holds(ctx0, ego0) }
    assertTrue { hasRelevantRedLight.holds(ctx0, ego0) }
    assertFalse { next(ego0) { v2 -> ego0.lane.road != v2.lane.road } }
    assertFalse { didCrossRedLight.holds(ctx0, ego0) }

    val ego1 = Vehicle(id = egoId, isEgo = true, lane = roadLane1, positionOnLane = 0.0)
    val tickData1 =
        TickData(
            currentTick = TickDataUnitSeconds(1.0),
            blocks = blocks,
            trafficLights = listOf(trafficLight),
            entities = listOf(ego1))
    ego1.tickData = tickData1
    ego1.setVelocityFromEffVelocityMPH(0.0)

    val segment = Segment(listOf(tickData0, tickData1), segmentSource = "")
    val ctx = PredicateContext(segment)

    assertTrue { hasRedLight.holds(ctx0, ego0) }
    assertTrue { isAtEndOfRoad.holds(ctx0, ego0) }
    assertTrue { hasRelevantRedLight.holds(ctx0, ego0) }
    assertFalse { next(ego0) { v2 -> ego0.lane.road != v2.lane.road } }
    assertFalse { didCrossRedLight.holds(ctx, TickDataUnitSeconds(0.0), egoId) }
  }

  @Test
  fun crossedGreenLight() {

    val trafficLight =
        TrafficLight(
            id = 1, relatedOpenDriveId = staticTrafficLight01.id, state = TrafficLightState.Green)

    val ego0 =
        Vehicle(
            id = egoId,
            isEgo = true,
            lane = roadLane1,
            positionOnLane = road0lane1.laneLength - 1.0)
    val tickData0 =
        TickData(
            currentTick = TickDataUnitSeconds(0.0),
            blocks = blocks,
            trafficLights = listOf(trafficLight),
            entities = listOf(ego0))
    ego0.tickData = tickData0
    ego0.setVelocityFromEffVelocityMPH(0.0)

    val ctx0 = PredicateContext(Segment(listOf(tickData0), segmentSource = ""))
    assertFalse { hasRedLight.holds(ctx0, ego0) }
    assertTrue { isAtEndOfRoad.holds(ctx0, ego0) }
    assertFalse { hasRelevantRedLight.holds(ctx0, ego0) }
    assertFalse { next(ego0) { v2 -> ego0.lane.road != v2.lane.road } }
    assertFalse { didCrossRedLight.holds(ctx0, ego0) }

    val ego1 = Vehicle(id = egoId, isEgo = true, lane = road0lane1, positionOnLane = 0.0)
    val tickData1 =
        TickData(
            currentTick = TickDataUnitSeconds(1.0),
            blocks = blocks,
            trafficLights = listOf(trafficLight),
            entities = listOf(ego1))
    ego1.tickData = tickData1

    val ctx = PredicateContext(Segment(listOf(tickData0, tickData1), segmentSource = ""))
    assertFalse { hasRedLight.holds(ctx, ego0) }
    assertTrue { isAtEndOfRoad.holds(ctx0, ego0) }
    assertFalse { hasRelevantRedLight.holds(ctx, ego0) }
    assertTrue { next(ego0) { v2 -> ego0.lane.road != v2.lane.road } }
    assertFalse { didCrossRedLight.holds(ctx, TickDataUnitSeconds(0.0), egoId) }
  }

  @Test
  fun crossedYellowLight() {
    val trafficLight =
        TrafficLight(
            id = 1, relatedOpenDriveId = staticTrafficLight01.id, state = TrafficLightState.Yellow)

    val ego0 =
        Vehicle(
            id = egoId,
            isEgo = true,
            lane = roadLane1,
            positionOnLane = road0lane1.laneLength - 1.0)
    val tickData0 =
        TickData(
            currentTick = TickDataUnitSeconds(0.0),
            blocks = blocks,
            trafficLights = listOf(trafficLight),
            entities = listOf(ego0))
    ego0.tickData = tickData0
    ego0.setVelocityFromEffVelocityMPH(0.0)

    val ctx0 = PredicateContext(Segment(listOf(tickData0), segmentSource = ""))
    assertFalse { hasRedLight.holds(ctx0, ego0) }
    assertTrue { isAtEndOfRoad.holds(ctx0, ego0) }
    assertFalse { hasRelevantRedLight.holds(ctx0, ego0) }
    assertFalse { next(ego0) { v2 -> ego0.lane.road != v2.lane.road } }
    assertFalse { didCrossRedLight.holds(ctx0, ego0) }

    val ego1 = Vehicle(id = egoId, isEgo = true, lane = road0lane1, positionOnLane = 0.0)
    val tickData1 =
        TickData(
            currentTick = TickDataUnitSeconds(1.0),
            blocks = blocks,
            trafficLights = listOf(trafficLight),
            entities = listOf(ego1))
    ego1.tickData = tickData1

    val ctx = PredicateContext(Segment(listOf(tickData0, tickData1), segmentSource = ""))
    assertFalse { hasRedLight.holds(ctx0, ego0) }
    assertTrue { isAtEndOfRoad.holds(ctx0, ego0) }
    assertFalse { hasRelevantRedLight.holds(ctx, ego0) }
    assertTrue { next(ego0) { v2 -> ego0.lane.road != v2.lane.road } }
    assertFalse { didCrossRedLight.holds(ctx, TickDataUnitSeconds(0.0), egoId) }
  }

  @Test
  fun crossedUnknownLight() {
    val trafficLight =
        TrafficLight(
            id = 1, relatedOpenDriveId = staticTrafficLight01.id, state = TrafficLightState.Unknown)

    val ego0 =
        Vehicle(
            id = egoId,
            isEgo = true,
            lane = roadLane1,
            positionOnLane = road0lane1.laneLength - 1.0)
    val tickData0 =
        TickData(
            currentTick = TickDataUnitSeconds(0.0),
            blocks = blocks,
            trafficLights = listOf(trafficLight),
            entities = listOf(ego0))
    ego0.tickData = tickData0
    ego0.setVelocityFromEffVelocityMPH(0.0)

    val ctx0 = PredicateContext(Segment(listOf(tickData0), segmentSource = ""))
    assertFalse { hasRedLight.holds(ctx0, ego0) }
    assertTrue { isAtEndOfRoad.holds(ctx0, ego0) }
    assertFalse { hasRelevantRedLight.holds(ctx0, ego0) }
    assertFalse { next(ego0) { v2 -> ego0.lane.road != v2.lane.road } }
    assertFalse { didCrossRedLight.holds(ctx0, ego0) }

    val ego1 = Vehicle(id = egoId, isEgo = true, lane = road0lane1, positionOnLane = 0.0)
    val tickData1 =
        TickData(
            currentTick = TickDataUnitSeconds(1.0),
            blocks = blocks,
            trafficLights = listOf(trafficLight),
            entities = listOf(ego1))
    ego1.tickData = tickData1

    val ctx = PredicateContext(Segment(listOf(tickData0, tickData1), segmentSource = ""))
    assertFalse { hasRedLight.holds(ctx0, ego0) }
    assertTrue { isAtEndOfRoad.holds(ctx0, ego0) }
    assertFalse { hasRelevantRedLight.holds(ctx, ego0) }
    assertTrue { next(ego0) { v2 -> ego0.lane.road != v2.lane.road } }
    assertFalse { didCrossRedLight.holds(ctx, TickDataUnitSeconds(0.0), egoId) }
  }
}
