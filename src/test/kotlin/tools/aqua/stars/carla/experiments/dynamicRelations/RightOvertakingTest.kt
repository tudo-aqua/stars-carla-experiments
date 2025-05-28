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
import tools.aqua.stars.carla.experiments.besides
import tools.aqua.stars.carla.experiments.bothOver10MPH
import tools.aqua.stars.carla.experiments.isBehind
import tools.aqua.stars.carla.experiments.noRightOvertaking
import tools.aqua.stars.carla.experiments.rightOf
import tools.aqua.stars.carla.experiments.rightOvertaking
import tools.aqua.stars.core.evaluation.PredicateContext
import tools.aqua.stars.data.av.dataclasses.Block
import tools.aqua.stars.data.av.dataclasses.Lane
import tools.aqua.stars.data.av.dataclasses.Road
import tools.aqua.stars.data.av.dataclasses.Segment
import tools.aqua.stars.data.av.dataclasses.TickData
import tools.aqua.stars.data.av.dataclasses.TickDataUnitSeconds
import tools.aqua.stars.data.av.dataclasses.Vehicle

class RightOvertakingTest {
  private lateinit var road0: Road
  private lateinit var road0lane1: Lane
  private lateinit var road0lane2: Lane
  private lateinit var road0lane3: Lane
  private lateinit var road0laneMinus1: Lane

  private lateinit var road1: Road
  private lateinit var road1lane1: Lane

  private lateinit var block: Block

  private val egoId = 0
  private val otherId = 1

  @BeforeTest
  fun setup() {
    road0lane1 = Lane(laneId = 1, laneLength = 50.0)
    road0lane2 = Lane(laneId = 2, laneLength = 50.0)
    road0lane3 = Lane(laneId = 3, laneLength = 50.0)
    road0laneMinus1 = Lane(laneId = -1, laneLength = 50.0)

    road1lane1 = Lane(laneId = 1, laneLength = 50.0)

    road0 =
        Road(id = 0, lanes = listOf(road0lane1, road0lane2, road0lane3, road0laneMinus1)).apply {
          road0lane1.road = this
          road0lane2.road = this
          road0lane3.road = this
          road0laneMinus1.road = this
        }

    road1 = Road(id = 1, lanes = listOf(road1lane1)).apply { road1lane1.road = this }

    block = Block(roads = listOf(road0, road1))
  }

  @Test
  fun behindRightOfInFront() {

    val ego0 = Vehicle(id = egoId, isEgo = true, lane = road0lane2, positionOnLane = 0.0)
    val other0 = Vehicle(id = otherId, isEgo = false, lane = road0lane2, positionOnLane = 10.0)
    val td0 =
        TickData(
            currentTick = TickDataUnitSeconds(0.0),
            blocks = listOf(block),
            entities = listOf(ego0, other0))
    ego0.tickData = td0
    ego0.setVelocityFromEffVelocityMPH(11.0)
    other0.tickData = td0
    other0.setVelocityFromEffVelocityMPH(11.0)

    val ctx0 = PredicateContext(Segment(listOf(td0), segmentSource = ""))
    assertTrue { isBehind.holds(ctx0, ego0, other0) }
    assertFalse { isBehind.holds(ctx0, other0, ego0) }
    assertFalse { besides.holds(ctx0, ego0, other0) }
    assertFalse { rightOf.holds(ctx0, ego0, other0) }
    assertFalse { rightOf.holds(ctx0, other0, ego0) }
    assertTrue { bothOver10MPH.holds(ctx0, ego0, other0) }
    assertTrue { bothOver10MPH.holds(ctx0, other0, ego0) }

    val ego1 = Vehicle(id = egoId, isEgo = true, lane = road0lane3, positionOnLane = 13.0)
    val other1 = Vehicle(id = otherId, isEgo = false, lane = road0lane2, positionOnLane = 13.0)
    val td1 =
        TickData(
            currentTick = TickDataUnitSeconds(1.0),
            blocks = listOf(block),
            entities = listOf(ego1, other1))
    ego1.tickData = td1
    ego1.setVelocityFromEffVelocityMPH(11.0)
    other1.tickData = td1
    other1.setVelocityFromEffVelocityMPH(11.0)

    val ctx1 = PredicateContext(Segment(listOf(td1), segmentSource = ""))
    assertFalse { isBehind.holds(ctx1, ego1, other1) }
    assertTrue { rightOf.holds(ctx1, ego1, other1) }
    assertTrue { besides.holds(ctx1, ego1, other1) }
    assertTrue { bothOver10MPH.holds(ctx1, ego1, other1) }

    val ego2 = Vehicle(id = egoId, isEgo = true, lane = road0lane2, positionOnLane = 20.0)
    val other2 = Vehicle(id = otherId, isEgo = false, lane = road0lane3, positionOnLane = 15.0)
    val td2 =
        TickData(
            currentTick = TickDataUnitSeconds(2.0),
            blocks = listOf(block),
            entities = listOf(ego2, other2))
    ego2.tickData = td2
    ego2.setVelocityFromEffVelocityMPH(11.0)
    other2.tickData = td2
    other2.setVelocityFromEffVelocityMPH(11.0)

    val ctx2 = PredicateContext(Segment(listOf(td2), segmentSource = ""))
    assertTrue { isBehind.holds(ctx2, other2, ego2) }
    assertFalse { rightOf.holds(ctx2, ego2, other2) }
    assertTrue { bothOver10MPH.holds(ctx2, ego2, other2) }

    val fullCtx = PredicateContext(Segment(listOf(td0, td1, td2), segmentSource = ""))
    assertTrue { rightOvertaking.holds(fullCtx, TickDataUnitSeconds(0.0), egoId, otherId) }
    assertFalse { noRightOvertaking.holds(fullCtx, TickDataUnitSeconds(0.0), egoId) }
    assertFalse { rightOvertaking.holds(fullCtx, TickDataUnitSeconds(0.0), otherId, egoId) }
    assertTrue { noRightOvertaking.holds(fullCtx, TickDataUnitSeconds(0.0), otherId) }
  }

