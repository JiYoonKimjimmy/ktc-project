import org.springframework.boot.gradle.tasks.bundling.BootJar
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

val group = "com.kona"
val testCoverageRatio = "0.00".toBigDecimal()

repositories {
	mavenCentral()
}

plugins {
	val kotlinVersion = "1.9.25"
	val springBootVersion = "3.4.4"

	kotlin("jvm") version kotlinVersion
	kotlin("kapt") version kotlinVersion
	kotlin("plugin.spring") version kotlinVersion
	kotlin("plugin.jpa") version kotlinVersion

	id("org.springframework.boot") version springBootVersion
	id("io.spring.dependency-management") version "1.1.7"
	id("com.gorylenko.gradle-git-properties") version "2.4.2"

	jacoco
}

subprojects {
	apply(plugin = "java")
	apply(plugin = "kotlin")
	apply(plugin = "kotlin-spring")
	apply(plugin = "org.springframework.boot")
	apply(plugin = "io.spring.dependency-management")
	apply(plugin = "com.gorylenko.gradle-git-properties")
	apply(plugin = "jacoco")

	group = "com.kona"

	repositories {
		mavenCentral()
	}

	java {
		toolchain {
			languageVersion = JavaLanguageVersion.of(21)
		}
	}

	kotlin {
		compilerOptions {
			freeCompilerArgs.addAll("-Xjsr305=strict")
		}
	}

	gitProperties {
		if (project.name != "common") {
			val primary = "${project.property("version.primary")}"
			val major = "${project.property("version.major")}"
			val minor = "${project.property("version.minor")}"

			val buildVersion = listOf(primary, major, minor).joinToString(".")
			val buildTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

			println("=====================================")
			println("Build Project  : ${project.name}")
			println("Build Version  : $buildVersion")
			println("Build DateTime : $buildTime")
			println("=====================================")

			customProperty("git.build.version", buildVersion)
			customProperty("git.build.time", buildTime)
		}
	}

	tasks.withType<Test> {
		useJUnitPlatform()
		finalizedBy(tasks.jacocoTestReport)
	}

	tasks.jacocoTestReport {
		reports {
			html.required = true
			xml.required = false
			csv.required = false
			html.outputLocation = layout.buildDirectory.dir("jacocoHtml")
		}

		classDirectories.setFrom(
			files(classDirectories.files.map {
				fileTree(it) {
					exclude("**/util/**")
					exclude("**/generated/**")
					exclude("**/api/**")
					exclude("**/dto/**")
					exclude("**/Q*.class")
				}
			})
		)

		finalizedBy("jacocoTestCoverageVerification")
	}

	tasks.jacocoTestCoverageVerification {
		violationRules {
			rule {
				enabled = false
				limit {
					minimum = "0.10".toBigDecimal()
				}
			}
		}
	}

}

tasks.bootJar {
	enabled = false
}

project("common") {
	// bootJar task disabled 처리
	val jar: Jar by tasks
	val bootJar: BootJar by tasks
	jar.enabled = true
	bootJar.enabled = false
}

project("ktc") {
	val bootJar: BootJar by tasks
	bootJar.enabled = true
	bootJar.archiveFileName.set("ktc.jar")
}

project("ktca") {
	val bootJar: BootJar by tasks
	bootJar.enabled = true
	bootJar.archiveFileName.set("ktca.jar")
}