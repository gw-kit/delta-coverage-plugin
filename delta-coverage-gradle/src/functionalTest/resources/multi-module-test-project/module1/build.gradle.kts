plugins {
    java
    kotlin("jvm") version "2.2.21"
    `jvm-test-suite`
}

dependencies {
    implementation(project(":module2"))
}

testing.suites {
    named<JvmTestSuite>("test") {
        useJUnitJupiter("5.11.4")
    }
}
