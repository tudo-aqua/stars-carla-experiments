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

plugins {
  kotlin("jvm") version "1.9.0"
  application
  id("com.diffplug.spotless") version "6.21.0"
}

group = "tools.aqua"

version = "0.2.2"

repositories {
  mavenCentral()
  mavenLocal()
}

var starsVersion = "0.2.2"

dependencies {
  testImplementation(kotlin("test"))
  implementation("tools.aqua:stars-core:$starsVersion")
  implementation("tools.aqua:stars-logic-kcmftbl:$starsVersion")
  implementation("tools.aqua:stars-data-av:$starsVersion")
  implementation("tools.aqua:stars-importer-carla:$starsVersion")
}

spotless {
  kotlin {
    licenseHeaderFile(rootProject.file("contrib/license-header.template.kt")).also {
      it.updateYearWithLatest(true)
    }
    ktfmt()
  }
  kotlinGradle {
    licenseHeaderFile(
            rootProject.file("contrib/license-header.template.kt"),
            "(import |@file|plugins |dependencyResolutionManagement|rootProject.name)")
        .also { it.updateYearWithLatest(true) }
    ktfmt()
  }
}

tasks.test { useJUnitPlatform() }

application { mainClass.set("tools.aqua.stars.carla.experiments.RunExperimentsKt") }

kotlin { jvmToolchain(17) }
