package manual

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.kotlin.dsl.getByType

@Suppress("UnstableApiUsage")
open class MavenCentralPublishPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        require(project == project.rootProject) {
            "Plugin must be applied to the root project!!"
        }

        val createStagingRepoTask = project.tasks.create("createStagingRepo", StagingRepoCreationTask::class.java)
        val closeStagingRepoTask = project.tasks.create("closeStagingRepo", StagingRepoCloseTask::class.java)
        val releaseStagingRepoTask = project.tasks.create("releaseStagingRepo", ReleaseStagingRepoTask::class.java)

        val publishToMavenCentralTask = project.tasks.create("publishToMavenCentral") {
            group = "maven central"
        }

        closeStagingRepoTask.apply {
            mustRunAfter(publishToMavenCentralTask)
        }
        releaseStagingRepoTask.apply {
            mustRunAfter(publishToMavenCentralTask)
        }

        project.afterEvaluate {

            if (!project.version.toString().endsWith("SNAPSHOT")) {
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
                                username = project.property("sonatypeUsername") as String
                                password = project.property("sonatypePassword") as String
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