  @Test
  fun behindRightOfBehindLeftOfInFront() {

    val ego0 = Vehicle(id = egoId, isEgo = true, lane = road0lane2, positionOnLane = 0.0)
    val other0 = Vehicle(id = otherId, isEgo = false, lane = road0lane2, positionOnLane = 10.0)
    val td0 =
        TickData(
            currentTick = TickDataUnitSeconds(0.0),
            blocks = listOf(block),
            entities = listOf(ego0, other0))
    ego0.tickData = td0
    ego0.setVelocityFromEffVelocityMPH(11.0)
    other0.tickData = td0
    other0.setVelocityFromEffVelocityMPH(11.0)

    val ctx0 = PredicateContext(Segment(listOf(td0), segmentSource = ""))
    assertTrue { isBehind.holds(ctx0, ego0, other0) }
    assertFalse { isBehind.holds(ctx0, other0, ego0) }
    assertFalse { rightOf.holds(ctx0, ego0, other0) }
    assertFalse { rightOf.holds(ctx0, other0, ego0) }
    assertFalse { besides.holds(ctx0, ego0, other0) }
    assertFalse { besides.holds(ctx0, other0, ego0) }
    assertTrue { bothOver10MPH.holds(ctx0, ego0, other0) }
    assertTrue { bothOver10MPH.holds(ctx0, other0, ego0) }

    val ego1 = Vehicle(id = egoId, isEgo = true, lane = road0lane3, positionOnLane = 15.0)
    val other1 = Vehicle(id = otherId, isEgo = false, lane = road0lane2, positionOnLane = 15.0)
    val td1 =
        TickData(
            currentTick = TickDataUnitSeconds(1.0),
            blocks = listOf(block),
            entities = listOf(ego1, other1))
    ego1.tickData = td1
    ego1.setVelocityFromEffVelocityMPH(11.0)
    other1.tickData = td1
    other1.setVelocityFromEffVelocityMPH(11.0)

    val ctx1 = PredicateContext(Segment(listOf(td1), segmentSource = ""))
    assertFalse { isBehind.holds(ctx1, ego1, other1) }
    assertTrue { besides.holds(ctx1, ego1, other1) }
    assertTrue { rightOf.holds(ctx1, ego1, other1) }
    assertFalse { rightOf.holds(ctx1, other1, ego1) }
    assertTrue { bothOver10MPH.holds(ctx1, ego1, other1) }

    val ego2 = Vehicle(id = egoId, isEgo = true, lane = road0lane2, positionOnLane = 18.0)
    val other2 = Vehicle(id = otherId, isEgo = false, lane = road0lane2, positionOnLane = 22.0)
    val td2 =
        TickData(
            currentTick = TickDataUnitSeconds(2.0),
            blocks = listOf(block),
            entities = listOf(ego2, other2))
    ego2.tickData = td2
    ego2.setVelocityFromEffVelocityMPH(11.0)
    other2.tickData = td2
    other2.setVelocityFromEffVelocityMPH(11.0)

    val ctx2 = PredicateContext(Segment(listOf(td2), segmentSource = ""))
    assertTrue { isBehind.holds(ctx2, ego2, other2) }
    assertFalse { rightOf.holds(ctx2, ego2, other2) }
    assertFalse { besides.holds(ctx2, ego2, other2) }
    assertTrue { bothOver10MPH.holds(ctx2, ego2, other2) }

    val ego3 = Vehicle(id = egoId, isEgo = true, lane = road0lane1, positionOnLane = 25.0)
    val other3 = Vehicle(id = otherId, isEgo = false, lane = road0lane2, positionOnLane = 25.0)
    val td3 =
        TickData(
            currentTick = TickDataUnitSeconds(3.0),
            blocks = listOf(block),
            entities = listOf(ego3, other3))
    ego3.tickData = td3
    ego3.setVelocityFromEffVelocityMPH(11.0)
    other3.tickData = td3
    other3.setVelocityFromEffVelocityMPH(11.0)

    val ctx3 = PredicateContext(Segment(listOf(td3), segmentSource = ""))
    assertFalse { isBehind.holds(ctx3, ego3, other3) }
    assertTrue { rightOf.holds(ctx3, other3, ego3) }
    assertTrue { besides.holds(ctx3, ego3, other3) }
    assertTrue { bothOver10MPH.holds(ctx3, ego3, other3) }

    val ego4 = Vehicle(id = egoId, isEgo = true, lane = road0lane1, positionOnLane = 35.0)
    val other4 = Vehicle(id = otherId, isEgo = false, lane = road0lane2, positionOnLane = 30.0)
    val td4 =
        TickData(
            currentTick = TickDataUnitSeconds(4.0),
            blocks = listOf(block),
            entities = listOf(ego4, other4))
    ego4.tickData = td4
    ego4.setVelocityFromEffVelocityMPH(11.0)
    other4.tickData = td4
    other4.setVelocityFromEffVelocityMPH(11.0)

    val ctx4 = PredicateContext(Segment(listOf(td4), segmentSource = ""))
    assertFalse { isBehind.holds(ctx4, ego4, other4) }
    assertTrue { isBehind.holds(ctx4, other4, ego4) }
    assertFalse { rightOf.holds(ctx4, ego4, other4) }
    assertFalse { besides.holds(ctx4, ego4, other4) }
    assertTrue { bothOver10MPH.holds(ctx4, ego4, other4) }

    val fullCtx = PredicateContext(Segment(listOf(td0, td1, td2, td3, td4), segmentSource = ""))
    assertFalse { rightOvertaking.holds(fullCtx, TickDataUnitSeconds(0.0), egoId, otherId) }
    assertTrue { noRightOvertaking.holds(fullCtx, TickDataUnitSeconds(0.0), egoId) }
    assertFalse { rightOvertaking.holds(fullCtx, TickDataUnitSeconds(0.0), otherId, egoId) }
    assertTrue { noRightOvertaking.holds(fullCtx, TickDataUnitSeconds(0.0), otherId) }
  }

