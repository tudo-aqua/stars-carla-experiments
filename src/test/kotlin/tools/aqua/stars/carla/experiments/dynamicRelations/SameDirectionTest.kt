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
import tools.aqua.stars.carla.experiments.sameDirection
import tools.aqua.stars.core.evaluation.PredicateContext
import tools.aqua.stars.data.av.dataclasses.Segment
import tools.aqua.stars.data.av.dataclasses.TickDataUnitSeconds

class SameDirectionTest {

  private val road0 = emptyRoad(id = 0)
  private val lane_road_0_n1 = emptyLane(laneId = -1, road = road0)
  private val lane_road_0_n2 = emptyLane(laneId = -2, road = road0)
  private val lane_road_0_1 = emptyLane(laneId = 1, road = road0)
  private val lane_road_0_2 = emptyLane(laneId = 2, road = road0)
  private val road1 = emptyRoad(id = 1)
  private val lane_road_1_n1 = emptyLane(laneId = -1, road = road1)
  private val lane_road_1_n2 = emptyLane(laneId = -2, road = road1)
  private val lane_road_1_1 = emptyLane(laneId = 1, road = road1)
  private val lane_road_1_2 = emptyLane(laneId = 2, road = road1)

  private val block = emptyBlock()

  @BeforeTest
  fun setup() {
    road0.lanes = listOf(lane_road_0_1, lane_road_0_2, lane_road_0_n1, lane_road_0_n2)
    road0.lanes = listOf(lane_road_1_1, lane_road_1_2, lane_road_1_n1, lane_road_1_n2)
    block.roads = listOf(road0, road1)
  }

  @Test
  fun testSameDirection() {
    val tickData = emptyTickData(currentTick = TickDataUnitSeconds(0.0), blocks = listOf(block))
    val vehicle0 =
        emptyVehicle(id = 0, lane = lane_road_0_1, tickData = tickData, egoVehicle = true)
    val vehicle1 = emptyVehicle(id = 1, lane = lane_road_0_1, tickData = tickData)
    val vehicle2 = emptyVehicle(id = 2, lane = lane_road_0_2, tickData = tickData)

    tickData.entities = listOf(vehicle0, vehicle1, vehicle2)

    val segment = Segment(listOf(tickData), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(sameDirection.holds(ctx, TickDataUnitSeconds(0.0), vehicle0.id, vehicle1.id))
    assert(sameDirection.holds(ctx, TickDataUnitSeconds(0.0), vehicle0.id, vehicle2.id))
    assert(sameDirection.holds(ctx, TickDataUnitSeconds(0.0), vehicle1.id, vehicle2.id))
  }

  @Test
  fun testSameDirectionOnDifferentRoads() {
    val tickData = emptyTickData(currentTick = TickDataUnitSeconds(0.0), blocks = listOf(block))
    val vehicle0 =
        emptyVehicle(id = 0, lane = lane_road_0_1, tickData = tickData, egoVehicle = true)
    val vehicle1 = emptyVehicle(id = 1, lane = lane_road_1_1, tickData = tickData)
    val vehicle2 = emptyVehicle(id = 2, lane = lane_road_1_2, tickData = tickData)

    tickData.entities = listOf(vehicle0, vehicle1, vehicle2)

    val segment = Segment(listOf(tickData), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(!sameDirection.holds(ctx, TickDataUnitSeconds(0.0), vehicle0.id, vehicle1.id))
    assert(!sameDirection.holds(ctx, TickDataUnitSeconds(0.0), vehicle0.id, vehicle2.id))
    assert(sameDirection.holds(ctx, TickDataUnitSeconds(0.0), vehicle1.id, vehicle2.id))
  }
}
