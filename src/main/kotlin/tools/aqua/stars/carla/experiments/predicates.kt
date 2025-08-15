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

import tools.aqua.stars.data.av.dataclasses.TickData
import tools.aqua.stars.data.av.dataclasses.WeatherType
import tools.aqua.stars.logic.kcmftbl.minPrevalence

/*
/** The [Block] of [Vehicle] v has less than 6 vehicles in it. */
val hasLowTrafficDensity =
    predicate(Vehicle::class) { ctx, v ->
      !(hasMidTrafficDensity.holds(ctx, v) || hasHighTrafficDensity.holds(ctx, v))
    }

/** The [Block] of [Vehicle] v has between 6 and 15 vehicles in it. */
val hasMidTrafficDensity =
    predicate(Vehicle::class) { _, v ->
      minPrevalence(v, 0.6) { v -> v.tickData.vehiclesInBlock(v.lane.road.block).size in 6..15 }
    }

/** The [Block] of [Vehicle] v has more than 15 vehicles in it. */
val hasHighTrafficDensity =
    predicate(Vehicle::class) { _, v ->
      minPrevalence(v, 0.6) { v -> v.tickData.vehiclesInBlock(v.lane.road.block).size > 15 }
    }

/** [Vehicle] v changes its lange at least once. */
val changedLane =
    predicate(Vehicle::class) { _, v ->
      eventually(v) { v0 ->
        eventually(v0) { v1 -> v0.lane.road == v1.lane.road && v0.lane != v1.lane }
      }
    }

/** The [Vehicle]s v0 and v1 are on the same [Road]. */
val onSameRoad =
    predicate(Vehicle::class to Vehicle::class) { _, v0, v1 -> v0.lane.road == v1.lane.road }

/** [Vehicle] v0 is on the same road as [Vehicle] v1 but drives on the other direction. */
val oncoming =
    predicate(Vehicle::class to Vehicle::class) { ctx, v0, v1 ->
      eventually(v0, v1) { v0, v1 ->
        onSameRoad.holds(ctx, v0, v1) && v0.lane.laneId.sign != v1.lane.laneId.sign
      }
    }

/** [Vehicle] v is mostly in a junction. */
val isInJunction =
    predicate(Vehicle::class) { _, v -> minPrevalence(v, 0.8) { v -> v.lane.road.isJunction } }

/** [Vehicle] v is mostly on a single lane. */
val isOnSingleLane =
    predicate(Vehicle::class) { ctx, v ->
      !isInJunction.holds(ctx, v) &&
          minPrevalence(v, 0.8) {
            v.lane.road.lanes.filter { v.lane.laneId.sign == it.laneId.sign }.size == 1
          }
    }

/** [Vehicle] v is mostly on a multi-lane. */
val isOnMultiLane =
    predicate(Vehicle::class) { ctx, v ->
      !isInJunction.holds(ctx, v) && !isOnSingleLane.holds(ctx, v)
    }

typealias ExperimentPredicateContext =
    PredicateContext<Actor, TickData, Segment, TickDataUnitSeconds, TickDataDifferenceSeconds>

/** The daytime was mostly [Daytime.Sunset]. */
fun ExperimentPredicateContext.sunset(): Boolean =
    minPrevalence(this.segment.tickData.first(), 0.6) { d -> d.daytime == Daytime.Sunset }

/** The daytime was mostly [Daytime.Noon]. */
fun ExperimentPredicateContext.noon(): Boolean =
    minPrevalence(this.segment.tickData.first(), 0.6) { d -> d.daytime == Daytime.Noon }
*/
/** The weather was mostly [WeatherType.Clear]. */
fun TickData.weatherClear(): Boolean =
    minPrevalence(this, 0.6) { d -> d.weather.type == WeatherType.Clear }

/** The weather was mostly [WeatherType.Cloudy]. */
fun TickData.weatherCloudy(): Boolean =
    minPrevalence(this, 0.6) { d -> d.weather.type == WeatherType.Cloudy }

/** The weather was mostly [WeatherType.Wet]. */
fun TickData.weatherWet(): Boolean =
    minPrevalence(this, 0.6) { d -> d.weather.type == WeatherType.Wet }

