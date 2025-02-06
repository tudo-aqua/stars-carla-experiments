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

import com.github.ajalt.clikt.core.main

/** Singleton the contains the [main] function and exit codes for this experiment. */
object Experiment {

  /** Normal exit and no reproduction run was conducted. */
  const val EXIT_CODE_NORMAL = 0

  /** Reproduction run yielded no results. */
  const val EXIT_CODE_NO_RESULTS = 4

  /** Reproduction run yielded unequal results. */
  const val EXIT_CODE_UNEQUAL_RESULTS = 2

  /** Reproduction run yielded equal results. */
  const val EXIT_CODE_EQUAL_RESULTS = 0

  /**
   * The main function that starts the experiment and passes all arguments to the Clikt-framework.
   */
  @JvmStatic
  fun main(args: Array<String>) {
    ExperimentConfiguration().main(args)
  }
}
