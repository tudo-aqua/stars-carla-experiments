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
import tools.aqua.stars.carla.experiments.mphLimit30
import tools.aqua.stars.carla.experiments.mphLimit60
import tools.aqua.stars.carla.experiments.mphLimit90
import tools.aqua.stars.core.evaluation.PredicateContext
import tools.aqua.stars.data.av.dataclasses.Block
import tools.aqua.stars.data.av.dataclasses.Lane
import tools.aqua.stars.data.av.dataclasses.Road
import tools.aqua.stars.data.av.dataclasses.Segment
import tools.aqua.stars.data.av.dataclasses.SpeedLimit
import tools.aqua.stars.data.av.dataclasses.TickData
import tools.aqua.stars.data.av.dataclasses.TickDataUnitSeconds
import tools.aqua.stars.data.av.dataclasses.Vehicle

class SpeedLimitTest {
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

  private lateinit var block0: Block
  private lateinit var block2: Block

  private lateinit var road0: Road
  private lateinit var road0lane1: Lane
  private lateinit var road0lane2: Lane

  private lateinit var road1: Road
  private lateinit var road1lane1: Lane

  private lateinit var road2: Road
  private lateinit var road2lane1: Lane

  private lateinit var road3: Road
  private lateinit var road3lane1: Lane

  private lateinit var blocks: List<Block>

  private val egoId = 0

  @BeforeTest
  fun setup() {
    // Lanes for block0
    road0lane1 = Lane(laneId = 1, laneLength = 50.0, speedLimits = listOf(mph30SpeedLimit))
    road0lane2 = Lane(laneId = 2, laneLength = 50.0, speedLimits = listOf(mph30SpeedLimit))
    road1lane1 = Lane(laneId = 1, laneLength = 50.0, speedLimits = listOf(mph60SpeedLimit))

    // Lanes for block2
    road2lane1 = Lane(laneId = 1, laneLength = 50.0, speedLimits = listOf(mph90SpeedLimit))
    road3lane1 =
        Lane(
            laneId = 1,
            laneLength = 150.0,
            speedLimits = listOf(mph30SpeedLimit1, mph60SpeedLimit2, mph90SpeedLimit3))

    // Roads and block0
    road0 =
        Road(id = 0, lanes = listOf(road0lane1, road0lane2)).apply {
          road0lane1.road = this
          road0lane2.road = this
        }
    road1 = Road(id = 1, lanes = listOf(road1lane1)).apply { road1lane1.road = this }
    block0 = Block(id = "1", roads = listOf(road0, road1))

    road0.block = block0
    road1.block = block0

    // Roads and block2
    road2 = Road(id = 2, lanes = listOf(road2lane1)).apply { road2lane1.road = this }
    road3 = Road(id = 3, lanes = listOf(road3lane1)).apply { road3lane1.road = this }
    block2 = Block(id = "2", roads = listOf(road2, road3))

    road2.block = block2
    road3.block = block2

    blocks = listOf(block0, block2)
  }

  @Test
  fun speedLimit30MPH() {

    val ego = Vehicle(id = egoId, isEgo = true, lane = road0lane1, positionOnLane = 10.0)

    val td =
        TickData(currentTick = TickDataUnitSeconds(0.0), blocks = blocks, entities = listOf(ego))

    ego.tickData = td
    ego.setVelocityFromEffVelocityMPH(11.0)

    val ctx = PredicateContext(Segment(listOf(td), segmentSource = ""))
    assertTrue { mphLimit30.holds(ctx, ego) }
    assertFalse { mphLimit60.holds(ctx, ego) }
    assertFalse { mphLimit90.holds(ctx, ego) }
  }

  @Test
  fun speedLimit60MPH() {
    val ego = Vehicle(id = egoId, isEgo = true, lane = road1lane1, positionOnLane = 10.0)

    val td =
        TickData(currentTick = TickDataUnitSeconds(0.0), blocks = blocks, entities = listOf(ego))

    ego.tickData = td
    ego.setVelocityFromEffVelocityMPH(11.0)

    val ctx = PredicateContext(Segment(listOf(td), segmentSource = ""))
    assertFalse { mphLimit30.holds(ctx, ego) }
    assertTrue { mphLimit60.holds(ctx, ego) }
    assertFalse { mphLimit90.holds(ctx, ego) }
  }

  @Test
  fun speedLimit90MPH() {
    val ego = Vehicle(id = egoId, isEgo = true, lane = road2lane1, positionOnLane = 10.0)

    val td =
        TickData(currentTick = TickDataUnitSeconds(0.0), blocks = blocks, entities = listOf(ego))

    ego.tickData = td
    ego.setVelocityFromEffVelocityMPH(11.0)

    val ctx = PredicateContext(Segment(listOf(td), segmentSource = ""))
    assertFalse { mphLimit30.holds(ctx, ego) }
    assertFalse { mphLimit60.holds(ctx, ego) }
    assertTrue { mphLimit90.holds(ctx, ego) }
  }

  @Test
  fun allSpeedLimitsOnOneLane() {
    val tds = mutableListOf<TickData>()

    for (i in 0..150) {
      val ego = Vehicle(id = egoId, isEgo = true, lane = road3lane1, positionOnLane = i.toDouble())
      val td =
          TickData(
              currentTick = TickDataUnitSeconds(i.toDouble()),
              blocks = blocks,
              entities = listOf(ego))

      ego.tickData = td
      ego.setVelocityFromEffVelocityMPH(11.0)

      tds += td
    }

    val ctx = PredicateContext(Segment(tds, segmentSource = ""))
    assertTrue { mphLimit30.holds(ctx, TickDataUnitSeconds(0.0), egoId) }
    assertTrue { mphLimit60.holds(ctx, TickDataUnitSeconds(0.0), egoId) }
    assertTrue { mphLimit90.holds(ctx, TickDataUnitSeconds(0.0), egoId) }
  }
}
