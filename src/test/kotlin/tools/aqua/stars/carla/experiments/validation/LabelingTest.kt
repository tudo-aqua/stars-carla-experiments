package tools.aqua.stars.carla.experiments.validation

import tools.aqua.stars.carla.experiments.hasMidTrafficDensity
import tools.aqua.stars.carla.experiments.isOnMultiLane
import tools.aqua.stars.carla.experiments.isOnSingleLane
import tools.aqua.stars.core.validation.labelFile

/**
 * Here you manually label “true” intervals
 * for each predicate (by its string form) in each file.
 */
val manualTests = labelFile("data/session1.csv") {
    predicate(::isOnSingleLane) {
        interval(0, 5)
        interval(20, 27)
    }
    predicate(::isOnMultiLane) {
        interval(13, 15)
    }
    predicate(::hasMidTrafficDensity) {
        interval(2, 8)
    }
}