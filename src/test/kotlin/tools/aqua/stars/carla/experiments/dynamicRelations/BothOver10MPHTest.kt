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
import tools.aqua.stars.carla.experiments.bothOver10MPH
import tools.aqua.stars.core.evaluation.PredicateContext
import tools.aqua.stars.data.av.dataclasses.*

class BothOver10MPHTest {
  private lateinit var road0: Road
  private lateinit var road0lane1: Lane
  private lateinit var road0lane2: Lane

  private lateinit var road1: Road
  private lateinit var road1lane1: Lane

  private lateinit var block: Block

  private val egoId = 0
  private val otherId = 1

  @BeforeTest
  fun setup() {
    road0lane1 = Lane(laneId = 1, laneLength = 50.0)
    road0lane2 = Lane(laneId = 2, laneLength = 50.0)
    road1lane1 = Lane(laneId = 1, laneLength = 50.0)

    road0 = Road(lanes = listOf(road0lane1, road0lane2))
    road1 = Road(lanes = listOf(road1lane1))

    road0lane1.road = road0
    road0lane2.road = road0
    road1lane1.road = road1

    block = Block(roads = listOf(road0, road1))
  }

  @Test
  fun bothOver10MPHSameLane() {

    val ego = Vehicle(id = egoId, isEgo = true, lane = road0lane1, positionOnLane = 0.0)
    val other = Vehicle(id = otherId, isEgo = false, lane = road0lane1, positionOnLane = 0.0)

    val tickData =
        TickData(
            currentTick = TickDataUnitSeconds(0.0),
            entities = listOf(ego, other),
            blocks = listOf(block))

    ego.tickData = tickData
    other.tickData = tickData

    ego.setVelocityFromEffVelocityMPH(11.0)
    other.setVelocityFromEffVelocityMPH(11.0)

    val ctx = PredicateContext(Segment(listOf(tickData), segmentSource = ""))
    assertTrue { bothOver10MPH.holds(ctx, ego, other) }
    assertTrue { bothOver10MPH.holds(ctx, other, ego) }
  }

  @Test
  fun bothOver10MPHDifferentLane() {
    val ego = Vehicle(id = egoId, isEgo = true, lane = road0lane1, positionOnLane = 0.0)
    val other = Vehicle(id = otherId, isEgo = false, lane = road0lane2, positionOnLane = 0.0)

    val tickData =
        TickData(
            currentTick = TickDataUnitSeconds(0.0),
            entities = listOf(ego, other),
            blocks = listOf(block))
    ego.tickData = tickData
    other.tickData = tickData

    ego.setVelocityFromEffVelocityMPH(11.0)
    other.setVelocityFromEffVelocityMPH(11.0)

    val ctx = PredicateContext(Segment(listOf(tickData), segmentSource = ""))
    assertTrue { bothOver10MPH.holds(ctx, ego, other) }
    assertTrue { bothOver10MPH.holds(ctx, other, ego) }
  }

  @Test
  fun bothOver10MPHDifferentRoad() {
    val ego = Vehicle(id = egoId, isEgo = true, lane = road0lane1, positionOnLane = 0.0)
    val other = Vehicle(id = otherId, isEgo = false, lane = road1lane1, positionOnLane = 0.0)

    val tickData =
        TickData(
            currentTick = TickDataUnitSeconds(0.0),
            entities = listOf(ego, other),
            blocks = listOf(block))
    ego.tickData = tickData
    other.tickData = tickData

    ego.setVelocityFromEffVelocityMPH(11.0)
    other.setVelocityFromEffVelocityMPH(11.0)

    val ctx = PredicateContext(Segment(listOf(tickData), segmentSource = ""))
    assertTrue { bothOver10MPH.holds(ctx, ego, other) }
    assertTrue { bothOver10MPH.holds(ctx, other, ego) }
  }

