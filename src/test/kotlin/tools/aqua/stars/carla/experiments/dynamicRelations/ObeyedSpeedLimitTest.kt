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
import tools.aqua.stars.carla.experiments.mphLimit30
import tools.aqua.stars.carla.experiments.mphLimit60
import tools.aqua.stars.carla.experiments.mphLimit90
import tools.aqua.stars.carla.experiments.obeyedSpeedLimit
import tools.aqua.stars.core.evaluation.PredicateContext
import tools.aqua.stars.data.av.dataclasses.Segment
import tools.aqua.stars.data.av.dataclasses.SpeedLimit
import tools.aqua.stars.data.av.dataclasses.TickData
import tools.aqua.stars.data.av.dataclasses.TickDataUnitSeconds

class ObeyedSpeedLimitTest {

  private val mph30SpeedLimit =
      SpeedLimit(speedLimit = 30.0, fromDistanceFromStart = 0.0, toDistanceFromStart = 50.0)
  private val mph60SpeedLimit =
      SpeedLimit(speedLimit = 60.0, fromDistanceFromStart = 0.0, toDistanceFromStart = 50.0)
  private val mph90SpeedLimit =
      SpeedLimit(speedLimit = 90.0, fromDistanceFromStart = 0.0, toDistanceFromStart = 50.0)

  private val mph30SpeedLimit1 =
      SpeedLimit(speedLimit = 30.0, fromDistanceFromStart = 0.0, toDistanceFromStart = 50.0)
  private val mph60SpeedLimit2 =
      SpeedLimit(speedLimit = 60.0, fromDistanceFromStart = 50.0, toDistanceFromStart = 100.0)
  private val mph90SpeedLimit3 =
      SpeedLimit(speedLimit = 90.0, fromDistanceFromStart = 100.0, toDistanceFromStart = 150.0)

  private val block = emptyBlock(id = "1")

  private val road0 = emptyRoad(id = 0, block = block)
  private val road0lane1 =
      emptyLane(laneId = 1, road = road0, laneLength = 50.0, speedLimits = listOf(mph30SpeedLimit))
  private val road0lane2 =
      emptyLane(laneId = 2, road = road0, laneLength = 50.0, speedLimits = listOf(mph30SpeedLimit))

  private val road1 = emptyRoad(id = 1, block = block)
  private val road1lane1 =
      emptyLane(laneId = 1, road = road1, laneLength = 50.0, speedLimits = listOf(mph60SpeedLimit))

  private val block2 = emptyBlock(id = "2")

  private val road2 = emptyRoad(id = 2, block = block2)
  private val road2lane1 =
      emptyLane(laneId = 1, road = road2, laneLength = 50.0, speedLimits = listOf(mph90SpeedLimit))

  private val road3 = emptyRoad(id = 3, block = block2)
  private val road3lane1 =
      emptyLane(
          laneId = 1,
          road = road3,
          laneLength = 150.0,
          speedLimits = listOf(mph30SpeedLimit1, mph60SpeedLimit2, mph90SpeedLimit3))

  private val blocks = listOf(block, block2)

  private val egoId = 0

  @BeforeTest
  fun setup() {
    road0.lanes = listOf(road0lane1, road0lane2)
    road1.lanes = listOf(road1lane1)

    block.roads = listOf(road0, road1)

    road2.lanes = listOf(road2lane1)
    road3.lanes = listOf(road3lane1)

    block2.roads = listOf(road2, road3)
  }

