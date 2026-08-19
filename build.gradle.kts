plugins {
    id("java")
    id("org.jetbrains.intellij.platform") version "2.18.1"
}

group = "me.nibo"
version = "0.1.0"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
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
            Initial Marketplace-ready release.
            <ul>
                <li>Scan Spring MVC controller mappings from project sources and libraries.</li>
                <li>Optionally include OpenFeign client interfaces.</li>
                <li>Filter discovered endpoints and navigate to source or decompiled classes.</li>
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
        token = providers.environmentVariable("PUBLISH_TOKEN")
        channels = listOf("default")
    }
}
