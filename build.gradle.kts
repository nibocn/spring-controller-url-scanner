plugins {
    id("java")
    id("org.jetbrains.intellij.platform") version "2.18.1"
}

group = "me.nibo"
version = "0.3.0"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.3")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    intellijPlatform {
        intellijIdeaCommunity("2024.1.7")
        bundledPlugin("com.intellij.java")
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

tasks.test {
    useJUnitPlatform()
}

intellijPlatform {
    pluginConfiguration {
        id = "me.nibo.spring-url-scanner"
        name = "Spring URL Scanner"
        version = project.version.toString()
        description = """
            Spring URL Scanner finds Spring MVC controller endpoints and OpenFeign client mappings in Java projects.

            <p>It scans project sources and dependency JARs, combines class-level and method-level mappings, and presents HTTP method, URL, controller/client type, handler, and source location in an IntelliJ IDEA tool window.</p>

            <p>The scanner runs locally inside the IDE. It does not upload source code, collect telemetry, or contact external services.</p>
        """.trimIndent()
        changeNotes = """
            Expand IntelliJ IDEA compatibility.
            <ul>
                <li>Lower the minimum supported IntelliJ IDEA version to 2024.1.</li>
                <li>Build the plugin against the 2024.1 platform baseline.</li>
                <li>Use a Java 17 bytecode baseline for compatibility with IntelliJ IDEA 2024.1.</li>
            </ul>
        """.trimIndent()

        ideaVersion {
            sinceBuild = "241"
            untilBuild = provider { null }
        }

        vendor {
            name = "NiBo"
            email = "nibocn@gmail.com"
        }
    }

    signing {
        certificateChain = providers.environmentVariable("CERTIFICATE_CHAIN")
        privateKey = providers.environmentVariable("PRIVATE_KEY")
        password = providers.environmentVariable("PRIVATE_KEY_PASSWORD")
    }

    publishing {
        token = providers.environmentVariable("JETBRAINS_PUBLISH_TOKEN")
        channels = listOf("default")
    }
}
