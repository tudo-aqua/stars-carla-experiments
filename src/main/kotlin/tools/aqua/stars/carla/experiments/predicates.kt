/*
 * Copyright 2023-2025 The STARS Carla Experiments Authors
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

package tools.aqua.stars.carla.experiments

import kotlin.math.sign
import tools.aqua.stars.core.evaluation.BinaryPredicate.Companion.predicate
import tools.aqua.stars.core.evaluation.NullaryPredicate.Companion.predicate
import tools.aqua.stars.core.evaluation.UnaryPredicate.Companion.predicate
import tools.aqua.stars.data.av.dataclasses.Actor
import tools.aqua.stars.data.av.dataclasses.Daytime
import tools.aqua.stars.data.av.dataclasses.TickData
import tools.aqua.stars.data.av.dataclasses.TickDataDifferenceSeconds
import tools.aqua.stars.data.av.dataclasses.TickDataUnitSeconds
import tools.aqua.stars.data.av.dataclasses.TrafficLightState
import tools.aqua.stars.data.av.dataclasses.Vehicle
import tools.aqua.stars.data.av.dataclasses.WeatherType
import tools.aqua.stars.logic.kcmftbl.bind
import tools.aqua.stars.logic.kcmftbl.exists
import tools.aqua.stars.logic.kcmftbl.historically
import tools.aqua.stars.logic.kcmftbl.minPrevalence
import tools.aqua.stars.logic.kcmftbl.once
import tools.aqua.stars.logic.kcmftbl.pastMinPrevalence
import tools.aqua.stars.logic.kcmftbl.previous

typealias E = Actor

typealias T = TickData

typealias U = TickDataUnitSeconds

typealias D = TickDataDifferenceSeconds

/** Distance used at the end of the road to argue about the relevance of stop types and yielding */
const val END_OF_ROAD_DISTANCE = 10.0
const val PEDESTRIAN_CROSSING_DISTANCE = 10.0
const val FOLLOWING_DISTANCE = 10.0
const val FOLLOWING_TIME = 10.0

// region Traffic Density
/**
 * The [tools.aqua.stars.data.av.dataclasses.Road] of [Vehicle] v has less than 6 vehicles in it.
 */
val hasLowTrafficDensity =
    predicate<E, T, U, D>("hasLowTrafficDensity") { tick ->
      pastMinPrevalence(tick, 0.6) {
        it.vehicles.filter { v -> v.lane.road == it.ego.lane.road }.size < 6
      }
    }

/**
 * The [tools.aqua.stars.data.av.dataclasses.Road] of [Vehicle] v has between 6 and 15 vehicles in
 * it.
 */
val hasMidTrafficDensity =
    predicate<E, T, U, D>("hasMidTrafficDensity") { tick ->
      !(hasLowTrafficDensity.holds(tick) || hasHighTrafficDensity.holds(tick))
    }

/**
 * The [tools.aqua.stars.data.av.dataclasses.Road] of [Vehicle] v has more than 15 vehicles in it.
 */
val hasHighTrafficDensity =
    predicate<E, T, U, D>("hasHighTrafficDensity") { tick ->
      pastMinPrevalence(tick, 0.6) {
        it.vehicles.filter { v -> v.lane.road == it.ego.lane.road }.size > 15
      }
    }
// endregion

// Traffic situations
/** [Vehicle] v changes its lange at least once. */
val changedLane =
    predicate<E, T, U, D>("changedLane") { tick ->
      bind(tick.ego.lane) { currentLane -> once(tick) { it.ego.lane != currentLane } }
    }

/** The ego and [Vehicle] v are on the same road but v drives on the other direction. */
val oncomingTraffic =
    predicate<E, T, U, D>("oncoming") { tick ->
      exists(tick.vehicles) { v ->
        tick.ego != v && // Implicit but clearer when explicitly excluded
            tick.ego.lane.road == v.lane.road &&
            tick.ego.lane.laneId.sign != v.lane.laneId.sign
      }
    }