  @Test
  fun behindLeftOfInFront() {

    val ego0 = Vehicle(id = egoId, isEgo = true, lane = road0lane2, positionOnLane = 0.0)
    val other0 = Vehicle(id = otherId, isEgo = false, lane = road0lane2, positionOnLane = 5.0)
    val td0 =
        TickData(
            currentTick = TickDataUnitSeconds(0.0),
            blocks = listOf(block),
            entities = listOf(ego0, other0))
    ego0.tickData = td0
    ego0.setVelocityFromEffVelocityMPH(11.0)
    other0.tickData = td0
    other0.setVelocityFromEffVelocityMPH(11.0)

    val ctx0 = PredicateContext(Segment(listOf(td0), segmentSource = ""))
    assertTrue { isBehind.holds(ctx0, ego0, other0) }
    assertFalse { isBehind.holds(ctx0, other0, ego0) }
    assertFalse { besides.holds(ctx0, ego0, other0) }
    assertFalse { rightOf.holds(ctx0, ego0, other0) }
    assertTrue { bothOver10MPH.holds(ctx0, ego0, other0) }

    val ego1 = Vehicle(id = egoId, isEgo = true, lane = road0lane1, positionOnLane = 10.0)
    val other1 = Vehicle(id = otherId, isEgo = false, lane = road0lane2, positionOnLane = 10.0)
    val td1 =
        TickData(
            currentTick = TickDataUnitSeconds(1.0),
            blocks = listOf(block),
            entities = listOf(ego1, other1))
    ego1.tickData = td1
    ego1.setVelocityFromEffVelocityMPH(11.0)
    other1.tickData = td1
    other1.setVelocityFromEffVelocityMPH(11.0)

    val ctx1 = PredicateContext(Segment(listOf(td1), segmentSource = ""))
    assertFalse { isBehind.holds(ctx1, ego1, other1) }
    assertTrue { besides.holds(ctx1, ego1, other1) }
    assertTrue { rightOf.holds(ctx1, other1, ego1) }
    assertTrue { bothOver10MPH.holds(ctx1, ego1, other1) }

    val ego2 = Vehicle(id = egoId, isEgo = true, lane = road0lane1, positionOnLane = 15.0)
    val other2 = Vehicle(id = otherId, isEgo = false, lane = road0lane2, positionOnLane = 12.0)
    val td2 =
        TickData(
            currentTick = TickDataUnitSeconds(2.0),
            blocks = listOf(block),
            entities = listOf(ego2, other2))
    ego2.tickData = td2
    ego2.setVelocityFromEffVelocityMPH(11.0)
    other2.tickData = td2
    other2.setVelocityFromEffVelocityMPH(11.0)

    val ctx2 = PredicateContext(Segment(listOf(td2), segmentSource = ""))
    assertFalse { isBehind.holds(ctx2, ego2, other2) }
    assertTrue { isBehind.holds(ctx2, other2, ego2) }
    assertFalse { besides.holds(ctx2, ego2, other2) }
    assertFalse { rightOf.holds(ctx2, ego2, other2) }
    assertTrue { bothOver10MPH.holds(ctx2, ego2, other2) }

    val fullCtx = PredicateContext(Segment(listOf(td0, td1, td2), segmentSource = ""))
    assertFalse { rightOvertaking.holds(fullCtx, TickDataUnitSeconds(0.0), egoId, otherId) }
    assertTrue { noRightOvertaking.holds(fullCtx, TickDataUnitSeconds(0.0), egoId) }
    assertFalse { rightOvertaking.holds(fullCtx, TickDataUnitSeconds(0.0), otherId, egoId) }
    assertTrue { noRightOvertaking.holds(fullCtx, TickDataUnitSeconds(0.0), otherId) }
  }

