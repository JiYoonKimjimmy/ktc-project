plugins {
    val kotlinVersion = "1.9.25"
    kotlin("kapt") version kotlinVersion
}

dependencies {
    implementation(project(":common"))

    implementation(kotlin("stdlib-jdk8"))
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-redis-reactive")
    implementation("org.springframework.boot:spring-boot-starter-amqp")

    kapt("org.springframework.boot:spring-boot-configuration-processor")

    implementation("org.apache.commons:commons-pool2:2.12.0")

    // embedded-redis
    implementation("com.github.codemonstur:embedded-redis:1.4.3")
    // rabbitmq-mock
    implementation("com.github.fridujo:rabbitmq-mock:1.2.0")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("com.ninja-squad:springmockk:4.0.2")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")

    // kotest
    val kotestVersion = "5.9.0"
    testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
    testImplementation("io.kotest:kotest-property:$kotestVersion")
    testImplementation("io.kotest.extensions:kotest-extensions-spring:1.3.0")

    // embedded-redis
    testImplementation("com.github.codemonstur:embedded-redis:1.4.3")
    // rabbitmq-mock
    testImplementation("com.github.fridujo:rabbitmq-mock:1.2.0")

    // fixture-monkey
//    val fixtureMonkeyVersion = "1.1.2"
//    testFixturesImplementation("com.navercorp.fixturemonkey:fixture-monkey-starter-kotlin:$fixtureMonkeyVersion")
//    testFixturesImplementation("com.navercorp.fixturemonkey:fixture-monkey-kotest:$fixtureMonkeyVersion")
//    testFixturesImplementation("com.navercorp.fixturemonkey:fixture-monkey-jackson:$fixtureMonkeyVersion")
//    testFixturesImplementation("com.navercorp.fixturemonkey:fixture-monkey-jakarta-validation:$fixtureMonkeyVersion")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}