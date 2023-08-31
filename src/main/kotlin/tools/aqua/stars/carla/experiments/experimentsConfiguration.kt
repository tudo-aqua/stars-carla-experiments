/*
 * Copyright 2023 The STARS Carla Experiments Authors
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

// The download size is approximately 1.3GB!
// Manual download via: https://zenodo.org/record/8131947
val DOWNLOAD_EXPERIMENTS_DATA = false

val USE_EVERY_VEHICLE_AS_EGO = false
val MIN_SEGMENT_TICK_COUNT = 10
val SIMULATION_RUN_FOLDER = ".\\stars-reproduction-source\\stars-experiments-data\\simulation_runs"
val PROJECTION_IGNORE_LIST = listOf<String>()
val FILTER_REGEX = "*"
val STATIC_FILTER_REGEX = "*"
