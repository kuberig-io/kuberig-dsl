package kinds

import eu.rigeldev.kuberig.dsl.DslType
import eu.rigeldev.kuberig.dsl.processing.DslTypePostProcessor
import eu.rigeldev.kuberig.dsl.processing.DslProcessingContext
import eu.rigeldev.kuberig.dsl.processing.DslTypePreProcessor
import io.k8s.api.core.v1.ContainerDsl
import kinds.apps.v1.deployment
import org.junit.jupiter.api.Test

class TryMeTest {



    @Test
    fun tryMe() {
        DslProcessingContext.init()
        DslProcessingContext.registerDslTypePreProcessor(object: DslTypePreProcessor {
            override fun preProcess(dslType: DslType<Any>) {

                val stack = DslProcessingContext.dslTypeStack()

                var indent = ""
                stack.forEach {
                    indent = "$indent\t|"
                }

                println("$indent> " + dslType::class.java.name)
            }
        })

        DslProcessingContext.registerDslTypePostProcessor(object: DslTypePostProcessor {
            override fun postProcess(dslType: DslType<Any>) {
                val stack = DslProcessingContext.dslTypeStack()

                var indent = ""
                stack.forEach {
                    indent = "$indent\t|"
                }

                val dslType = stack.get(stack.size-1)

                if (dslType is ContainerDsl) {
                    val resources = dslType.getResources()

                    if (!resources.getLimits().toValue().containsKey("memory")) {
                        // TODO need stack for additional information
                    }
                }

                println("$indent< " + dslType::class.java.name)
            }
        })

        deployment {
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
                replicas(20)
            }
        }


        DslProcessingContext.process()
    }

}