plugins {
    kotlin("jvm") version "2.1.21"
}

group = "dev.alllexey"
version = "1.0.0b"

repositories {
    mavenCentral()
}

dependencies {
    implementation("dev.alllexey:my-itmo-api:1.3.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}