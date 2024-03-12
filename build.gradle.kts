@file:Suppress("UnstableApiUsage")

import com.google.protobuf.gradle.id
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

// define the gav coordinates of this project
group = "net.scrayos"
version = "1.0.0-SNAPSHOT"
description = "Xenos Client (Kotlin/Java)"

// hook the plugins for the builds
plugins {
    `java-library`
    `maven-publish`
    idea
    alias(libs.plugins.kotlin)
    alias(libs.plugins.dokka)
    alias(libs.plugins.kover)
    alias(libs.plugins.protobuf)
    alias(libs.plugins.sonarqube)
    alias(libs.plugins.pitest)
    alias(libs.plugins.ktlint)
}

// configure the repositories for the dependencies
repositories {
    // official maven repository
    mavenCentral()
}

// declare all dependencies (for compilation and runtime)
dependencies {
    // add gRPC dependencies that are necessary for compilation and execution
    implementation(libs.bundles.grpc)

    // add coroutines for our coroutine based communication
    implementation(libs.kotlin.coroutines.core)

    // compile against the slf4j API for logging
    compileOnly(libs.slf4j)

    // specify test dependencies
    testImplementation(libs.kotlin.test)
    testImplementation(libs.bundles.kotest)
    testImplementation(libs.mockk)
    testImplementation(libs.bundles.log4j)
    testRuntimeOnly(libs.grpc.netty)

    // integrate the dokka html export plugin
    dokkaHtmlPlugin(libs.dokka.html)
}

// configure the java extension
java {
    // also generate javadoc and sources
    withSourcesJar()
}

// configure the kotlin extension
kotlin {
    // set the toolchain version that is required to build this project
    // replaces sourceCompatibility and targetCompatibility as it also sets these implicitly
    // https://kotlinlang.org/docs/gradle-configure-project.html#gradle-java-toolchains-support
    jvmToolchain(17)
}

// configure the protobuf extension (protoc + grpc)
protobuf {
    // configure the protobuf compiler for the proto compilation
    protoc {
        // set the artifact for protoc (the compiler version to use)
        artifact = libs.protoc.core.get().toString()
    }

    // configure the plugins for the protobuf build process
    plugins {
        // add a new "grpc" plugin for the java stub generation
        id("grpc") {
            // set the artifact for protobuf code generation (stubs)
            artifact = libs.protoc.genJava.get().toString()
        }
        // add a new "grpckt" plugin for the kotlin stub generation
        id("grpckt") {
            artifact = libs.protoc.genKotlin.get().toString() + ":jdk8@jar"
        }
    }

    // configure the proto tasks (extraction, generation, etc.)
    generateProtoTasks {
        // only modify the main source set, we don't have any proto files in test
        all().configureEach {
            // apply the "java" and "kotlin" builtin tasks as we are compiling against java and kotlin
            builtins {
                // id("java") – is added implicitly by default
                id("kotlin")
            }

            // apply the "grpc" and "grpckt" plugins whose specs are defined above, without special options
            plugins {
                id("grpc")
                id("grpckt")
            }
        }
    }
}

// configure testing suites within gradle check phase
testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter(libs.versions.junit)
        }
    }
}

// configure dokka task for html output
val dokkaHtmlJar = tasks.register<Jar>("dokkaHtmlJar") {
    description = "Generates the HTML documentation for this project."
    group = JavaBasePlugin.DOCUMENTATION_GROUP
    dependsOn(tasks.dokkaHtml)
    from(tasks.dokkaHtml.flatMap { it.outputDirectory })
    archiveClassifier.set("html-docs")
}

// configure dokka task for javadoc output
val dokkaJavadocJar = tasks.register<Jar>("dokkaJavadocJar") {
    description = "Generates the Javadoc documentation for this project."
    group = JavaBasePlugin.DOCUMENTATION_GROUP
    dependsOn(tasks.dokkaJavadoc)
    from(tasks.dokkaJavadoc.flatMap { it.outputDirectory })
    archiveClassifier.set("javadoc")
}

// configure the publishing in the maven repository
publishing {
    // define the repositories that shall be used for publishing
    repositories {
        maven {
            name = "OSSRH"
            url = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                username = System.getenv("MAVEN_USERNAME")
                password = System.getenv("MAVEN_PASSWORD")
            }
        }
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/scrayosnet/xenos-client-kotlin")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }

    // define the java components as publications for the repository
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}

// configure pitest plugin
pitest {
    // configure the most recent versions
    pitestVersion = libs.versions.pitest.core
    junit5PluginVersion = libs.versions.pitest.junit

    // set target classes
    targetClasses.addAll("net.scrayos.xenos.client.*")

    // speed up performance by incremental, parallel builds
    threads = 4
    enableDefaultIncrementalAnalysis = true

    // output results as xml and html
    outputFormats.addAll("XML", "HTML")
    timestampedReports = false

    // add the individual source sets
    mainSourceSets.add(sourceSets.main)
    testSourceSets.add(sourceSets.test)
}

// configure ktlint
ktlint {
    // explicitly use a recent ktlint version for latest checks
    version = libs.versions.ktlint

    // exclude any generated files
    filter {
        // exclude generated protobuf files
        exclude { element -> element.file.path.contains("/generated/") }
    }

    // configure the reporting to use checkstyle syntax
    reporters {
        reporter(ReporterType.PLAIN)
        reporter(ReporterType.CHECKSTYLE)
        reporter(ReporterType.SARIF)
    }
}

// configure sonarqube plugin
sonarqube {
    properties {
        property("sonar.projectKey", "scrayosnet_xenos-client-kotlin")
        property("sonar.organization", "scrayosnet")
        property("sonar.projectName", "xenos-client-kotlin")
        property("sonar.projectVersion", version)
        property("sonar.projectDescription", description!!)
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.pitest.mode", "reuseReport")
        property(
            "sonar.kotlin.ktlint.reportPaths",
            "build/reports/ktlint/ktlintKotlinScriptCheck/ktlintKotlinScriptCheck.xml," +
                "build/reports/ktlint/ktlintMainSourceSetCheck/ktlintMainSourceSetCheck.xml," +
                "build/reports/ktlint/ktlintTestSourceSetCheck/ktlintTestSourceSetCheck.xml",
        )
        property("sonar.coverage.jacoco.xmlReportPaths", "build/reports/kover/report.xml")
    }
}

// configure tasks
tasks {

    jar {
        // exclude the proto files as we won't need them in downstream projects
        exclude("**/*.proto")

        // exclude the now empty folders (because the proto files were removed)
        includeEmptyDirs = false

        // remove duplicates from the final jar
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }

    javadoc {
        // exclude the generated protobuf files
        exclude("scrayosnet/xenos/**")
    }
}
