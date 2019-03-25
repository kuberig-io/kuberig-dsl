import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

dependencies {
    implementation("com.fasterxml.jackson.core:jackson-core:2.9.8")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.9.8")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.9.8")
    implementation("com.fasterxml.jackson.module:jackson-modules-java8:2.9.8")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.9.8")

    implementation(project(":kuberig-dsl-base"))

    testImplementation("org.skyscreamer:jsonassert:1.5.0")
}

sourceSets["main"].withConvention(KotlinSourceSet::class) {
    kotlin.srcDir("$buildDir/generated-src/main/kotlin")
}
idea.module {
    // Marks the already(!) added srcDir as "generated"
    generatedSourceDirs.add(project.file("$buildDir/generated-src/main/kotlin"))
}

tasks.getByName("compileKotlin", KotlinCompile::class) {
    dependsOn("generateDsl")
}

tasks {
    create("generateDsl", JavaExec::class) {
        group = "dsl-generation"
        main = "eu.rigeldev.kuberig.dsl.generator.DslCodeGeneratorKt"
        workingDir = project.rootProject.projectDir
        args = listOf(project.projectDir.absolutePath)
        classpath = project(":kuberig-dsl-generator").sourceSets["main"].runtimeClasspath
    }
}