/*
 * Copyright 2023 The STARS Carla Experiments Authors
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

import kotlin.math.abs
import kotlin.math.sign
import tools.aqua.stars.core.evaluation.BinaryPredicate.Companion.predicate
import tools.aqua.stars.core.evaluation.PredicateContext
import tools.aqua.stars.core.evaluation.UnaryPredicate.Companion.predicate
import tools.aqua.stars.data.av.dataclasses.*
import tools.aqua.stars.logic.kcmftbl.*

// region predicates/formulas

val hasLowTrafficDensity =
    predicate(Vehicle::class) { ctx, v ->
      !(hasMidTrafficDensity.holds(ctx, v) || hasHighTrafficDensity.holds(ctx, v))
    }

val hasMidTrafficDensity =
    predicate(Vehicle::class) { _, v ->
      minPrevalence(v, 0.6) { v -> v.tickData.vehiclesInBlock(v.lane.road.block).size in 6..15 }
    }

val hasHighTrafficDensity =
    predicate(Vehicle::class) { _, v ->
      minPrevalence(v, 0.6) { v -> v.tickData.vehiclesInBlock(v.lane.road.block).size > 15 }
    }

val changedLane =
    predicate(Vehicle::class) { _, v ->
      eventually(v) { v0 ->
        eventually(v0) { v1 -> v0.lane.road == v1.lane.road && v0.lane != v1.lane }
      }
    }

val onSameRoad =
    predicate(Vehicle::class to Vehicle::class) { _, v0, v1 -> v0.lane.road == v1.lane.road }

val oncoming =
    predicate(Vehicle::class to Vehicle::class) { ctx, vehicle1, vehicle2 ->
      eventually(vehicle1, vehicle2) { v0, v1 ->
        onSameRoad.holds(ctx, v0, v1) && v0.lane.laneId.sign != v1.lane.laneId.sign
      }
    }

val isInJunction =
    predicate(Vehicle::class) { _, it -> minPrevalence(it, 0.8) { it.lane.road.isJunction } }

val isInSingleLane =
    predicate(Vehicle::class) { ctx, v ->
      !isInJunction.holds(ctx, v) &&
          minPrevalence(v, 0.8) {
            v.lane.road.lanes.filter { v.lane.laneId.sign == it.laneId.sign }.size == 1
          }
    }

val isInMultiLane =
    predicate(Vehicle::class) { ctx, v ->
      !isInJunction.holds(ctx, v) && !isInSingleLane.holds(ctx, v)
    }

fun PredicateContext<Actor, TickData, Segment>.sunset(): Boolean =
    minPrevalence(this.segment.tickData.first(), 0.6) { d -> d.daytime == Daytime.Sunset }

typealias ExperimentPredicateContext = PredicateContext<Actor, TickData, Segment>

fun ExperimentPredicateContext.noon(): Boolean =
    minPrevalence(this.segment.tickData.first(), 0.6) { d -> d.daytime == Daytime.Noon }

fun PredicateContext<Actor, TickData, Segment>.weatherClear(): Boolean =
    minPrevalence(this.segment.tickData.first(), 0.6) { d -> d.weather.type == WeatherType.Clear }

fun PredicateContext<Actor, TickData, Segment>.weatherCloudy(): Boolean =
    minPrevalence(this.segment.tickData.first(), 0.6) { d -> d.weather.type == WeatherType.Cloudy }

fun PredicateContext<Actor, TickData, Segment>.weatherWet(): Boolean =
    minPrevalence(this.segment.tickData.first(), 0.6) { d -> d.weather.type == WeatherType.Wet }

fun PredicateContext<Actor, TickData, Segment>.weatherWetCloudy(): Boolean =
    minPrevalence(this.segment.tickData.first(), 0.6) { d ->
      d.weather.type == WeatherType.WetCloudy
    }

fun PredicateContext<Actor, TickData, Segment>.weatherSoftRain(): Boolean =
    minPrevalence(this.segment.tickData.first(), 0.6) { d ->
      d.weather.type == WeatherType.SoftRainy
    }

fun PredicateContext<Actor, TickData, Segment>.weatherMidRain(): Boolean =
    minPrevalence(this.segment.tickData.first(), 0.6) { d ->
      d.weather.type == WeatherType.MidRainy
    }

fun PredicateContext<Actor, TickData, Segment>.weatherHardRain(): Boolean =
    minPrevalence(this.segment.tickData.first(), 0.6) { d ->
      d.weather.type == WeatherType.HardRainy
    }

val soBetween =
    predicate(Vehicle::class to Vehicle::class) { _, v0, v1 ->
      v1.tickData.vehicles
          .filter { it.id != v0.id && it.id != v1.id }
          .any { vx ->
            (v0.lane.uid == vx.lane.uid || v1.lane.uid == vx.lane.uid) &&
                (!(v0.lane.uid == vx.lane.uid) || (v0.positionOnLane < vx.positionOnLane)) &&
                (!(v1.lane.uid == vx.lane.uid) || (v1.positionOnLane > vx.positionOnLane))
          }
    }

val behind =
    predicate(Vehicle::class to Vehicle::class) { ctx, v0, v1 ->
      ((v0.lane.uid == v1.lane.uid && v0.positionOnLane < v1.positionOnLane) ||
          v0.lane.successorLanes.any { it.lane.uid == v1.lane.uid }) &&
          !soBetween.holds(ctx, v0, v1)
    }

val follows =
    predicate(Vehicle::class to Vehicle::class) { ctx, v0, v1 ->
      eventually(v0, v1) { v0, v1 ->
        globally(v0, v1, 0.0 to 30.0) { v0, v1 -> behind.holds(ctx, v0, v1) } &&
            eventually(v0, v1, 30.0 to 31.0) { _, _ -> true }
      }
    }

val mphLimit90 =
    predicate(Vehicle::class) { _, v ->
      eventually(v) { v -> v.lane.speedAt(v.positionOnLane) == 90.0 }
    }

val mphLimit60 =
    predicate(Vehicle::class) { _, v ->
      eventually(v) { v -> v.lane.speedAt(v.positionOnLane) == 60.0 }
    }

val mphLimit30 =
    predicate(Vehicle::class) { _, it ->
      eventually(it) { it.lane.speedAt(it.positionOnLane) == 30.0 }
    }

val onSameLane = predicate(Actor::class to Actor::class) { _, a1, a2 -> a1.lane.uid == a2.lane.uid }

/**
 * pedestrian p is on the same lane es vehicle, v and v is driving towards p with a distance of < 10
 * m
 */
