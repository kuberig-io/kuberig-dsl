package io.kuberig.dsl.generator.gradle;

import io.kuberig.test.support.TemporaryFolderExtension;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;
import org.gradle.testkit.runner.internal.PluginUnderTestMetadataReading;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertSame;

public abstract class AbstractDslGenerationTest {

    @RegisterExtension
    static TemporaryFolderExtension temporaryFolder = new TemporaryFolderExtension();

    private final String platformName;


    protected AbstractDslGenerationTest(String platformName) {
        this.platformName = platformName;
    }

    protected void attemptDslGenerationAndProjectCompilation(String platformVersion) throws Exception {
        final File sourceSwaggerFile = new File("src/test/resources/swagger/" + platformName + "/swagger-" + platformVersion + ".json");
        final List<String> sourceSwaggerLines = Files.readAllLines(sourceSwaggerFile.toPath(), StandardCharsets.UTF_8);

        final File buildFile = temporaryFolder.newFile("build.gradle.kts");
        Files.write(buildFile.toPath(), Arrays.asList(
                "plugins {",
                "   id(\"io.kuberig.dsl.generator\") ",
                "}",
                "repositories {",
                "   flatDir {",
                "       dirs(\"libs\")",
                "   }",
                "   jcenter()",
                "}",
                "",
                "kuberigDsl {",
                "   kubeRigDslVersion = \"0.0.21\"",
                "   jacksonVersion = \"2.9.8\"",
                "}"), StandardCharsets.UTF_8);

        final File swaggerFile = temporaryFolder.newFile("src/main/resources/swagger.json");
        Files.write(swaggerFile.toPath(), sourceSwaggerLines, StandardCharsets.UTF_8);

        /*
        This part uses the gradle provided plugin-under-test-classpath info but cleans it up.
        - removes directories
        - and switches to the jar artifact of the plugin instead.
        This is needed otherwise the test project won't find the plugin because it expects a jar file.
         */
        final List<File> classpathFiles = PluginUnderTestMetadataReading.readImplementationClasspath();
        final List<File> cleanedClasspathFiles = new ArrayList<>();
        for (File classpathFile : classpathFiles) {
            if (classpathFile.exists() && classpathFile.isFile()) {
                cleanedClasspathFiles.add(classpathFile);
            }
        }
        File buildDir = new File("build").getAbsoluteFile();
        /*
        The version 0.0.0 is passed to the tests via the projectVersion system property.
        In case no version is specified on the gradle project (in case of local developer or untagged builds) version 0.0.0 is passed.

        In case no version is specified the jar generated in the libs directory does not have a versions.
        This is problematic in for the fileTree repository so we add it when missing.
         */
        String projectVersion = System.getProperty("projectVersion");
        final File sourcePluginJarFile;
        if (projectVersion.equals("0.0.0")) {
            sourcePluginJarFile = new File(buildDir, "libs/kuberig-dsl-generator-gradle-plugin.jar");
        } else {
            sourcePluginJarFile = new File(buildDir, "libs/kuberig-dsl-generator-gradle-plugin-" + projectVersion + ".jar");
        }
        File tmpBuildDir = new File(buildDir, "tmp");
        File pluginJarFile = new File(tmpBuildDir, "kuberig-dsl-generator-gradle-plugin-" + projectVersion + ".jar");
        if (!pluginJarFile.exists()) {
            Files.copy(sourcePluginJarFile.toPath(), pluginJarFile.toPath());
        }
        cleanedClasspathFiles.add(pluginJarFile);

        /*
        Additionally we make the cleaned up classpath available in the libs directory of the test project so it can
        actually use the plugin jar that is being generated.
         */
        final File libsDirectory = temporaryFolder.newFolder("libs");
        for (File cleanedClasspathFile : cleanedClasspathFiles) {
            File copyFile = new File(libsDirectory, cleanedClasspathFile.getName());
            Files.copy(cleanedClasspathFile.toPath(), copyFile.toPath());
        }

        try {
            final BuildResult result = GradleRunner.create()
                    .withProjectDir(temporaryFolder.getRoot())
                    .withArguments("build")
                    .withPluginClasspath(cleanedClasspathFiles)
                    .build();

            System.out.println(result.getOutput());

            assertSame(result.task(":build").getOutcome(), TaskOutcome.SUCCESS);
            assertSame(result.task(":generateDslSource").getOutcome(), TaskOutcome.SUCCESS);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
