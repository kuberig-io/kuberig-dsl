plugins {
    id("io.kuberig.root-conventions")
}

fun generateModules(platformDir: File) {
    val platformName = platformDir.name.replace("kuberig-dsl-", "")

    val platformCiFile = File(rootProject.projectDir, ".gitlab-ci/kuberig-dsl-$platformName.yml")

    platformCiFile.writeText(
        """
        .generate-dsl-$platformName:
          stage: dsls
          script:
            - |
              cd kuberig-dsl/vanilla-dsls/kuberig-dsl-$platformName/kuberig-dsl-$platformName-$${platformName.toUpperCase()}_VERSION
              source ../../../../ci-gradle-init.sh
              gradle publishToMavenLocal
          needs:
            - kuberig-dsl-deploy
            
    """.trimIndent())
    platformCiFile.appendText("\n\n")

    val modules = platformDir.listFiles()

    if (modules != null) {
        for (k8sModule in modules) {
            println("Generating ${k8sModule.name}...")
            val settingsFile = File(k8sModule, "settings.gradle.kts")

            settingsFile.writeText(
                """
                rootProject.name = "${k8sModule.name}"
    
                pluginManagement {
                    includeBuild("../../../../build-logic")
                    includeBuild("../../../..")
                }
            """.trimIndent()
            )

            val buildFile = File(k8sModule, "build.gradle.kts")

            buildFile.writeText(
                """
                plugins {
                    id("io.kuberig.vanilla-dsl-conventions")
                    id("io.kuberig.dsl.generator")
                }
            """.trimIndent()
            )

            val gradlewFile = File(k8sModule, "gradlew")
            val gradlewBatFile = File(k8sModule, "gradlew.bat")

            val rootGradleDirectory = File(rootProject.projectDir, "gradle")
            rootGradleDirectory.copyRecursively(File(k8sModule, "gradle"), true)
            val rootGradlewFile = File(rootProject.projectDir, "gradlew")
            rootGradlewFile.copyTo(gradlewFile, true)
            val rootGradleBatFile = File(rootProject.projectDir, "gradlew.bat")
            rootGradleBatFile.copyTo(gradlewBatFile, true)

            exec {
                workingDir = k8sModule
                commandLine("chmod", "+x", "gradlew")
            }

            val platformVersion = k8sModule.name.replace("kuberig-dsl-$platformName-", "")

            platformCiFile.appendText(
                """
                kuberig-dsl-$platformName-$platformVersion:
                  extends:
                    - .generate-dsl-$platformName
                  variables:
                    ${platformName.toUpperCase()}_VERSION: '$platformVersion'
                    
            """.trimIndent()
            )
            platformCiFile.appendText("\n\n")
        }
    }
}

tasks.create("generateDslModules") {
    group = "setup"

    doLast {
        generateModules(File("kuberig-dsl/vanilla-dsls/kuberig-dsl-kubernetes"))
        generateModules(File("kuberig-dsl/vanilla-dsls/kuberig-dsl-openshift"))
    }

    outputs.upToDateWhen { false }
}