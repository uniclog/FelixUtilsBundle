
val platformVersion = if (project.hasProperty("platformVersion")) project.property("platformVersion") as String else "2023.3.2"
val buildNumber = if (project.extra.has("buildNumber")) project.extra.get("buildNumber") as String else "232"

val buildNumberFile = file("build-number.txt")

fun currentBuildNumber(): Int =
    if (buildNumberFile.exists())
        buildNumberFile.readText().trim().toInt()
    else 1

val majorIdeaVersion: String = platformVersion.substringBefore(".")
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