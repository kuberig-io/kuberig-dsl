[![pipeline status](https://gitlab.com/kuberig/kuberig-dsl/badges/master/pipeline.svg)](https://gitlab.com/kuberig/kuberig-dsl/pipelines)

# kuberig-dsl

KubeRig DSL generation.

Generates a Kotlin DSL based on the swagger api spec of Kubernetes and OpenShift.

## Examples

### Kubernetes

```kotlin
dslRoot.apps.v1.deployment("nginx") {
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
```

### OpenShift

```kotlin
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
```

## Something more complex

```kotlin
val name = "my-spring-boot-service"

val metadata = objectMeta {
    namespace("my-spring-boot-service")
    name(name)
}

val podLabel = Pair("app", name)
val containerPort = 8080

val springBootActuatorHealth = probe {
    httpGet {
        path("/actuator/health")
        port(9090)
    }
}

val springBootResources = resourceRequirements {
    requests {
        request("cpu") {
            quantity("500m")
        }
        request("memory") {
            quantity("250Mi")
        }
    }
    limits {
        limit("cpu") {
            quantity("1000m")
        }
        limit("memory") {
            quantity("500Mi")
        }
    }
}

dslRoot.apply {
    apps{
        v1{
            deployment(name) {
                metadata(metadata)
                spec {
                    template {
                        metadata {
                            labels {
                                label(podLabel)
                            }
                        }
                        spec {
                            containers {
                                container {
                                    name(name)
                                    image("my-spring-boot-service:0.1.0")
                                    ports {
                                        port {
                                            name("http")
                                            containerPort(containerPort)
                                        }
                                    }
                                    readinessProbe(springBootActuatorHealth)
                                    resources(springBootResources)
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
        service(name) {
            metadata(metadata)
            spec {
                ports {
                    port {
                        port(80)
                        targetPort(containerPort)
                        protocol("TCP")
                    }
                }
                selector(podLabel)
            }
        }

        secret(name) {
            metadata(metadata)
            stringData("some-key", "super-secret")
        }
    }
}
```
