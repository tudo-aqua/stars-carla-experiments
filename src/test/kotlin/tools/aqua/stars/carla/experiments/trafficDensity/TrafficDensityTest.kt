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

package tools.aqua.stars.carla.experiments.trafficDensity

import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import tools.aqua.stars.carla.experiments.hasHighTrafficDensity
import tools.aqua.stars.carla.experiments.hasLowTrafficDensity
import tools.aqua.stars.carla.experiments.hasMidTrafficDensity
import tools.aqua.stars.core.evaluation.PredicateContext
import tools.aqua.stars.data.av.dataclasses.Block
import tools.aqua.stars.data.av.dataclasses.Lane
import tools.aqua.stars.data.av.dataclasses.Road
import tools.aqua.stars.data.av.dataclasses.Segment
import tools.aqua.stars.data.av.dataclasses.TickData
import tools.aqua.stars.data.av.dataclasses.TickDataUnitSeconds
import tools.aqua.stars.data.av.dataclasses.Vehicle

class TrafficDensityTest {

  private lateinit var block0: Block
  private lateinit var block2: Block

  private lateinit var road0: Road
  private lateinit var road0lane1: Lane

  private lateinit var road1: Road
  private lateinit var road1lane1: Lane

  private lateinit var road2: Road
  private lateinit var road2lane1: Lane

  @BeforeTest
  fun setup() {
    road0lane1 = Lane(laneId = 1, laneLength = 50.0)
    road1lane1 = Lane(laneId = 1, laneLength = 50.0)
    road2lane1 = Lane(laneId = 1, laneLength = 50.0)

    road0 = Road(id = 0, lanes = listOf(road0lane1))
    road0lane1.road = road0

    road1 = Road(id = 1, lanes = listOf(road1lane1))
    road1lane1.road = road1

    block0 = Block(id = "1", roads = listOf(road0, road1))
    road0.block = block0
    road1.block = block0

    road2 = Road(id = 2, lanes = listOf(road2lane1))
    road2lane1.road = road2

    block2 = Block(id = "2", roads = listOf(road2))
    road2.block = block2
  }

  @Test
  fun hasLowTrafficDensity() {

    val actors =
        (1..5).map { i ->
          Vehicle(id = i, isEgo = i == 1, lane = road0lane1, positionOnLane = i.toDouble())
        }

    // 2) single TickData containing them all
    val td =
        TickData(currentTick = TickDataUnitSeconds(0.0), blocks = listOf(block0), entities = actors)

    // 3) wire back‐refs and speed
    actors.forEach {
      it.apply {
        tickData = td
        setVelocityFromEffVelocityMPH(11.0)
      }
    }

    // 4) assert
    val ctx = PredicateContext(Segment(listOf(td), segmentSource = ""))
    assertTrue { hasLowTrafficDensity.holds(ctx, TickDataUnitSeconds(0.0), 1) }
    assertFalse { hasMidTrafficDensity.holds(ctx, TickDataUnitSeconds(0.0), 1) }
    assertFalse { hasHighTrafficDensity.holds(ctx, TickDataUnitSeconds(0.0), 1) }
  }

  @Test
  fun hasMidTrafficDensity() {

    val actors =
        (1..10).map { i ->
          Vehicle(id = i, isEgo = i == 1, lane = road0lane1, positionOnLane = i.toDouble())
        }

    // 2) single TickData containing them all
    val td =
        TickData(currentTick = TickDataUnitSeconds(0.0), blocks = listOf(block0), entities = actors)

    // 3) wire back‐refs and speed
    actors.forEach {
      it.apply {
        tickData = td
        setVelocityFromEffVelocityMPH(11.0)
      }
    }

    // 4) assert
    val ctx = PredicateContext(Segment(listOf(td), segmentSource = ""))
    assertFalse { hasLowTrafficDensity.holds(ctx, TickDataUnitSeconds(0.0), 1) }
    assertTrue { hasMidTrafficDensity.holds(ctx, TickDataUnitSeconds(0.0), 1) }
    assertFalse { hasHighTrafficDensity.holds(ctx, TickDataUnitSeconds(0.0), 1) }
  }

