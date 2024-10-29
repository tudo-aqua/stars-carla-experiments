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
import tools.aqua.stars.carla.experiments.emptyRoad
import tools.aqua.stars.carla.experiments.emptyTickData
import tools.aqua.stars.carla.experiments.emptyVehicle
import tools.aqua.stars.carla.experiments.stopAtEnd
import tools.aqua.stars.core.evaluation.PredicateContext
import tools.aqua.stars.data.av.dataclasses.Segment
import tools.aqua.stars.data.av.dataclasses.TickDataUnitSeconds

class StopAtEndTest {

  private val road0 = emptyRoad(id = 0)
  private val road0lane1 = emptyLane(laneId = 1, road = road0, laneLength = 50.0)

  private val road1 = emptyRoad(id = 1)
  private val road1lane1 = emptyLane(laneId = 1, road = road1, laneLength = 2.5)

  private val block = emptyBlock()

  @BeforeTest
  fun setup() {
    road0.lanes = listOf(road0lane1)
    road1.lanes = listOf(road1lane1)

    block.roads = listOf(road0, road1)
  }

  @Test
  fun is2MetersFromEndOfRoadAndStopped() {
    val tickData = emptyTickData(TickDataUnitSeconds(0.0), blocks = listOf(block))
    val ego =
        emptyVehicle(
            id = 0,
            egoVehicle = true,
            lane = road0lane1,
            tickData = tickData,
            effVelocityMPH = 1.0,
            positionOnLane = road0lane1.laneLength - 2.0)
    tickData.entities = listOf(ego)

    val segment = Segment(listOf(tickData), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(stopAtEnd.holds(ctx, ego))
  }

  @Test
  fun is2MetersFromEndOfRoadAndDidNotStop() {
    val tickData = emptyTickData(TickDataUnitSeconds(0.0), blocks = listOf(block))
    val ego =
        emptyVehicle(
            id = 0,
            egoVehicle = true,
            lane = road0lane1,
            tickData = tickData,
            effVelocityMPH = 11.0,
            positionOnLane = road0lane1.laneLength - 2.0)
    tickData.entities = listOf(ego)

    val segment = Segment(listOf(tickData), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(!stopAtEnd.holds(ctx, ego))
  }

  @Test
  fun is3MetersFromEndOfRoadAndStopped() {
    val tickData = emptyTickData(TickDataUnitSeconds(0.0), blocks = listOf(block))
    val ego =
        emptyVehicle(
            id = 0,
            egoVehicle = true,
            lane = road0lane1,
            tickData = tickData,
            effVelocityMPH = 1.0,
            positionOnLane = road0lane1.laneLength - 3.0)
    tickData.entities = listOf(ego)

    val segment = Segment(listOf(tickData), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(stopAtEnd.holds(ctx, ego))
  }

  @Test
  fun is3MetersFromEndOfRoadAndDidNotStop() {
    val tickData = emptyTickData(TickDataUnitSeconds(0.0), blocks = listOf(block))
    val ego =
        emptyVehicle(
            id = 0,
            egoVehicle = true,
            lane = road0lane1,
            tickData = tickData,
            effVelocityMPH = 11.0,
            positionOnLane = road0lane1.laneLength - 3.0)
    tickData.entities = listOf(ego)

    val segment = Segment(listOf(tickData), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(!stopAtEnd.holds(ctx, ego))
  }

  @Test
  fun is4MetersFromEndOfRoadAndStopped() {
    val tickData = emptyTickData(TickDataUnitSeconds(0.0), blocks = listOf(block))
    val ego =
        emptyVehicle(
            id = 0,
            egoVehicle = true,
            lane = road0lane1,
            tickData = tickData,
            effVelocityMPH = 1.0,
            positionOnLane = road0lane1.laneLength - 4.0)
    tickData.entities = listOf(ego)

    val segment = Segment(listOf(tickData), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(!stopAtEnd.holds(ctx, ego))
  }

  @Test
  fun is4MetersFromEndOfRoadAndDidNotStop() {
    val tickData = emptyTickData(TickDataUnitSeconds(0.0), blocks = listOf(block))
    val ego =
        emptyVehicle(
            id = 0,
            egoVehicle = true,
            lane = road0lane1,
            tickData = tickData,
            effVelocityMPH = 11.0,
            positionOnLane = road0lane1.laneLength - 4.0)
    tickData.entities = listOf(ego)

    val segment = Segment(listOf(tickData), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(!stopAtEnd.holds(ctx, ego))
  }

  @Test
  fun laneShorterThan3MetersEgoAt1MeterAndStopped() {
    val tickData = emptyTickData(TickDataUnitSeconds(0.0), blocks = listOf(block))
    val ego =
        emptyVehicle(
            id = 0,
            egoVehicle = true,
            lane = road1lane1,
            tickData = tickData,
            effVelocityMPH = 1.0,
            positionOnLane = road0lane1.laneLength - 1.0)
    tickData.entities = listOf(ego)

    val segment = Segment(listOf(tickData), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(stopAtEnd.holds(ctx, ego))
  }

  @Test
  fun laneShorterThan3MetersEgoAt1MeterAndDidNotStop() {
    val tickData = emptyTickData(TickDataUnitSeconds(0.0), blocks = listOf(block))
    val ego =
        emptyVehicle(
            id = 0,
            egoVehicle = true,
            lane = road1lane1,
            tickData = tickData,
            effVelocityMPH = 11.0,
            positionOnLane = road0lane1.laneLength - 1.0)
    tickData.entities = listOf(ego)

    val segment = Segment(listOf(tickData), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(!stopAtEnd.holds(ctx, ego))
  }

  @Test
  fun egoFasterBeforeAndStopped2MetersBeforeEnd() {
    val tickData0 = emptyTickData(TickDataUnitSeconds(0.0), blocks = listOf(block))
    val ego0 =
        emptyVehicle(
            id = 0,
            egoVehicle = true,
            lane = road0lane1,
            tickData = tickData0,
            effVelocityMPH = 11.0,
            positionOnLane = road0lane1.laneLength - 4.0)
    tickData0.entities = listOf(ego0)

    val segment0 = Segment(listOf(tickData0), segmentSource = "")
    val ctx0 = PredicateContext(segment0)

    assert(!stopAtEnd.holds(ctx0, ego0))

    val tickData1 = emptyTickData(TickDataUnitSeconds(1.0), blocks = listOf(block))
    val ego1 =
        emptyVehicle(
            id = 0,
            egoVehicle = true,
            lane = road0lane1,
            tickData = tickData1,
            effVelocityMPH = 1.0,
            positionOnLane = road0lane1.laneLength - 2.0)
    tickData1.entities = listOf(ego1)

    val segment1 = Segment(listOf(tickData1), segmentSource = "")
    val ctx1 = PredicateContext(segment1)

    assert(stopAtEnd.holds(ctx1, ego1))

    val segment = Segment(listOf(tickData0, tickData1), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(stopAtEnd.holds(ctx, TickDataUnitSeconds(0.0), 0))
  }
}
