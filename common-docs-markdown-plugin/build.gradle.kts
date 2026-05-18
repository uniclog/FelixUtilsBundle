plugins {
    id("org.jetbrains.intellij.platform")
}

val platformVersionStr: String = project.findProperty("platformVersion")?.toString() ?: "2023.3.2"

val javaVersion: JavaVersion = when {
    platformVersionStr.startsWith("2021") -> JavaVersion.VERSION_11
    platformVersionStr.startsWith("2026") -> JavaVersion.VERSION_21
    else -> JavaVersion.VERSION_17
}

val buildNumber: String = when {
    platformVersionStr.startsWith("2021") -> "212"
    platformVersionStr.startsWith("2026") -> "261"
    else -> "232"
}

version = "1.0.1-$buildNumber-${javaVersion.majorVersion}"

sourceSets {
    main {
        java.srcDirs("src/main/java", "src/main/kotlin")
        kotlin.srcDirs("src/main/java", "src/main/kotlin")
    }
    test {
        java.srcDirs("src/test/java", "src/test/kotlin")
        kotlin.srcDirs("src/test/java", "src/test/kotlin")
    }
}