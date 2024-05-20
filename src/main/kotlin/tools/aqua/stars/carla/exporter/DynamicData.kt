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
 * This class represents the dynamic data of an experiment. It holds [Segment]s that can be analysed
 * in the STARS-Visualizer tool.
 *
 * @property segments A [List] of [Segment]s for this experiment.
 * @property actorTypes A [List] of [ActorType]s that are used in this experiment.
 */
@Serializable
data class DynamicData(
    @SerialName("segments") val segments: List<Segment>,
    @SerialName("actorTypes") val actorTypes: List<ActorType>
)
