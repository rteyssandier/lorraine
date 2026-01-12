import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType
import java.io.ByteArrayOutputStream

plugins {
    alias(libs.plugins.android.kotlin)

    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.cocoapods)

    alias(libs.plugins.ksp)

    alias(libs.plugins.androidx.room)

    alias(libs.plugins.publish)
}

@Suppress("UnstableApiUsage")
abstract class GitVersionValueSource : ValueSource<String, GitVersionValueSource.Parameters> {
    interface Parameters : ValueSourceParameters {
        val projectDirectory: DirectoryProperty
    }

    @get:Inject
    abstract val execOperations: ExecOperations

    override fun obtain(): String {
        return try {
            val byteOut = ByteArrayOutputStream()
            execOperations.exec {
                workingDir = parameters.projectDirectory.get().asFile
                commandLine("git", "describe", "--tags", "--abbrev=0")
                standardOutput = byteOut
            }
            byteOut.toString().trim().removePrefix("v")
        } catch (e: Exception) {
            "0.0.1" // Fallback version
        }
    }
}

val lorraineVersionProvider = providers.of(GitVersionValueSource::class.java) {
    parameters.projectDirectory.set(rootProject.layout.projectDirectory)
}

group = "io.github.dottttt.lorraine"
version = lorraineVersionProvider.get()

kotlin {

    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    androidLibrary {
        namespace = "io.github.dottttt.lorraine"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "lorraine"
            isStatic = true
        }
    }

    cocoapods {
        version = lorraineVersionProvider.get()
        summary = "NO_DESCRIPTION"
        homepage = "NO_HOMEPAGE"
        ios.deploymentTarget = "15.0"

        framework {
            baseName = "Lorraine"
            isStatic = true
        }

        //pod("Reachability")

        xcodeConfigurationToNativeBuildType["CUSTOM_DEBUG"] = NativeBuildType.DEBUG
        xcodeConfigurationToNativeBuildType["CUSTOM_RELEASE"] = NativeBuildType.RELEASE
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.kotlin.coroutine.core)
                implementation(libs.kotlin.serialization.core)

                implementation(libs.androidx.room.runtime)

                implementation(libs.androidx.sqlite)

                implementation(libs.squareup.okio)
            }
        }

        commonTest.dependencies {
            implementation(libs.bundles.test.unit)
        }

        androidMain.dependencies {
            implementation(libs.androidx.work.runtime)
        }
    }
}

room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    listOf(
        "kspAndroid",
        "kspIosSimulatorArm64",
        "kspIosX64",
        "kspIosArm64"
    ).forEach {
        add(it, libs.androidx.room.compiler)
    }
}

mavenPublishing {
    coordinates(
        groupId = "io.github.dottttt.lorraine",
        artifactId = "lorraine",
        version = lorraineVersionProvider.get()
    )

    pom {
        name.set("KMP Library for work management")
        description.set("Target Android & iOS")
        inceptionYear.set("2026")
        url.set("https://github.com/rteyssandier/lorraine")

        licenses {
            license {
                name.set("MIT")
                url.set("https://opensource.org/licenses/MIT")
            }
        }

        developers {
            developer {
                id.set("dot")
                name.set("Raphael Teyssandier")
                email.set("raphael.teyssandier@gmail.com")
            }
        }

        scm {
            url.set("https://github.com/doTTTTT/lorraine")
        }
    }

    publishToMavenCentral()
    signAllPublications()
}
