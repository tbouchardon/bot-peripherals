plugins {
    id("ksuto.java-library")
}

group = "fr.ksuto.bot"
version = "2.2.0"

dependencies {
    api(libs.guice)
    api(libs.ksuto.logger)
    api(libs.ksuto.commons)
    implementation(libs.postgresql)
    implementation(libs.commons.dbutils)
    compileOnly(libs.jetbrains.annotations)
}
