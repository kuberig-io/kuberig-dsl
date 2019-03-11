package kinds

import org.junit.jupiter.api.Test


class DslKindsRootTest {



    @Test
    fun tryOut() {
        val dslRoot = DslKindsRoot(YamlOutputSink())

        dslRoot.apply {
            apps{
                v1{
                    deployment("ktrack-simple") {
                        metadata {
                            namespace("ktrack")
                            name("ktrack-simple")
                        }
                        spec {
                            template {
                                metadata {
                                    labels {
                                        label("app", "ktrack-simple")
                                    }
                                }
                                spec {
                                    containers {
                                        container {
                                            name("krack-simple")
                                            image("eu.gcr.io/rigeldev-io/ktrack-simple:0.1.2")
                                            ports {
                                                port {
                                                    name("http")
                                                    containerPort(8080)
                                                }
                                            }
                                            readinessProbe {
                                                httpGet {
                                                    path("/actuator/health")
                                                    port(9090)
                                                }
                                            }
                                            resources {
                                                limits {
                                                    limit("cpu") {
                                                        quantity("100Mi")
                                                    }
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

            v1 {
                service("ktrack-simple") {
                    metadata {
                        namespace("ktrack")
                        name("ktrack-simple")
                    }
                    spec {
                        ports {
                            port {
                                port(80)
                                targetPort(8080)
                                protocol("TCP")
                            }
                        }
                        selector("app", "ktrack-simple")
                    }
                }

                secret("test-secret") {
                    stringData("some-key", "super-secret")
                }
            }
        }
    }
}