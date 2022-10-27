plugins {
    id("com.cognifide.aem.instance.local")
}

val instancePassword = common.prop.string("instance.default.password")
val publishHttpUrl = common.prop.string("publish.httpUrl") ?: aem.findInstance("local-publish")?.httpUrl?.orNull ?: "http://127.0.0.1:4503"
val servicePackUrl = common.prop.string("localInstance.spUrl")
val coreComponentsUrl = common.prop.string("localInstance.coreComponentsUrl")

aem {
    instance { // https://github.com/Cognifide/gradle-aem-plugin/blob/main/docs/instance-plugin.md
        provisioner {
            enableCrxDe()
            servicePackUrl?.let { deployPackage(it) }
            coreComponentsUrl?.let { deployPackage(it) }
            configureReplicationAgentAuthor("publish") {
                agent { configure(transportUri = "$publishHttpUrl/bin/receive?sling:authRequestLogin=1", transportUser = "admin", transportPassword = instancePassword, userId = "admin") }
                version.set(publishHttpUrl)
            }
        }
    }
}

tasks {
    instanceSetup { if (rootProject.aem.mvnBuild.available) dependsOn(":all:deploy") }
    instanceResolve { dependsOn(":requireProps") }
    instanceCreate { dependsOn(":requireProps") }
}
