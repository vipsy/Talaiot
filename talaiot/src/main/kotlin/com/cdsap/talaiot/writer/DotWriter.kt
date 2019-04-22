package com.cdsap.talaiot.writer

import com.cdsap.talaiot.writer.FileWriter.Companion.TALAIOT_OUTPUT_DIR
import guru.nidi.graphviz.engine.Renderer
import org.gradle.api.Project
import java.io.File


class DotWriter(override var project: Project) : FileWriter<Renderer> {
    override fun prepareFile(content: Renderer, name: String) {
        val fileName = File("${project.rootDir}/$TALAIOT_OUTPUT_DIR/$name")
        createFile {
            content.toFile(fileName)
        }
    }
}
