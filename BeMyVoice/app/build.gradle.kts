plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.easychat"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.easychat"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

}

dependencies {

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.12.0")
    implementation (platform("com.google.firebase:firebase-bom:33.0.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.firebase:firebase-firestore:25.0.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    implementation("com.hbb20:ccp:2.7.0")

    implementation("com.squareup.okhttp3:okhttp:4.11.0")

    implementation("com.github.3llomi:RecordView:3.1.3")

    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.14.2")

    implementation("com.google.firebase:firebase-auth") {
        exclude (module= "protolite-well-known-types")
        exclude (module= "protobuf-javalite")
        exclude (module= "protobuf-java")
        exclude (module= "protobuf-java-util")
    }
    implementation("com.firebaseui:firebase-ui-firestore:8.0.2"){
        exclude (module= "protolite-well-known-types")
        exclude (module= "protobuf-javalite")
        exclude (module= "protobuf-java")
        exclude (module= "protobuf-java-util")
    }
    implementation("com.google.firebase:firebase-storage-ktx:20.3.0"){
        exclude (module= "protolite-well-known-types")
        exclude (module= "protobuf-javalite")
        exclude (module= "protobuf-java")
        exclude (module= "protobuf-java-util")
    }
    implementation (platform("com.google.firebase:firebase-bom:33.0.0"))
    implementation("com.google.firebase:firebase-firestore-ktx:24.10.0"){
        exclude (module= "protolite-well-known-types")
        exclude (module= "protobuf-javalite")
        exclude (module= "protobuf-java")
        exclude (module= "protobuf-java-util")
    }
    implementation("com.google.firebase:firebase-messaging-ktx:23.4.0"){
        exclude (module= "protolite-well-known-types")
        exclude (module= "protobuf-javalite")
        exclude (module= "protobuf-java")
        exclude (module= "protobuf-java-util")
    }

    implementation("com.github.dhaval2404:imagepicker:2.1")

    implementation("androidx.room:room-runtime:2.6.1")
    annotationProcessor("androidx.room:room-compiler:2.6.1")

    implementation("com.github.ismaeldivita:chip-navigation-bar:1.4.0")

    implementation("com.google.firebase:protolite-well-known-types:18.0.0") {
        exclude(group = "com.google.api.grpc", module = "proto-google-common-protos")
    }
//    implementation ("edu.cmu.pocketsphinx:pocketsphinx-android:5prealpha-SNAPSHOT")


    /*implementation("com.google.cloud:google-cloud-speech:1.29.1") {
        exclude(group = "com.google.api.grpc", module = "proto-google-common-protos")
    }*/

}