  @Test
  fun behindLeftOfBehindRightOfInFront() {

    val ego0 = Vehicle(id = egoId, isEgo = true, lane = road0lane2, positionOnLane = 0.0)
    val other0 = Vehicle(id = otherId, isEgo = false, lane = road0lane2, positionOnLane = 10.0)
    val td0 =
        TickData(
            currentTick = TickDataUnitSeconds(0.0),
            blocks = listOf(block),
            entities = listOf(ego0, other0))
    ego0.tickData = td0
    ego0.setVelocityFromEffVelocityMPH(11.0)
    other0.tickData = td0
    other0.setVelocityFromEffVelocityMPH(11.0)

    val ctx0 = PredicateContext(Segment(listOf(td0), segmentSource = ""))
    assertTrue { isBehind.holds(ctx0, ego0, other0) }
    assertFalse { isBehind.holds(ctx0, other0, ego0) }
    assertFalse { rightOf.holds(ctx0, ego0, other0) }
    assertFalse { rightOf.holds(ctx0, other0, ego0) }
    assertFalse { besides.holds(ctx0, ego0, other0) }
    assertFalse { besides.holds(ctx0, other0, ego0) }
    assertTrue { bothOver10MPH.holds(ctx0, ego0, other0) }

    val ego1 = Vehicle(id = egoId, isEgo = true, lane = road0lane1, positionOnLane = 15.0)
    val other1 = Vehicle(id = otherId, isEgo = false, lane = road0lane2, positionOnLane = 15.0)
    val td1 =
        TickData(
            currentTick = TickDataUnitSeconds(1.0),
            blocks = listOf(block),
            entities = listOf(ego1, other1))
    ego1.tickData = td1
    ego1.setVelocityFromEffVelocityMPH(11.0)
    other1.tickData = td1
    other1.setVelocityFromEffVelocityMPH(11.0)

    val ctx1 = PredicateContext(Segment(listOf(td1), segmentSource = ""))
    assertFalse { isBehind.holds(ctx1, ego1, other1) }
    assertTrue { besides.holds(ctx1, ego1, other1) }
    assertTrue { rightOf.holds(ctx1, other1, ego1) }
    assertTrue { bothOver10MPH.holds(ctx1, ego1, other1) }

    val ego2 = Vehicle(id = egoId, isEgo = true, lane = road0lane2, positionOnLane = 18.0)
    val other2 = Vehicle(id = otherId, isEgo = false, lane = road0lane2, positionOnLane = 22.0)
    val td2 =
        TickData(
            currentTick = TickDataUnitSeconds(2.0),
            blocks = listOf(block),
            entities = listOf(ego2, other2))
    ego2.tickData = td2
    ego2.setVelocityFromEffVelocityMPH(11.0)
    other2.tickData = td2
    other2.setVelocityFromEffVelocityMPH(11.0)

    val ctx2 = PredicateContext(Segment(listOf(td2), segmentSource = ""))
    assertTrue { isBehind.holds(ctx2, ego2, other2) }
    assertFalse { rightOf.holds(ctx2, ego2, other2) }
    assertFalse { besides.holds(ctx2, ego2, other2) }
    assertTrue { bothOver10MPH.holds(ctx2, ego2, other2) }

    val ego3 = Vehicle(id = egoId, isEgo = true, lane = road0lane3, positionOnLane = 25.0)
    val other3 = Vehicle(id = otherId, isEgo = false, lane = road0lane2, positionOnLane = 25.0)
    val td3 =
        TickData(
            currentTick = TickDataUnitSeconds(3.0),
            blocks = listOf(block),
            entities = listOf(ego3, other3))
    ego3.tickData = td3
    ego3.setVelocityFromEffVelocityMPH(11.0)
    other3.tickData = td3
    other3.setVelocityFromEffVelocityMPH(11.0)

    val ctx3 = PredicateContext(Segment(listOf(td3), segmentSource = ""))
    assertFalse { isBehind.holds(ctx3, ego3, other3) }
    assertTrue { rightOf.holds(ctx3, ego3, other3) }
    assertTrue { besides.holds(ctx3, ego3, other3) }
    assertTrue { bothOver10MPH.holds(ctx3, ego3, other3) }

    val ego4 = Vehicle(id = egoId, isEgo = true, lane = road0lane1, positionOnLane = 35.0)
    val other4 = Vehicle(id = otherId, isEgo = false, lane = road0lane2, positionOnLane = 30.0)
    val td4 =
        TickData(
            currentTick = TickDataUnitSeconds(4.0),
            blocks = listOf(block),
            entities = listOf(ego4, other4))
    ego4.tickData = td4
    ego4.setVelocityFromEffVelocityMPH(11.0)
    other4.tickData = td4
    other4.setVelocityFromEffVelocityMPH(11.0)

    val ctx4 = PredicateContext(Segment(listOf(td4), segmentSource = ""))
    assertFalse { isBehind.holds(ctx4, ego4, other4) }
    assertTrue { isBehind.holds(ctx4, other4, ego4) }
    assertFalse { besides.holds(ctx4, ego4, other4) }
    assertFalse { rightOf.holds(ctx4, ego4, other4) }
    assertTrue { bothOver10MPH.holds(ctx4, ego4, other4) }

    // final assertions over full segment
    val fullCtx = PredicateContext(Segment(listOf(td0, td1, td2, td3, td4), segmentSource = ""))
    assertTrue { rightOvertaking.holds(fullCtx, TickDataUnitSeconds(0.0), egoId, otherId) }
    assertFalse { noRightOvertaking.holds(fullCtx, TickDataUnitSeconds(0.0), egoId) }
    assertFalse { rightOvertaking.holds(fullCtx, TickDataUnitSeconds(0.0), otherId, egoId) }
    assertTrue { noRightOvertaking.holds(fullCtx, TickDataUnitSeconds(0.0), otherId) }
  }