val inReach =
    predicate(Pedestrian::class to Vehicle::class) { ctx, p, v ->
      onSameLane.holds(ctx, p, v) && (p.positionOnLane - v.positionOnLane) in 0.0..10.0
    }

/**
 * true if at any one time stamp in the future there exists a pedestrian that crosses the lane right
 * before v
 */
val pedestrianCrossed =
    predicate(Vehicle::class) { ctx, v ->
      eventually(v) { v -> v.tickData.pedestrians.any { p -> inReach.holds(ctx, p, v) } }
    }

/** v0/v1 driving on the same road into the same direction */
val sameDirection =
    predicate(Vehicle::class to Vehicle::class) { ctx, v0, v1 ->
      onSameRoad.holds(ctx, v0, v1) && v0.lane.laneId.sign == v1.lane.laneId.sign
    }

/** v0/v1 driving in the same direction on the same road with position on lane diff max 2.0 m. */
val besides =
    predicate(Vehicle::class to Vehicle::class) { ctx, v0, v1 ->
      sameDirection.holds(ctx, v0, v1) && abs(v1.positionOnLane - v0.positionOnLane) <= 2.0
    }

/** v0/v1 driving in the same direction on the same road with v0 more than 2.0 m behind v1. */
val isBehind =
    predicate(Vehicle::class to Vehicle::class) { ctx, v0, v1 ->
      sameDirection.holds(ctx, v0, v1) && (v0.positionOnLane + 2.0) < v1.positionOnLane
    }

/** v0/v1 driving at speeds over 10 mph */
val bothOver10MPH =
    predicate(Vehicle::class to Vehicle::class) { _, v0, v1 ->
      v0.effVelocityInMPH > 10 && v1.effVelocityInMPH > 10
    }

/**
 * v0 is behind v1 then besides v1 and then in front of v1. Both vehicles move with more than 10 mph
 */
val overtaking =
    predicate(Vehicle::class to Vehicle::class) { ctx, v0, v1 ->
      eventually(v0, v1) { v0, v1 ->
        isBehind.holds(ctx, v0, v1) &&
            bothOver10MPH.holds(ctx, v0, v1) &&
            next(v0, v1) { v0, v1 ->
              until(
                  v0,
                  v1,
                  phi1 = { v0, v1 ->
                    isBehind.holds(ctx, v0, v1) && bothOver10MPH.holds(ctx, v0, v1)
                  },
                  phi2 = { v0, v1 ->
                    besides.holds(ctx, v0, v1) &&
                        bothOver10MPH.holds(ctx, v0, v1) &&
                        next(v0, v1) { v0, v1 ->
                          until(
                              v0,
                              v1,
                              phi1 = { v0, v1 ->
                                besides.holds(ctx, v0, v1) && bothOver10MPH.holds(ctx, v0, v1)
                              },
                              phi2 = { v0, v1 ->
                                isBehind.holds(ctx, v1, v0) && bothOver10MPH.holds(ctx, v0, v1)
                              })
                        }
                  })
            }
      }
    }

val hasOvertaken =
    predicate(Vehicle::class) { ctx, v ->
      v.tickData.vehicles.any { v1 -> overtaking.holds(ctx, v, v1) }
    }