/** The weather was mostly [WeatherType.WetCloudy]. */
fun TickData.weatherWetCloudy(): Boolean =
    minPrevalence(this, 0.6) { d ->
      d.weather.type == WeatherType.WetCloudy
    }

/** The weather was mostly [WeatherType.SoftRainy]. */
fun TickData.weatherSoftRain(): Boolean =
    minPrevalence(this, 0.6) { d ->
      d.weather.type == WeatherType.SoftRainy
    }

/** The weather was mostly [WeatherType.MidRainy]. */
fun TickData.weatherMidRain(): Boolean =
    minPrevalence(this, 0.6) { d ->
      d.weather.type == WeatherType.MidRainy
    }

/** The weather was mostly [WeatherType.HardRainy]. */
fun TickData.weatherHardRain(): Boolean =
    minPrevalence(this, 0.6) { d ->
      d.weather.type == WeatherType.HardRainy
    }
/*
/** There is a [Vehicle] between the two [Vehicle]s v0 and v1. */
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

/** [Vehicle] v0 is behind [Vehicle] v1 on the same [Lane]. */
val behind =
    predicate(Vehicle::class to Vehicle::class) { ctx, v0, v1 ->
      ((v0.lane.uid == v1.lane.uid && v0.positionOnLane < v1.positionOnLane) ||
          v0.lane.successorLanes.any { it.lane.uid == v1.lane.uid }) &&
          !soBetween.holds(ctx, v0, v1)
    }

/** [Vehicle] v0 follows [Vehicle] v1 for at least 30 seconds. */
val follows =
    predicate(Vehicle::class to Vehicle::class) { ctx, v0, v1 ->
      eventually(v0, v1) { v0, v1 ->
        globally(v0, v1, TickDataDifferenceSeconds(0.0) to TickDataDifferenceSeconds(30.0)) { v0, v1
          ->
          behind.holds(ctx, v0, v1)
        } &&
            eventually(
                v0, v1, TickDataDifferenceSeconds(30.0) to TickDataDifferenceSeconds(31.0)) { _, _
                  ->
                  true
                }
      }
    }

/** There is a speed limit of 90mph. */
val mphLimit90 =
    predicate(Vehicle::class) { _, v ->
      eventually(v) { v -> v.lane.speedAt(v.positionOnLane) == 90.0 }
    }

/** There is a speed limit of 60mph. */
val mphLimit60 =
    predicate(Vehicle::class) { _, v ->
      eventually(v) { v -> v.lane.speedAt(v.positionOnLane) == 60.0 }
    }

/** There is a speed limit of 30mph. */
val mphLimit30 =
    predicate(Vehicle::class) { _, v ->
      eventually(v) { v -> v.lane.speedAt(v.positionOnLane) == 30.0 }
    }

/** [Actor] a0 and [Actor] a1 are on the same lane. */
val onSameLane = predicate(Actor::class to Actor::class) { _, a1, a2 -> a1.lane.uid == a2.lane.uid }

/**
 * pedestrian p is on the same lane es vehicle, v and v is driving towards p with a distance of < 10
 * meters.
 */
val inReach =
    predicate(Pedestrian::class to Vehicle::class) { ctx, p, v ->
      onSameLane.holds(ctx, p, v) && (p.positionOnLane - v.positionOnLane) in 0.0..10.0
    }

/**
 * true if at any one time stamp in the future there exists a pedestrian that crosses the lane right
 * before v.
 */
val pedestrianCrossed =
    predicate(Vehicle::class) { ctx, v ->
      eventually(v) { v -> v.tickData.pedestrians.any { p -> inReach.holds(ctx, p, v) } }
    }

/** v0/v1 driving on the same road into the same direction. */
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

/** v0/v1 driving at speeds over 10 mph. */
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

/** [Vehicle] v was overtaking by at least one other [Vehicle]. */
val hasOvertaken =
    predicate(Vehicle::class) { ctx, v ->
      v.tickData.vehicles.any { v1 -> overtaking.holds(ctx, v, v1) }
    }

/** [Vehicle] v0 is on a right [Lane] of [Vehicle] v1. */
val rightOf =
    predicate(Vehicle::class to Vehicle::class) { ctx, v0, v1 ->
      besides.holds(ctx, v0, v1) && abs(v0.lane.laneId) > abs(v1.lane.laneId)
    }

/** [Vehicle] v0 overtook [Vehicle] v1 on the right. */
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

