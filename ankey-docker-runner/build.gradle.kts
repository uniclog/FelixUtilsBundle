plugins {
    id("org.jetbrains.intellij.platform")
}

version = "1.0.4-232-17"

sourceSets {
    main {
        java.srcDirs("src/main/kotlin")
        kotlin.srcDirs("src/main/java")
    }
    test {
        java.srcDirs("src/test/kotlin")
        kotlin.srcDirs("src/test/java")
    }
}