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

import tools.aqua.stars.core.tsc.*
import tools.aqua.stars.core.tsc.builder.*
import tools.aqua.stars.data.av.dataclasses.*

fun tsc() =
    tsc<Actor, TickData, Segment, TickDataUnitSeconds, TickDataDifferenceSeconds> {
          all("TSCRoot") {
            // valueFunction = { "TSCRoot" }
            projections {
              projectionRecursive("all")
              projection("static")
              projection("dynamic")
              projection("static+dynamic")
              projection("environment")
              projection("pedestrian")
              projection("multi-lane-dynamic-relations")
            }

            exclusive("Weather") {
              projections {
                projectionRecursive("environment")
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
                projection("static")
                projection("dynamic")
                projection("static+dynamic")
                projection("pedestrian")
                projection("multi-lane-dynamic-relations")
              }

              all("Junction") {
                condition { ctx -> isInJunction.holds(ctx) }

                projections {
                  projection("pedestrian")
                  projection("static")
                  projection("dynamic")
                  projection("static+dynamic")
                }

                optional("Dynamic Relation") {
                  projections {
                    projection("pedestrian")
                    projectionRecursive("dynamic")
                    projectionRecursive("static+dynamic")
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
                      monitor("Must Yield") { ctx ->
                        ctx.entityIds.any { otherVehicleId ->
                          hasYielded.holds(ctx, entityId2 = otherVehicleId)
                        }
                      }
                    }
                  }

                  leaf("Following Leading Vehicle") {
                    projections { projection("dynamic") }

                    condition { ctx ->
                      ctx.entityIds.any { otherVehicleId ->
                        follows.holds(ctx, entityId2 = otherVehicleId)
                      }
                    }
                  }
                }

                exclusive("Maneuver") {
                  projections {
                    projectionRecursive("static")
                    projectionRecursive("static+dynamic")
                  }

                  leaf("Lane Follow") { condition { ctx -> makesNoTurn.holds(ctx) } }
                  leaf("Right Turn") { condition { ctx -> makesRightTurn.holds(ctx) } }
                  leaf("Left Turn") { condition { ctx -> makesLeftTurn.holds(ctx) } }
                }
              }
              all("Multi-Lane") {
                projections {
                  projection("pedestrian")
                  projection("static")
                  projection("dynamic")
                  projection("static+dynamic")
                  projection("multi-lane-dynamic-relations")
                }

                condition { ctx ->
                  isInMultiLane.holds(
                      ctx, ctx.segment.tickData.first().currentTick, ctx.segment.primaryEntityId)
                }

                optional("Dynamic Relation") {
                  projections {
                    projection("pedestrian")
                    projectionRecursive("dynamic")
                    projectionRecursive("static+dynamic")
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
                    monitors { monitor("Overtaking") { ctx -> noRightOvertaking.holds(ctx) } }
                  }
                  leaf("Pedestrian Crossed") {
                    projections { projection("pedestrian") }

                    condition { ctx -> pedestrianCrossed.holds(ctx) }
                  }
                  leaf("Following Leading Vehicle") {
                    projections { projection("dynamic") }

                    condition { ctx ->
                      ctx.entityIds.any { otherVehicleId ->
                        follows.holds(ctx, entityId2 = otherVehicleId)
                      }
                    }
                  }
                }

                exclusive("Maneuver") {
                  projections {
                    projectionRecursive("static")
                    projectionRecursive("static+dynamic")
                  }
                  leaf("Lane Change") { condition { ctx -> changedLane.holds(ctx) } }
                  leaf("Lane Follow") { condition { ctx -> !changedLane.holds(ctx) } }
                }

                bounded("Stop Type", Pair(0, 1)) {
                  projections {
                    projectionRecursive("static")
                    projectionRecursive("static+dynamic")
                  }

                  leaf("Has Red Light") {
                    condition { ctx -> hasRelevantRedLight.holds(ctx) }
                    monitors { monitor("Has Red Light") { ctx -> !didCrossRedLight.holds(ctx) } }
                  }
                }
              }
              all("Single-Lane") {
                projections {
                  projection("pedestrian")
                  projection("static")
                  projection("dynamic")
                  projection("static+dynamic")
                }

                condition { ctx ->
                  isInSingleLane.holds(
                      ctx, ctx.segment.tickData.first().currentTick, ctx.segment.primaryEntityId)
                }

                optional("Dynamic Relation") {
                  projections {
                    projection("pedestrian")
                    projectionRecursive("dynamic")
                    projectionRecursive("static+dynamic")
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
                      projection("dynamic")
                      projection("static+dynamic")
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
                    projectionRecursive("static")
                    projectionRecursive("static+dynamic")
                  }

                  leaf("Has Stop Sign") {
                    condition { ctx -> hasStopSign.holds(ctx) }
                    monitors { monitor("Has Stop Sign") { ctx -> stopAtEnd.holds(ctx) } }
                  }
                  leaf("Has Yield Sign") { condition { ctx -> hasYieldSign.holds(ctx) } }
                  leaf("Has Red Light") {
                    condition { ctx -> hasRelevantRedLight.holds(ctx) }
                    monitors { monitor("Has Red Light") { ctx -> !didCrossRedLight.holds(ctx) } }
                  }
                }
              }
            }

            exclusive("Traffic Density") {
              projections {
                projectionRecursive("environment")
                projectionRecursive("dynamic")
                projectionRecursive("static+dynamic")
              }

              leaf("High Traffic") { condition { ctx -> hasHighTrafficDensity.holds(ctx) } }
              leaf("Middle Traffic") { condition { ctx -> hasMidTrafficDensity.holds(ctx) } }
              leaf("Low Traffic") { condition { ctx -> hasLowTrafficDensity.holds(ctx) } }
            }

            exclusive("Time of Day") {
              projections {
                projectionRecursive("environment")
                projectionRecursive("pedestrian")
              }

              leaf("Sunset") { condition { ctx -> ctx.sunset() } }

              leaf("Noon") { condition { ctx -> ctx.noon() } }
            }
          }
        }
