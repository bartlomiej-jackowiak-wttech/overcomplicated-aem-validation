import com.cognifide.gradle.aem.common.instance.local.OpenMode
import com.neva.gradle.fork.ForkExtension
import com.cognifide.gradle.common.utils.Patterns

configure<ForkExtension> {
    properties {
        group("Instances") {
            define("instanceType") {
                label = "Type"
                select("local", "remote")
                description = "Local - instance will be created on local file system\nRemote - connecting to remote instance only"
                controller { 
                    toggle(value == "local", "instanceRunModes", "instanceJvmOpts", "localInstance*")
                    toggle(value == "remote", "instanceServiceCredentialsUri")
                    force(value == "local", "http://localhost:4502", "instanceAuthorHttpUrl")
                    force(value == "local", "http://localhost:4503", "instancePublishHttpUrl")
                }
            }
            define("instanceAuthorHttpUrl") {
                label = "Author HTTP URL"
                url("http://localhost:4502")
                optional()
                controller {
                    toggle(Patterns.wildcard(value,"*.adobeaemcloud.com"), "instanceServiceCredentialsUri")
                    toggle(!Patterns.wildcard(value,"*.adobeaemcloud.com"), "instancePassword")
                    clear(!Patterns.wildcard(value,"*.adobeaemcloud.com"), "instanceServiceCredentialsUri")
                }
            }
            define("instanceAuthorEnabled") {
                label = "Author Enabled"
                checkbox(true)
            }
            define("instancePublishHttpUrl") {
                label = "Publish HTTP URL"
                url("http://localhost:4503")
                optional()
            }
            define("instancePublishEnabled") {
                label = "Publish Enabled"
                checkbox(true)
            }
            define("instancePassword") {
                label = "Password"
                password("admin")
                optional()
            }
            define("instanceServiceCredentialsUri") {
                label = "Service Credentials Uri"
                description = "JSON file downloaded from AEMaaCS developer console"
                optional()
            }
            define("localInstanceRunModes") {
                label = "Run Modes"
                optional()
            }
            define("localInstanceQuickstartDistUri") {
                label = "AEM distribution URI"
                description = "Typically AEM SDK zip file or AEM jar file"
            }
            define("localInstanceQuickstartLicenseUri") {
                label = "Quickstart License URI"
                description = "Typically file named 'license.properties'"
            }
            define("localInstanceSpUri") {
                label = "Service Pack URI"
                description = "Only for on-prem AEM instances. Typically file named 'aem-service-pkg-*.zip'"
                optional()
            }
            define("localInstanceCoreComponentsUri") {
                label = "Core Components package URI"
                description = "Only for on-prem AEM instances. Typically file named 'core.wcm.components.all-*.zip'"
                optional()
            }
            define("localInstanceOpenMode") {
                label = "Open Automatically"
                description = "Open web browser when instances are up."
                select(OpenMode.values().map { it.name.toLowerCase() }, OpenMode.ALWAYS.name.toLowerCase())
            }
        }
        group("Build") {
            define("mvnBuildProfiles") {
                label = "Maven Profiles"
                text("fedDev")
                description = "Comma delimited"
                optional()
            }
            define("mvnBuildArgs") {
                label = "Maven Args"
                description = "Added extra"
                optional()
            }
            define("packageDeployAvoidance") {
                label = "Deploy Avoidance"
                description = "Avoids uploading and installing package if identical is already deployed on instance."
                checkbox(true)
            }
            define("packageDamAssetToggle") {
                label = "Deploy Without DAM Worklows"
                description = "Turns on/off temporary disablement of assets processing for package deployment time.\n" +
                        "Useful to avoid redundant rendition generation when package contains renditions synchronized earlier."
                checkbox(true)
                dynamic("props")
            }
        }
        group("Deploy") {
            define("packageDeployAvoidance") {
                label = "Avoidance"
                description = "Avoids uploading and installing package if identical is already deployed on instance."
                checkbox(true)
            }
            define("packageDamAssetToggle") {
                label = "Toggle DAM Worklows"
                description = "Turns on/off temporary disablement of assets processing for package deployment time.\n" +
                        "Useful to avoid redundant rendition generation when package contains renditions synchronized earlier."
                checkbox(true)
                dynamic("props")
            }
        }
        group("Authorization") {
            define("companyUser") {
                label = "User"
                text(System.getProperty("user.name").orEmpty())
                description = "For resolving AEM files from authorized URL"
                optional()
            }
            define("companyPassword") {
                label = "Password"
                optional()
            }
            define("companyDomain") {
                label = "Domain"
                text(System.getenv("USERDOMAIN").orEmpty())
                description = "For files resolved using SMB"
                optional()
            }
        }
    }
}