val rightOf =
    predicate(Vehicle::class to Vehicle::class) { ctx, v0, v1 ->
      besides.holds(ctx, v0, v1) && abs(v0.lane.laneId) > abs(v1.lane.laneId)
    }

val rightOvertaking =
    predicate(Vehicle::class to Vehicle::class) { ctx, v0, v1 ->
      eventually(v0, v1) { v0, v1 ->
        isBehind.holds(ctx, v0, v1) &&
            bothOver10MPH.holds(ctx, v0, v1) &&
            next(v0, v1) { v0, v1 ->
              until(
                  v0,
                  v1,
                  phi1 = { v0, v1 ->
                    isBehind.holds(ctx, v0, v1) && bothOver10MPH.holds(ctx, v0, v1)
                  },
                  phi2 = { v0, v1 ->
                    rightOf.holds(ctx, v0, v1) &&
                        bothOver10MPH.holds(ctx, v0, v1) &&
                        next(v0, v1) { v0, v1 ->
                          until(
                              v0,
                              v1,
                              phi1 = { v0, v1 ->
                                rightOf.holds(ctx, v0, v1) && bothOver10MPH.holds(ctx, v0, v1)
                              },
                              phi2 = { v0, v1 ->
                                isBehind.holds(ctx, v1, v0) && bothOver10MPH.holds(ctx, v0, v1)
                              })
                        }
                  })
            }
      }
    }

val noRightOvertaking =
    predicate(Vehicle::class) { ctx, v ->
      v.tickData.vehicles.all { v1 -> !rightOvertaking.holds(ctx, v, v1) }
    }

val stopped = predicate(Vehicle::class) { _, v -> v.effVelocityInMPH < 1.8 }

val stopAtEnd =
    predicate(Vehicle::class) { ctx, v ->
      eventually(v) { v1 -> isAtEndOfRoad.holds(ctx, v1) && stopped.holds(ctx, v1) }
    }

val passedContactPoint =
    predicate(Vehicle::class to Vehicle::class) { _, v0, v1 ->
      v0.lane.contactPointPos(v1.lane)?.let { it < v0.positionOnLane } ?: false
    }

val hasYielded =
    predicate(Vehicle::class to Vehicle::class) { ctx, v0, v1 ->
      until(
          v0,
          v1,
          phi1 = { v0, v1 -> !passedContactPoint.holds(ctx, v0, v1) },
          phi2 = { v0, v1 -> passedContactPoint.holds(ctx, v1, v0) })
    }

val obeyedSpeedLimit =
    predicate(Vehicle::class) { _, v ->
      globally(v) { v -> (v.effVelocityInMPH) <= v.lane.speedAt(v.positionOnLane) }
    }

val hasRedLight =
    predicate(Vehicle::class) { _, v ->
      v.lane.successorLanes.any { contactLaneInfo ->
        contactLaneInfo.lane.trafficLights.any { staticTrafficLight ->
          staticTrafficLight.getStateInTick(v.tickData) == TrafficLightState.Red
        }
      }
    }

val hasRelevantRedLight =
    predicate(Vehicle::class) { ctx, v ->
      eventually(v) { v -> hasRedLight.holds(ctx, v) && isAtEndOfRoad.holds(ctx, v) }
    }

// Did not cross Stefan: G[v'](v'.hasRelevantRedLight => X[v''](v'.road == v''.road))
// Did cross Till: F[v'](v'.hasRelevantRedLight && X[v''](v'.road != v''.road))
val didCrossRedLight =
    predicate(Vehicle::class) { ctx, v ->
      eventually(v) { v1 ->
        hasRelevantRedLight.holds(ctx, v1) && next(v1) { v2 -> v1.lane.road != v2.lane.road }
      }
    }

val isAtEndOfRoad =
    predicate(Vehicle::class) { _, v -> v.positionOnLane >= v.lane.laneLength - 3.0 }

val hasStopSign = predicate(Vehicle::class) { _, v -> eventually(v) { v -> v.lane.hasStopSign } }

val hasYieldSign = predicate(Vehicle::class) { _, v -> eventually(v) { v -> v.lane.hasYieldSign } }

val mustYield =
    predicate(Vehicle::class to Vehicle::class) { _, v0, v1 ->
      eventually(v0, v1) { v0, v1 -> v0.lane.yieldLanes.any { it.lane == v1.lane } }
    }

val makesRightTurn =
    predicate(Vehicle::class) { _, v -> minPrevalence(v, 0.8) { v -> v.lane.isTurningRight } }

val makesLeftTurn =
    predicate(Vehicle::class) { _, v -> minPrevalence(v, 0.8) { v -> v.lane.isTurningLeft } }

val makesNoTurn =
    predicate(Vehicle::class) { _, v -> minPrevalence(v, 0.8) { v -> v.lane.isStraight } }

// endregion
