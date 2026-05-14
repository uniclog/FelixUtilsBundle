plugins {
    id("org.jetbrains.intellij.platform")
}

group = "io.github.uniclog"
version = "1.0.1-232-17"

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