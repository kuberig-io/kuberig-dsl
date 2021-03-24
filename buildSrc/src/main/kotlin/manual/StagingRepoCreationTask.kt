package manual

import kong.unirest.Unirest.post
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class StagingRepoCreationTask : DefaultTask() {

    @TaskAction
    fun createStagingRepo() {

        val username = project.properties["sonatypeUsername"]!! as String
        val password = project.properties["sonatypePassword"]!! as String

        val asString = post("https://oss.sonatype.org/service/local/staging/profiles/a75126268d08/start")
            .basicAuth(username, password)
            .header("Content-Type", "application/xml")
            .body(
                """<promoteRequest>
  <data>
    <description>kuberig-dsl 0.1.7-RC21</description>
  </data>
</promoteRequest>
"""
            ).asString()

        println("asString.status = ${asString.status}")
        println("asString.statusText = ${asString.statusText}")
        println("asString = ${asString.body}")
    }

}