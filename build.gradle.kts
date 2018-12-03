buildscript {
    repositories {
        google()
        jcenter()
        mavenLocal()
        maven(url = "https://dl.bintray.com/kotlin/ktor")


    }
    dependencies {
        classpath("com.android.tools.build:gradle:3.2.1")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.2.61")
         classpath("cdsap:talaiot:0.1.3")
    }
}

allprojects {
    repositories {
        google()
        jcenter()
        mavenLocal()
    }
}

tasks {

    val plugin by registering(GradleBuild::class) {
        dir = file("talaiot")
        tasks = listOf("publish")
    }

    val consumer by registering(GradleBuild::class) {
        dir = file("app")
        tasks = listOf("myCopyTask")
    }

    consumer {
        dependsOn(plugin)
    }
}