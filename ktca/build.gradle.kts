import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

buildscript {
    dependencies {
        classpath("org.openapitools:openapi-generator-gradle-plugin:6.6.0")
    }
}

plugins {
    id("org.openapi.generator") version "6.6.0"
}

repositories {
    mavenCentral()
}

dependencies {
    // Module-specific dependencies
    implementation(project(":common"))

    // Kotlin
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    // Spring
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-undertow")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-data-redis-reactive")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    developmentOnly("org.springframework.boot:spring-boot-devtools")

    // Swagger
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.7.0") //for spring 3.4.x
    implementation("javax.servlet:javax.servlet-api:4.0.1") //for compiling generated source

    // Generator
    implementation("org.openapitools:openapi-generator:7.12.0")
    implementation("org.openapitools:openapi-generator-gradle-plugin:7.12.0")

    // ys
//    implementation("com.kona.ys:ys-library:2.2.33")

    //ksl - for common lib
//    implementation("com.konasl.commonlibs:spring-web:7.0.2") {
//        exclude(group = "org.springframework.boot", module = "spring-boot-starter-amqp") //why automatic rabbit?
//    }
//    implementation("com.konasl.commonlibs:logger:7.0.1")
    implementation(fileTree(rootProject.projectDir.resolve("libs")).matching {
        include("*.jar")
    })
//    implementation("commons-io:commons-io:2.4")
//    implementation("com.konasl:kona-iso8583:1.0.5") //for ppSender
//    implementation("org.apache.httpcomponents.client5:httpclient5:5.4")

//    implementation("com.konasl.commonlibs:spring-web:3.6.4")
//    implementation("org.apache.httpcomponents:httpclient:4.5.14")
//    implementation("org.springframework.retry:spring-retry:2.0.4")

    // JPA QueryDSL
    implementation("com.querydsl:querydsl-jpa:5.0.0")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

sourceSets {
    main {
        java {
            setSrcDirs(listOf("src/main/kotlin", "generated/src/main/kotlin"))
        }
        resources {
            setSrcDirs(listOf("src/main/resources"))
        }
    }
}

configurations.all {
    exclude(group = "org.slf4j", module = "slf4j-simple") //logback 과 slf4j 가 겹쳐서 에러발생...
    exclude(group = "org.springframework.boot", module = "spring-boot-starter-web")
}

tasks.register<GenerateTask>("generateFromYaml") {
    inputSpec.set("$projectDir/src/main/resources/specs/${project.name}.yaml")
    outputDir.set("$projectDir/generated")
    configFile.set("$projectDir/src/main/resources/specs/spec.json")
    generatorName.set("kotlin-spring")
    configOptions.set(
        mapOf(
            "dateLibrary" to "java8",
            "useSpringBoot3" to "true",
            "springBootVersion" to "3.4.4"
        )
    )
    group = "1.action"
}

tasks.register("patchGeneratedGradle") {
    doLast {
        val gradleFile = file("$projectDir/generated/build.gradle.kts")
        if (gradleFile.exists()) {
            gradleFile.writeText(
                gradleFile.readText()
                    .replace("val kotlinVersion = \"1.7.10\"", "val kotlinVersion = \"1.9.25\"")
                    .replace("id(\"org.springframework.boot\") version \"3.0.2\"", "id(\"org.springframework.boot\") version \"3.4.4\"")
                    .replace("kotlinOptions.jvmTarget = \"17\"", "kotlinOptions.jvmTarget = \"21\"")
                    .replace(
                        "java.sourceCompatibility = JavaVersion.VERSION_17",
                        "java.sourceCompatibility = JavaVersion.VERSION_21\njava.targetCompatibility = JavaVersion.VERSION_21"
                    )
            )
        }

        val filaPath = "$projectDir/generated/src/main/kotlin/com/kona/ktca"
        val sourceFileName = "Application"
        val targetFileName = "KtcaApplication"
        val sourceFile = file("$filaPath/$sourceFileName.kt")
        val targetFile = file("$filaPath/$targetFileName.kt")

        if (sourceFile.exists()) {
            val content = sourceFile.readText()
                .replace("class $sourceFileName", "class $targetFileName")
                .replace("<$sourceFileName>", "<$targetFileName>")
            targetFile.writeText(content)
            sourceFile.delete()
        }
    }
}

tasks.named("generateFromYaml") {
    finalizedBy("patchGeneratedGradle")
}

tasks.named("test").configure { group = "1.action" }
tasks.named("build").configure { group = "1.action" }
tasks.named("clean").configure { group = "1.action" }
tasks.named("check").configure { enabled = false }
tasks.named("bootRun").configure { group = "1.action" }
tasks.named("compileKotlin").configure { dependsOn(tasks.named("generateFromYaml")) }

tasks.register<Delete>("removeGenerateFromYaml", fun Delete.() {
    group = "1.action"
    delete(
        fileTree("$projectDir/generated/src/main/kotlin/com/kona/${project.name}/dto"),
        fileTree("$projectDir/generated/src/main/kotlin/com/kona/${project.name}/api")
    )
})

tasks.named("generateFromYaml").configure {
    dependsOn("removeGenerateFromYaml")
}

tasks.named("clean").configure {
    dependsOn("removeGenerateFromYaml")
}