plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.0.0-Beta4"
    id("org.jetbrains.intellij.platform") version "2.3.0"
}

group = "com.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin.html
dependencies {
    intellijPlatform {
        // Default configuration for 2025
        create("PY", "2025.1.1", false)
        bundledPlugin("PythonCore")
    }
}

intellijPlatform {
    pluginConfiguration {
        ideaVersion {
            sinceBuild = "251"
        }

        changeNotes = """
      Plugin for PyCharm 2025.1+
    """.trimIndent()
    }
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "21"
        targetCompatibility = "21"
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "21"
    }
    
    // Disable searchable options
    named("buildSearchableOptions") {
        enabled = false
    }
    
    // Also disable jar searchable options
    named("prepareJarSearchableOptions") {
        enabled = false
    }
}

// Task to build plugin for PyCharm 2024
tasks.register("buildPlugin2024") {
    group = "intellij"
    description = "Build plugin for PyCharm 2024"
    
    doFirst {
        // Switch to Kotlin 1.9.x and Java 17 for 2024 build
        plugins.withId("org.jetbrains.kotlin.jvm") {
            kotlin.jvmToolchain(17)
        }
        
        // Update plugin configuration
        intellijPlatform {
            pluginConfiguration {
                ideaVersion {
                    sinceBuild = "241"
                }
            }
            dependencies {
                intellijPlatform {
                    create("PY", "2024.2.5", false)
                    bundledPlugin("PythonCore")
                }
            }
        }
    }
    
    // Build the plugin
    finalizedBy(tasks.named("buildPlugin"))
}

// Task to build both versions
tasks.register("buildAllVersions") {
    group = "intellij"
    description = "Build plugin for both PyCharm 2024 and 2025"
    dependsOn("buildPlugin", "buildPlugin2024")
}
