package com.cdsap.talaiot.publisher.graphpublisher

import com.cdsap.talaiot.entities.TaskMeasurementAggregated
import com.cdsap.talaiot.logger.LogTracker
import com.cdsap.talaiot.publisher.graphpublisher.resources.ResourcesGexf
import com.cdsap.talaiot.writer.FileWriter
import java.util.concurrent.Executor

class GexfPublisher(
    override var logTracker: LogTracker,
    override var fileWriter: FileWriter,
    private val executor: Executor
) : DefaultDiskPublisher(logTracker, fileWriter) {

    private val fileName: String = "gexfTaskDependency.gexf"
    private var internalCounterEdges = 0

    override fun publish(taskMeasurementAggregated: TaskMeasurementAggregated) {
        executor.execute {
            val content = contentComposer(
                task = buildGraph(taskMeasurementAggregated),
                header = ResourcesGexf.HEADER,
                footer = ResourcesGexf.FOOTER
            )
            logTracker.log("GexfPublisher: writing file")
            writeFile(content, fileName)
        }
    }

    override fun formatNode(
        internalId: Int,
        module: String,
        taskName: String,
        numberDependencies: Int,
        cached: Boolean
    ): String = "       <node id=\"$internalId\" label=\"$taskName\">\n" +
            "              <attvalues>\n" +
            "                     <attvalue for=\"0\" value=\"$module\"/>\n" +
            "                     <attvalue for=\"1\" value=\"$cached\"/>\n" +
            "              </attvalues>\n" +
            "       </node>\n"


    override fun formatEdge(
        from: Int,
        to: Int?
    ) = "       <edge id=\"${internalCounterEdges++}\" " +
            "source=\"$from\" target=\"$to\" />\n"
}
