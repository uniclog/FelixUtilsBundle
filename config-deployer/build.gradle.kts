plugins {
    id("org.jetbrains.intellij.platform")
}

group = "io.github.uniclog"
version = "1.0-SNAPSHOT"

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