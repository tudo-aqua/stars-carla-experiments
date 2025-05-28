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
import tools.aqua.stars.carla.experiments.stopAtEnd
import tools.aqua.stars.core.evaluation.PredicateContext
import tools.aqua.stars.data.av.dataclasses.Block
import tools.aqua.stars.data.av.dataclasses.Lane
import tools.aqua.stars.data.av.dataclasses.Road
import tools.aqua.stars.data.av.dataclasses.Segment
import tools.aqua.stars.data.av.dataclasses.TickData
import tools.aqua.stars.data.av.dataclasses.TickDataUnitSeconds
import tools.aqua.stars.data.av.dataclasses.Vehicle

class StopAtEndTest {
  private lateinit var road0: Road
  private lateinit var road0lane1: Lane

  private lateinit var road1: Road
  private lateinit var road1lane1: Lane

  private lateinit var block: Block

  @BeforeTest
  fun setup() {
    road0lane1 = Lane(laneId = 1, laneLength = 50.0)
    road1lane1 = Lane(laneId = 1, laneLength = 2.5)

    road0 = Road(id = 0, lanes = listOf(road0lane1))
    road1 = Road(id = 1, lanes = listOf(road1lane1))

    road0lane1.road = road0
    road1lane1.road = road1

    block = Block(roads = listOf(road0, road1))
  }

  @Test
  fun is2MetersFromEndOfRoadAndStopped() {

    val ego =
        Vehicle(
            id = 0, isEgo = true, lane = road0lane1, positionOnLane = road0lane1.laneLength - 2.0)

    val td =
        TickData(
            currentTick = TickDataUnitSeconds(0.0), blocks = listOf(block), entities = listOf(ego))

    ego.tickData = td
    ego.setVelocityFromEffVelocityMPH(1.0)

    val ctx = PredicateContext(Segment(listOf(td), segmentSource = ""))
    assertTrue { stopAtEnd.holds(ctx, ego) }
  }

  @Test
  fun is2MetersFromEndOfRoadAndDidNotStop() {
    val ego =
        Vehicle(
            id = 0, isEgo = true, lane = road0lane1, positionOnLane = road0lane1.laneLength - 2.0)
    val td =
        TickData(
            currentTick = TickDataUnitSeconds(0.0), blocks = listOf(block), entities = listOf(ego))
    ego.tickData = td
    ego.setVelocityFromEffVelocityMPH(11.0)

    val ctx = PredicateContext(Segment(listOf(td), segmentSource = ""))
    assertFalse { stopAtEnd.holds(ctx, ego) }
  }

  @Test
  fun is3MetersFromEndOfRoadAndStopped() {
    val ego =
        Vehicle(
            id = 0, isEgo = true, lane = road0lane1, positionOnLane = road0lane1.laneLength - 3.0)
    val td =
        TickData(
            currentTick = TickDataUnitSeconds(0.0), blocks = listOf(block), entities = listOf(ego))
    ego.tickData = td
    ego.setVelocityFromEffVelocityMPH(1.0)

    val ctx = PredicateContext(Segment(listOf(td), segmentSource = ""))
    assertTrue { stopAtEnd.holds(ctx, ego) }
  }

  @Test
  fun is3MetersFromEndOfRoadAndDidNotStop() {
    val ego =
        Vehicle(
            id = 0, isEgo = true, lane = road0lane1, positionOnLane = road0lane1.laneLength - 3.0)
    val td =
        TickData(
            currentTick = TickDataUnitSeconds(0.0), blocks = listOf(block), entities = listOf(ego))
    ego.tickData = td
    ego.setVelocityFromEffVelocityMPH(11.0)

    val ctx = PredicateContext(Segment(listOf(td), segmentSource = ""))
    assertFalse { stopAtEnd.holds(ctx, ego) }
  }

