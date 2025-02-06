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
import tools.aqua.stars.carla.experiments.sameDirection
import tools.aqua.stars.core.evaluation.PredicateContext
import tools.aqua.stars.data.av.dataclasses.Segment
import tools.aqua.stars.data.av.dataclasses.TickDataUnitSeconds

class SameDirectionTest {

  private val road0 = emptyRoad(id = 0)
  private val laneRoad0IdMinus1 = emptyLane(laneId = -1, road = road0)
  private val laneRoad0IdMinus2 = emptyLane(laneId = -2, road = road0)
  private val laneRoad0Id1 = emptyLane(laneId = 1, road = road0)
  private val laneRoad0Id2 = emptyLane(laneId = 2, road = road0)
  private val road1 = emptyRoad(id = 1)
  private val laneRoad1IdMinus1 = emptyLane(laneId = -1, road = road1)
  private val laneRoad1IdMinus2 = emptyLane(laneId = -2, road = road1)
  private val laneRoad1Id1 = emptyLane(laneId = 1, road = road1)
  private val laneRoad1Id2 = emptyLane(laneId = 2, road = road1)

  private val block = emptyBlock()

  @BeforeTest
  fun setup() {
    road0.lanes = listOf(laneRoad0Id1, laneRoad0Id2, laneRoad0IdMinus1, laneRoad0IdMinus2)
    road0.lanes = listOf(laneRoad1Id1, laneRoad1Id2, laneRoad1IdMinus1, laneRoad1IdMinus2)
    block.roads = listOf(road0, road1)
  }

  @Test
  fun testSameDirection() {
    val tickData = emptyTickData(currentTick = TickDataUnitSeconds(0.0), blocks = listOf(block))
    val vehicle0 = emptyVehicle(id = 0, lane = laneRoad0Id1, tickData = tickData, egoVehicle = true)
    val vehicle1 = emptyVehicle(id = 1, lane = laneRoad0Id1, tickData = tickData)
    val vehicle2 = emptyVehicle(id = 2, lane = laneRoad0Id2, tickData = tickData)

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
    val vehicle0 = emptyVehicle(id = 0, lane = laneRoad0Id1, tickData = tickData, egoVehicle = true)
    val vehicle1 = emptyVehicle(id = 1, lane = laneRoad1Id1, tickData = tickData)
    val vehicle2 = emptyVehicle(id = 2, lane = laneRoad1Id2, tickData = tickData)

    tickData.entities = listOf(vehicle0, vehicle1, vehicle2)

    val segment = Segment(listOf(tickData), segmentSource = "")
    val ctx = PredicateContext(segment)

    assert(!sameDirection.holds(ctx, TickDataUnitSeconds(0.0), vehicle0.id, vehicle1.id))
    assert(!sameDirection.holds(ctx, TickDataUnitSeconds(0.0), vehicle0.id, vehicle2.id))
    assert(sameDirection.holds(ctx, TickDataUnitSeconds(0.0), vehicle1.id, vehicle2.id))
  }
}
