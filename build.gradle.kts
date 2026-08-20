plugins {
    id("java")
    id("org.jetbrains.intellij.platform") version "2.18.1"
}

group = "me.nibo"
version = "0.2.0"

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
        intellijIdeaCommunity("2024.2.5")
        bundledPlugin("com.intellij.java")
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
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
            Add endpoint copy support.
            <ul>
                <li>Copy the selected endpoint cell with the standard copy shortcut.</li>
                <li>Copy the current cell or the whole endpoint row from the context menu.</li>
                <li>Show scan progress in the tool window and export all scanned results to CSV.</li>
                <li>Read the Marketplace publishing token from the <code>JETBRAINS_PUBLISH_TOKEN</code> environment variable.</li>
            </ul>
        """.trimIndent()

        ideaVersion {
            sinceBuild = "242"
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
