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
import tools.aqua.stars.carla.experiments.emptyBlock
import tools.aqua.stars.carla.experiments.emptyLane
import tools.aqua.stars.carla.experiments.emptyRoad
import tools.aqua.stars.carla.experiments.emptyTickData
import tools.aqua.stars.carla.experiments.emptyVehicle
import tools.aqua.stars.carla.experiments.follows
import tools.aqua.stars.core.evaluation.PredicateContext
import tools.aqua.stars.data.av.dataclasses.ContactLaneInfo
import tools.aqua.stars.data.av.dataclasses.Lane
import tools.aqua.stars.data.av.dataclasses.Road
import tools.aqua.stars.data.av.dataclasses.Segment
import tools.aqua.stars.data.av.dataclasses.TickData
import tools.aqua.stars.data.av.dataclasses.TickDataUnitSeconds

class FollowsTest {

  private val road: Road = emptyRoad(id = 1)
  private val road2: Road = emptyRoad(id = 2)
  private val laneRoad2 = emptyLane(laneId = 1, road = road2, laneLength = 50.0)
  private val lane1 = emptyLane(laneId = 1, road = road, laneLength = 50.0)
  private val lane2 = emptyLane(laneId = 2, road = road, laneLength = 50.0)
  private val successorRoad: Road = emptyRoad(id = 3)
  private val laneSuccessorRoad: Lane = emptyLane(laneId = 1)
  private val block = emptyBlock()
  private val vehicle0 = emptyVehicle(egoVehicle = true, id = 0)
  private val vehicle1 = emptyVehicle(egoVehicle = false, id = 1)
  private val vehicle2 = emptyVehicle(egoVehicle = false, id = 2)

  @BeforeTest
  fun setup() {
    road.lanes = listOf(lane1, lane2)
    road2.lanes = listOf(laneRoad2)
    successorRoad.lanes = listOf(laneSuccessorRoad)
    lane1.successorLanes = listOf(ContactLaneInfo(laneSuccessorRoad))
    block.roads = listOf(road, road2)
  }

  @Test
  fun testFollows() {
    val tickDataList = mutableListOf<TickData>()

    for (i in 0..30) {
      val tickData =
          emptyTickData(currentTick = TickDataUnitSeconds(i.toDouble()), blocks = listOf(block))
      val v1 =
          emptyVehicle(
              egoVehicle = true,
              id = vehicle1.id,
              tickData = tickData,
              positionOnLane = i.toDouble(),
              lane = lane1)
      val v2 =
          emptyVehicle(
              egoVehicle = false,
              id = vehicle2.id,
              tickData = tickData,
              positionOnLane = i.toDouble() + 10.0,
              lane = lane1)
      tickData.entities = listOf(v1, v2)
      tickDataList.add(tickData)
    }
    val segment = Segment(tickDataList, segmentSource = "")
    val ctx = PredicateContext(segment)

    // Vehicle with id 0 should follow Vehicle with id 1
    assert(follows.holds(ctx, TickDataUnitSeconds(0.0), vehicle1.id, vehicle2.id))
    assert(follows.holds(ctx, tickDataList.first().vehicles[0], tickDataList.first().vehicles[1]))

    // Vehicle with id 1 should follow Vehicle with id 0
    assert(!follows.holds(ctx, TickDataUnitSeconds(0.0), vehicle2.id, vehicle1.id))
    assert(!follows.holds(ctx, tickDataList.first().vehicles[1], tickDataList.first().vehicles[0]))
  }

  @Test
  fun testFollowsWithTooShortSegment() {
    val tickDataList = mutableListOf<TickData>()

    // The minimum duration for the follows predicate is set to 30.0
    for (i in 0..29) {
      val tickData =
          emptyTickData(currentTick = TickDataUnitSeconds(i.toDouble()), blocks = listOf(block))
      val v1 =
          emptyVehicle(
              egoVehicle = true,
              id = vehicle1.id,
              tickData = tickData,
              positionOnLane = i.toDouble(),
              lane = lane1)
      val v2 =
          emptyVehicle(
              egoVehicle = false,
              id = vehicle2.id,
              tickData = tickData,
              positionOnLane = i.toDouble() + 10.0,
              lane = lane1)
      tickData.entities = listOf(v1, v2)
      tickDataList.add(tickData)
    }
    val segment = Segment(tickDataList, segmentSource = "")
    val ctx = PredicateContext(segment)

    // Vehicle with id 0 should follow Vehicle with id 1
    assert(!follows.holds(ctx, TickDataUnitSeconds(0.0), vehicle1.id, vehicle2.id))
    assert(!follows.holds(ctx, tickDataList.first().vehicles[0], tickDataList.first().vehicles[1]))

    // Vehicle with id 1 should follow Vehicle with id 0
    assert(!follows.holds(ctx, TickDataUnitSeconds(0.0), vehicle2.id, vehicle1.id))
    assert(!follows.holds(ctx, tickDataList.first().vehicles[1], tickDataList.first().vehicles[0]))
  }

