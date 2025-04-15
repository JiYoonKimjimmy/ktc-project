import org.springframework.boot.gradle.tasks.bundling.BootJar

dependencies {

}

// just reference module
tasks {
    withType<BootJar> {
        enabled = false
    }

    withType<Jar> {
        enabled = true
    }
}