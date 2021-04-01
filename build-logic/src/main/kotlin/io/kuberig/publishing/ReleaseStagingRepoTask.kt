package io.kuberig.publishing

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class ReleaseStagingRepoTask : DefaultTask() {

    init {
        group = "maven central"
    }

    @TaskAction
    fun releaseStagingRepo() {
        println("TODO release staging repo")
    }
}

