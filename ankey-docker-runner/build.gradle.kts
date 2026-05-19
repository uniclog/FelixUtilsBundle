plugins {
    id("org.jetbrains.intellij")
}

version = "1.0.9"

sourceSets {
    main {
        java.srcDirs("src/main/java", "src/main/kotlin")
        kotlin.srcDirs("src/main/java", "src/main/kotlin")
    }
}
