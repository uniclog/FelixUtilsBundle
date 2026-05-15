plugins {
    id("org.jetbrains.intellij.platform")
}

version = "1.0.0-232-17"

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