/** A pedestrian that crosses the lane right before v. */
val pedestrianCrossed =
    predicate<E, T, U, D>("pedestrianCrossed") { tick ->
      exists(tick.pedestrians) { p ->
        // Lane change happens on current tick
        previous(tick) { exists(it.pedestrians) { p1 -> p1 == p && p1.lane != p.lane } } &&

            // Pedestrian is close to ego
            p.positionOnLane - tick.ego.positionOnLane in 0.0..PEDESTRIAN_CROSSING_DISTANCE &&

            // Cross from right to left
            (((p.lane.laneId == tick.ego.lane.laneId + 1 ||
                p.lane.laneId.sign != tick.ego.lane.laneId.sign) &&
                once(tick) {
                  exists(tick.pedestrians) { p1 ->
                    p1 == p && p1.lane.laneId == tick.ego.lane.laneId - 1
                  }
                }) ||

                // Cross from left to right
                ((p.lane.laneId == tick.ego.lane.laneId - 1 ||
                    p.lane.laneId.sign != tick.ego.lane.laneId.sign) &&
                    once(tick) {
                      exists(tick.pedestrians) { p1 ->
                        p1 == p && p1.lane.laneId == tick.ego.lane.laneId + 1
                      }
                    }))
      }
    }

/** The ego is behind [Vehicle] v on the same [Lane]. */
val behind =
    predicate("behind", Vehicle::class) { tick, v ->
      ((tick.ego.lane == v.lane && tick.ego.positionOnLane < v.positionOnLane) ||
          tick.ego.lane.successorLanes.any { it.lane == v.lane }) && !soBetween.holds(tick, v)
    }

/** There is a [Vehicle] between the two [Vehicle]s v0 and v1. */
val soBetween =
    predicate("soBetween", Vehicle::class) { tick, v ->
      exists(tick.vehicles) { vx ->
        vx != tick.ego &&
            vx != v &&
            (tick.ego.lane == vx.lane && tick.ego.positionOnLane < vx.positionOnLane) &&
            (v.lane == vx.lane && v.positionOnLane > vx.positionOnLane)
      }
    }

/** [Vehicle] v0 follows [Vehicle] v1 for at least 30 seconds. */
val follows =
    predicate<E, T, U, D>("follows") { tick ->
      exists(tick.vehicles) { otherVehicle ->
        historically(
            tick, TickDataDifferenceSeconds(0.0) to TickDataDifferenceSeconds(FOLLOWING_TIME)) {
              behind.holds(tick, otherVehicle)
            } && tickExists(tick, 30.0)
      }
    }

/** v0/v1 driving in the same direction on the same road with v0 more than 2.0 m behind v1. */
val isBehind =
    predicate("isBehind", Vehicle::class to Vehicle::class) { tick, v0, v1 ->
      v0.lane.road == v1.lane.road &&
          v0.lane.laneId.sign == v1.lane.laneId.sign &&
          v0.positionOnLane < v1.positionOnLane
    }

/** The ego has overtaken at least one other [Vehicle]. */
val hasOvertaken =
    predicate<E, T, U, D>("hasOvertaken") { tick ->
      exists(tick.vehicles) { v ->
        tick.ego != v &&
            // Ego is behind the other vehicle
            isBehind.holds(tick, tick.ego, v) &&
            // Both vehicles drive faster than 10 mph
            tick.ego.effVelocityInMPH > 10 &&
            v.effVelocityInMPH > 10 &&

            // The other vehicle was behind the ego in the previous tick
            previous(tick) { isBehind.holds(tick, v, tick.ego) }
      }
    }

fun tickExists(tickData: TickData, tick: Double) =
    predicate<E, T, U, D>("tickExists") {
          once(it, TickDataDifferenceSeconds(tick) to TickDataDifferenceSeconds(tick + 1)) { true }
        }
        .holds(tickData)
// endregion

// region Stop types
/** [Vehicle] v has a stop sign at the end of its [Lane]. */
val hasStopSign =
    predicate<E, T, U, D>("hastStopSign") { tick ->
      atEndOfRoad.holds(tick, tick.ego) && tick.ego.lane.hasStopSign
    }

/** [Vehicle] v has a yield sign at the end of its [Lane]. */
val hasYieldSign =
    predicate<E, T, U, D>("hasYieldSign") { tick ->
      atEndOfRoad.holds(tick, tick.ego) && tick.ego.lane.hasYieldSign
    }

/** [Vehicle] v has a red light on its [Lane] and v is close to the traffic light. */
val hasRelevantRedLight =
    predicate<E, T, U, D>("hasRelevantRedLight") { tick ->
      atEndOfRoad.holds(tick, tick.ego) &&
          tick.ego.lane.successorLanes.any {
            it.lane.trafficLights.any { staticTrafficLight ->
              staticTrafficLight.getStateInTick(tick) == TrafficLightState.Red
            }
          }
    }

/** The ego must yield to [Vehicle] v1. */
val mustYield =
    predicate<E, T, U, D>("mustYield") { tick ->
      // Close to the end of the lane
      atEndOfRoad.holds(tick, tick.ego) &&

          // There is a vehicle on a lane that must be yielded to
          exists(tick.vehicles) { v ->
            tick.ego.lane.yieldLanes.any { it.lane == v.lane && atEndOfRoad.holds(tick, v) }
          }
    }

