import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeCompile

plugins {
    alias(libs.plugins.android.library)

    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.cocoapods)

    alias(libs.plugins.ksp)

    alias(libs.plugins.androidx.room)

    alias(libs.plugins.publish)
}

val lorraineVersion = "0.2.0"

group = "io.github.dottttt.lorraine"
version = lorraineVersion

kotlin {
//    sourceSets.iosMain {
//        kotlin.srcDir("build/generated/ksp/metadata")
//    }

    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_1_8)
        }
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
        version = lorraineVersion
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
//dependencies {
//    ksp(libs.androidx.room.compiler)
////    kspCommonMainMetadata(libs.androidx.room.compiler)
////    add("kspAndroid", libs.androidx.room.compiler)
////    add("kspIosX64", libs.androidx.room.compiler)
////    add("kspIosArm64", libs.androidx.room.compiler)
////    add("kspIosSimulatorArm64", libs.androidx.room.compiler)
//}

//tasks.withType<KotlinNativeCompile>().configureEach {
//    if (name != "kspCommonMainKotlinMetadata") {
//        dependsOn("kspCommonMainKotlinMetadata")
//    }
//}

//tasks.matching { it.name.startsWith("kspKotlinIos") }.configureEach {
//    dependsOn("kspCommonMainKotlinMetadata")
//}

android {
    namespace = "fr.modulotech.workmanager"
    compileSdk = 35
    defaultConfig {
        minSdk = 24
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

mavenPublishing {
    coordinates(
        groupId = "io.github.dottttt.lorraine",
        artifactId = "lorraine",
        version = lorraineVersion
    )

    pom {
        name.set("KMP Library for work management")
        description.set("Target Android & iOS")
        inceptionYear.set("2024")
        url.set("https://github.com/doTTTTT/lorraine")

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
