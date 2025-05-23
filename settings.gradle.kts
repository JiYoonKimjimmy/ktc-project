rootProject.name = "ktc-project"

include("ktc", "ktca", "ktcw", "common")

pluginManagement {
    repositories {
        mavenCentral()
        maven { url = uri("https://plugins.gradle.org/m2/") }
    }
}