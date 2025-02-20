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
import tools.aqua.stars.carla.experiments.didCrossRedLight
import tools.aqua.stars.carla.experiments.emptyBlock
import tools.aqua.stars.carla.experiments.emptyLane
import tools.aqua.stars.carla.experiments.emptyLocation
import tools.aqua.stars.carla.experiments.emptyRoad
import tools.aqua.stars.carla.experiments.emptyRotation
import tools.aqua.stars.carla.experiments.emptyTickData
import tools.aqua.stars.carla.experiments.emptyTrafficLight
import tools.aqua.stars.carla.experiments.emptyVehicle
import tools.aqua.stars.carla.experiments.hasRedLight
import tools.aqua.stars.carla.experiments.hasRelevantRedLight
import tools.aqua.stars.carla.experiments.isAtEndOfRoad
import tools.aqua.stars.core.evaluation.PredicateContext
import tools.aqua.stars.data.av.dataclasses.ContactLaneInfo
import tools.aqua.stars.data.av.dataclasses.Segment
import tools.aqua.stars.data.av.dataclasses.StaticTrafficLight
import tools.aqua.stars.data.av.dataclasses.TickDataUnitSeconds
import tools.aqua.stars.data.av.dataclasses.TrafficLightState
import tools.aqua.stars.logic.kcmftbl.next

class DidCrossRedLightTest {

  private val block = emptyBlock(id = "1")

  private val staticTrafficLight01 =
      StaticTrafficLight(
          id = 10, location = emptyLocation(), rotation = emptyRotation(), stopLocations = listOf())
  private val staticTrafficLight02 =
      StaticTrafficLight(
          id = 11, location = emptyLocation(), rotation = emptyRotation(), stopLocations = listOf())
  private val staticTrafficLight11 =
      StaticTrafficLight(
          id = 12, location = emptyLocation(), rotation = emptyRotation(), stopLocations = listOf())

  private val road0 = emptyRoad(id = 0, block = block)
  private val road0lane1 =
      emptyLane(
          laneId = 1,
          road = road0,
          laneLength = 50.0,
          staticTrafficLights = listOf(staticTrafficLight01))
  private val road0lane2 =
      emptyLane(
          laneId = 2,
          road = road0,
          laneLength = 50.0,
          staticTrafficLights = listOf(staticTrafficLight02))

  private val road = emptyRoad(id = -1, block = block)
  private val contactLaneInfo = ContactLaneInfo(lane = road0lane1)
  private val roadLane1 =
      emptyLane(
          laneId = 1, road = road, laneLength = 50.0, successorLanes = listOf(contactLaneInfo))

  private val road1 = emptyRoad(id = 1, block = block)
  private val road1lane1 =
      emptyLane(
          laneId = 1,
          road = road1,
          laneLength = 50.0,
          staticTrafficLights = listOf(staticTrafficLight11))

  private val blocks = listOf(block)

  private val egoId = 0

  @BeforeTest
  fun setup() {
    road0.lanes = listOf(road0lane1, road0lane2)
    road1.lanes = listOf(road1lane1)

    block.roads = listOf(road0, road1)
  }