  @Test
  fun egoUnder10MPHSameLane() {
    val ego = Vehicle(id = egoId, isEgo = true, lane = road0lane1, positionOnLane = 0.0)
    val other = Vehicle(id = otherId, isEgo = false, lane = road0lane1, positionOnLane = 0.0)

    val tickData =
        TickData(
            currentTick = TickDataUnitSeconds(0.0),
            entities = listOf(ego, other),
            blocks = listOf(block))
    ego.tickData = tickData
    other.tickData = tickData

    ego.setVelocityFromEffVelocityMPH(9.0)
    other.setVelocityFromEffVelocityMPH(11.0)

    val ctx = PredicateContext(Segment(listOf(tickData), segmentSource = ""))
    assertFalse { bothOver10MPH.holds(ctx, ego, other) }
    assertFalse { bothOver10MPH.holds(ctx, other, ego) }
  }

  @Test
  fun egoUnder10MPHDifferentLane() {

    val ego = Vehicle(id = egoId, isEgo = true, lane = road0lane1, positionOnLane = 0.0)
    val other = Vehicle(id = otherId, isEgo = false, lane = road0lane2, positionOnLane = 0.0)

    val tickData =
        TickData(
            currentTick = TickDataUnitSeconds(0.0),
            blocks = listOf(block),
            entities = listOf(ego, other))

    ego.tickData = tickData
    other.tickData = tickData

    ego.setVelocityFromEffVelocityMPH(9.0)
    other.setVelocityFromEffVelocityMPH(11.0)

    val ctx = PredicateContext(Segment(listOf(tickData), segmentSource = ""))
    assertFalse { bothOver10MPH.holds(ctx, ego, other) }
    assertFalse { bothOver10MPH.holds(ctx, other, ego) }
  }

  @Test
  fun egoUnder10MPHDifferentRoad() {
    val ego = Vehicle(id = egoId, isEgo = true, lane = road0lane1, positionOnLane = 0.0)
    val other = Vehicle(id = otherId, isEgo = false, lane = road1lane1, positionOnLane = 0.0)

    val tickData =
        TickData(
            currentTick = TickDataUnitSeconds(0.0),
            blocks = listOf(block),
            entities = listOf(ego, other))
    ego.tickData = tickData
    other.tickData = tickData

    ego.setVelocityFromEffVelocityMPH(9.0)
    other.setVelocityFromEffVelocityMPH(11.0)

    val ctx = PredicateContext(Segment(listOf(tickData), segmentSource = ""))
    assertFalse { bothOver10MPH.holds(ctx, ego, other) }
    assertFalse { bothOver10MPH.holds(ctx, other, ego) }
  }

  @Test
  fun otherUnder10MPHSameLane() {
    val ego = Vehicle(id = egoId, isEgo = true, lane = road0lane1, positionOnLane = 0.0)
    val other = Vehicle(id = otherId, isEgo = false, lane = road0lane1, positionOnLane = 0.0)

    val tickData =
        TickData(
            currentTick = TickDataUnitSeconds(0.0),
            blocks = listOf(block),
            entities = listOf(ego, other))
    ego.tickData = tickData
    other.tickData = tickData

    ego.setVelocityFromEffVelocityMPH(11.0)
    other.setVelocityFromEffVelocityMPH(9.0)

    val ctx = PredicateContext(Segment(listOf(tickData), segmentSource = ""))
    assertFalse { bothOver10MPH.holds(ctx, ego, other) }
    assertFalse { bothOver10MPH.holds(ctx, other, ego) }
  }

  @Test
  fun otherUnder10MPHDifferentLane() {
    val ego = Vehicle(id = egoId, isEgo = true, lane = road0lane1, positionOnLane = 0.0)
    val other = Vehicle(id = otherId, isEgo = false, lane = road0lane2, positionOnLane = 0.0)

    val tickData =
        TickData(
            currentTick = TickDataUnitSeconds(0.0),
            blocks = listOf(block),
            entities = listOf(ego, other))
    ego.tickData = tickData
    other.tickData = tickData

    ego.setVelocityFromEffVelocityMPH(11.0)
    other.setVelocityFromEffVelocityMPH(9.0)

    val ctx = PredicateContext(Segment(listOf(tickData), segmentSource = ""))
    assertFalse { bothOver10MPH.holds(ctx, ego, other) }
    assertFalse { bothOver10MPH.holds(ctx, other, ego) }
  }

