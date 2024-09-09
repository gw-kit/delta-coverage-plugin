package io.github.surpsg.deltacoverage.report.jacoco.csv

import com.opencsv.bean.CsvBindByName

internal class CsvCoverageView {

    @CsvBindByName(column = "GROUP")
    lateinit var group: String

    @CsvBindByName(column = "PACKAGE")
    lateinit var aPackage: String

    @CsvBindByName(column = "CLASS")
    lateinit var aClass: String

    @CsvBindByName(column = "BRANCH_MISSED")
    lateinit var branchesMissed: String

    @CsvBindByName(column = "BRANCH_COVERED")
    lateinit var branchCovered: String

    @CsvBindByName(column = "LINE_MISSED")
    lateinit var lineMissed: String

    @CsvBindByName(column = "LINE_COVERED")
    lateinit var lineCovered: String

    @CsvBindByName(column = "INSTRUCTION_COVERED")
    lateinit var instrCovered: String

    @CsvBindByName(column = "INSTRUCTION_MISSED")
    lateinit var instrMissed: String
}