  @Test
  fun speedLimit30MPHObeyed() {
    val tickData = emptyTickData(currentTick = TickDataUnitSeconds(0.0), blocks)
    val ego =
        emptyVehicle(
            id = egoId,
            egoVehicle = true,
            lane = road0lane1,
            effVelocityMPH = 11.0,
            tickData = tickData,
            positionOnLane = 10.0)
    tickData.entities = listOf(ego)

    val segment = Segment(listOf(tickData), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(obeyedSpeedLimit.holds(ctx, ego))
  }

  @Test
  fun speedLimit30MPHNotObeyed() {
    val tickData = emptyTickData(currentTick = TickDataUnitSeconds(0.0), blocks)
    val ego =
        emptyVehicle(
            id = egoId,
            egoVehicle = true,
            lane = road0lane1,
            effVelocityMPH = 40.0,
            tickData = tickData,
            positionOnLane = 10.0)
    tickData.entities = listOf(ego)

    val segment = Segment(listOf(tickData), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(!obeyedSpeedLimit.holds(ctx, ego))
  }

  @Test
  fun speedLimit60MPHObeyed() {
    val tickData = emptyTickData(currentTick = TickDataUnitSeconds(0.0), blocks)
    val ego =
        emptyVehicle(
            id = egoId,
            egoVehicle = true,
            lane = road1lane1,
            effVelocityMPH = 55.0,
            tickData = tickData,
            positionOnLane = 10.0)
    tickData.entities = listOf(ego)

    val segment = Segment(listOf(tickData), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(obeyedSpeedLimit.holds(ctx, ego))
  }

  @Test
  fun speedLimit60MPHNotObeyed() {
    val tickData = emptyTickData(currentTick = TickDataUnitSeconds(0.0), blocks)
    val ego =
        emptyVehicle(
            id = egoId,
            egoVehicle = true,
            lane = road1lane1,
            effVelocityMPH = 65.0,
            tickData = tickData,
            positionOnLane = 10.0)
    tickData.entities = listOf(ego)

    val segment = Segment(listOf(tickData), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(!obeyedSpeedLimit.holds(ctx, ego))
  }

  @Test
  fun speedLimit90MPH() {
    val tickData = emptyTickData(currentTick = TickDataUnitSeconds(0.0), blocks)
    val ego =
        emptyVehicle(
            id = egoId,
            egoVehicle = true,
            lane = road2lane1,
            effVelocityMPH = 11.0,
            tickData = tickData,
            positionOnLane = 10.0)
    tickData.entities = listOf(ego)

    val segment = Segment(listOf(tickData), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(!mphLimit30.holds(ctx, ego))
    assert(!mphLimit60.holds(ctx, ego))
    assert(mphLimit90.holds(ctx, ego))
  }

  @Test
  fun allSpeedLimitsOnOneLaneObeyed() {
    val tickDataList = mutableListOf<TickData>()
    for (i in 0..49) {
      val tickData = emptyTickData(currentTick = TickDataUnitSeconds(i.toDouble()), blocks)
      val ego =
          emptyVehicle(
              id = egoId,
              egoVehicle = true,
              lane = road3lane1,
              effVelocityMPH = 29.0,
              tickData = tickData,
              positionOnLane = i.toDouble())
      tickData.entities = listOf(ego)
      tickDataList += tickData
    }

    val segment0 = Segment(tickDataList, segmentSource = "")
    val ctx0 = PredicateContext(segment0)

    assert(obeyedSpeedLimit.holds(ctx0, TickDataUnitSeconds(0.0), egoId))

    for (i in 50..99) {
      val tickData = emptyTickData(currentTick = TickDataUnitSeconds(i.toDouble()), blocks)
      val ego =
          emptyVehicle(
              id = egoId,
              egoVehicle = true,
              lane = road3lane1,
              effVelocityMPH = 59.0,
              tickData = tickData,
              positionOnLane = i.toDouble())
      tickData.entities = listOf(ego)
      tickDataList += tickData
    }

    val segment1 = Segment(tickDataList, segmentSource = "")
    val ctx1 = PredicateContext(segment1)

    assert(obeyedSpeedLimit.holds(ctx1, TickDataUnitSeconds(0.0), egoId))

    for (i in 100..149) {
      val tickData = emptyTickData(currentTick = TickDataUnitSeconds(i.toDouble()), blocks)
      val ego =
          emptyVehicle(
              id = egoId,
              egoVehicle = true,
              lane = road3lane1,
              effVelocityMPH = 89.0,
              tickData = tickData,
              positionOnLane = i.toDouble())
      tickData.entities = listOf(ego)
      tickDataList += tickData
    }

    val segment = Segment(tickDataList, segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(obeyedSpeedLimit.holds(ctx, TickDataUnitSeconds(0.0), egoId))
  }

  @Test
  fun allSpeedLimitsOnOneLaneNotObeyed() {
    val tickDataList = mutableListOf<TickData>()
    for (i in 0..49) {
      val tickData = emptyTickData(currentTick = TickDataUnitSeconds(i.toDouble()), blocks)
      val ego =
          emptyVehicle(
              id = egoId,
              egoVehicle = true,
              lane = road3lane1,
              effVelocityMPH = 29.0,
              tickData = tickData,
              positionOnLane = i.toDouble())
      tickData.entities = listOf(ego)
      tickDataList += tickData
    }

    val segment0 = Segment(tickDataList, segmentSource = "")
    val ctx0 = PredicateContext(segment0)

    assert(obeyedSpeedLimit.holds(ctx0, TickDataUnitSeconds(0.0), egoId))

    for (i in 50..99) {
      val tickData = emptyTickData(currentTick = TickDataUnitSeconds(i.toDouble()), blocks)
      val ego =
          emptyVehicle(
              id = egoId,
              egoVehicle = true,
              lane = road3lane1,
              effVelocityMPH = 59.0,
              tickData = tickData,
              positionOnLane = i.toDouble())
      tickData.entities = listOf(ego)
      tickDataList += tickData
    }

    val segment1 = Segment(tickDataList, segmentSource = "")
    val ctx1 = PredicateContext(segment1)

    assert(obeyedSpeedLimit.holds(ctx1, TickDataUnitSeconds(0.0), egoId))

    for (i in 100..149) {
      val tickData = emptyTickData(currentTick = TickDataUnitSeconds(i.toDouble()), blocks)
      val ego =
          emptyVehicle(
              id = egoId,
              egoVehicle = true,
              lane = road3lane1,
              effVelocityMPH = 99.0,
              tickData = tickData,
              positionOnLane = i.toDouble())
      tickData.entities = listOf(ego)
      tickDataList += tickData
    }

    val segment = Segment(tickDataList, segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(!obeyedSpeedLimit.holds(ctx, TickDataUnitSeconds(0.0), egoId))
  }
}
