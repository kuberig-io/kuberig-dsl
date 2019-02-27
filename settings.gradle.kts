rootProject.name = "kuberig-dsl"

include ("kuberig-dsl-generator")
include ("kuberig-dsl-base")

include ("kuberig-dsl-kubernetes:kuberig-dsl-kubernetes-1-14")
include("kuberig-dsl-openshift:kuberig-dsl-openshift-3-6")