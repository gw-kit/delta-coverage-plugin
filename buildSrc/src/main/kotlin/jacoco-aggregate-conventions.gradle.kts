plugins {
    `jacoco`
}

tasks.register<JacocoReport>("jacocoRootReport") {
    group = "verification"
    description = "Generates an aggregate report from all subprojects"

    dependsOn(subprojects.map { it.tasks.named("test") })

    sourceDirectories.from(sourceFromJacoco { sourceDirectories })
    classDirectories.from(sourceFromJacoco { classDirectories })
    executionData.from(sourceFromJacoco { executionData })

    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

fun sourceFromJacoco(jacocoSource: JacocoReportBase.() -> FileCollection): FileCollection {
    val sourcesProvider: Provider<FileCollection> = provider {
        subprojects.asSequence()
            .map { it.tasks.findByName("jacocoTestReport") }
            .filterNotNull()
            .map { it as JacocoReportBase }
            .map { it.jacocoSource() }
            .fold(files() as FileCollection) { all, next ->
                all + next
            }
    }
    return files(sourcesProvider)
}
