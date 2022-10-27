plugins {
    id("com.cognifide.aem.common")
    id("com.neva.fork")
}

apply(from = "gradle/fork/props.gradle.kts")
if (file("gradle.user.properties").exists()) {
    if (aem.mvnBuild.available) defaultTasks(":env:setup")
    else defaultTasks(":env:instanceSetup")
}

allprojects {
    repositories {
        mavenCentral()
    }
}

aem {
    mvnBuild {
        depGraph {
            // softRedundantModule("ui.content" to "ui.apps")
        }
        discover()
    }
}