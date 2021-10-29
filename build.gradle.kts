plugins {
    java
}

sourceSets {
    create("modern") {
        java {
            srcDir("src/modern/java")
            compileClasspath += main.get().output
            runtimeClasspath += main.get().output
        }
    }
}

val modernCompileOnly: Configuration by configurations

repositories {
    mavenCentral()
    maven("https://repo.codemc.io/repository/nms/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    mavenLocal()
}

dependencies {
    val spigot = "org.spigotmc:spigot:1.8.8-R0.1-SNAPSHOT";
    val annotations = "org.jetbrains:annotations:21.0.0";

    modernCompileOnly("io.papermc.paper:paper:1.17-R0.1-SNAPSHOT")

    // Required libraries
    compileOnly(spigot)
    compileOnly(annotations)

    // You must run the deps.sh script to have this dependency
    compileOnly("me.fixeddev:EzChat:2.5.0")

    // Optional plugin hooks
    compileOnly("me.clip:placeholderapi:2.10.10")

    // Testing
    testImplementation(spigot)
    testImplementation(annotations)
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

tasks {
    test {
        useJUnitPlatform()
    }

    java {
        toolchain {
            // use java 16 by default
            languageVersion.set(JavaLanguageVersion.of(16))
        }
    }

    register<Jar>("jar16") {
        description = "Builds this project using Java 16, supporting Paper 1.17+ and Minestom"
        archiveClassifier.set("all-java16")
        from(
            project.sourceSets.main.get().output,
            project.sourceSets["modern"].output
        )

        java {
            toolchain {
                // use java 16 in this case
                languageVersion.set(JavaLanguageVersion.of(16))
            }
        }
    }

    register<Jar>("jar8") {
        description = "Builds this project using Java 8, doesn't support Paper 1.17+ or Minestom"
        archiveClassifier.set("all-java8")
        from(project.sourceSets.main.get().output)

        java {
            toolchain {
                // use java 8 in this case
                languageVersion.set(JavaLanguageVersion.of(8))
            }
        }
    }

    processResources {
        filesMatching("**/*.yml") {
            filter<org.apache.tools.ant.filters.ReplaceTokens>(
                "tokens" to mapOf("version" to project.version)
            )
        }
    }
}
