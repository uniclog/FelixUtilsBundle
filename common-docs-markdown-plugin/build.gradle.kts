plugins {
    id("org.jetbrains.intellij")
}

val platformVersion = project.findProperty("platformVersion")?.toString() ?: "2023.3.2"
val buildNumber = when {
    platformVersion.startsWith("2021") -> "212"
    platformVersion.startsWith("2026") -> "261"
    else -> "232"
}
val javaVersionMajor = when {
    platformVersion.startsWith("2021") -> "11"
    platformVersion.startsWith("2026") -> "21"
    else -> "17"
}

version = "1.0.5-$buildNumber-$javaVersionMajor"

sourceSets {
    main {
        java.srcDirs("src/main/java", "src/main/kotlin")
        kotlin.srcDirs("src/main/java", "src/main/kotlin")
    }
}
