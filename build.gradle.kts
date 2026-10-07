plugins {
    id("ksuto.java-library")
}

group = "fr.ksuto.bot"
version = "2.3.0"

dependencies {
    api(libs.guice)
    api(libs.slf4j.api)
    api(libs.ksuto.commons)
    implementation(libs.sqlite.jdbc)
    compileOnly(libs.jetbrains.annotations)
}

// SQLite charge sa bibliothèque native : autorisé explicitement, sans avertissement de la JVM
tasks.test {
    jvmArgs("--enable-native-access=ALL-UNNAMED")
}
