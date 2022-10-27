plugins {
    id("com.cognifide.aem.instance.local")
    id("com.cognifide.environment")
}

val instancePassword = common.prop.string("instance.default.password")
val publishHttpUrl = common.prop.string("publish.httpUrl") ?: aem.findInstance("local-publish")?.httpUrl?.orNull ?: "http://127.0.0.1:4503"
val dispatcherHttpUrl = common.prop.string("dispatcher.httpUrl") ?: "http://127.0.0.1:80"
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
            configureReplicationAgentPublish("flush") {
                agent { configure(transportUri = "$dispatcherHttpUrl/dispatcher/invalidate.cache") }
                version.set(dispatcherHttpUrl)
            }
        }
    }
}

environment { // https://github.com/Cognifide/gradle-environment-plugin
    docker {
        containers {
            "dispatcher" {
                load("dispatcherImage") { aem.localInstanceManager.dispatcherImage }
                resolve { listOf("cache", "logs").forEach { ensureDir(aem.localInstanceManager.dispatcherDir.resolve(it)) } }
                reload { cleanDir("/mnt/var/www/html") }
                dev { watchRootDir("app/aem/maven/dispatcher/src") }
            }
        }
    }
    hosts {
        "http://publish.aem.local" { tag("publish") }
    }
    healthChecks {
        aem.findInstance("local-author")?.let { instance ->
            http("Author Sites Editor", "${instance.httpUrl}/sites.html") {
                containsText("Sites")
                options { basicCredentials = instance.credentials }
            }
            http("Author Replication Agent - Publish", "${instance.httpUrl}/etc/replication/agents.author/publish.test.html") {
                containsText("succeeded")
                options { basicCredentials = instance.credentials }
            }
        }
        aem.findInstance("local-publish")?.let { instance ->
            http("Publish Replication Agent - Flush", "${instance.httpUrl}/etc/replication/agents.publish/flush.test.html") {
                containsText("succeeded")
                options { basicCredentials = instance.credentials }
            }
            /*
            http("Site Home", "http://publish.aem.local/us/en.html") {
                containsText("My Site")
            }
            */
        }
    }
}

tasks {
    instanceSetup { if (rootProject.aem.mvnBuild.available) dependsOn(":all:deploy") }
    instanceResolve { dependsOn(":requireProps") }
    instanceCreate { dependsOn(":requireProps") }
    environmentUp { mustRunAfter(instanceAwait, instanceUp, instanceProvision, instanceSetup) }
    environmentAwait { mustRunAfter(instanceAwait, instanceUp, instanceProvision, instanceSetup) }
}