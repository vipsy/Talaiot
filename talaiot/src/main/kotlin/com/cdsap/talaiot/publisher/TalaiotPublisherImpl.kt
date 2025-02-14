package com.cdsap.talaiot.publisher

import com.cdsap.talaiot.TalaiotExtension
import com.cdsap.talaiot.entities.ExecutionReport
import com.cdsap.talaiot.entities.TaskLength
import com.cdsap.talaiot.filter.TaskFilterProcessor
import com.cdsap.talaiot.logger.LogTracker
import com.cdsap.talaiot.provider.Provider

/**
 * Implementation of TalaiotPublisher.
 * It will retrieve all the metrics trough the MetricsProvider and the Publishers defined in the configuration
 * trough the PublisherProvider.
 * At the publishing phase it will aggregate the data of in a TaskMeasurementAggregated to publish the result
 * on each publisher retrieved.
 * Before the publishing phase we will apply the TaskFilterProcessor. Filtering doesn't apply to
 * the TaskDependencyGraphPublisher
 */
class TalaiotPublisherImpl(
    private val extension: TalaiotExtension,
    logger: LogTracker,
    private val metricsProvider: Provider<ExecutionReport>,
    private val publisherProvider: Provider<List<Publisher>>
) : TalaiotPublisher {
    private val taskFilterProcessor: TaskFilterProcessor = TaskFilterProcessor(logger, extension.filter)

    override fun publish(
        taskLengthList: MutableList<TaskLength>,
        startMs: Long,
        configuraionMs: Long?,
        endMs: Long,
        success: Boolean
    ) {
        val report = metricsProvider.get().apply {
            tasks = taskLengthList.filter { taskFilterProcessor.taskLengthFilter(it) }
            unfilteredTasks = taskLengthList
            this.beginMs = startMs.toString()
            this.endMs = endMs.toString()
            this.success = success

            this.durationMs = (endMs - startMs).toString()

            this.configurationDurationMs = when {
                configuraionMs != null -> (configuraionMs - startMs).toString()
                else -> "undefined"
            }

            this.estimateCriticalPath()
        }

        publisherProvider.get().forEach {
            it.publish(report)
        }
    }
}