  @Test
  fun crossedRedLight() {
    val trafficLight =
        emptyTrafficLight(
            id = 1, relatedOpenDriveId = staticTrafficLight01.id, state = TrafficLightState.Red)
    val tickData0 =
        emptyTickData(
            currentTick = TickDataUnitSeconds(0.0), blocks, trafficLights = listOf(trafficLight))
    val ego0 =
        emptyVehicle(
            id = egoId,
            egoVehicle = true,
            lane = roadLane1,
            positionOnLane = road0lane1.laneLength - 1.0,
            effVelocityMPH = 0.0,
            tickData = tickData0)
    tickData0.entities = listOf(ego0)

    val segment0 = Segment(listOf(tickData0), segmentSource = "")
    val ctx0 = PredicateContext(segment0)

    assert(hasRedLight.holds(ctx0, ego0))
    assert(isAtEndOfRoad.holds(ctx0, ego0))
    assert(hasRelevantRedLight.holds(ctx0, ego0))
    assert(!next(ego0) { v2 -> ego0.lane.road == v2.lane.road })
    assert(!didCrossRedLight.holds(ctx0, ego0))

    val tickData1 =
        emptyTickData(
            currentTick = TickDataUnitSeconds(1.0), blocks, trafficLights = listOf(trafficLight))
    val ego1 =
        emptyVehicle(
            id = egoId,
            egoVehicle = true,
            lane = road0lane1,
            positionOnLane = 0.0,
            tickData = tickData1)
    tickData1.entities = listOf(ego1)

    val segment = Segment(listOf(tickData0, tickData1), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(hasRedLight.holds(ctx0, ego0))
    assert(isAtEndOfRoad.holds(ctx0, ego0))
    assert(hasRelevantRedLight.holds(ctx0, ego0))
    assert(next(ego0) { v2 -> ego0.lane.road != v2.lane.road })
    assert(didCrossRedLight.holds(ctx, TickDataUnitSeconds(0.0), egoId))
  }

  @Test
  fun didNotCrossRedLight() {
    val trafficLight =
        emptyTrafficLight(
            id = 1, relatedOpenDriveId = staticTrafficLight01.id, state = TrafficLightState.Red)
    val tickData0 =
        emptyTickData(
            currentTick = TickDataUnitSeconds(0.0), blocks, trafficLights = listOf(trafficLight))
    val ego0 =
        emptyVehicle(
            id = egoId,
            egoVehicle = true,
            lane = roadLane1,
            positionOnLane = road0lane1.laneLength - 1.0,
            effVelocityMPH = 0.0,
            tickData = tickData0)
    tickData0.entities = listOf(ego0)

    val segment0 = Segment(listOf(tickData0), segmentSource = "")
    val ctx0 = PredicateContext(segment0)

    assert(hasRedLight.holds(ctx0, ego0))
    assert(isAtEndOfRoad.holds(ctx0, ego0))
    assert(hasRelevantRedLight.holds(ctx0, ego0))
    assert(!next(ego0) { v2 -> ego0.lane.road != v2.lane.road })
    assert(!didCrossRedLight.holds(ctx0, ego0))

    val tickData1 =
        emptyTickData(
            currentTick = TickDataUnitSeconds(1.0), blocks, trafficLights = listOf(trafficLight))
    val ego1 =
        emptyVehicle(
            id = egoId,
            egoVehicle = true,
            lane = roadLane1,
            positionOnLane = 0.0,
            tickData = tickData1)
    tickData1.entities = listOf(ego1)

    val segment = Segment(listOf(tickData0, tickData1), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(hasRedLight.holds(ctx0, ego0))
    assert(isAtEndOfRoad.holds(ctx0, ego0))
    assert(hasRelevantRedLight.holds(ctx0, ego0))
    assert(!next(ego0) { v2 -> ego0.lane.road != v2.lane.road })
    assert(!didCrossRedLight.holds(ctx, TickDataUnitSeconds(0.0), egoId))
  }

  @Test
  fun crossedGreenLight() {
    val trafficLight =
        emptyTrafficLight(
            id = 1, relatedOpenDriveId = staticTrafficLight01.id, state = TrafficLightState.Green)
    val tickData0 =
        emptyTickData(
            currentTick = TickDataUnitSeconds(0.0), blocks, trafficLights = listOf(trafficLight))
    val ego0 =
        emptyVehicle(
            id = egoId,
            egoVehicle = true,
            lane = roadLane1,
            positionOnLane = road0lane1.laneLength - 1.0,
            effVelocityMPH = 0.0,
            tickData = tickData0)
    tickData0.entities = listOf(ego0)

    val segment0 = Segment(listOf(tickData0), segmentSource = "")
    val ctx0 = PredicateContext(segment0)

    assert(!hasRedLight.holds(ctx0, ego0))
    assert(isAtEndOfRoad.holds(ctx0, ego0))
    assert(!hasRelevantRedLight.holds(ctx0, ego0))
    assert(!next(ego0) { v2 -> ego0.lane.road != v2.lane.road })
    assert(!didCrossRedLight.holds(ctx0, ego0))

    val tickData1 =
        emptyTickData(
            currentTick = TickDataUnitSeconds(1.0), blocks, trafficLights = listOf(trafficLight))
    val ego1 =
        emptyVehicle(
            id = egoId,
            egoVehicle = true,
            lane = road0lane1,
            positionOnLane = 0.0,
            tickData = tickData1)
    tickData1.entities = listOf(ego1)

    val segment = Segment(listOf(tickData0, tickData1), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(!hasRedLight.holds(ctx0, ego0))
    assert(isAtEndOfRoad.holds(ctx0, ego0))
    assert(!hasRelevantRedLight.holds(ctx0, ego0))
    assert(next(ego0) { v2 -> ego0.lane.road != v2.lane.road })
    assert(!didCrossRedLight.holds(ctx, TickDataUnitSeconds(0.0), egoId))
  }

  @Test
  fun crossedYellowLight() {
    val trafficLight =
        emptyTrafficLight(
            id = 1, relatedOpenDriveId = staticTrafficLight01.id, state = TrafficLightState.Yellow)
    val tickData0 =
        emptyTickData(
            currentTick = TickDataUnitSeconds(0.0), blocks, trafficLights = listOf(trafficLight))
    val ego0 =
        emptyVehicle(
            id = egoId,
            egoVehicle = true,
            lane = roadLane1,
            positionOnLane = road0lane1.laneLength - 1.0,
            effVelocityMPH = 0.0,
            tickData = tickData0)
    tickData0.entities = listOf(ego0)

    val segment0 = Segment(listOf(tickData0), segmentSource = "")
    val ctx0 = PredicateContext(segment0)

    assert(!hasRedLight.holds(ctx0, ego0))
    assert(isAtEndOfRoad.holds(ctx0, ego0))
    assert(!hasRelevantRedLight.holds(ctx0, ego0))
    assert(!next(ego0) { v2 -> ego0.lane.road != v2.lane.road })
    assert(!didCrossRedLight.holds(ctx0, ego0))

    val tickData1 =
        emptyTickData(
            currentTick = TickDataUnitSeconds(1.0), blocks, trafficLights = listOf(trafficLight))
    val ego1 =
        emptyVehicle(
            id = egoId,
            egoVehicle = true,
            lane = road0lane1,
            positionOnLane = 0.0,
            tickData = tickData1)
    tickData1.entities = listOf(ego1)

    val segment = Segment(listOf(tickData0, tickData1), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(!hasRedLight.holds(ctx0, ego0))
    assert(isAtEndOfRoad.holds(ctx0, ego0))
    assert(!hasRelevantRedLight.holds(ctx0, ego0))
    assert(next(ego0) { v2 -> ego0.lane.road != v2.lane.road })
    assert(!didCrossRedLight.holds(ctx, TickDataUnitSeconds(0.0), egoId))
  }

  @Test
  fun crossedUnknownLight() {
    val trafficLight =
        emptyTrafficLight(
            id = 1, relatedOpenDriveId = staticTrafficLight01.id, state = TrafficLightState.Unknown)
    val tickData0 =
        emptyTickData(
            currentTick = TickDataUnitSeconds(0.0), blocks, trafficLights = listOf(trafficLight))
    val ego0 =
        emptyVehicle(
            id = egoId,
            egoVehicle = true,
            lane = roadLane1,
            positionOnLane = road0lane1.laneLength - 1.0,
            effVelocityMPH = 0.0,
            tickData = tickData0)
    tickData0.entities = listOf(ego0)

    val segment0 = Segment(listOf(tickData0), segmentSource = "")
    val ctx0 = PredicateContext(segment0)

    assert(!hasRedLight.holds(ctx0, ego0))
    assert(isAtEndOfRoad.holds(ctx0, ego0))
    assert(!hasRelevantRedLight.holds(ctx0, ego0))
    assert(!next(ego0) { v2 -> ego0.lane.road != v2.lane.road })
    assert(!didCrossRedLight.holds(ctx0, ego0))

    val tickData1 =
        emptyTickData(
            currentTick = TickDataUnitSeconds(1.0), blocks, trafficLights = listOf(trafficLight))
    val ego1 =
        emptyVehicle(
            id = egoId,
            egoVehicle = true,
            lane = road0lane1,
            positionOnLane = 0.0,
            tickData = tickData1)
    tickData1.entities = listOf(ego1)

    val segment = Segment(listOf(tickData0, tickData1), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(!hasRedLight.holds(ctx0, ego0))
    assert(isAtEndOfRoad.holds(ctx0, ego0))
    assert(!hasRelevantRedLight.holds(ctx0, ego0))
    assert(next(ego0) { v2 -> ego0.lane.road != v2.lane.road })
    assert(!didCrossRedLight.holds(ctx, TickDataUnitSeconds(0.0), egoId))
  }
}
