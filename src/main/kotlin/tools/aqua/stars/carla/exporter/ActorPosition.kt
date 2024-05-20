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

package tools.aqua.stars.carla.exporter

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * This class contains data about one actor for a specific timestamp (tick).
 *
 * @property actorId The id of this actor. Unique per actor.
 * @property actorTypeId The type of this actor. References to [ActorType.actorTypeId] to get
 *   information about e.g. the dimensions of this actor.
 * @property location The [Location] of this actor for this tick.
 * @property rotation The [Rotation] of this actor for this tick.
 * @property description A description of this [ActorPosition]. Can be used to store additional
 *   information about this actor for this tick.
 * @property trajectoryColors The colors of the trajectories of this [ActorPosition]. Each index of
 *   the list corresponds to a trajectory. Therefore, the length of this list is equal to the number
 *   of trajectories and each [ActorPosition] has the same number of trajectories.
 */
@Serializable
data class ActorPosition(
    @SerialName("actorId") val actorId: Int,
    @SerialName("actorTypeId") val actorTypeId: String,
    @SerialName("location") val location: Location,
    @SerialName("rotation") val rotation: Rotation,
    @SerialName("description") val description: String = "",
    @SerialName("trajectoryColors") val trajectoryColors: List<String> = emptyList(),
)
