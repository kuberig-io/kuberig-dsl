package manual

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.kotlin.dsl.getByType

@Suppress("UnstableApiUsage")
open class MavenCentralPublishPlugin : Plugin<Project> {

    override fun apply(rootProject: Project) {
        require(rootProject == rootProject.rootProject) {
            "Plugin must be applied to the root project!!"
        }

        rootProject.tasks.register("createStagingRepo", StagingRepoCreationTask::class.java)
        rootProject.tasks.register("closeStagingRepo", StagingRepoCloseTask::class.java)
        rootProject.tasks.register("releaseStagingRepo", ReleaseStagingRepoTask::class.java)

        rootProject.tasks.register("publishToMavenCentral") {
            group = "maven central"
        }

        rootProject.afterEvaluate {
            if (!rootProject.version.toString().endsWith("SNAPSHOT")) {
                val createStagingRepoTask = rootProject.tasks.getByName("createStagingRepo")
                val closeStagingRepoTask = rootProject.tasks.getByName("closeStagingRepo")
                val releaseStagingRepoTask = rootProject.tasks.getByName("releaseStagingRepo")
                val publishToMavenCentralTask = rootProject.tasks.getByName("publishToMavenCentral")

                closeStagingRepoTask.apply {
                    mustRunAfter(publishToMavenCentralTask)
                }
                releaseStagingRepoTask.apply {
                    mustRunAfter(publishToMavenCentralTask)
                }

                subprojects {
                    val subProject = this

                    plugins.withId("maven-publish") {

                        val repoName = "sonatype"

                        subProject.extensions.getByType<PublishingExtension>().repositories.maven {
                            name = "sonatype"
                            setUrl(subProject.provider {
                                subProject.uri(StagingRepoInfoHolder.getStageRepoInfo().stagedRepositoryUrl)
                            })

                            credentials {
                                username = rootProject.property("sonatypeUsername") as String
                                password = rootProject.property("sonatypePassword") as String
                            }
                        }

                        val publishTask =
                            subProject.tasks.getByName("publish${subProject.name.capitalize()}-mavenPublicationTo${repoName.capitalize()}Repository")

                        publishTask.dependsOn(createStagingRepoTask)
                        publishToMavenCentralTask.dependsOn(publishTask)

                        closeStagingRepoTask.mustRunAfter(publishTask)
                        releaseStagingRepoTask.mustRunAfter(publishTask)
                    }
                }
            }
        }
    }


}