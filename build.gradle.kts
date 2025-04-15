import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.tasks.bundling.BootJar

val group = "com.kona"
val testCoverageRatio = "0.00".toBigDecimal()

repositories {
	mavenCentral()
}

plugins {
	kotlin("jvm") version "1.9.25" apply false
	kotlin("plugin.spring") version "1.9.25" apply false
	id("org.springframework.boot") version "3.4.4" apply false
	id("io.spring.dependency-management") version "1.1.7" apply false
	id("com.gorylenko.gradle-git-properties") version "2.4.1" apply false
	`java-test-fixtures`
}

allprojects {
	group = "com.kona"

	repositories {
		mavenCentral()
	}
}

subprojects {
	apply(plugin = "org.jetbrains.kotlin.jvm")
	apply(plugin = "org.jetbrains.kotlin.plugin.spring")
	apply(plugin = "org.springframework.boot")
	apply(plugin = "io.spring.dependency-management")
	apply(plugin = "com.gorylenko.gradle-git-properties")
	apply(plugin = "java")
	apply(plugin = "java-test-fixtures")

	configure<JavaPluginExtension> {
		sourceCompatibility = JavaVersion.VERSION_21
		targetCompatibility = JavaVersion.VERSION_21
	}

	val implementation by configurations
	val developmentOnly by configurations
	val annotationProcessor by configurations
	val testImplementation by configurations
	val testRuntimeOnly by configurations

	dependencies {
		implementation("org.jetbrains.kotlin:kotlin-reflect")
		implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

		implementation("org.jacoco:org.jacoco.core:0.8.11")
		implementation("org.jacoco:org.jacoco.ant:0.8.11")

		developmentOnly("org.springframework.boot:spring-boot-devtools")
		annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
	}

	tasks.withType<KotlinCompile> {
		kotlinOptions {
			freeCompilerArgs = listOf("-Xjsr305=strict")
			jvmTarget = "21"
		}
	}

	tasks.withType<Test> {
		useJUnitPlatform()
	}

	// when generate .jar -> locate at each module /build/libs
	tasks {
		withType<Jar> {
			enabled = false
		}

		withType<BootJar> {
			enabled = true
			archiveFileName.set("${project.name}.jar")
			val targetDir = "${projectDir}/build/libs"
			destinationDirectory.set(project.rootProject.file(targetDir))
		}
	}

	// jacoco generate report, verification
	plugins.withId("java") {
		apply(plugin = "jacoco")

		tasks {
			withType<Test> {
				finalizedBy("jacocoTestReport", "jacocoTestCoverageVerification")
			}
			named<JacocoReport>("jacocoTestReport") {
				group = "verification"
				description = "Generates code coverage reports."
				dependsOn("test")
				reports {
					html.required.set(true)
					xml.required.set(true)

					xml.outputLocation.set(file("$projectDir/report/jacoco-${project.name}/jacoco.xml"))
					html.outputLocation.set(file("$projectDir/report/jacoco-${project.name}"))
				}
				classDirectories.setFrom(files(classDirectories.files.map {
					fileTree(it) {
						exclude("**/util/**")
					}
				}))
			}
			named<JacocoCoverageVerification>("jacocoTestCoverageVerification") {
				group = "verification"
				description = "Generates code coverage verification."
				dependsOn("test")
				violationRules {
					rule {
						limit {
							minimum = testCoverageRatio
						}
					}
				}
			}
		}
	}
}