  @Test
  fun slowlyOvertakingRight() {

    val ego0 = Vehicle(id = egoId, isEgo = true, lane = road0lane2, positionOnLane = 0.0)
    val other0 = Vehicle(id = otherId, isEgo = false, lane = road0lane2, positionOnLane = 10.0)
    val td0 =
        TickData(
            currentTick = TickDataUnitSeconds(0.0),
            blocks = listOf(block),
            entities = listOf(ego0, other0))
    ego0.tickData = td0
    ego0.setVelocityFromEffVelocityMPH(9.0)
    other0.tickData = td0
    other0.setVelocityFromEffVelocityMPH(11.0)
    val ctx0 = PredicateContext(Segment(listOf(td0), segmentSource = ""))
    assertTrue { isBehind.holds(ctx0, ego0, other0) }
    assertFalse { isBehind.holds(ctx0, other0, ego0) }
    assertFalse { besides.holds(ctx0, ego0, other0) }
    assertFalse { rightOf.holds(ctx0, ego0, other0) }
    assertFalse { bothOver10MPH.holds(ctx0, ego0, other0) }

    val ego1 = Vehicle(id = egoId, isEgo = true, lane = road0lane3, positionOnLane = 13.0)
    val other1 = Vehicle(id = otherId, isEgo = false, lane = road0lane2, positionOnLane = 13.0)
    val td1 =
        TickData(
            currentTick = TickDataUnitSeconds(1.0),
            blocks = listOf(block),
            entities = listOf(ego1, other1))
    ego1.tickData = td1
    ego1.setVelocityFromEffVelocityMPH(9.0)
    other1.tickData = td1
    other1.setVelocityFromEffVelocityMPH(11.0)
    val ctx1 = PredicateContext(Segment(listOf(td1), segmentSource = ""))
    assertFalse { isBehind.holds(ctx1, ego1, other1) }
    assertTrue { rightOf.holds(ctx1, ego1, other1) }
    assertTrue { besides.holds(ctx1, ego1, other1) }
    assertFalse { bothOver10MPH.holds(ctx1, ego1, other1) }

    val ego2 = Vehicle(id = egoId, isEgo = true, lane = road0lane2, positionOnLane = 20.0)
    val other2 = Vehicle(id = otherId, isEgo = false, lane = road0lane3, positionOnLane = 15.0)
    val td2 =
        TickData(
            currentTick = TickDataUnitSeconds(2.0),
            blocks = listOf(block),
            entities = listOf(ego2, other2))
    ego2.tickData = td2
    ego2.setVelocityFromEffVelocityMPH(9.0)
    other2.tickData = td2
    other2.setVelocityFromEffVelocityMPH(11.0)
    val ctx2 = PredicateContext(Segment(listOf(td2), segmentSource = ""))
    assertTrue { isBehind.holds(ctx2, other2, ego2) }
    assertFalse { rightOf.holds(ctx2, ego2, other2) }
    assertFalse { besides.holds(ctx2, ego2, other2) }
    assertFalse { bothOver10MPH.holds(ctx2, ego2, other2) }

    val fullCtx = PredicateContext(Segment(listOf(td0, td1, td2), segmentSource = ""))
    assertFalse { rightOvertaking.holds(fullCtx, TickDataUnitSeconds(0.0), egoId, otherId) }
    assertTrue { noRightOvertaking.holds(fullCtx, TickDataUnitSeconds(0.0), egoId) }
    assertFalse { rightOvertaking.holds(fullCtx, TickDataUnitSeconds(0.0), otherId, egoId) }
    assertTrue { noRightOvertaking.holds(fullCtx, TickDataUnitSeconds(0.0), otherId) }
  }

