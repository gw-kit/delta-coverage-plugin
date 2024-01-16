plugins {
    id("basic-coverage-conventions")
}

tasks.register<JacocoReport>("jacocoRootReport") {
    group = "verification"
    description = "Generates an aggregate report from all subprojects"

    mustRunAfter(
        provider {
            subprojects.flatMap { proj -> proj.tasks.withType<Test>() }
        }
    )

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
            .flatMap { it.tasks.withType<JacocoReportBase>() }
            .map { it.jacocoSource() }
            .fold(files() as FileCollection) { all, next ->
                all + next
            }
    }
    return files(sourcesProvider)
}