  @Test
  fun otherUnder10MPHDifferentRoad() {
    val ego = Vehicle(id = egoId, isEgo = true, lane = road0lane1, positionOnLane = 0.0)
    val other = Vehicle(id = otherId, isEgo = false, lane = road1lane1, positionOnLane = 0.0)

    val tickData =
        TickData(
            currentTick = TickDataUnitSeconds(0.0),
            blocks = listOf(block),
            entities = listOf(ego, other))
    ego.tickData = tickData
    other.tickData = tickData

    ego.setVelocityFromEffVelocityMPH(11.0)
    other.setVelocityFromEffVelocityMPH(9.0)

    val ctx = PredicateContext(Segment(listOf(tickData), segmentSource = ""))
    assertFalse { bothOver10MPH.holds(ctx, ego, other) }
    assertFalse { bothOver10MPH.holds(ctx, other, ego) }
  }

  @Test
  fun bothUnder10MPHSameLane() {
    val ego = Vehicle(id = egoId, isEgo = true, lane = road0lane1, positionOnLane = 0.0)
    val other = Vehicle(id = otherId, isEgo = false, lane = road0lane1, positionOnLane = 0.0)

    val tickData =
        TickData(
            currentTick = TickDataUnitSeconds(0.0),
            blocks = listOf(block),
            entities = listOf(ego, other))
    ego.tickData = tickData
    other.tickData = tickData

    ego.setVelocityFromEffVelocityMPH(9.0)
    other.setVelocityFromEffVelocityMPH(9.0)

    val ctx = PredicateContext(Segment(listOf(tickData), segmentSource = ""))
    assertFalse { bothOver10MPH.holds(ctx, ego, other) }
    assertFalse { bothOver10MPH.holds(ctx, other, ego) }
  }

  @Test
  fun bothUnder10MPHDifferentLane() {
    val ego = Vehicle(id = egoId, isEgo = true, lane = road0lane1, positionOnLane = 0.0)
    val other = Vehicle(id = otherId, isEgo = false, lane = road0lane2, positionOnLane = 0.0)

    val tickData =
        TickData(
            currentTick = TickDataUnitSeconds(0.0),
            blocks = listOf(block),
            entities = listOf(ego, other))
    ego.tickData = tickData
    other.tickData = tickData

    ego.setVelocityFromEffVelocityMPH(9.0)
    other.setVelocityFromEffVelocityMPH(9.0)

    val ctx = PredicateContext(Segment(listOf(tickData), segmentSource = ""))
    assertFalse { bothOver10MPH.holds(ctx, ego, other) }
    assertFalse { bothOver10MPH.holds(ctx, other, ego) }
  }

  @Test
  fun bothUnder10MPHDifferentRoad() {
    val ego = Vehicle(id = egoId, isEgo = true, lane = road0lane1, positionOnLane = 0.0)
    val other = Vehicle(id = otherId, isEgo = false, lane = road1lane1, positionOnLane = 0.0)

    val tickData =
        TickData(
            currentTick = TickDataUnitSeconds(0.0),
            blocks = listOf(block),
            entities = listOf(ego, other))
    ego.tickData = tickData
    other.tickData = tickData

    ego.setVelocityFromEffVelocityMPH(9.0)
    other.setVelocityFromEffVelocityMPH(9.0)

    val ctx = PredicateContext(Segment(listOf(tickData), segmentSource = ""))
    assertFalse { bothOver10MPH.holds(ctx, ego, other) }
    assertFalse { bothOver10MPH.holds(ctx, other, ego) }
  }

  @Test
  fun bothEqual10MPHSameLane() {
    val ego = Vehicle(id = egoId, isEgo = true, lane = road0lane1, positionOnLane = 0.0)
    val other = Vehicle(id = otherId, isEgo = false, lane = road0lane1, positionOnLane = 0.0)

    val tickData =
        TickData(
            currentTick = TickDataUnitSeconds(0.0),
            blocks = listOf(block),
            entities = listOf(ego, other))
    ego.tickData = tickData
    other.tickData = tickData

    ego.setVelocityFromEffVelocityMPH(10.0)
    other.setVelocityFromEffVelocityMPH(10.0)

    val ctx = PredicateContext(Segment(listOf(tickData), segmentSource = ""))
    assertFalse { bothOver10MPH.holds(ctx, ego, other) }
    assertFalse { bothOver10MPH.holds(ctx, other, ego) }
  }

