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
 * This class represents a line (e.g. an outer or inner line of a lane) of the static data to be
 * rendered in the STARS-visualizer tool.
 *
 * @property width The width of this [Line].
 * @property coordinates The coordinates of the line as a point curve of [Location] in a [List].
 * @property color The color of this [Line] in hex notation.
 */
@Serializable
data class Line(
    @SerialName("width") val width: Float,
    @SerialName("coordinates") val coordinates: List<Location>,
    @SerialName("color") val color: String = "#808080"
)
