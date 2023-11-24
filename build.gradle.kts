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

plugins {
  kotlin("jvm") version "1.9.0"
  application
  id("com.diffplug.spotless") version "6.21.0"
}

group = "tools.aqua"

version = "1.0-SNAPSHOT"

repositories {
  mavenCentral()
  //mavenLocal()
}

//var starsVersion = "1.0"

dependencies {
  testImplementation(kotlin("test"))
  implementation(group="tools.aqua", name="stars-core")
  implementation(group="tools.aqua", name="stars-logic-kcmftbl")
  implementation(group="tools.aqua", name="stars-data-av")
  implementation(group="tools.aqua", name="stars-importer-carla")
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