  @Test
  fun rightOvertakingOfSlowVehicle() {

    val ego0 = Vehicle(id = egoId, isEgo = true, lane = road0lane2, positionOnLane = 0.0)
    val other0 = Vehicle(id = otherId, isEgo = false, lane = road0lane2, positionOnLane = 10.0)
    val td0 =
        TickData(
            currentTick = TickDataUnitSeconds(0.0),
            blocks = listOf(block),
            entities = listOf(ego0, other0))
    ego0.tickData = td0
    ego0.setVelocityFromEffVelocityMPH(11.0)
    other0.tickData = td0
    other0.setVelocityFromEffVelocityMPH(9.0)
    val ctx0 = PredicateContext(Segment(listOf(td0), segmentSource = ""))
    assertTrue { isBehind.holds(ctx0, ego0, other0) }
    assertFalse { isBehind.holds(ctx0, other0, ego0) }
    assertFalse { besides.holds(ctx0, ego0, other0) }
    assertFalse { rightOf.holds(ctx0, ego0, other0) }
    assertFalse { bothOver10MPH.holds(ctx0, ego0, other0) }

    val ego1 = Vehicle(id = egoId, isEgo = true, lane = road0lane3, positionOnLane = 13.0)
    val other1 = Vehicle(id = otherId, isEgo = false, lane = road0lane2, positionOnLane = 13.0)
    val td1 =
        TickData(
            currentTick = TickDataUnitSeconds(1.0),
            blocks = listOf(block),
            entities = listOf(ego1, other1))
    ego1.tickData = td1
    ego1.setVelocityFromEffVelocityMPH(11.0)
    other1.tickData = td1
    other1.setVelocityFromEffVelocityMPH(9.0)
    val ctx1 = PredicateContext(Segment(listOf(td1), segmentSource = ""))
    assertFalse { isBehind.holds(ctx1, ego1, other1) }
    assertTrue { rightOf.holds(ctx1, ego1, other1) }
    assertTrue { besides.holds(ctx1, ego1, other1) }
    assertFalse { bothOver10MPH.holds(ctx1, ego1, other1) }

    val ego2 = Vehicle(id = egoId, isEgo = true, lane = road0lane2, positionOnLane = 20.0)
    val other2 = Vehicle(id = otherId, isEgo = false, lane = road0lane3, positionOnLane = 15.0)
    val td2 =
        TickData(
            currentTick = TickDataUnitSeconds(2.0),
            blocks = listOf(block),
            entities = listOf(ego2, other2))
    ego2.tickData = td2
    ego2.setVelocityFromEffVelocityMPH(11.0)
    other2.tickData = td2
    other2.setVelocityFromEffVelocityMPH(9.0)
    val ctx2 = PredicateContext(Segment(listOf(td2), segmentSource = ""))
    assertTrue { isBehind.holds(ctx2, other2, ego2) }
    assertFalse { rightOf.holds(ctx2, ego2, other2) }
    assertFalse { besides.holds(ctx2, ego2, other2) }
    assertFalse { bothOver10MPH.holds(ctx2, ego2, other2) }

    val fullCtx = PredicateContext(Segment(listOf(td0, td1, td2), segmentSource = ""))
    assertFalse { rightOvertaking.holds(fullCtx, TickDataUnitSeconds(0.0), egoId, otherId) }
    assertTrue { noRightOvertaking.holds(fullCtx, TickDataUnitSeconds(0.0), egoId) }
    assertFalse { rightOvertaking.holds(fullCtx, TickDataUnitSeconds(0.0), otherId, egoId) }
    assertTrue { noRightOvertaking.holds(fullCtx, TickDataUnitSeconds(0.0), otherId) }
  }