  @Test
  fun bothEqual10MPHDifferentLane() {
    val ego = Vehicle(id = egoId, isEgo = true, lane = road0lane1, positionOnLane = 0.0)
    val other = Vehicle(id = otherId, isEgo = false, lane = road0lane2, positionOnLane = 0.0)

    val tickData =
        TickData(
            currentTick = TickDataUnitSeconds(0.0),
            blocks = listOf(block),
            entities = listOf(ego, other))
    ego.tickData = tickData
    other.tickData = tickData

    ego.setVelocityFromEffVelocityMPH(10.0)
    other.setVelocityFromEffVelocityMPH(10.0)

    val ctx = PredicateContext(Segment(listOf(tickData), segmentSource = ""))
    assertFalse { bothOver10MPH.holds(ctx, ego, other) }
    assertFalse { bothOver10MPH.holds(ctx, other, ego) }
  }

  @Test
  fun bothEqual10MPHDifferentRoad() {

    val ego = Vehicle(id = egoId, isEgo = true, lane = road0lane1, positionOnLane = 0.0)
    val other = Vehicle(id = otherId, isEgo = false, lane = road1lane1, positionOnLane = 0.0)

    val tickData =
        TickData(
            currentTick = TickDataUnitSeconds(0.0),
            blocks = listOf(block),
            entities = listOf(ego, other))

    ego.tickData = tickData
    other.tickData = tickData

    ego.setVelocityFromEffVelocityMPH(10.0)
    other.setVelocityFromEffVelocityMPH(10.0)

    // assert neither exceeds 10 MPH
    val ctx = PredicateContext(Segment(listOf(tickData), segmentSource = ""))
    assertFalse { bothOver10MPH.holds(ctx, ego, other) }
    assertFalse { bothOver10MPH.holds(ctx, other, ego) }
  }

  @Test
  fun egoEqual10MPHSameLane() {
    val ego = Vehicle(id = egoId, isEgo = true, lane = road0lane1, positionOnLane = 0.0)
    val other = Vehicle(id = otherId, isEgo = false, lane = road0lane1, positionOnLane = 0.0)

    val tickData =
        TickData(
            currentTick = TickDataUnitSeconds(0.0),
            blocks = listOf(block),
            entities = listOf(ego, other))
    ego.tickData = tickData
    other.tickData = tickData

    ego.setVelocityFromEffVelocityMPH(10.0)
    other.setVelocityFromEffVelocityMPH(11.0)

    val ctx = PredicateContext(Segment(listOf(tickData), segmentSource = ""))
    assertFalse { bothOver10MPH.holds(ctx, ego, other) }
    assertFalse { bothOver10MPH.holds(ctx, other, ego) }
  }

  @Test
  fun egoEqual10MPHDifferentLane() {
    val ego = Vehicle(id = egoId, isEgo = true, lane = road0lane1, positionOnLane = 0.0)
    val other = Vehicle(id = otherId, isEgo = false, lane = road0lane2, positionOnLane = 0.0)

    val tickData =
        TickData(
            currentTick = TickDataUnitSeconds(0.0),
            blocks = listOf(block),
            entities = listOf(ego, other))
    ego.tickData = tickData
    other.tickData = tickData

    ego.setVelocityFromEffVelocityMPH(10.0)
    other.setVelocityFromEffVelocityMPH(11.0)

    val ctx = PredicateContext(Segment(listOf(tickData), segmentSource = ""))
    assertFalse { bothOver10MPH.holds(ctx, ego, other) }
    assertFalse { bothOver10MPH.holds(ctx, other, ego) }
  }

  @Test
  fun egoEqual10MPHDifferentRoad() {
    val ego = Vehicle(id = egoId, isEgo = true, lane = road0lane1, positionOnLane = 0.0)
    val other = Vehicle(id = otherId, isEgo = false, lane = road1lane1, positionOnLane = 0.0)

    val tickData =
        TickData(
            currentTick = TickDataUnitSeconds(0.0),
            blocks = listOf(block),
            entities = listOf(ego, other))
    ego.tickData = tickData
    other.tickData = tickData

    ego.setVelocityFromEffVelocityMPH(10.0)
    other.setVelocityFromEffVelocityMPH(11.0)

    val ctx = PredicateContext(Segment(listOf(tickData), segmentSource = ""))
    assertFalse { bothOver10MPH.holds(ctx, ego, other) }
    assertFalse { bothOver10MPH.holds(ctx, other, ego) }
  }

