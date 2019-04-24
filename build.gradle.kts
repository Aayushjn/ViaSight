buildscript {
    repositories {
        google()
        jcenter()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:3.4.0")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.3.30")
    }
}

allprojects {
    repositories {
        google()
        jcenter()
    }
}

tasks {
    withType(Delete::class.java) {
        delete(rootProject.buildDir)
    }
}
