plugins {
    java
}

group = "com.myportal"
version = "1.0"

repositories {
    mavenCentral()
    maven("https://papermc.io")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21-R0.1-SNAPSHOT")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(21)
}
