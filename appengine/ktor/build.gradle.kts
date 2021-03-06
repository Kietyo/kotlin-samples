plugins {
    application
    kotlin("jvm") version "1.6.0"
    war
    id("com.google.cloud.tools.appengine") version "2.4.2"
    id("com.google.cloud.tools.jib") version "2.7.1"
}

val main_class = "io.ktor.server.netty.EngineMain"

application {
    mainClass.set(main_class)

    applicationDefaultJvmArgs = listOf(
        "-server",
        "-Djava.awt.headless=true",
        "-Xms128m",
        "-Xmx256m",
        "-XX:+UseG1GC",
        "-XX:MaxGCPauseMillis=100"
    )
}

val projectId = project.findProperty("projectId") ?: "ktor-hello-world-test"
val image = "gcr.io/$projectId/ktor-hello-world"

jib {
    to.image = image

    container {
        ports = listOf("8080")
        mainClass = main_class
        // good defauls intended for Java 8 (>= 8u191) containers
        jvmFlags = listOf(
            "-server",
            "-Djava.awt.headless=true",
            "-XX:InitialRAMFraction=2",
            "-XX:MinRAMFraction=2",
            "-XX:MaxRAMFraction=2",
            "-XX:+UseG1GC",
            "-XX:MaxGCPauseMillis=100",
            "-XX:+UseStringDeduplication"
        )
    }
}

val deploy by tasks.registering(Exec::class) {
    commandLine = "gcloud run deploy ktor-hello-world --image $image --project $projectId --platform managed --region us-central1".split(" ")
    dependsOn += tasks.findByName("jib")
}

repositories {
    mavenCentral()
    // kotlinx-html-jvm is not available in mavenCentral yet
    // See https://github.com/Kotlin/kotlinx.html/issues/173
    jcenter {
        content {
            includeModule("org.jetbrains.kotlinx", "kotlinx-html-jvm")
        }
    }
}

require (JavaVersion.current() <= JavaVersion.VERSION_11) {
    "AppEngine supports only Java 8 or 11 for now, the current Java is ${JavaVersion.current()}"
}

dependencies {
    implementation(platform("io.ktor:ktor-bom:1.6.4"))
    implementation("io.ktor:ktor-server-servlet")
    implementation("io.ktor:ktor-html-builder")
    implementation("io.ktor:ktor-server-netty")
    implementation("io.ktor:ktor-websockets:1.6.7")
//    implementation("com.google.cloud:google-cloud-logging-logback:0.117.0-alpha")
    implementation("ch.qos.logback:logback-classic:1.2.3")

    runtimeOnly("com.google.appengine:appengine:1.9.92")
}

appengine {
    deploy {
        projectId = "GCLOUD_CONFIG"
        version = "GCLOUD_CONFIG"
    }
}

tasks {
    withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
    }

    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        kotlinOptions {
            jvmTarget = "1.8"
        }
    }

    register("runn") {
        dependsOn("appengineRun")
    }
}
