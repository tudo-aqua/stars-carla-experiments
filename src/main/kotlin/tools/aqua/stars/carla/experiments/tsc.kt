/*
 * Copyright 2023-2026 The STARS Carla Experiments Authors
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

import tools.aqua.stars.core.tsc.TSC
import tools.aqua.stars.core.tsc.builder.*
import tools.aqua.stars.data.av.dataclasses.*

private const val FULL_TSC = "full TSC"
private const val LAYER_1_2 = "layer 1+2"
private const val LAYER_4 = "layer 4"
private const val LAYER_1_2_4 = "layer 1+2+4"
private const val LAYER_4_5 = "layer (4)+5"
private const val LAYER_PEDESTRIAN = "pedestrian"
private const val LAYER_MULTI_LANE_DYNAMIC_RELATIONS = "multi-lane-dynamic-relations"

/**
 * Returns the [TSC] with the dataclasses [Actor], [TickData], [Segment], [TickDataUnitSeconds], and
 * [TickDataDifferenceSeconds] that is used in this experiment.
 */
@Suppress("StringLiteralDuplication")
fun tsc() =
    tsc<Actor, TickData, Segment, TickDataUnitSeconds, TickDataDifferenceSeconds> {
      all("TSCRoot") {
        projections {
          projectionRecursive(FULL_TSC) // all
          projection(LAYER_1_2) // static
          projection(LAYER_4) // dynamic
          projection(LAYER_1_2_4) // static + dynamic
          projection(LAYER_4_5) // environment
          projection(LAYER_PEDESTRIAN) // pedestrian
          projection(LAYER_MULTI_LANE_DYNAMIC_RELATIONS)
        }

        exclusive("Weather") {
          projections {
            projectionRecursive(LAYER_4_5)
            projectionRecursive(LAYER_PEDESTRIAN)
          }

          leaf("Clear") { condition { ctx -> ctx.weatherClear() } }
          leaf("Cloudy") { condition { ctx -> ctx.weatherCloudy() } }
          leaf("Wet") { condition { ctx -> ctx.weatherWet() } }
          leaf("Wet Cloudy") { condition { ctx -> ctx.weatherWetCloudy() } }
          leaf("Soft Rain") { condition { ctx -> ctx.weatherSoftRain() } }
          leaf("Mid Rain") { condition { ctx -> ctx.weatherMidRain() } }
          leaf("Hard Rain") { condition { ctx -> ctx.weatherHardRain() } }
        }

        exclusive("Road Type") {
          projections {
            projection(LAYER_1_2)
            projection(LAYER_4)
            projection(LAYER_1_2_4)
            projection(LAYER_PEDESTRIAN)
            projection(LAYER_MULTI_LANE_DYNAMIC_RELATIONS)
          }

          all("Junction") {
            condition { ctx -> isInJunction.holds(ctx) }

            projections {
              projection(LAYER_PEDESTRIAN)
              projection(LAYER_1_2)
              projection(LAYER_4)
              projection(LAYER_1_2_4)
            }

            optional("Dynamic Relation") {
              projections {
                projection(LAYER_PEDESTRIAN)
                projectionRecursive(LAYER_4)
                projectionRecursive(LAYER_1_2_4)
              }

              leaf("Pedestrian Crossed") {
                projections { projection(LAYER_PEDESTRIAN) }

                condition { ctx -> pedestrianCrossed.holds(ctx) }
              }

              leaf("Must Yield") {
                condition { ctx ->
                  ctx.entityIds.any { otherVehicleId ->
                    mustYield.holds(ctx, entityId2 = otherVehicleId)
                  }
                }

                monitors {
                  monitor("Did not yield") { ctx ->
                    ctx.entityIds.any { otherVehicleId ->
                      hasYielded.holds(ctx, entityId2 = otherVehicleId)
                    }
                  }
                }
              }

              leaf("Following Leading Vehicle") {
                projections { projection(LAYER_4) }

                condition { ctx ->
                  ctx.entityIds.any { otherVehicleId ->
                    follows.holds(ctx, entityId2 = otherVehicleId)
                  }
                }
              }
            }

            exclusive("Maneuver") {
              projections {
                projectionRecursive(LAYER_1_2)
                projectionRecursive(LAYER_1_2_4)
              }

              leaf("Lane Follow") { condition { ctx -> makesNoTurn.holds(ctx) } }
              leaf("Right Turn") { condition { ctx -> makesRightTurn.holds(ctx) } }
              leaf("Left Turn") { condition { ctx -> makesLeftTurn.holds(ctx) } }
            }
          }
          all("Multi-Lane") {
            projections {
              projection(LAYER_PEDESTRIAN)
              projection(LAYER_1_2)
              projection(LAYER_4)
              projection(LAYER_1_2_4)
              projection(LAYER_MULTI_LANE_DYNAMIC_RELATIONS)
            }

            condition { ctx ->
              isOnMultiLane.holds(
                  ctx,
                  ctx.segment.tickData.first().currentTick,
                  ctx.segment.primaryEntityId,
              )
            }

            optional("Dynamic Relation") {
              projections {
                projection(LAYER_PEDESTRIAN)
                projectionRecursive(LAYER_4)
                projectionRecursive(LAYER_1_2_4)
                projectionRecursive(LAYER_MULTI_LANE_DYNAMIC_RELATIONS)
              }
              leaf("Oncoming traffic") {
                condition { ctx ->
                  ctx.entityIds.any { otherVehicleId ->
                    oncoming.holds(ctx, entityId2 = otherVehicleId)
                  }
                }
              }
              leaf("Overtaking") {
                condition { ctx -> hasOvertaken.holds(ctx) }
                monitors { monitor("Right Overtaking") { ctx -> noRightOvertaking.holds(ctx) } }
              }
              leaf("Pedestrian Crossed") {
                projections { projection(LAYER_PEDESTRIAN) }

                condition { ctx -> pedestrianCrossed.holds(ctx) }
              }
              leaf("Following Leading Vehicle") {
                projections { projection(LAYER_4) }

                condition { ctx ->
                  ctx.entityIds.any { otherVehicleId ->
                    follows.holds(ctx, entityId2 = otherVehicleId)
                  }
                }
              }
            }

            exclusive("Maneuver") {
              projections {
                projectionRecursive(LAYER_1_2)
                projectionRecursive(LAYER_1_2_4)
              }
              leaf("Lane Change") { condition { ctx -> changedLane.holds(ctx) } }
              leaf("Lane Follow") { condition { ctx -> !changedLane.holds(ctx) } }
            }

            bounded("Stop Type", Pair(0, 1)) {
              projections {
                projectionRecursive(LAYER_1_2)
                projectionRecursive(LAYER_1_2_4)
              }

              leaf("Has Red Light") {
                condition { ctx -> hasRelevantRedLight.holds(ctx) }
                monitors { monitor("Crossed red light") { ctx -> !didCrossRedLight.holds(ctx) } }
              }
            }
          }
          all("Single-Lane") {
            projections {
              projection(LAYER_PEDESTRIAN)
              projection(LAYER_1_2)
              projection(LAYER_4)
              projection(LAYER_1_2_4)
            }

            condition { ctx ->
              isOnSingleLane.holds(
                  ctx,
                  ctx.segment.tickData.first().currentTick,
                  ctx.segment.primaryEntityId,
              )
            }

            optional("Dynamic Relation") {
              projections {
                projection(LAYER_PEDESTRIAN)
                projectionRecursive(LAYER_4)
                projectionRecursive(LAYER_1_2_4)
              }

              leaf("Oncoming traffic") {
                condition { ctx ->
                  ctx.entityIds.any { otherVehicleId ->
                    oncoming.holds(ctx, entityId2 = otherVehicleId)
                  }
                }
              }

              leaf("Pedestrian Crossed") {
                projections { projection(LAYER_PEDESTRIAN) }

                condition { ctx -> pedestrianCrossed.holds(ctx) }
              }

              leaf("Following Leading Vehicle") {
                projections {
                  projection(LAYER_4)
                  projection(LAYER_1_2_4)
                }

                condition { ctx ->
                  ctx.entityIds.any { otherVehicleId ->
                    follows.holds(ctx, entityId2 = otherVehicleId)
                  }
                }
              }
            }

            bounded("Stop Type", Pair(0, 1)) {
              projections {
                projectionRecursive(LAYER_1_2)
                projectionRecursive(LAYER_1_2_4)
              }

              leaf("Has Stop Sign") {
                condition { ctx -> hasStopSign.holds(ctx) }
                monitors { monitor("Stopped at stop sign") { ctx -> stopAtEnd.holds(ctx) } }
              }
              leaf("Has Yield Sign") { condition { ctx -> hasYieldSign.holds(ctx) } }
              leaf("Has Red Light") {
                condition { ctx -> hasRelevantRedLight.holds(ctx) }
                monitors { monitor("Crossed red light") { ctx -> !didCrossRedLight.holds(ctx) } }
              }
            }
          }
        }

        exclusive("Traffic Density") {
          projections {
            projectionRecursive(LAYER_4_5)
            projectionRecursive(LAYER_4)
            projectionRecursive(LAYER_1_2_4)
          }

          leaf("High Traffic") { condition { ctx -> hasHighTrafficDensity.holds(ctx) } }
          leaf("Middle Traffic") { condition { ctx -> hasMidTrafficDensity.holds(ctx) } }
          leaf("Low Traffic") { condition { ctx -> hasLowTrafficDensity.holds(ctx) } }
        }

        exclusive("Time of Day") {
          projections {
            projectionRecursive(LAYER_4_5)
            projectionRecursive(LAYER_PEDESTRIAN)
          }

          leaf("Sunset") { condition { ctx -> ctx.sunset() } }

          leaf("Noon") { condition { ctx -> ctx.noon() } }
        }
      }
    }
