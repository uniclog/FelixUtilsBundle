plugins {
    id("org.jetbrains.intellij")
}

group = "io.github.uniclog"
version = "1.0.1"

sourceSets {
    main {
        java.srcDirs("src/main/java", "src/main/kotlin")
        kotlin.srcDirs("src/main/java", "src/main/kotlin")
    }
}
