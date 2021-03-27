package manual

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement
import kong.unirest.Unirest
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.time.Duration

/**
 * https://support.sonatype.com/hc/en-us/articles/213465868-Uploading-to-a-Staging-Repository-via-REST-API
 */
open class StagingRepoCreationTask : DefaultTask() {

    init {
        group = "maven central"
    }

    @TaskAction
    fun createStagingRepo() {
        val xmlMapper = XmlMapper()
        xmlMapper.findAndRegisterModules()

        val username = project.properties["sonatypeUsername"]!! as String
        val password = project.properties["sonatypePassword"]!! as String

        val promoteRequestRequest = PromoteRequestRequest(
            PromoteRequestRequestData("${project.rootProject.name} ${project.version}")
        )

        val spawnInstance = Unirest.spawnInstance()

        val config = spawnInstance.config()
        config.connectTimeout(Duration.ofMinutes(5).toMillis().toInt())
        config.socketTimeout(Duration.ofMinutes(5).toMillis().toInt())

        val baseUri = "https://oss.sonatype.org/service/local/"
        val stagingProfileId = "a75126268d08"

        val apiResponse = spawnInstance.post("$baseUri/staging/profiles/$stagingProfileId/start")
            .basicAuth(username, password)
            .header("Content-Type", "application/xml")
            .body(xmlMapper.writeValueAsString(promoteRequestRequest)).asString()

        println("asString.status = ${apiResponse.status}")
        println("asString.statusText = ${apiResponse.statusText}")
        println("asString = ${apiResponse.body}")

        val promoteRequestResponse = xmlMapper.readValue(apiResponse.body, PromoteRequestResponse::class.java)

        val contentUrl = "${baseUri}repositories/${promoteRequestResponse.data.stagedRepositoryId}/content"
        val stagedRepositoryUrl = "${baseUri}staging/deployByRepositoryId/${promoteRequestResponse.data.stagedRepositoryId}"

        println("Publishing to staged repository: $stagedRepositoryUrl")
        println("Content URL: $contentUrl")

        StagingRepoInfoHolder.setStagedRepoInfo(StagedRepoInfo(
            promoteRequestResponse.data.stagedRepositoryId,
            stagedRepositoryUrl,
            promoteRequestResponse.data.description
        ))

        Thread.sleep(5000)
    }
}

@JacksonXmlRootElement(localName="promoteRequest")
data class PromoteRequestRequest(val data: PromoteRequestRequestData)
data class PromoteRequestRequestData(val description: String)

@JacksonXmlRootElement(localName="promoteRequest")
data class PromoteRequestResponse(val data: PromoteRequestResponseData)
data class PromoteRequestResponseData(
    val stagedRepositoryId: String,
    val description: String)