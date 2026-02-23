plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("maven-publish")
}

group = "com.arefbhrn.libraries"

android {
    namespace = "com.arefbhrn.irdebitcardscanner"
    compileSdk = 36
    defaultConfig {
        minSdk = 21
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        renderscriptTargetApi = 18
        renderscriptSupportModeEnabled = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    testFixtures {
        enable = false
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(platform(libs.kotlin.bom))
    implementation(libs.bundles.coroutines)
    implementation(libs.tensorflow.lite)
//    implementation(libs.tensorflow.lite.gpu)
}

publishing {
    publications {
        create<MavenPublication>("release") {
            afterEvaluate {
                from(components["release"])
            }

            description = "Iranian Debit Card Scanner using Deep Learning and TensorFlow Lite for Android"
            groupId = "com.arefbhrn.libraries"
            artifactId = "IRDebitCardScanner"
            version = "1.0.2"

            pom {
                name.set("IRDebitCardScanner")
                description.set("Iranian Debit Card Scanner using Deep Learning and TensorFlow Lite for Android")
                url.set("https://github.com/arefbhrn/IRDebitCardScanner")
                licenses {
                    license {
                        name.set("The Apache License 2.0")
                        url.set("https://opensource.org/licenses/Apache-2.0")
                    }
                }
                developers {
                    developer {
                        id.set("arefbhrn")
                        name.set("Aref Bahreini")
                    }
                }
            }
        }
    }
}
