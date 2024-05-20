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
 * This class holds the sliced analysis data in a semantic [Segment].
 *
 * @property segmentSource Specifies the source file of this [Segment].
 * @property startTick The start tick of this [Segment].
 * @property endTick The last tick of this [Segment].
 * @property primaryActorId The primary actor id of this [Segment].
 * @property tickData A [List] of [TickData] objects contained in this [Segment].
 */
@Serializable
data class Segment(
    @SerialName("segmentSource") val segmentSource: String,
    @SerialName("startTick") val startTick: Double,
    @SerialName("endTick") val endTick: Double,
    @SerialName("primaryActorId") val primaryActorId: Int,
    @SerialName("tickData") val tickData: List<TickData>
)