  @Test
  fun testFollowsWithSomeoneBetween() {
    val tickDataList = mutableListOf<TickData>()

    for (i in 0..30) {
      val tickData =
          emptyTickData(currentTick = TickDataUnitSeconds(i.toDouble()), blocks = listOf(block))
      val v1 =
          emptyVehicle(
              egoVehicle = true,
              id = vehicle0.id,
              tickData = tickData,
              positionOnLane = i.toDouble(),
              lane = lane1)
      val v2 =
          emptyVehicle(
              egoVehicle = false,
              id = vehicle1.id,
              tickData = tickData,
              positionOnLane = i.toDouble() + 10.0,
              lane = lane1)
      val v3 =
          emptyVehicle(
              egoVehicle = false,
              id = vehicle2.id,
              tickData = tickData,
              positionOnLane = i.toDouble() + 5.0,
              lane = lane1)
      tickData.entities = listOf(v1, v2, v3)
      tickDataList.add(tickData)
    }
    val segment = Segment(tickDataList, segmentSource = "")
    val ctx = PredicateContext(segment)

    // Vehicle with id 0 should not follow Vehicle with id 1 as there is some between
    assert(!follows.holds(ctx, TickDataUnitSeconds(0.0), vehicle0.id, vehicle1.id))

    // Vehicle with id 2 should follow Vehicle with id 1
    assert(follows.holds(ctx, TickDataUnitSeconds(0.0), vehicle2.id, vehicle1.id))

    // Vehicle with id 0 should follow Vehicle with id 2
    assert(follows.holds(ctx, TickDataUnitSeconds(0.0), vehicle0.id, vehicle2.id))

    // All other combinations should not hold
    assert(!follows.holds(ctx, TickDataUnitSeconds(0.0), vehicle1.id, vehicle0.id))
    assert(!follows.holds(ctx, TickDataUnitSeconds(0.0), vehicle1.id, vehicle2.id))
  }

  @Test
  fun testFollowsWithDifferentLanes() {
    val tickDataList = mutableListOf<TickData>()

    for (i in 0..30) {
      val tickData =
          emptyTickData(currentTick = TickDataUnitSeconds(i.toDouble()), blocks = listOf(block))
      val v1 =
          emptyVehicle(
              egoVehicle = true,
              id = vehicle0.id,
              tickData = tickData,
              positionOnLane = i.toDouble(),
              lane = lane1)
      val v2 =
          emptyVehicle(
              egoVehicle = false,
              id = vehicle1.id,
              tickData = tickData,
              positionOnLane = i.toDouble() + 10.0,
              lane = lane2)
      tickData.entities = listOf(v1, v2)
      tickDataList.add(tickData)
    }
    val segment = Segment(tickDataList, segmentSource = "")
    val ctx = PredicateContext(segment)

    // Vehicle with id 0 should not follow Vehicle with id 1
    assert(!follows.holds(ctx, TickDataUnitSeconds(0.0), vehicle0.id, vehicle1.id))
    assert(!follows.holds(ctx, tickDataList.first().vehicles[0], tickDataList.first().vehicles[1]))

    // Vehicle with id 1 should not follow Vehicle with id 0
    assert(!follows.holds(ctx, TickDataUnitSeconds(0.0), vehicle1.id, vehicle0.id))
    assert(!follows.holds(ctx, tickDataList.first().vehicles[1], tickDataList.first().vehicles[0]))
  }

