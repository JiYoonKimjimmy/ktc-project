import java.text.SimpleDateFormat
import java.util.*

val phaseVersion = "4.81.0.00"
gitProperties {
    customProperty("git.build.time", SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date()))
    version = phaseVersion
}

dependencies {
    // Module-specific dependencies
    implementation(project(":common"))

    // Redis dependency (specific to ktc module)
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
}