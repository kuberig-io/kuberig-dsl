rootProject.name = "kuberig"

pluginManagement {
    includeBuild("build-logic")
}

include (":kuberig-dsl:kuberig-dsl-generator")
include (":kuberig-dsl:kuberig-dsl-base")
include (":kuberig-dsl:kuberig-dsl-generator-gradle-plugin")


buildCache {
    remote<HttpBuildCache> {
        url = uri("http://build-cache.rigel.dev:5071/cache/")
    }
}