  @Test
  fun rightOvertakingOfStandingVehicle() {

    val ego0 = Vehicle(id = egoId, isEgo = true, lane = road0lane2, positionOnLane = 0.0)
    val other0 = Vehicle(id = otherId, isEgo = false, lane = road0lane2, positionOnLane = 10.0)
    val td0 =
        TickData(
            currentTick = TickDataUnitSeconds(0.0),
            blocks = listOf(block),
            entities = listOf(ego0, other0))
    ego0.tickData = td0
    ego0.setVelocityFromEffVelocityMPH(11.0)
    other0.tickData = td0
    other0.setVelocityFromEffVelocityMPH(0.0)
    val ctx0 = PredicateContext(Segment(listOf(td0), segmentSource = ""))
    assertTrue { isBehind.holds(ctx0, ego0, other0) }
    assertFalse { isBehind.holds(ctx0, other0, ego0) }
    assertFalse { besides.holds(ctx0, ego0, other0) }
    assertFalse { rightOf.holds(ctx0, ego0, other0) }
    assertFalse { bothOver10MPH.holds(ctx0, ego0, other0) }

    val ego1 = Vehicle(id = egoId, isEgo = true, lane = road0lane3, positionOnLane = 13.0)
    val other1 = Vehicle(id = otherId, isEgo = false, lane = road0lane2, positionOnLane = 13.0)
    val td1 =
        TickData(
            currentTick = TickDataUnitSeconds(1.0),
            blocks = listOf(block),
            entities = listOf(ego1, other1))
    ego1.tickData = td1
    ego1.setVelocityFromEffVelocityMPH(11.0)
    other1.tickData = td1
    other1.setVelocityFromEffVelocityMPH(0.0)
    val ctx1 = PredicateContext(Segment(listOf(td1), segmentSource = ""))
    assertFalse { isBehind.holds(ctx1, ego1, other1) }
    assertTrue { rightOf.holds(ctx1, ego1, other1) }
    assertTrue { besides.holds(ctx1, ego1, other1) }
    assertFalse { bothOver10MPH.holds(ctx1, ego1, other1) }

    val ego2 = Vehicle(id = egoId, isEgo = true, lane = road0lane2, positionOnLane = 20.0)
    val other2 = Vehicle(id = otherId, isEgo = false, lane = road0lane3, positionOnLane = 15.0)
    val td2 =
        TickData(
            currentTick = TickDataUnitSeconds(2.0),
            blocks = listOf(block),
            entities = listOf(ego2, other2))
    ego2.tickData = td2
    ego2.setVelocityFromEffVelocityMPH(11.0)
    other2.tickData = td2
    other2.setVelocityFromEffVelocityMPH(0.0)
    val ctx2 = PredicateContext(Segment(listOf(td2), segmentSource = ""))
    assertTrue { isBehind.holds(ctx2, other2, ego2) }
    assertFalse { rightOf.holds(ctx2, ego2, other2) }
    assertFalse { besides.holds(ctx2, ego2, other2) }
    assertFalse { bothOver10MPH.holds(ctx2, ego2, other2) }

    val fullCtx = PredicateContext(Segment(listOf(td0, td1, td2), segmentSource = ""))
    assertFalse { rightOvertaking.holds(fullCtx, TickDataUnitSeconds(0.0), egoId, otherId) }
    assertTrue { noRightOvertaking.holds(fullCtx, TickDataUnitSeconds(0.0), egoId) }
    assertFalse { rightOvertaking.holds(fullCtx, TickDataUnitSeconds(0.0), otherId, egoId) }
    assertTrue { noRightOvertaking.holds(fullCtx, TickDataUnitSeconds(0.0), otherId) }
  }

