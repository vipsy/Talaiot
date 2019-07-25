package com.cdsap.talaiot.entities

/**
 * Since the user of the plugin might customize metrics provider we have to make almost everything here optional
 *
 * @property beginMs timestamp of gradle execution start
 * @property endMs timestamp of gradle execution finish
 * @property durationMs duration in millis of gradle execution
 * @property configurationDurationMs duration of the configuration phase
 *
 * @property tasks list of executed tasks filtered according to [com.cdsap.talaiot.configuration.FilterConfiguration]
 * @property unfilteredTasks list of executed tasks
 * @property requestedTasks gradle tasks requested by user, e.g. "assemble test"
 *
 * @property buildId unique build identifier generated by Talaiot
 * @property buildInvocationId unique build identifier generated by Gradle's [org.gradle.internal.scan.scopeids.BuildScanScopeIds]. It has the same value for multi-stage builds, e.g. wrapper invocation and actual tasks requested. For more info see comments at [com.cdsap.talaiot.metrics.GradleBuildInvocationIdMetric]
 *
 * @property rootProject name of the root gradle project
 * @property success true if build finished successfully, false otherwise
 *
 * @property scanLink link to the generated gradle scan
 *
 * @property environment information about the environment of gradle execution
 * @property customProperties custom properties defined in [com.cdsap.talaiot.configuration.MetricsConfiguration]
 */
data class ExecutionReport(
    var environment: Environment = Environment(),
    var customProperties: CustomProperties = CustomProperties(),
    var beginMs: String? = null,
    var endMs: String? = null,
    var durationMs: String? = null,
    var configurationDurationMs: String? = null,
    var tasks: List<TaskLength>? = null,
    var unfilteredTasks: List<TaskLength>? = null,
    var buildId: String? = null,
    var rootProject: String? = null,
    var requestedTasks: String? = null,
    var success: Boolean = false,
    var scanLink: String? = null,
    var buildInvocationId: String? = null
) {

    /**
     * Cache ratio of the tasks = tasks_from_cache / all_tasks
     */
    val cacheRatio: String?
        get() = tasks?.let {
            it.count { taskLength -> taskLength.state == TaskMessageState.FROM_CACHE } / it.size.toDouble()
        }?.toString()

    fun flattenBuildEnv(): Map<String, String> {
        val map = mutableMapOf<String, String>()

        with(environment) {
            cacheMode?.let { map["cacheMode"] = it }
            cachePushEnabled?.let { map["cachePushEnabled"] = it }
            cacheUrl?.let { map["cacheUrl"] = it }
            cacheHit?.let { map["cacheHit"] = it }
            cacheMiss?.let { map["cacheMiss"] = it }
            cacheStore?.let { map["cacheStore"] = it }

            switches.buildCache?.let { map["switch.cache"] = it }
            switches.buildScan?.let { map["switch.scan"] = it }
            switches.configurationOnDemand?.let { map["switch.configurationOnDemand"] = it }
            switches.continueOnFailure?.let { map["switch.continueOnFailure"] = it }
            switches.daemon?.let { map["switch.daemon"] = it }
            switches.dryRun?.let { map["switch.dryRun"] = it }
            switches.offline?.let { map["switch.offline"] = it }
            switches.parallel?.let { map["switch.parallel"] = it }
            switches.refreshDependencies?.let { map["switch.refreshDependencies"] = it }
            switches.rerunTasks?.let { map["switch.rerunTasks"] = it }
        }

        environment.osVersion?.let { map["osVersion"] = it }
        environment.javaVmName?.let { map["javaVmName"] = it }
        environment.cpuCount?.let { map["cpuCount"] = it }
        environment.username?.let { map["username"] = it }
        environment.gradleVersion?.let { map["gradleVersion"] = it }

        buildId?.let { map["buildId"] = it }
        rootProject?.let { map["rootProject"] = it }
        requestedTasks?.let { map["requestedTasks"] = it }

        //These come last to have an ability to override calculation
        map.putAll(customProperties.buildProperties)

        return map.filter { (k, v) -> v != "undefined" }
    }

    /**
     * Fills in the [TaskLength.critical] to later check which task was on the critical (longest in terms of time) path
     *
     * This would be a lot faster if it was actually a weighted graph
     */
    fun estimateCriticalPath() {
        var currentRoot: TaskLength? = tasks?.find { it.rootNode } ?: return

        while (currentRoot != null) {
            currentRoot.critical = true

            val dependencies = currentRoot.taskDependencies
                .mapNotNull { dep ->
                    tasks?.find { it.taskPath == dep }
                }

            currentRoot = dependencies.maxBy { it.stopMs } ?: null
        }
    }
}

data class Environment(
    var cpuCount: String? = null,
    var osVersion: String? = null,
    var maxWorkers: String? = null,
    var javaRuntime: String? = null,
    var javaVmName: String? = null,
    var javaXmsBytes: String? = null,
    var javaXmxBytes: String? = null,
    var javaMaxPermSize: String? = null,
    var totalRamAvailableBytes: String? = null,
    var locale: String? = null,
    var username: String? = null,
    var publicIp: String? = null,
    var defaultChartset: String? = null,
    var ideVersion: String? = null,
    var gradleVersion: String? = null,
    var cacheMode: String? = null,
    var cachePushEnabled: String? = null,
    var cacheUrl: String? = null,
    var cacheHit: String? = null,
    var cacheMiss: String? = null,
    var cacheStore: String? = null,
    var plugins: List<Plugin> = emptyList(),
    var gitBranch: String? = null,
    var gitUser: String? = null,
    var switches: Switches = Switches(),
    var hostname: String? = null,
    var osManufacturer: String? = null
)

data class Switches(
    var buildCache: String? = null,
    var configurationOnDemand: String? = null,
    var daemon: String? = null,
    var parallel: String? = null,
    var continueOnFailure: String? = null,
    var dryRun: String? = null,
    var offline: String? = null,
    var rerunTasks: String? = null,
    var refreshDependencies: String? = null,
    var buildScan: String? = null
)

data class CustomProperties(
    var buildProperties: MutableMap<String, String> = mutableMapOf(),
    var taskProperties: MutableMap<String, String> = mutableMapOf()
)

/**
 * TODO: figure out how to get the list of current plugins applied to the project
 */
data class Plugin(
    var id: String,
    var mainClass: String,
    var version: String
)