/** [Vehicle] v has not overtaken another [Vehicle] on the right. */
val noRightOvertaking =
    predicate(Vehicle::class) { ctx, v ->
      v.tickData.vehicles.all { v1 -> !rightOvertaking.holds(ctx, v, v1) }
    }

/** [Vehicle] v has stopped. */
val stopped = predicate(Vehicle::class) { _, v -> v.effVelocityInMPH < 1.8 }

/** [Vehicle] v has stopped at the end of its [Road]. */
val stopAtEnd =
    predicate(Vehicle::class) { ctx, v ->
      eventually(v) { v1 -> isAtEndOfRoad.holds(ctx, v1) && stopped.holds(ctx, v1) }
    }

/** [Vehicle] v0 has passed the contact point of the crossing [Lane] of [Vehicle] v1. */
val passedContactPoint =
    predicate(Vehicle::class to Vehicle::class) { _, v0, v1 ->
      v0.lane.contactPointPos(v1.lane)?.let { it < v0.positionOnLane } == true
    }

/** [Vehicle] v0 has yielded to [Vehicle] v1. */
val hasYielded =
    predicate(Vehicle::class to Vehicle::class) { ctx, v0, v1 ->
      until(
          v0,
          v1,
          phi1 = { v0, v1 -> !passedContactPoint.holds(ctx, v0, v1) },
          phi2 = { v0, v1 -> passedContactPoint.holds(ctx, v1, v0) })
    }

/** [Vehicle] v always had a speed lower than the allowed speed limit. */
val obeyedSpeedLimit =
    predicate(Vehicle::class) { _, v ->
      globally(v) { v -> (v.effVelocityInMPH) <= v.lane.speedAt(v.positionOnLane) }
    }

/** [Vehicle] v has a red light on its [Lane]. */
val hasRedLight =
    predicate(Vehicle::class) { _, v ->
      v.lane.successorLanes.any { contactLaneInfo ->
        contactLaneInfo.lane.trafficLights.any { staticTrafficLight ->
          staticTrafficLight.getStateInTick(v.tickData) == TrafficLightState.Red
        }
      }
    }

/** [Vehicle] v has a red light on its [Lane] and v is close to the traffic light. */
val hasRelevantRedLight =
    predicate(Vehicle::class) { ctx, v ->
      eventually(v) { v -> hasRedLight.holds(ctx, v) && isAtEndOfRoad.holds(ctx, v) }
    }

/** [Vehicle] v has crossed a red light. */
val didCrossRedLight =
    predicate(Vehicle::class) { ctx, v ->
      eventually(v) { v1 ->
        hasRelevantRedLight.holds(ctx, v1) && next(v1) { v2 -> v1.lane.road != v2.lane.road }
      }
    }

/** [Vehicle] v is located in the last 3 meters of its [Lane]. */
val isAtEndOfRoad =
    predicate(Vehicle::class) { _, v -> v.positionOnLane >= v.lane.laneLength - 3.0 }

/** [Vehicle] v has a stop sign at the end of its [Lane]. */
val hasStopSign = predicate(Vehicle::class) { _, v -> eventually(v) { v -> v.lane.hasStopSign } }

/** [Vehicle] v has a yield sign at the end of its [Lane]. */
val hasYieldSign = predicate(Vehicle::class) { _, v -> eventually(v) { v -> v.lane.hasYieldSign } }

/** [Vehicle] v0 must yield to [Vehicle] v1. */
val mustYield =
    predicate(Vehicle::class to Vehicle::class) { _, v0, v1 ->
      eventually(v0, v1) { v0, v1 -> v0.lane.yieldLanes.any { it.lane == v1.lane } }
    }

/** [Vehicle] v made a right turn. */
val makesRightTurn =
    predicate(Vehicle::class) { _, v -> minPrevalence(v, 0.8) { v -> v.lane.isTurningRight } }

/** [Vehicle] v made a left turn. */
val makesLeftTurn =
    predicate(Vehicle::class) { _, v -> minPrevalence(v, 0.8) { v -> v.lane.isTurningLeft } }

/** [Vehicle] v made no turn. */
val makesNoTurn =
    predicate(Vehicle::class) { _, v -> minPrevalence(v, 0.8) { v -> v.lane.isStraight } }
*/