  @Test
  fun slowedDownWhileRightOf() {

    val ego0 = Vehicle(id = egoId, isEgo = true, lane = road0lane2, positionOnLane = 0.0)
    val other0 = Vehicle(id = otherId, isEgo = false, lane = road0lane2, positionOnLane = 10.0)
    val td0 =
        TickData(
            currentTick = TickDataUnitSeconds(0.0),
            blocks = listOf(block),
            entities = listOf(ego0, other0))
    ego0.tickData = td0
    ego0.setVelocityFromEffVelocityMPH(11.0)
    other0.tickData = td0
    other0.setVelocityFromEffVelocityMPH(11.0)
    val ctx0 = PredicateContext(Segment(listOf(td0), segmentSource = ""))
    assertTrue { isBehind.holds(ctx0, ego0, other0) }
    assertFalse { rightOf.holds(ctx0, ego0, other0) }
    assertTrue { bothOver10MPH.holds(ctx0, ego0, other0) }

    val ego1 = Vehicle(id = egoId, isEgo = true, lane = road0lane3, positionOnLane = 13.0)
    val other1 = Vehicle(id = otherId, isEgo = false, lane = road0lane2, positionOnLane = 13.0)
    val td1 =
        TickData(
            currentTick = TickDataUnitSeconds(1.0),
            blocks = listOf(block),
            entities = listOf(ego1, other1))
    ego1.tickData = td1
    ego1.setVelocityFromEffVelocityMPH(10.0)
    other1.tickData = td1
    other1.setVelocityFromEffVelocityMPH(11.0)
    val ctx1 = PredicateContext(Segment(listOf(td1), segmentSource = ""))
    assertTrue { rightOf.holds(ctx1, ego1, other1) }
    assertFalse { bothOver10MPH.holds(ctx1, ego1, other1) }

    val ego2 = Vehicle(id = egoId, isEgo = true, lane = road0lane2, positionOnLane = 20.0)
    val other2 = Vehicle(id = otherId, isEgo = false, lane = road0lane3, positionOnLane = 15.0)
    val td2 =
        TickData(
            currentTick = TickDataUnitSeconds(2.0),
            blocks = listOf(block),
            entities = listOf(ego2, other2))
    ego2.tickData = td2
    ego2.setVelocityFromEffVelocityMPH(9.0)
    other2.tickData = td2
    other2.setVelocityFromEffVelocityMPH(11.0)
    val ctx2 = PredicateContext(Segment(listOf(td2), segmentSource = ""))
    assertTrue { isBehind.holds(ctx2, other2, ego2) }
    assertFalse { bothOver10MPH.holds(ctx2, ego2, other2) }

    val fullCtx = PredicateContext(Segment(listOf(td0, td1, td2), segmentSource = ""))
    assertFalse { rightOvertaking.holds(fullCtx, TickDataUnitSeconds(0.0), egoId, otherId) }
    assertTrue { noRightOvertaking.holds(fullCtx, TickDataUnitSeconds(0.0), egoId) }
    assertFalse { rightOvertaking.holds(fullCtx, TickDataUnitSeconds(0.0), otherId, egoId) }
    assertTrue { noRightOvertaking.holds(fullCtx, TickDataUnitSeconds(0.0), otherId) }
  }
}
