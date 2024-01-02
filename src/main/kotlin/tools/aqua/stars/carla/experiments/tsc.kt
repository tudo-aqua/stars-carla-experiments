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

import tools.aqua.stars.core.evaluation.PredicateContext
import tools.aqua.stars.core.tsc.*
import tools.aqua.stars.data.av.Actor
import tools.aqua.stars.data.av.Segment
import tools.aqua.stars.data.av.TickData

fun tsc() =
    TSC(
        root<Actor, TickData, Segment> {
          all("TSCRoot") {
            valueFunction = { "TSCRoot" }
            projectionIDs =
                mapOf(
                    projRec("all"),
                    proj("static"),
                    proj("dynamic"),
                    proj("static+dynamic"),
                    proj("environment"),
                    proj("pedestrian"),
                    proj("multi-lane-dynamic-relations"))
            exclusive("Weather") {
              projectionIDs = mapOf(projRec("environment"), projRec("pedestrian"))
              leaf("Clear") { condition = PredicateContext<Actor, TickData, Segment>::weatherClear }
              leaf("Cloudy") {
                condition = PredicateContext<Actor, TickData, Segment>::weatherCloudy
              }
              leaf("Wet") { condition = PredicateContext<Actor, TickData, Segment>::weatherWet }
              leaf("Wet Cloudy") {
                condition = PredicateContext<Actor, TickData, Segment>::weatherWetCloudy
              }
              leaf("Soft Rain") {
                condition = PredicateContext<Actor, TickData, Segment>::weatherSoftRain
              }
              leaf("Mid Rain") {
                condition = PredicateContext<Actor, TickData, Segment>::weatherMidRain
              }
              leaf("Hard Rain") {
                condition = PredicateContext<Actor, TickData, Segment>::weatherHardRain
              }
            }
            exclusive("Road Type") {
              projectionIDs =
                  mapOf(
                      proj("static"),
                      proj("dynamic"),
                      proj("static+dynamic"),
                      proj("pedestrian"),
                      proj("multi-lane-dynamic-relations"))
              all("Junction") {
                condition = { ctx -> isInJunction.holds(ctx) }
                projectionIDs =
                    mapOf(
                        proj("pedestrian"), proj("static"), proj("dynamic"), proj("static+dynamic"))
                optional("Dynamic Relation") {
                  projectionIDs =
                      mapOf(proj("pedestrian"), projRec("dynamic"), projRec("static+dynamic"))
                  leaf("Pedestrian Crossed") {
                    projectionIDs = mapOf(proj("pedestrian"))
                    condition = { ctx -> pedestrianCrossed.holds(ctx) }
                  }
                  leaf("Must Yield") {
                    condition = { ctx ->
                      ctx.entityIds.any { otherVehicleId ->
                        mustYield.holds(ctx, actor2Id = otherVehicleId)
                      }
                    }
                    monitorFunction = { ctx ->
                      ctx.entityIds.any { otherVehicleId ->
                        hasYielded.holds(ctx, actor2Id = otherVehicleId)
                      }
                    }
                  }
                  leaf("Following Leading Vehicle") {
                    projectionIDs = mapOf(proj("dynamic"))
                    condition = { ctx ->
                      ctx.entityIds.any { otherVehicleId ->
                        follows.holds(ctx, actor2Id = otherVehicleId)
                      }
                    }
                  }
                }
                exclusive("Maneuver") {
                  projectionIDs = mapOf(projRec("static"), projRec("static+dynamic"))
                  leaf("Lane Follow") { condition = { ctx -> makesNoTurn.holds(ctx) } }
                  leaf("Right Turn") { condition = { ctx -> makesRightTurn.holds(ctx) } }
                  leaf("Left Turn") { condition = { ctx -> makesLeftTurn.holds(ctx) } }
                }
              }
              all("Multi-Lane") {
                condition = { ctx ->
                  isInMultiLane.holds(ctx, ctx.segment.firstTickId, ctx.segment.primaryEntityId)
                }
                projectionIDs =
                    mapOf(
                        proj("pedestrian"),
                        proj("static"),
                        proj("dynamic"),
                        proj("static+dynamic"),
                        proj("multi-lane-dynamic-relations"))
                optional("Dynamic Relation") {
                  projectionIDs =
                      mapOf(
                          proj("pedestrian"),
                          projRec("dynamic"),
                          projRec("static+dynamic"),
                          projRec("multi-lane-dynamic-relations"))
                  leaf("Oncoming traffic") {
                    condition = { ctx ->
                      ctx.entityIds.any { otherVehicleId ->
                        oncoming.holds(ctx, actor2Id = otherVehicleId)
                      }
                    }
                  }
                  leaf("Overtaking") {
                    condition = { ctx -> hasOvertaken.holds(ctx) }
                    monitorFunction = { ctx -> noRightOvertaking.holds(ctx) }
                  }
                  leaf("Pedestrian Crossed") {
                    projectionIDs = mapOf(proj("pedestrian"))
                    condition = { ctx -> pedestrianCrossed.holds(ctx) }
                  }
                  leaf("Following Leading Vehicle") {
                    projectionIDs = mapOf(proj("dynamic"))
                    condition = { ctx ->
                      ctx.entityIds.any { otherVehicleId ->
                        follows.holds(ctx, actor2Id = otherVehicleId)
                      }
                    }
                  }
                }
                exclusive("Maneuver") {
                  projectionIDs = mapOf(projRec("static"), projRec("static+dynamic"))
                  leaf("Lane Change") { condition = { ctx -> changedLane.holds(ctx) } }
                  leaf("Lane Follow") { condition = { ctx -> !changedLane.holds(ctx) } }
                }
                bounded("Stop Type", Pair(0, 1)) {
                  projectionIDs = mapOf(projRec("static"), projRec("static+dynamic"))
                  leaf("Has Red Light") {
                    condition = { ctx -> hasRelevantRedLight.holds(ctx) }
                    monitorFunction = { ctx -> !didCrossRedLight.holds(ctx) }
                  }
                }
              }
              all("Single-Lane") {
                condition = { ctx ->
                  isInSingleLane.holds(ctx, ctx.segment.firstTickId, ctx.segment.primaryEntityId)
                }
                projectionIDs =
                    mapOf(
                        proj("pedestrian"), proj("static"), proj("dynamic"), proj("static+dynamic"))
                optional("Dynamic Relation") {
                  projectionIDs =
                      mapOf(proj("pedestrian"), projRec("dynamic"), projRec("static+dynamic"))
                  leaf("Oncoming traffic") {
                    condition = { ctx ->
                      ctx.entityIds.any { otherVehicleId ->
                        oncoming.holds(ctx, actor2Id = otherVehicleId)
                      }
                    }
                  }
                  leaf("Pedestrian Crossed") {
                    projectionIDs = mapOf(proj("pedestrian"))
                    condition = { ctx -> pedestrianCrossed.holds(ctx) }
                  }
                  leaf("Following Leading Vehicle") {
                    projectionIDs = mapOf(proj("dynamic"), proj("static+dynamic"))
                    condition = { ctx ->
                      ctx.entityIds.any { otherVehicleId ->
                        follows.holds(ctx, actor2Id = otherVehicleId)
                      }
                    }
                  }
                }
                bounded("Stop Type", Pair(0, 1)) {
                  projectionIDs = mapOf(projRec("static"), projRec("static+dynamic"))
                  leaf("Has Stop Sign") {
                    condition = { ctx -> hasStopSign.holds(ctx) }
                    monitorFunction = { ctx -> stopAtEnd.holds(ctx) }
                  }
                  leaf("Has Yield Sign") { condition = { ctx -> hasYieldSign.holds(ctx) } }
                  leaf("Has Red Light") {
                    condition = { ctx -> hasRelevantRedLight.holds(ctx) }
                    monitorFunction = { ctx -> !didCrossRedLight.holds(ctx) }
                  }
                }
              }
            }
            exclusive("Traffic Density") {
              projectionIDs =
                  mapOf(projRec("environment"), projRec("dynamic"), projRec("static+dynamic"))
              leaf("High Traffic") { condition = { ctx -> hasHighTrafficDensity.holds(ctx) } }
              leaf("Middle Traffic") { condition = { ctx -> hasMidTrafficDensity.holds(ctx) } }
              leaf("Low Traffic") { condition = { ctx -> hasLowTrafficDensity.holds(ctx) } }
            }
            exclusive("Time of Day") {
              projectionIDs = mapOf(projRec("environment"), projRec("pedestrian"))
              leaf("Sunset") { condition = PredicateContext<Actor, TickData, Segment>::sunset }
              leaf("Noon") { condition = ExperimentPredicateContext::noon }
            }
          }
        })
