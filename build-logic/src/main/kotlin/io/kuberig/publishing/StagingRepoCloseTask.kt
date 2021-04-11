package io.kuberig.publishing

import com.fasterxml.jackson.databind.ObjectMapper
import kong.unirest.Unirest
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.time.Duration

open class StagingRepoCloseTask : DefaultTask() {

    init {
        group = "maven central"
    }

    @TaskAction
    fun closeStagingRepo() {
        println("do close staging repo...")

        val mapper = ObjectMapper()
        mapper.findAndRegisterModules()

        val username = project.properties["sonatypeUsername"]!! as String
        val password = project.properties["sonatypePassword"]!! as String

        val spawnInstance = Unirest.spawnInstance()

        val config = spawnInstance.config()
        config.connectTimeout(Duration.ofMinutes(5).toMillis().toInt())
        config.socketTimeout(Duration.ofMinutes(5).toMillis().toInt())

        val baseUri = "https://oss.sonatype.org/service/local/"

        val stagedRepoInfo = StagingRepoInfoHolder.getStageRepoInfo()
        val closeRequest = CloseRequest(
            CloseRequestData(
                listOf(
//                    "iokuberig-1043"
                     stagedRepoInfo.stagedRepositoryId
                ),
//                "kuberig-dsl 0.1.7"
                stagedRepoInfo.stagedRepositoryDescription
            )
        )

        val writeValueAsString = mapper.writeValueAsString(closeRequest)

        println("[close] $writeValueAsString")

        val apiResponse = spawnInstance.post("${baseUri}staging/bulk/close")
            .basicAuth(username, password)
            .header("Content-Type", "application/json")
            .body(writeValueAsString).asString()

        println("[close] asString.status = ${apiResponse.status}")
        println("[close] asString.statusText = ${apiResponse.statusText}")
        println("[close] asString = ${apiResponse.body}")

        spawnInstance.shutDown()

        if (!apiResponse.isSuccess) {
            throw IllegalStateException("Failed to close staging repo.")
        }
    }
}

data class CloseRequest(val data: CloseRequestData)
data class CloseRequestData(
    val stagedRepositoryIds: List<String>,
    val description: String,
    val autoDropAfterRelease: Boolean = true)