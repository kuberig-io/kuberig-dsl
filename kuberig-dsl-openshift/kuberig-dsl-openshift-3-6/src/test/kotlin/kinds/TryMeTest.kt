package kinds

import org.junit.jupiter.api.Test

class TryMeTest {

    @Test
    fun tryMe() {
        val dslRoot = DslKindsRoot(YamlOutputSink())

        dslRoot.apply {
            v1 {
                deploymentConfig("nginx") {
                    metadata {
                        name("nginx")
                    }
                    spec {
                        template {
                            metadata {
                                name("nginx")
                            }
                            spec {
                                containers {
                                    container {
                                        image("nginx")
                                        ports {
                                            port {
                                                containerPort(80)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        replicas(1)
                    }
                }
            }
        }
    }

}