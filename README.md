# kuberig-dsl
KubeRig DSL generation.

Generates a Kotlin DSL based on the swagger api spec of Kubernetes and OpenShift.

## Examples

### Kubernetes

```kotlin
dslRoot.apps.v1.deployment("nginx") {
    metadata {
        name = "nginx"
    }
    spec {
        template {
            metadata {
                name = "nginx"
            }
            spec {
                containers {
                    container {
                        image = "nginx"
                        ports {
                            port {
                                containerPort = 80
                            }
                        }
                    }
                }
            }
        }
        replicas = 1
    }
}
```

### OpenShift

```kotlin
dslRoot.v1.deploymentConfig("nginx") {
    metadata {
        name = "nginx"
    }
    spec {
        template {
            metadata {
                name = "nginx"
            }
            spec {
                containers {
                    container {
                        image = "nginx"
                        ports {
                            port {
                                containerPort = 80
                            }
                        }
                    }
                }
            }
        }
        replicas = 1
    }
}
```