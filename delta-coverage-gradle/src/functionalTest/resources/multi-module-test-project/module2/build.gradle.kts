plugins {
    java
    kotlin("jvm") version "2.2.21"
    `jvm-test-suite`
}

testing.suites {
    named<JvmTestSuite>("test") {
        useJUnitJupiter("5.11.4")
    }
    val intTest by registering(JvmTestSuite::class) {
        useJUnitJupiter("5.11.4")
        dependencies {
            implementation(project())
        }
    }
}