  @Test
  fun is4MetersFromEndOfRoadAndStopped() {
    val ego =
        Vehicle(
            id = 0, isEgo = true, lane = road0lane1, positionOnLane = road0lane1.laneLength - 4.0)
    val td =
        TickData(
            currentTick = TickDataUnitSeconds(0.0), blocks = listOf(block), entities = listOf(ego))
    ego.tickData = td
    ego.setVelocityFromEffVelocityMPH(1.0)

    val ctx = PredicateContext(Segment(listOf(td), segmentSource = ""))
    assertFalse { stopAtEnd.holds(ctx, ego) }
  }

  @Test
  fun is4MetersFromEndOfRoadAndDidNotStop() {

    val ego =
        Vehicle(
            id = 0, isEgo = true, lane = road0lane1, positionOnLane = road0lane1.laneLength - 4.0)

    val td =
        TickData(
            currentTick = TickDataUnitSeconds(0.0), blocks = listOf(block), entities = listOf(ego))

    ego.tickData = td
    ego.setVelocityFromEffVelocityMPH(11.0)

    val ctx = PredicateContext(Segment(listOf(td), segmentSource = ""))
    assertFalse { stopAtEnd.holds(ctx, ego) }
  }

  @Test
  fun laneShorterThan3MetersEgoAt1MeterAndStopped() {
    val ego =
        Vehicle(
            id = 0,
            isEgo = true,
            lane = road1lane1, // this lane is only 2.5m long
            positionOnLane = 1.0)
    val td =
        TickData(
            currentTick = TickDataUnitSeconds(0.0), blocks = listOf(block), entities = listOf(ego))
    ego.tickData = td
    ego.setVelocityFromEffVelocityMPH(1.0)

    val ctx = PredicateContext(Segment(listOf(td), segmentSource = ""))
    assertTrue { stopAtEnd.holds(ctx, ego) }
  }

  @Test
  fun laneShorterThan3MetersEgoAt1MeterAndDidNotStop() {
    val ego = Vehicle(id = 0, isEgo = true, lane = road1lane1, positionOnLane = 1.0)
    val td =
        TickData(
            currentTick = TickDataUnitSeconds(0.0), blocks = listOf(block), entities = listOf(ego))
    ego.tickData = td
    ego.setVelocityFromEffVelocityMPH(11.0)

    val ctx = PredicateContext(Segment(listOf(td), segmentSource = ""))
    assertFalse { stopAtEnd.holds(ctx, ego) }
  }

  @Test
  fun egoFasterBeforeAndStopped2MetersBeforeEnd() {
    // tick 0: ego is 4 m from end, fast
    val ego0 =
        Vehicle(
            id = 0, isEgo = true, lane = road0lane1, positionOnLane = road0lane1.laneLength - 4.0)
    val td0 =
        TickData(
            currentTick = TickDataUnitSeconds(0.0), blocks = listOf(block), entities = listOf(ego0))
    ego0.tickData = td0
    ego0.setVelocityFromEffVelocityMPH(11.0)

    val ctx0 = PredicateContext(Segment(listOf(td0), segmentSource = ""))
    assertFalse { stopAtEnd.holds(ctx0, ego0) }

    // tick 1: ego is 2 m from end, slow
    val ego1 =
        Vehicle(
            id = 0, isEgo = true, lane = road0lane1, positionOnLane = road0lane1.laneLength - 2.0)
    val td1 =
        TickData(
            currentTick = TickDataUnitSeconds(1.0), blocks = listOf(block), entities = listOf(ego1))
    ego1.tickData = td1
    ego1.setVelocityFromEffVelocityMPH(1.0)

    val ctx1 = PredicateContext(Segment(listOf(td1), segmentSource = ""))
    assertTrue { stopAtEnd.holds(ctx1, ego1) }

    val combinedCtx = PredicateContext(Segment(listOf(td0, td1), segmentSource = ""))
    assertTrue { stopAtEnd.holds(combinedCtx, TickDataUnitSeconds(0.0), 0) }
  }
}
