package eu.rigeldev.kuberig.dsl.generator.gradle;

import eu.rigeldev.test.support.TemporaryFolderExtension;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;

public class OpenShiftDslGenerationTest {

    @RegisterExtension
    static TemporaryFolderExtension temporaryFolder = new TemporaryFolderExtension();

    /**
     * We include this OpenShift version specifically because the kind meta data in this version was specified with
     * camel case Group, Kind, Version instead of all lower case like in the Kubernetes api specifications.
     */
    @Test
    public void testOpenShiftVersion3dot6dot0() throws Exception {
        this.attemptDslGenerationAndProjectCompilation("3.6.0");
    }

    /**
     * From OpenShift 3.7.0 onwards the OpenShift api specifications also contain the kind meta data in all lower
     * case like in the Kubernetes api specifications.
     */
    @Test
    public void testOpenShiftVersion3dot11dot0() throws Exception {
        this.attemptDslGenerationAndProjectCompilation("3.11.0");
    }

    private void attemptDslGenerationAndProjectCompilation(String openshiftVersion) throws Exception {
        final File sourceSwaggerFile = new File("src/test/resources/swagger/openshift/swagger-"+openshiftVersion+".json");
        final List<String> sourceSwaggerLines = Files.readAllLines(sourceSwaggerFile.toPath(), StandardCharsets.UTF_8);

        final File buildFile = temporaryFolder.newFile("build.gradle.kts");
        Files.write(buildFile.toPath(), Arrays.asList("plugins {",
                "    id(\"eu.rigeldev.kuberig.dsl.generator\") ",
                "}",
                "",
                "repositories {",
                "    jcenter()",
                "}"), StandardCharsets.UTF_8);

        final File swaggerFile = temporaryFolder.newFile("src/main/resources/swagger.json");
        Files.write(swaggerFile.toPath(), sourceSwaggerLines, StandardCharsets.UTF_8);

        /*final File kuberigDslBaseDir = new File(new File(System.getProperty("user.dir")).getParentFile(), "kuberig-dsl-base");
        final File settingsFile = temporaryFolder.newFile("settings.gradle.kts");
        Files.write(settingsFile.toPath(), Arrays.asList(
                "includeBuild(\""+kuberigDslBaseDir.getAbsolutePath()+"\")"
        ), StandardCharsets.UTF_8);*/

        final BuildResult result = GradleRunner.create()
                .withProjectDir(temporaryFolder.getRoot())
                .withArguments("build")
                .withDebug(true)
                .withPluginClasspath()
                .build();

        assertSame(result.task(":build").getOutcome(), TaskOutcome.SUCCESS);
        assertSame(result.task(":generateDslSource").getOutcome(), TaskOutcome.SUCCESS);
    }
}
