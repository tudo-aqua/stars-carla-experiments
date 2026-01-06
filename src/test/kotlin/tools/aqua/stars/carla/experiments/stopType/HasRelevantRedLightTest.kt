/*
 * Copyright 2024-2026 The STARS Carla Experiments Authors
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

class HasRelevantRedLightTest {

  private val block = emptyBlock(id = "1")

  private val staticTrafficLight01 =
      StaticTrafficLight(
          id = 10,
          location = emptyLocation(),
          rotation = emptyRotation(),
          stopLocations = listOf(),
      )
  private val staticTrafficLight02 =
      StaticTrafficLight(
          id = 11,
          location = emptyLocation(),
          rotation = emptyRotation(),
          stopLocations = listOf(),
      )
  private val staticTrafficLight11 =
      StaticTrafficLight(
          id = 12,
          location = emptyLocation(),
          rotation = emptyRotation(),
          stopLocations = listOf(),
      )

  private val road0 = emptyRoad(id = 0, block = block)
  private val road0lane1 =
      emptyLane(
          laneId = 1,
          road = road0,
          laneLength = 50.0,
          staticTrafficLights = listOf(staticTrafficLight01),
      )
  private val road0lane2 =
      emptyLane(
          laneId = 2,
          road = road0,
          laneLength = 50.0,
          staticTrafficLights = listOf(staticTrafficLight02),
      )

  private val road = emptyRoad(id = -1, block = block)
  private val contactLaneInfo = ContactLaneInfo(lane = road0lane1)
  private val roadLane1 =
      emptyLane(
          laneId = 1,
          road = road,
          laneLength = 50.0,
          successorLanes = listOf(contactLaneInfo),
      )

  private val road1 = emptyRoad(id = 1, block = block)
  private val road1lane1 =
      emptyLane(
          laneId = 1,
          road = road1,
          laneLength = 50.0,
          staticTrafficLights = listOf(staticTrafficLight11),
      )

  private val blocks = listOf(block)

  private val egoId = 0

  @BeforeTest
  fun setup() {
    road0.lanes = listOf(road0lane1, road0lane2)
    road1.lanes = listOf(road1lane1)

    block.roads = listOf(road0, road1)
  }

  @Test
  fun laneHasRedLightAndEgoIsAtStart() {
    val trafficLight =
        emptyTrafficLight(
            id = 1,
            relatedOpenDriveId = staticTrafficLight01.id,
            state = TrafficLightState.Red,
        )
    val tickData =
        emptyTickData(
            currentTick = TickDataUnitSeconds(0.0),
            blocks,
            trafficLights = listOf(trafficLight),
        )
    val ego =
        emptyVehicle(
            id = egoId,
            egoVehicle = true,
            lane = road0lane1,
            positionOnLane = 0.0,
            effVelocityMPH = 0.0,
            tickData = tickData,
        )
    tickData.entities = listOf(ego)

    val segment = Segment(listOf(tickData), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(!hasRedLight.holds(ctx, ego))
    assert(!isAtEndOfRoad.holds(ctx, ego))
    assert(!hasRelevantRedLight.holds(ctx, ego))
  }

  @Test
  fun laneHasRedLightAndEgoIsAtEnd() {
    val trafficLight =
        emptyTrafficLight(
            id = 1,
            relatedOpenDriveId = staticTrafficLight01.id,
            state = TrafficLightState.Red,
        )
    val tickData =
        emptyTickData(
            currentTick = TickDataUnitSeconds(0.0),
            blocks,
            trafficLights = listOf(trafficLight),
        )
    val ego =
        emptyVehicle(
            id = egoId,
            egoVehicle = true,
            lane = road0lane1,
            positionOnLane = road0lane1.laneLength - 1.0,
            effVelocityMPH = 0.0,
            tickData = tickData,
        )
    tickData.entities = listOf(ego)

    val segment = Segment(listOf(tickData), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(!hasRedLight.holds(ctx, ego))
    assert(isAtEndOfRoad.holds(ctx, ego))
    assert(!hasRelevantRedLight.holds(ctx, ego))
  }

  @Test
  fun laneHasGreenLightAndEgoIstAtStart() {
    val trafficLight =
        emptyTrafficLight(
            id = 1,
            relatedOpenDriveId = staticTrafficLight01.id,
            state = TrafficLightState.Green,
        )
    val tickData =
        emptyTickData(
            currentTick = TickDataUnitSeconds(0.0),
            blocks,
            trafficLights = listOf(trafficLight),
        )
    val ego =
        emptyVehicle(
            id = egoId,
            egoVehicle = true,
            lane = road0lane1,
            positionOnLane = 0.0,
            effVelocityMPH = 0.0,
            tickData = tickData,
        )
    tickData.entities = listOf(ego)

    val segment = Segment(listOf(tickData), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(!hasRedLight.holds(ctx, ego))
    assert(!isAtEndOfRoad.holds(ctx, ego))
    assert(!hasRelevantRedLight.holds(ctx, ego))
  }

  @Test
  fun laneHasGreenLightAndEgoIstAtEnd() {
    val trafficLight =
        emptyTrafficLight(
            id = 1,
            relatedOpenDriveId = staticTrafficLight01.id,
            state = TrafficLightState.Green,
        )
    val tickData =
        emptyTickData(
            currentTick = TickDataUnitSeconds(0.0),
            blocks,
            trafficLights = listOf(trafficLight),
        )
    val ego =
        emptyVehicle(
            id = egoId,
            egoVehicle = true,
            lane = road0lane1,
            positionOnLane = road0lane1.laneLength - 1.0,
            effVelocityMPH = 0.0,
            tickData = tickData,
        )
    tickData.entities = listOf(ego)

    val segment = Segment(listOf(tickData), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(!hasRedLight.holds(ctx, ego))
    assert(isAtEndOfRoad.holds(ctx, ego))
    assert(!hasRelevantRedLight.holds(ctx, ego))
  }

  @Test
  fun successorLaneHasRedLightAndEgoIsAtStart() {
    val trafficLight =
        emptyTrafficLight(
            id = 1,
            relatedOpenDriveId = staticTrafficLight01.id,
            state = TrafficLightState.Red,
        )
    val tickData =
        emptyTickData(
            currentTick = TickDataUnitSeconds(0.0),
            blocks,
            trafficLights = listOf(trafficLight),
        )
    val ego =
        emptyVehicle(
            id = egoId,
            egoVehicle = true,
            lane = roadLane1,
            positionOnLane = 0.0,
            effVelocityMPH = 0.0,
            tickData = tickData,
        )
    tickData.entities = listOf(ego)

    val segment = Segment(listOf(tickData), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(hasRedLight.holds(ctx, ego))
    assert(!isAtEndOfRoad.holds(ctx, ego))
    assert(!hasRelevantRedLight.holds(ctx, ego))
  }

  @Test
  fun successorLaneHasRedLightAndEgoIsAtEnd() {
    val trafficLight =
        emptyTrafficLight(
            id = 1,
            relatedOpenDriveId = staticTrafficLight01.id,
            state = TrafficLightState.Red,
        )
    val tickData =
        emptyTickData(
            currentTick = TickDataUnitSeconds(0.0),
            blocks,
            trafficLights = listOf(trafficLight),
        )
    val ego =
        emptyVehicle(
            id = egoId,
            egoVehicle = true,
            lane = roadLane1,
            positionOnLane = road0lane1.laneLength - 1.0,
            effVelocityMPH = 0.0,
            tickData = tickData,
        )
    tickData.entities = listOf(ego)

    val segment = Segment(listOf(tickData), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(hasRedLight.holds(ctx, ego))
    assert(isAtEndOfRoad.holds(ctx, ego))
    assert(hasRelevantRedLight.holds(ctx, ego))
  }

  @Test
  fun successorLaneHasGreenLightAndEgoIsAtStart() {
    val trafficLight =
        emptyTrafficLight(
            id = 1,
            relatedOpenDriveId = staticTrafficLight01.id,
            state = TrafficLightState.Green,
        )
    val tickData =
        emptyTickData(
            currentTick = TickDataUnitSeconds(0.0),
            blocks,
            trafficLights = listOf(trafficLight),
        )
    val ego =
        emptyVehicle(
            id = egoId,
            egoVehicle = true,
            lane = roadLane1,
            positionOnLane = 0.0,
            effVelocityMPH = 0.0,
            tickData = tickData,
        )
    tickData.entities = listOf(ego)

    val segment = Segment(listOf(tickData), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(!hasRedLight.holds(ctx, ego))
    assert(!isAtEndOfRoad.holds(ctx, ego))
    assert(!hasRelevantRedLight.holds(ctx, ego))
  }

  @Test
  fun successorLaneHasGreenLightAndEgoIsAtEnd() {
    val trafficLight =
        emptyTrafficLight(
            id = 1,
            relatedOpenDriveId = staticTrafficLight01.id,
            state = TrafficLightState.Green,
        )
    val tickData =
        emptyTickData(
            currentTick = TickDataUnitSeconds(0.0),
            blocks,
            trafficLights = listOf(trafficLight),
        )
    val ego =
        emptyVehicle(
            id = egoId,
            egoVehicle = true,
            lane = roadLane1,
            positionOnLane = road0lane1.laneLength - 1.0,
            effVelocityMPH = 0.0,
            tickData = tickData,
        )
    tickData.entities = listOf(ego)

    val segment = Segment(listOf(tickData), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(!hasRedLight.holds(ctx, ego))
    assert(isAtEndOfRoad.holds(ctx, ego))
    assert(!hasRelevantRedLight.holds(ctx, ego))
  }

  @Test
  fun differentLaneHasRedLightAndEgoIsAtStart() {
    val trafficLight =
        emptyTrafficLight(
            id = 2,
            relatedOpenDriveId = staticTrafficLight11.id,
            state = TrafficLightState.Green,
        )
    val tickData =
        emptyTickData(
            currentTick = TickDataUnitSeconds(0.0),
            blocks,
            trafficLights = listOf(trafficLight),
        )
    val ego =
        emptyVehicle(
            id = egoId,
            egoVehicle = true,
            lane = roadLane1,
            positionOnLane = 0.0,
            effVelocityMPH = 0.0,
            tickData = tickData,
        )
    tickData.entities = listOf(ego)

    val segment = Segment(listOf(tickData), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(!hasRedLight.holds(ctx, ego))
    assert(!isAtEndOfRoad.holds(ctx, ego))
    assert(!hasRelevantRedLight.holds(ctx, ego))
  }

  @Test
  fun differentLaneHasRedLightAndEgoIsAtEnd() {
    val trafficLight =
        emptyTrafficLight(
            id = 2,
            relatedOpenDriveId = staticTrafficLight11.id,
            state = TrafficLightState.Green,
        )
    val tickData =
        emptyTickData(
            currentTick = TickDataUnitSeconds(0.0),
            blocks,
            trafficLights = listOf(trafficLight),
        )
    val ego =
        emptyVehicle(
            id = egoId,
            egoVehicle = true,
            lane = roadLane1,
            positionOnLane = roadLane1.laneLength - 1,
            effVelocityMPH = 0.0,
            tickData = tickData,
        )
    tickData.entities = listOf(ego)

    val segment = Segment(listOf(tickData), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(!hasRedLight.holds(ctx, ego))
    assert(isAtEndOfRoad.holds(ctx, ego))
    assert(!hasRelevantRedLight.holds(ctx, ego))
  }
}