  @Test
  fun otherEqual10MPHSameLane() {
    val ego = Vehicle(id = egoId, isEgo = true, lane = road0lane1, positionOnLane = 0.0)
    val other = Vehicle(id = otherId, isEgo = false, lane = road0lane1, positionOnLane = 0.0)

    val tickData =
        TickData(
            currentTick = TickDataUnitSeconds(0.0),
            blocks = listOf(block),
            entities = listOf(ego, other))
    ego.tickData = tickData
    other.tickData = tickData

    ego.setVelocityFromEffVelocityMPH(11.0)
    other.setVelocityFromEffVelocityMPH(10.0)

    val ctx = PredicateContext(Segment(listOf(tickData), segmentSource = ""))
    assertFalse { bothOver10MPH.holds(ctx, ego, other) }
    assertFalse { bothOver10MPH.holds(ctx, other, ego) }
  }

  @Test
  fun otherEqual10MPHDifferentLane() {
    val ego = Vehicle(id = egoId, isEgo = true, lane = road0lane1, positionOnLane = 0.0)
    val other = Vehicle(id = otherId, isEgo = false, lane = road0lane2, positionOnLane = 0.0)

    val tickData =
        TickData(
            currentTick = TickDataUnitSeconds(0.0),
            blocks = listOf(block),
            entities = listOf(ego, other))
    ego.tickData = tickData
    other.tickData = tickData

    ego.setVelocityFromEffVelocityMPH(11.0)
    other.setVelocityFromEffVelocityMPH(10.0)

    val ctx = PredicateContext(Segment(listOf(tickData), segmentSource = ""))
    assertFalse { bothOver10MPH.holds(ctx, ego, other) }
    assertFalse { bothOver10MPH.holds(ctx, other, ego) }
  }

  @Test
  fun otherEqual10MPHDifferentRoad() {
    val ego = Vehicle(id = egoId, isEgo = true, lane = road0lane1, positionOnLane = 0.0)
    val other = Vehicle(id = otherId, isEgo = false, lane = road1lane1, positionOnLane = 0.0)

    val tickData =
        TickData(
            currentTick = TickDataUnitSeconds(0.0),
            blocks = listOf(block),
            entities = listOf(ego, other))
    ego.tickData = tickData
    other.tickData = tickData

    ego.setVelocityFromEffVelocityMPH(11.0)
    other.setVelocityFromEffVelocityMPH(10.0)

    val ctx = PredicateContext(Segment(listOf(tickData), segmentSource = ""))
    assertFalse { bothOver10MPH.holds(ctx, ego, other) }
    assertFalse { bothOver10MPH.holds(ctx, other, ego) }
  }

  @Test
  fun testWithNoOtherVehicle() {

    val ego = Vehicle(id = egoId, isEgo = true, lane = road0lane1, positionOnLane = 0.0)

    val tickData =
        TickData(
            currentTick = TickDataUnitSeconds(0.0), blocks = listOf(block), entities = listOf(ego))

    ego.tickData = tickData
    ego.setVelocityFromEffVelocityMPH(11.0)

    val ctx = PredicateContext(Segment(listOf(tickData), segmentSource = ""))
    assertFalse { bothOver10MPH.holds(ctx, TickDataUnitSeconds(0.0), egoId, otherId) }
  }

  @Test
  fun testWithItselfVehicle() {

    val ego = Vehicle(id = egoId, isEgo = true, lane = road0lane1, positionOnLane = 0.0)

    val tickData =
        TickData(
            currentTick = TickDataUnitSeconds(0.0), blocks = listOf(block), entities = listOf(ego))

    ego.tickData = tickData
    ego.setVelocityFromEffVelocityMPH(11.0)

    val ctx = PredicateContext(Segment(listOf(tickData), segmentSource = ""))
    assertFalse { bothOver10MPH.holds(ctx, TickDataUnitSeconds(0.0), egoId, egoId) }
  }
}
