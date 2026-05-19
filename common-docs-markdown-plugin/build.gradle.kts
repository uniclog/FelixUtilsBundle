/*
plugins {
    id("org.jetbrains.intellij")
}
*/

val platformVersion = project.extra["platformVersion"] as String
val buildNumber = project.extra["buildNumber"] as String

val buildNumberFile = file("build-number.txt")

fun currentBuildNumber(): Int =
    if (buildNumberFile.exists())
        buildNumberFile.readText().trim().toInt()
    else 1

val majorIdeaVersion = platformVersion.substringBefore(".")
version = "1.0.${currentBuildNumber()}-$majorIdeaVersion"

sourceSets {
    main {
        java.srcDirs("src/main/java", "src/main/kotlin")
        kotlin.srcDirs("src/main/java", "src/main/kotlin")
    }
}

tasks.register("incrementBuildNumber") {
    doLast {
        buildNumberFile.writeText((currentBuildNumber() + 1).toString())
    }
}

tasks.named("buildPlugin") {
    dependsOn("incrementBuildNumber")
}