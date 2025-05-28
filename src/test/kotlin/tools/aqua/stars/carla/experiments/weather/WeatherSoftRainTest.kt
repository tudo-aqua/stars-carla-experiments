/*
 * Copyright 2024-2025 The STARS Carla Experiments Authors
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
import tools.aqua.stars.carla.experiments.weatherSoftRain
import tools.aqua.stars.core.evaluation.PredicateContext
import tools.aqua.stars.data.av.dataclasses.Lane
import tools.aqua.stars.data.av.dataclasses.Segment
import tools.aqua.stars.data.av.dataclasses.TickData
import tools.aqua.stars.data.av.dataclasses.TickDataUnitSeconds
import tools.aqua.stars.data.av.dataclasses.Vehicle
import tools.aqua.stars.data.av.dataclasses.WeatherParameters
import tools.aqua.stars.data.av.dataclasses.WeatherType

class WeatherSoftRainTest {

  private val currentWeatherCondition = WeatherType.SoftRainy
  private val otherWeatherCondition = WeatherType.entries.first { it != currentWeatherCondition }

  @Test
  fun testOtherWeatherConditions() {
    WeatherType.entries.forEach { weatherType ->
      if (weatherType == currentWeatherCondition) {
        return@forEach
      }
      val weatherParameters = WeatherParameters(weatherType)
      val ego = Vehicle(isEgo = true, lane = Lane())
      val tick = TickData(weather = weatherParameters, entities = listOf(ego))
      val segment = Segment(listOf(tick), segmentSource = "")
      val context = PredicateContext(segment)

      assertFalse { context.weatherSoftRain() }
    }
  }

  @Test
  fun testWeatherConditions() {
    val weatherParameters = WeatherParameters(currentWeatherCondition)
    val ego = Vehicle(isEgo = true, lane = Lane())
    val tick = TickData(weather = weatherParameters, entities = listOf(ego))
    val segment = Segment(listOf(tick), segmentSource = "")
    val context = PredicateContext(segment)

    assertTrue { context.weatherSoftRain() }
  }

  @Test
  fun testWeatherConditionsOver60Percent() {
    val ticks = mutableListOf<TickData>()
    for (i in 1..6) {
      val weatherParameters = WeatherParameters(currentWeatherCondition)
      val ego = Vehicle(isEgo = true, lane = Lane())
      val tick = TickData(weather = weatherParameters, entities = listOf(ego))
      ticks.add(tick)
    }
    for (i in 1..4) {
      val weatherParameters = WeatherParameters(otherWeatherCondition)
      val ego = Vehicle(isEgo = true, lane = Lane())
      val tick = TickData(weather = weatherParameters, entities = listOf(ego))
      ticks.add(tick)
    }
    val segment = Segment(ticks, segmentSource = "")
    val context = PredicateContext(segment)

    assertTrue { context.weatherSoftRain() }
  }

  @Test
  fun testWeatherConditionsUnder60Percent() {
    val ticks = mutableListOf<TickData>()
    for (i in 1..5) {
      val weatherParameters = WeatherParameters(currentWeatherCondition)
      val ego = Vehicle(isEgo = true, lane = Lane())
      val tick =
          TickData(
              currentTick = TickDataUnitSeconds(i.toDouble()),
              weather = weatherParameters,
              entities = listOf(ego))
      ticks.add(tick)
    }
    for (i in 6..10) {
      val weatherParameters = WeatherParameters(otherWeatherCondition)
      val ego = Vehicle(isEgo = true, lane = Lane())
      val tick =
          TickData(
              currentTick = TickDataUnitSeconds(i.toDouble()),
              weather = weatherParameters,
              entities = listOf(ego))
      ticks.add(tick)
    }
    val segment = Segment(ticks, segmentSource = "")
    val context = PredicateContext(segment)

    assertFalse { context.weatherSoftRain() }
  }
}
