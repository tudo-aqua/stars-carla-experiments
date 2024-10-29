/*
 * Copyright 2024 The STARS Carla Experiments Authors
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

package tools.aqua.stars.carla.experiments.weather

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import tools.aqua.stars.carla.experiments.emptyTickData
import tools.aqua.stars.carla.experiments.emptyVehicle
import tools.aqua.stars.carla.experiments.emptyWeatherParameters
import tools.aqua.stars.carla.experiments.weatherHardRain
import tools.aqua.stars.core.evaluation.PredicateContext
import tools.aqua.stars.data.av.dataclasses.Segment
import tools.aqua.stars.data.av.dataclasses.TickData
import tools.aqua.stars.data.av.dataclasses.TickDataUnitSeconds
import tools.aqua.stars.data.av.dataclasses.WeatherType

class WeatherHardRainTest {

  private val currentWeatherCondition = WeatherType.HardRainy
  private val otherWeatherCondition = WeatherType.entries.first { it != currentWeatherCondition }

  @Test
  fun testOtherWeatherConditions() {
    WeatherType.entries.forEach { weatherType ->
      if (weatherType == currentWeatherCondition) {
        return@forEach
      }
      val weatherParameters = emptyWeatherParameters(weatherType)
      val ego = emptyVehicle(egoVehicle = true)
      val tick = emptyTickData(weatherParameters = weatherParameters, actors = listOf(ego))
      val segment = Segment(listOf(tick), segmentSource = "")
      val context = PredicateContext(segment)

      assertFalse { context.weatherHardRain() }
    }
  }

  @Test
  fun testWeatherConditions() {
    val weatherParameters = emptyWeatherParameters(currentWeatherCondition)
    val ego = emptyVehicle(egoVehicle = true)
    val tick = emptyTickData(weatherParameters = weatherParameters, actors = listOf(ego))
    val segment = Segment(listOf(tick), segmentSource = "")
    val context = PredicateContext(segment)

    assertTrue { context.weatherHardRain() }
  }

  @Test
  fun testWeatherConditionsOver60Percent() {
    val ticks = mutableListOf<TickData>()
    for (i in 1..6) {
      val weatherParameters = emptyWeatherParameters(currentWeatherCondition)
      val ego = emptyVehicle(egoVehicle = true)
      val tick = emptyTickData(weatherParameters = weatherParameters, actors = listOf(ego))
      ticks.add(tick)
    }
    for (i in 1..4) {
      val weatherParameters = emptyWeatherParameters(otherWeatherCondition)
      val ego = emptyVehicle(egoVehicle = true)
      val tick = emptyTickData(weatherParameters = weatherParameters, actors = listOf(ego))
      ticks.add(tick)
    }
    val segment = Segment(ticks, segmentSource = "")
    val context = PredicateContext(segment)

    assertTrue { context.weatherHardRain() }
  }

  @Test
  fun testWeatherConditionsUnder60Percent() {
    val ticks = mutableListOf<TickData>()
    for (i in 1..5) {
      val weatherParameters = emptyWeatherParameters(currentWeatherCondition)
      val ego = emptyVehicle(egoVehicle = true)
      val tick =
          emptyTickData(
              currentTick = TickDataUnitSeconds(i.toDouble()),
              weatherParameters = weatherParameters,
              actors = listOf(ego))
      ticks.add(tick)
    }
    for (i in 6..10) {
      val weatherParameters = emptyWeatherParameters(otherWeatherCondition)
      val ego = emptyVehicle(egoVehicle = true)
      val tick =
          emptyTickData(
              currentTick = TickDataUnitSeconds(i.toDouble()),
              weatherParameters = weatherParameters,
              actors = listOf(ego))
      ticks.add(tick)
    }
    val segment = Segment(ticks, segmentSource = "")
    val context = PredicateContext(segment)

    assertFalse { context.weatherHardRain() }
  }
}
