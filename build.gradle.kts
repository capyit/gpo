val ktorVersion: String by project
val kotlinVersion: String by project
val logbackVersion: String by project
val exposedVersion: String by project
val postgresVersion: String by project

kotlin {
    sourceSets.all {
        languageSettings {
            languageVersion = "2.0"
        }
    }
}

plugins {
    kotlin("jvm") version "1.9.20"
    id("io.ktor.plugin") version "2.3.6"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.20"
    id("com.bmuschko.docker-java-application") version "9.4.0"
}

ktor {
    docker {
        jreVersion.set(JavaVersion.VERSION_21)
    }
}

group = "de.uni_mainz.fs_physik_meteo.mk"
version = "0.0.1"

application {
    mainClass.set("ApplicationKt")
    val isDevelopment = true
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

docker {
    javaApplication {
        baseImage.set("openjdk:11")
        maintainer.set("Filipe Ramalho 'fdesousa@students.uni-mainz.de'")
        ports.set(listOf(8080))
    }
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlinVersion")
    implementation("org.kotlincrypto:secure-random:0.1.0")
    implementation("org.mindrot:jbcrypt:0.4")

    implementation("org.postgresql:postgresql:$postgresVersion")

    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-crypt:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-kotlin-datetime:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-json:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-money:$exposedVersion")

    implementation("io.ktor:ktor-server-core-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-auth-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-http-redirect-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-netty-jvm:$ktorVersion")
    testImplementation("io.ktor:ktor-server-tests-jvm:$ktorVersion")
    implementation("ch.qos.logback:logback-classic:$logbackVersion")
}