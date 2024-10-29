/*
 * Copyright 2023-2024 The STARS Carla Experiments Authors
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

import tools.aqua.stars.core.tsc.builder.*
import tools.aqua.stars.data.av.dataclasses.*

fun tsc() =
    tsc<Actor, TickData, Segment, TickDataUnitSeconds, TickDataDifferenceSeconds> {
      all("TSCRoot") {
        // valueFunction = { "TSCRoot" }
        projections {
          projectionRecursive("full TSC") // all
          projection("layer 1+2") // static
          projection("layer 4") // dynamic
          projection("layer 1+2+4") // static + dynamic
          projection("layer (4)+5") // environment
          projection("pedestrian") // pedestrian
          projection("multi-lane-dynamic-relations")
        }

        exclusive("Weather") {
          projections {
            projectionRecursive("layer (4)+5")
            projectionRecursive("pedestrian")
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
            projection("layer 1+2")
            projection("layer 4")
            projection("layer 1+2+4")
            projection("pedestrian")
            projection("multi-lane-dynamic-relations")
          }

          all("Junction") {
            condition { ctx -> isInJunction.holds(ctx) }

            projections {
              projection("pedestrian")
              projection("layer 1+2")
              projection("layer 4")
              projection("layer 1+2+4")
            }

            optional("Dynamic Relation") {
              projections {
                projection("pedestrian")
                projectionRecursive("layer 4")
                projectionRecursive("layer 1+2+4")
              }

              leaf("Pedestrian Crossed") {
                projections { projection("pedestrian") }

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
                projections { projection("layer 4") }

                condition { ctx ->
                  ctx.entityIds.any { otherVehicleId ->
                    follows.holds(ctx, entityId2 = otherVehicleId)
                  }
                }
              }
            }

            exclusive("Maneuver") {
              projections {
                projectionRecursive("layer 1+2")
                projectionRecursive("layer 1+2+4")
              }

              leaf("Lane Follow") { condition { ctx -> makesNoTurn.holds(ctx) } }
              leaf("Right Turn") { condition { ctx -> makesRightTurn.holds(ctx) } }
              leaf("Left Turn") { condition { ctx -> makesLeftTurn.holds(ctx) } }
            }
          }
          all("Multi-Lane") {
            projections {
              projection("pedestrian")
              projection("layer 1+2")
              projection("layer 4")
              projection("layer 1+2+4")
              projection("multi-lane-dynamic-relations")
            }

            condition { ctx ->
              isInMultiLane.holds(
                  ctx, ctx.segment.tickData.first().currentTick, ctx.segment.primaryEntityId)
            }

            optional("Dynamic Relation") {
              projections {
                projection("pedestrian")
                projectionRecursive("layer 4")
                projectionRecursive("layer 1+2+4")
                projectionRecursive("multi-lane-dynamic-relations")
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
                projections { projection("pedestrian") }

                condition { ctx -> pedestrianCrossed.holds(ctx) }
              }
              leaf("Following Leading Vehicle") {
                projections { projection("layer 4") }

                condition { ctx ->
                  ctx.entityIds.any { otherVehicleId ->
                    follows.holds(ctx, entityId2 = otherVehicleId)
                  }
                }
              }
            }

            exclusive("Maneuver") {
              projections {
                projectionRecursive("layer 1+2")
                projectionRecursive("layer 1+2+4")
              }
              leaf("Lane Change") { condition { ctx -> changedLane.holds(ctx) } }
              leaf("Lane Follow") { condition { ctx -> !changedLane.holds(ctx) } }
            }

            bounded("Stop Type", Pair(0, 1)) {
              projections {
                projectionRecursive("layer 1+2")
                projectionRecursive("layer 1+2+4")
              }

              leaf("Has Red Light") {
                condition { ctx -> hasRelevantRedLight.holds(ctx) }
                monitors { monitor("Crossed red light") { ctx -> !didCrossRedLight.holds(ctx) } }
              }
            }
          }
          all("Single-Lane") {
            projections {
              projection("pedestrian")
              projection("layer 1+2")
              projection("layer 4")
              projection("layer 1+2+4")
            }

            condition { ctx ->
              isInSingleLane.holds(
                  ctx, ctx.segment.tickData.first().currentTick, ctx.segment.primaryEntityId)
            }

            optional("Dynamic Relation") {
              projections {
                projection("pedestrian")
                projectionRecursive("layer 4")
                projectionRecursive("layer 1+2+4")
              }

              leaf("Oncoming traffic") {
                condition { ctx ->
                  ctx.entityIds.any { otherVehicleId ->
                    oncoming.holds(ctx, entityId2 = otherVehicleId)
                  }
                }
              }

              leaf("Pedestrian Crossed") {
                projections { projection("pedestrian") }

                condition { ctx -> pedestrianCrossed.holds(ctx) }
              }

              leaf("Following Leading Vehicle") {
                projections {
                  projection("layer 4")
                  projection("layer 1+2+4")
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
                projectionRecursive("layer 1+2")
                projectionRecursive("layer 1+2+4")
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
            projectionRecursive("layer (4)+5")
            projectionRecursive("layer 4")
            projectionRecursive("layer 1+2+4")
          }

          leaf("High Traffic") { condition { ctx -> hasHighTrafficDensity.holds(ctx) } }
          leaf("Middle Traffic") { condition { ctx -> hasMidTrafficDensity.holds(ctx) } }
          leaf("Low Traffic") { condition { ctx -> hasLowTrafficDensity.holds(ctx) } }
        }

        exclusive("Time of Day") {
          projections {
            projectionRecursive("layer (4)+5")
            projectionRecursive("pedestrian")
          }

          leaf("Sunset") { condition { ctx -> ctx.sunset() } }

          leaf("Noon") { condition { ctx -> ctx.noon() } }
        }
      }
    }