val atEndOfRoad =
    predicate("atEndOfRoad", Vehicle::class) { _, v ->
      v.lane.laneLength - v.positionOnLane < END_OF_ROAD_DISTANCE
    }
// endregion

// region Road type
/** [Vehicle] is in a junction. */
val isInJunction = predicate("IsInJunction", Vehicle::class) { _, v -> v.lane.road.isJunction }

/** [Vehicle] is mostly on a single lane. */
val isOnSingleLane =
    predicate("isOnSingleLane", Vehicle::class) { _, v ->
      !v.lane.road.isJunction &&
          v.lane.road.lanes.filter { it.laneId.sign == v.lane.laneId.sign }.size == 1
    }

/** [Vehicle] is mostly on a multi-lane. */
val isOnMultiLane =
    predicate("isOnMultilane", Vehicle::class) { _, v ->
      !v.lane.road.isJunction &&
          v.lane.road.lanes.filter { it.laneId.sign == v.lane.laneId.sign }.size > 1
    }
// endregion

// region Turning
/** [Vehicle] v made a right turn. */
val makesRightTurn =
    predicate<E, T, U, D>("makesRightTurn") { tick -> tick.ego.lane.isTurningRight }

/** [Vehicle] v made a left turn. */
val makesLeftTurn = predicate<E, T, U, D>("makesLeftTurn") { tick -> tick.ego.lane.isTurningLeft }

/** [Vehicle] v made no turn. */
val makesNoTurn = predicate<E, T, U, D>("makesNoTurn") { tick -> tick.ego.lane.isStraight }

// endregion

// region Time of day
/** The daytime was mostly [Daytime.Sunset]. */
val sunset =
    predicate<Actor, TickData, TickDataUnitSeconds, TickDataDifferenceSeconds>("Sunset") {
      minPrevalence(it, 0.6) { d -> d.daytime == Daytime.Sunset }
    }

/** The daytime was mostly [Daytime.Noon]. */
val noon =
    predicate<Actor, TickData, TickDataUnitSeconds, TickDataDifferenceSeconds>("Noon") {
      minPrevalence(it, 0.6) { d -> d.daytime == Daytime.Noon }
    }
// endregion

// region Weather
/** The weather was mostly [WeatherType.Clear]. */
val weatherClear =
    predicate<Actor, TickData, TickDataUnitSeconds, TickDataDifferenceSeconds>("Clear") {
      minPrevalence(it, 0.6) { d -> d.weather.type == WeatherType.Clear }
    }

/** The weather was mostly [WeatherType.Cloudy]. */
val weatherCloudy =
    predicate<Actor, TickData, TickDataUnitSeconds, TickDataDifferenceSeconds>("Cloudy") {
      minPrevalence(it, 0.6) { d -> d.weather.type == WeatherType.Cloudy }
    }

/** The weather was mostly [WeatherType.Wet]. */
val weatherWet =
    predicate<Actor, TickData, TickDataUnitSeconds, TickDataDifferenceSeconds>("Wet") {
      minPrevalence(it, 0.6) { d -> d.weather.type == WeatherType.Wet }
    }

/** The weather was mostly [WeatherType.WetCloudy]. */
val weatherWetCloudy =
    predicate<Actor, TickData, TickDataUnitSeconds, TickDataDifferenceSeconds>("WetCloudy") {
      minPrevalence(it, 0.6) { d -> d.weather.type == WeatherType.WetCloudy }
    }

/** The weather was mostly [WeatherType.SoftRainy]. */
val weatherSoftRain =
    predicate<Actor, TickData, TickDataUnitSeconds, TickDataDifferenceSeconds>("SoftRain") {
      minPrevalence(it, 0.6) { d -> d.weather.type == WeatherType.SoftRainy }
    }

/** The weather was mostly [WeatherType.MidRainy]. */
val weatherMidRain =
    predicate<Actor, TickData, TickDataUnitSeconds, TickDataDifferenceSeconds>("MidRain") {
      minPrevalence(it, 0.6) { d -> d.weather.type == WeatherType.MidRainy }
    }

/** The weather was mostly [WeatherType.HardRainy]. */
val weatherHardRain =
    predicate<Actor, TickData, TickDataUnitSeconds, TickDataDifferenceSeconds>("HardRain") {
      minPrevalence(it, 0.6) { d -> d.weather.type == WeatherType.HardRainy }
    }
// endregion
