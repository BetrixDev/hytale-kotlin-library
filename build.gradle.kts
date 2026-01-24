import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm") version "2.2.20"
    `java-library`
    `maven-publish`
}

group = property("maven_group") as String
version = property("version") as String

val hytaleHome: String by lazy {
    if (project.hasProperty("hytale_home")) {
        project.property("hytale_home") as String
    } else {
        val os = System.getProperty("os.name").lowercase()
        val userHome = System.getProperty("user.home")
        when {
            os.contains("win") -> "$userHome/AppData/Roaming/Hytale"
            os.contains("mac") -> "$userHome/Library/Application Support/Hytale"
            else -> {
                val flatpakPath = "$userHome/.var/app/com.hypixel.HytaleLauncher/data/Hytale"
                if (file(flatpakPath).exists()) flatpakPath
                else "$userHome/.local/share/Hytale"
            }
        }
    }
}

val patchline: String = property("patchline") as String

repositories {
    mavenCentral()
}

dependencies {
    // Hytale server as compile-only dependency (provided at runtime)
    compileOnly(files("$hytaleHome/install/$patchline/package/game/latest/Server/HytaleServer.jar"))
    
    // Kotlin standard library
    implementation(kotlin("stdlib"))
    
    // Kotlin coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(property("java_version") as String))
    }
    withSourcesJar()
    withJavadocJar()
}

kotlin {
    jvmToolchain((property("java_version") as String).toInt())
    
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
        freeCompilerArgs.addAll(
            "-Xjvm-default=all",      // Generate default methods in interfaces
            "-Xexplicit-api=strict"   // Require explicit visibility modifiers
        )
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

tasks.jar {
    manifest {
        attributes(
            "Implementation-Title" to project.name,
            "Implementation-Version" to project.version
        )
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            
            pom {
                name.set("Hytale Kotlin Library")
                description.set("Kotlin extensions and DSLs for Hytale server plugin development")
            }
        }
    }
}
