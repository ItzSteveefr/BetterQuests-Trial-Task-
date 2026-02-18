plugins {
    java
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "dev.itzstevee"
version = "1.0.0"

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.8-R0.1-SNAPSHOT")
    compileOnly("me.clip:placeholderapi:2.11.6")
    compileOnly("com.github.ben-manes.caffeine:caffeine:3.1.8")

    implementation("org.mongodb:mongodb-driver-reactivestreams:5.5.1")
    implementation("org.reactivestreams:reactive-streams:1.0.4")
}

tasks {
    shadowJar {
        archiveClassifier.set("")
        relocate("org.mongodb", "dev.itzstevee.quests.libs.mongodb")
        relocate("org.reactivestreams", "dev.itzstevee.quests.libs.reactivestreams")
        relocate("com.mongodb", "dev.itzstevee.quests.libs.com.mongodb")
        relocate("org.bson", "dev.itzstevee.quests.libs.bson")
    }

    build {
        dependsOn(shadowJar)
    }

    compileJava {
        options.encoding = "UTF-8"
        options.release.set(21)
    }

    processResources {
        filteringCharset = "UTF-8"
    }
}
