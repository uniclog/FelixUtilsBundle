/*
plugins {
    id("org.jetbrains.intellij")
}
*/

val platformVersion = project.findProperty("platformVersion")?.toString() ?: "2023.3.2"
val buildNumber = when {
    platformVersion.startsWith("2021") -> "212"
    platformVersion.startsWith("2022") -> "222"
    platformVersion.startsWith("2024") -> "241"
    platformVersion.startsWith("2025") -> "251"
    platformVersion.startsWith("2026") -> "261"
    else -> "232"
}

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