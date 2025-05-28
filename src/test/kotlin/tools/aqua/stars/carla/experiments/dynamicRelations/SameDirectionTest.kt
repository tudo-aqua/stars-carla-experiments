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
import tools.aqua.stars.carla.experiments.sameDirection
import tools.aqua.stars.core.evaluation.PredicateContext
import tools.aqua.stars.data.av.dataclasses.Block
import tools.aqua.stars.data.av.dataclasses.Lane
import tools.aqua.stars.data.av.dataclasses.Road
import tools.aqua.stars.data.av.dataclasses.Segment
import tools.aqua.stars.data.av.dataclasses.TickData
import tools.aqua.stars.data.av.dataclasses.TickDataUnitSeconds
import tools.aqua.stars.data.av.dataclasses.Vehicle

class SameDirectionTest {
  private lateinit var road0: Road
  private lateinit var laneRoad0IdMinus1: Lane
  private lateinit var laneRoad0IdMinus2: Lane
  private lateinit var laneRoad0Id1: Lane
  private lateinit var laneRoad0Id2: Lane

  private lateinit var road1: Road
  private lateinit var laneRoad1IdMinus1: Lane
  private lateinit var laneRoad1IdMinus2: Lane
  private lateinit var laneRoad1Id1: Lane
  private lateinit var laneRoad1Id2: Lane

  private lateinit var block: Block

  @BeforeTest
  fun setup() {
    laneRoad0IdMinus1 = Lane(laneId = -1)
    laneRoad0IdMinus2 = Lane(laneId = -2)
    laneRoad0Id1 = Lane(laneId = 1)
    laneRoad0Id2 = Lane(laneId = 2)

    road0 =
        Road(
                id = 0,
                lanes = listOf(laneRoad0Id1, laneRoad0Id2, laneRoad0IdMinus1, laneRoad0IdMinus2))
            .apply {
              laneRoad0IdMinus1.road = this
              laneRoad0IdMinus2.road = this
              laneRoad0Id1.road = this
              laneRoad0Id2.road = this
            }

    laneRoad1IdMinus1 = Lane(laneId = -1)
    laneRoad1IdMinus2 = Lane(laneId = -2)
    laneRoad1Id1 = Lane(laneId = 1)
    laneRoad1Id2 = Lane(laneId = 2)

    road1 =
        Road(
                id = 1,
                lanes = listOf(laneRoad1Id1, laneRoad1Id2, laneRoad1IdMinus1, laneRoad1IdMinus2))
            .apply {
              laneRoad1IdMinus1.road = this
              laneRoad1IdMinus2.road = this
              laneRoad1Id1.road = this
              laneRoad1Id2.road = this
            }

    block = Block(roads = listOf(road0, road1))
  }

  @Test
  fun testSameDirection() {

    val v0 = Vehicle(id = 0, lane = laneRoad0Id1, isEgo = true)
    val v1 = Vehicle(id = 1, lane = laneRoad0Id1)
    val v2 = Vehicle(id = 2, lane = laneRoad0Id2)

    val td =
        TickData(
            currentTick = TickDataUnitSeconds(0.0),
            blocks = listOf(block),
            entities = listOf(v0, v1, v2))

    listOf(v0, v1, v2).forEach { it.tickData = td }

    val ctx = PredicateContext(Segment(listOf(td), segmentSource = ""))
    assertTrue { sameDirection.holds(ctx, TickDataUnitSeconds(0.0), v0.id, v1.id) }
    assertTrue { sameDirection.holds(ctx, TickDataUnitSeconds(0.0), v0.id, v2.id) }
    assertTrue { sameDirection.holds(ctx, TickDataUnitSeconds(0.0), v1.id, v2.id) }
  }

  @Test
  fun testSameDirectionOnDifferentRoads() {
    val v0 = Vehicle(id = 0, lane = laneRoad0Id1, isEgo = true)
    val v1 = Vehicle(id = 1, lane = laneRoad1Id1)
    val v2 = Vehicle(id = 2, lane = laneRoad1Id2)

    val td =
        TickData(
            currentTick = TickDataUnitSeconds(0.0),
            blocks = listOf(block),
            entities = listOf(v0, v1, v2))

    listOf(v0, v1, v2).forEach { it.tickData = td }

    val ctx = PredicateContext(Segment(listOf(td), segmentSource = ""))
    assertFalse { sameDirection.holds(ctx, TickDataUnitSeconds(0.0), v0.id, v1.id) }
    assertFalse { sameDirection.holds(ctx, TickDataUnitSeconds(0.0), v0.id, v2.id) }
    assertTrue { sameDirection.holds(ctx, TickDataUnitSeconds(0.0), v1.id, v2.id) }
  }
}