  @Test
  fun testFollowsWithDifferentRoads() {
    val tickDataList = mutableListOf<TickData>()

    for (i in 0..30) {
      val tickData =
          emptyTickData(currentTick = TickDataUnitSeconds(i.toDouble()), blocks = listOf(block))
      val v0 =
          emptyVehicle(
              egoVehicle = true,
              id = vehicle0.id,
              tickData = tickData,
              positionOnLane = i.toDouble(),
              lane = lane1)
      val v1 =
          emptyVehicle(
              egoVehicle = false,
              id = vehicle1.id,
              tickData = tickData,
              positionOnLane = i.toDouble(),
              lane = laneRoad2)
      val v2 =
          emptyVehicle(
              egoVehicle = false,
              id = vehicle2.id,
              tickData = tickData,
              positionOnLane = i.toDouble() + 10.0,
              lane = laneRoad2)
      tickData.entities = listOf(v0, v1, v2)
      tickDataList.add(tickData)
    }
    val segment = Segment(tickDataList, segmentSource = "")
    val ctx = PredicateContext(segment)

    // The vehicle with id 0 should not follow either vehicle with id 1 or 2
    assert(!follows.holds(ctx, TickDataUnitSeconds(0.0), vehicle0.id, vehicle1.id))
    assert(!follows.holds(ctx, TickDataUnitSeconds(0.0), vehicle0.id, vehicle2.id))

    // The vehicle with id 1 should follow the vehicle with id 2
    assert(follows.holds(ctx, TickDataUnitSeconds(0.0), vehicle1.id, vehicle2.id))
  }

  @Test
  fun testFollowsSuccessorLane() {
    val tickDataList = mutableListOf<TickData>()

    for (i in 0..30) {
      val tickData =
          emptyTickData(currentTick = TickDataUnitSeconds(i.toDouble()), blocks = listOf(block))
      val v1 =
          emptyVehicle(
              egoVehicle = true,
              id = vehicle1.id,
              tickData = tickData,
              positionOnLane = i.toDouble(),
              lane = lane1)
      val v2 =
          emptyVehicle(
              egoVehicle = false,
              id = vehicle2.id,
              tickData = tickData,
              positionOnLane = i.toDouble(),
              lane = laneSuccessorRoad)
      tickData.entities = listOf(v1, v2)
      tickDataList.add(tickData)
    }
    val segment = Segment(tickDataList, segmentSource = "")
    val ctx = PredicateContext(segment)

    // Vehicle with id 0 should follow Vehicle with id 1
    assert(follows.holds(ctx, TickDataUnitSeconds(0.0), vehicle1.id, vehicle2.id))
    assert(follows.holds(ctx, tickDataList.first().vehicles[0], tickDataList.first().vehicles[1]))

    // Vehicle with id 1 should follow Vehicle with id 0
    assert(!follows.holds(ctx, TickDataUnitSeconds(0.0), vehicle2.id, vehicle1.id))
    assert(!follows.holds(ctx, tickDataList.first().vehicles[1], tickDataList.first().vehicles[0]))
  }

  @Test
  fun testFollowsSuccessorLaneWithSomeOneBetween() {
    val tickDataList = mutableListOf<TickData>()

    for (i in 0..30) {
      val tickData =
          emptyTickData(currentTick = TickDataUnitSeconds(i.toDouble()), blocks = listOf(block))
      val v0 =
          emptyVehicle(
              egoVehicle = true,
              id = vehicle0.id,
              tickData = tickData,
              positionOnLane = i.toDouble(),
              lane = lane1)
      val v1 =
          emptyVehicle(
              egoVehicle = false,
              id = vehicle1.id,
              tickData = tickData,
              positionOnLane = i.toDouble() + 10.0,
              lane = lane1)
      val v2 =
          emptyVehicle(
              egoVehicle = false,
              id = vehicle2.id,
              tickData = tickData,
              positionOnLane = i.toDouble(),
              lane = laneSuccessorRoad)
      tickData.entities = listOf(v0, v1, v2)
      tickDataList.add(tickData)
    }
    val segment = Segment(tickDataList, segmentSource = "")
    val ctx = PredicateContext(segment)

    // The vehicle with id 0 should follow the vehicle with id 1
    assert(follows.holds(ctx, TickDataUnitSeconds(0.0), vehicle0.id, vehicle1.id))

    // The vehicle with id 1 should follow the vehicle with id 2
    assert(follows.holds(ctx, TickDataUnitSeconds(0.0), vehicle1.id, vehicle2.id))

    // The vehicle with id 0 should not follow the vehicle with id 2
    assert(!follows.holds(ctx, TickDataUnitSeconds(0.0), vehicle0.id, vehicle2.id))
  }
}