  @Test
  fun hasHighTrafficDensity() {

    val actors =
        (1..20).map { i ->
          Vehicle(id = i, isEgo = i == 1, lane = road0lane1, positionOnLane = i.toDouble())
        }

    // 2) single TickData containing them all
    val td =
        TickData(currentTick = TickDataUnitSeconds(0.0), blocks = listOf(block0), entities = actors)

    // 3) wire back‐refs and speed
    actors.forEach {
      it.apply {
        tickData = td
        setVelocityFromEffVelocityMPH(11.0)
      }
    }

    // 4) assert
    val ctx = PredicateContext(Segment(listOf(td), segmentSource = ""))
    assertFalse { hasLowTrafficDensity.holds(ctx, TickDataUnitSeconds(0.0), 1) }
    assertFalse { hasMidTrafficDensity.holds(ctx, TickDataUnitSeconds(0.0), 1) }
    assertTrue { hasHighTrafficDensity.holds(ctx, TickDataUnitSeconds(0.0), 1) }
  }

  @Test
  fun hasLowTrafficAndOtherBlockHasHighTrafficDensity() {

    val egoBlockActors =
        (1..5).map { i ->
          Vehicle(id = i, isEgo = i == 1, lane = road0lane1, positionOnLane = i.toDouble())
        }
    val otherBlockActors =
        (6..25).map { i ->
          Vehicle(id = i, isEgo = false, lane = road2lane1, positionOnLane = i.toDouble())
        }
    val allActors = egoBlockActors + otherBlockActors

    // 2) build TickData with both blocks
    val tickData =
        TickData(
            currentTick = TickDataUnitSeconds(0.0),
            blocks = listOf(block0, block2),
            entities = allActors)

    // 3) wire back–references and set speed
    allActors.forEach {
      it.tickData = tickData
      it.setVelocityFromEffVelocityMPH(11.0)
    }

    // 4) assert
    val ctx = PredicateContext(Segment(listOf(tickData), segmentSource = ""))
    assertTrue { hasLowTrafficDensity.holds(ctx, TickDataUnitSeconds(0.0), 1) }
    assertFalse { hasMidTrafficDensity.holds(ctx, TickDataUnitSeconds(0.0), 1) }
    assertFalse { hasHighTrafficDensity.holds(ctx, TickDataUnitSeconds(0.0), 1) }
  }

  @Test
  fun hasMidTrafficAndOtherBlockHasHighTrafficDensity() {

    val egoBlockActors =
        (1..10).map { i ->
          Vehicle(id = i, isEgo = i == 1, lane = road0lane1, positionOnLane = i.toDouble())
        }
    val otherBlockActors =
        (11..100).map { i ->
          Vehicle(id = i, isEgo = false, lane = road2lane1, positionOnLane = i.toDouble())
        }
    val allActors = egoBlockActors + otherBlockActors

    // 2) build TickData with both blocks
    val tickData =
        TickData(
            currentTick = TickDataUnitSeconds(0.0),
            blocks = listOf(block0, block2),
            entities = allActors)

    // 3) wire back–references and set speed
    allActors.forEach {
      it.tickData = tickData
      it.setVelocityFromEffVelocityMPH(11.0)
    }

    // 4) assert
    val ctx = PredicateContext(Segment(listOf(tickData), segmentSource = ""))
    assertFalse { hasLowTrafficDensity.holds(ctx, TickDataUnitSeconds(0.0), 1) }
    assertTrue { hasMidTrafficDensity.holds(ctx, TickDataUnitSeconds(0.0), 1) }
    assertFalse { hasHighTrafficDensity.holds(ctx, TickDataUnitSeconds(0.0), 1) }
  }

  @Test
  fun hasHighTrafficAndOtherBlockHasLowTrafficDensity() {

    val egoBlockActors =
        (1..20).map { i ->
          Vehicle(id = i, isEgo = i == 1, lane = road0lane1, positionOnLane = i.toDouble())
        }
    val otherBlockActors =
        (21..25).map { i ->
          Vehicle(id = i, isEgo = false, lane = road2lane1, positionOnLane = i.toDouble())
        }
    val allActors = egoBlockActors + otherBlockActors

    // 2) build TickData with both blocks
    val tickData =
        TickData(
            currentTick = TickDataUnitSeconds(0.0),
            blocks = listOf(block0, block2),
            entities = allActors)

    // 3) wire back–references and set speed
    allActors.forEach {
      it.tickData = tickData
      it.setVelocityFromEffVelocityMPH(11.0)
    }

    // 4) assert
    val ctx = PredicateContext(Segment(listOf(tickData), segmentSource = ""))
    assertFalse { hasLowTrafficDensity.holds(ctx, TickDataUnitSeconds(0.0), 1) }
    assertFalse { hasMidTrafficDensity.holds(ctx, TickDataUnitSeconds(0.0), 1) }
    assertTrue { hasHighTrafficDensity.holds(ctx, TickDataUnitSeconds(0.0), 1) }
  }
}
