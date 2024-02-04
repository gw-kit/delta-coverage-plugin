plugins {
    `kotlin-dsl`
}

dependencies {
    // https://github.com/gradle/gradle/issues/15383
    implementation(files(deps.javaClass.superclass.protectionDomain.codeSource.location))

    implementation(deps.kotlinJvm)
    implementation(kotlin("gradle-plugin"))
    implementation(deps.pluginPublish)

    implementation(deps.detekt)
    implementation(deps.koverPlugin)
    implementation(deps.deltaCoverage)
}
