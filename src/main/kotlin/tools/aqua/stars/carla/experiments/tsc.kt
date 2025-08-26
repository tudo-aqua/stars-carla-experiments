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
 * Returns the [TSC] with the dataclasses [Actor], [TickData], [TickDataUnitSeconds], and
 * [TickDataDifferenceSeconds] that is used in this experiment.
 */
@Suppress("StringLiteralDuplication")
fun tsc() =
    tsc<Actor, TickData, TickDataUnitSeconds, TickDataDifferenceSeconds> {
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

          leaf("Clear") { condition { weatherClear.holds(it) } }
          leaf("Cloudy") { condition { weatherCloudy.holds(it) } }
          leaf("Wet") { condition { weatherWet.holds(it) } }
          leaf("Wet Cloudy") { condition { weatherWetCloudy.holds(it) } }
          leaf("Soft Rain") { condition { weatherSoftRain.holds(it) } }
          leaf("Mid Rain") { condition { weatherMidRain.holds(it) } }
          leaf("Hard Rain") { condition { weatherHardRain.holds(it) } }
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
            condition { isInJunction.holds(it) }

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

                condition { pedestrianCrossed.holds(it) }
              }

              leaf("Must Yield") {
                condition { mustYield.holds(it) }

                //              monitors {
                //                monitor("Did not yield") {
                //                  exists(it.vehicles) {
                //                    hasYielded.holds(it, entityId2 = otherVehicleId)
                //                  }
                //                }
                //              }
              }

              leaf("Following Leading Vehicle") {
                projections { projection(LAYER_4) }

                condition { follows.holds(it) }
              }
            }
          }

          exclusive("Maneuver") {
            projections {
              projectionRecursive(LAYER_1_2)
              projectionRecursive(LAYER_1_2_4)
            }

            leaf("Lane Follow") { condition { makesNoTurn.holds(it) } }
            leaf("Right Turn") { condition { makesRightTurn.holds(it) } }
            leaf("Left Turn") { condition { makesLeftTurn.holds(it) } }
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

          condition { isOnMultiLane.holds(it) }

          optional("Dynamic Relation") {
            projections {
              projection(LAYER_PEDESTRIAN)
              projectionRecursive(LAYER_4)
              projectionRecursive(LAYER_1_2_4)
              projectionRecursive(LAYER_MULTI_LANE_DYNAMIC_RELATIONS)
            }
            leaf("Oncoming traffic") { condition { oncomingTraffic.holds(it) } }
            leaf("Overtaking") {
              condition { hasOvertaken.holds(it) }
              //              monitors { monitor("Right Overtaking") { noRightOvertaking.holds(it) }
              // }
            }
            leaf("Pedestrian Crossed") {
              projections { projection(LAYER_PEDESTRIAN) }

              condition { pedestrianCrossed.holds(it) }
            }
            leaf("Following Leading Vehicle") {
              projections { projection(LAYER_4) }

              condition { follows.holds(it) }
            }
          }

          exclusive("Maneuver") {
            projections {
              projectionRecursive(LAYER_1_2)
              projectionRecursive(LAYER_1_2_4)
            }
            leaf("Lane Change") { condition { changedLane.holds(it) } }
            leaf("Lane Follow") { condition { !changedLane.holds(it) } }
          }

          bounded("Stop Type", Pair(0, 1)) {
            projections {
              projectionRecursive(LAYER_1_2)
              projectionRecursive(LAYER_1_2_4)
            }

            leaf("Has Red Light") {
              condition { hasRelevantRedLight.holds(it) }
              //              monitors { monitor("Crossed red light") { !didCrossRedLight.holds(it)
              // } }
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

          condition { isOnSingleLane.holds(it) }

          optional("Dynamic Relation") {
            projections {
              projection(LAYER_PEDESTRIAN)
              projectionRecursive(LAYER_4)
              projectionRecursive(LAYER_1_2_4)
            }

            leaf("Oncoming traffic") { condition { oncomingTraffic.holds(it) } }

            leaf("Pedestrian Crossed") {
              projections { projection(LAYER_PEDESTRIAN) }

              condition { pedestrianCrossed.holds(it) }
            }

            leaf("Following Leading Vehicle") {
              projections {
                projection(LAYER_4)
                projection(LAYER_1_2_4)
              }

              condition { follows.holds(it) }
            }
          }

          bounded("Stop Type", Pair(0, 1)) {
            projections {
              projectionRecursive(LAYER_1_2)
              projectionRecursive(LAYER_1_2_4)
            }

            leaf("Has Stop Sign") {
              condition { hasStopSign.holds(it) }
              //              monitors { monitor("Stopped at stop sign") { stopAtEnd.holds(it) } }
            }
            leaf("Has Yield Sign") { condition { hasYieldSign.holds(it) } }
            leaf("Has Red Light") {
              condition { hasRelevantRedLight.holds(it) }
              //              monitors { monitor("Crossed red light") { !didCrossRedLight.holds(it)
              // } }
            }
          }
        }

        exclusive("Traffic Density") {
          projections {
            projectionRecursive(LAYER_4_5)
            projectionRecursive(LAYER_4)
            projectionRecursive(LAYER_1_2_4)
          }

          leaf("High Traffic") { condition { hasHighTrafficDensity.holds(it) } }
          leaf("Middle Traffic") { condition { hasMidTrafficDensity.holds(it) } }
          leaf("Low Traffic") { condition { hasLowTrafficDensity.holds(it) } }
        }

        exclusive("Time of Day") {
          projections {
            projectionRecursive(LAYER_4_5)
            projectionRecursive(LAYER_PEDESTRIAN)
          }

          leaf("Sunset") { condition { sunset.holds(it) } }

          leaf("Noon") { condition { noon.holds(it) } }
        }
      }
    }
