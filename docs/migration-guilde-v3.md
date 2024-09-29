# Migrating to v3

### Configuration for v2
```kts
configure<io.github.surpsg.deltacoverage.gradle.DeltaCoverageConfiguration> {
    // ...
    coverageBinaryFiles = files("/path/to/jacoco/exec/file.exec")
    
    violationRules {
        // ...
    }
}
```

### Configuration for v3
```kts

configure<io.github.surpsg.deltacoverage.gradle.DeltaCoverageConfiguration> {
    // ...
    reportViews {
        val aggregated by getting {
            coverageBinaryFiles = files("/path/to/jacoco/exec/file.exec")
            violationRules {
                // ...
            }
        }
    }
}

```
In the example above the configuration was migrated as-is without changing the behavior. 
