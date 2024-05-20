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
 * This class is used to declare actor types (e.g. a vehicle type like "Car1") with its dimensions
 * for a realistic visualization.
 *
 * @property actorTypeId The id of this actor type.
 * @property width The width of this actor type.
 * @property length The length of this actor type.
 * @property height The height of this actor type.
 */
@Serializable
data class ActorType(
    @SerialName("actorTypeId") val actorTypeId: String,
    @SerialName("width") val width: Float,
    @SerialName("length") val length: Float,
    @SerialName("height") val height: Float
)
