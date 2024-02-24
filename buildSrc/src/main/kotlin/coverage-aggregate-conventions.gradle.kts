plugins {
    id("basic-coverage-conventions")
}

if (project != project.rootProject) {
    throw GradleException("This plugin must be applied to the root project")
}

dependencies {
    subprojects.forEach(::kover)
}
