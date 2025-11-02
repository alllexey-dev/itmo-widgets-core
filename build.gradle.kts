plugins {
    id("com.vanniktech.maven.publish") version "0.34.0"
    kotlin("jvm") version "2.1.21"
}

group = "dev.alllexey"
version = "1.0.4"

repositories {
    mavenCentral()
}

mavenPublishing {
    publishToMavenCentral()

    signAllPublications()
}

mavenPublishing {
    coordinates(group as String, rootProject.name, version as String)

    pom {
        name.set("itmo-widgets-core")
        description.set("A component of ITMO.Widgets applications.")
        inceptionYear.set("2025")
        url.set("https://github.com/alllexey-dev/itmo-widgets-core")
        licenses {
            license {
                name.set("MIT License")
                url.set("https://www.opensource.org/licenses/mit-license.php")
                distribution.set("https://www.opensource.org/licenses/mit-license.php")
            }
        }
        developers {
            developer {
                id.set("alllexey")
                name.set("Aleksey Makarov")
                url.set("https://github.com/alllexey-dev")
            }
        }
        scm {
            url.set("https://github.com/alllexey-dev/itmo-widgets-core")
            connection.set("scm:git:git://github.com/alllexey-dev/itmo-widgets-core.git")
            developerConnection.set("scm:git:ssh://git@github.com/alllexey-dev/itmo-widgets-core.git")
        }
    }
}

dependencies {
    api("dev.alllexey:my-itmo-api:1.3.6")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(11)
}