module.exports = (ctx) => {

    const NO_EXPECTED = -1;
    const NO_COVERAGE = -1;
    const ENTITIES = ['INSTRUCTION', 'BRANCH', 'LINE'];
    const TOOLTIPS = new Map([
        ['INSTRUCTION', 'The Java bytecode instructions executed during testing'],
        ['BRANCH', 'he branches in conditional statements like if, switch, or loops that are executed.'],
        ['LINE', 'The source code lines covered by the tests.']
    ])

    const buildViewSummaryData = (checkRun) => {
        const entitiesRules = checkRun.coverageRules.entitiesRules;
        const entityToExpectedRatio = new Map();
        for (const [entityName, entityConfig] of Object.entries(entitiesRules)) {
            entityToExpectedRatio.set(entityName, entityConfig.minCoverageRatio);
        }

        const entityToActualPercents = checkRun.coverageInfo.reduce((acc, it) => {
            if (it.total !== 0) {
                acc.set(it.coverageEntity, it.percents);
            }
            return acc;
        }, new Map());

        return ENTITIES.map((entity) => {
            const expectedRatio = entityToExpectedRatio.get(entity) || NO_EXPECTED;
            const expectedPercents = expectedRatio * 100;
            const actualPercents = entityToActualPercents.get(entity) || NO_COVERAGE;
            const isFailed = actualPercents > NO_COVERAGE && actualPercents < expectedPercents;
            return {
                entity,
                isFailed,
                expected: expectedPercents,
                actual: actualPercents
            }
        });
    };

    const buildCheckRunForViewText = (checkRun) => {
        const buildProgressImgLink = (entityData) => {
            const color = entityData.actual < entityData.expected ? 'C4625A' : '7AB56D';
            const actualInteger = Math.round(entityData.actual);
            return `https://progress-bar.xyz/${actualInteger}/?progress_color=${color}`;
        }

        const isSameExpectedForAllEntities = (viewSummaryData) => {
            const allExpected = viewSummaryData.map(it => it.expected);
            return new Set(allExpected).size === 1;
        }

        const buildRuleValueColumnHtml = (entityData, entityIndex, shouldFoldExpectedColumn) => {
            if (shouldFoldExpectedColumn && entityIndex > 0) {
                return '';
            }
            const rowSpanAttr = (shouldFoldExpectedColumn && entityIndex === 0) ? `rowspan=3` : '';
            const expectedText = (entityData.expected > NO_EXPECTED) ? `🎯 ${entityData.expected}% 🎯` : '';
            return `<td ${rowSpanAttr}>${expectedText}</td>`;
        }

        const viewSummaryData = buildViewSummaryData(checkRun);
        const hasFailure = viewSummaryData.some(it => it.isFailed);
        const shouldFoldExpectedColumn = isSameExpectedForAllEntities(viewSummaryData);

        const statusSymbol = hasFailure ? '🔴' : '🟢';
        const viewCellValue = `
            <td rowspan=3>${statusSymbol} <a href="${checkRun.url}">${checkRun.viewName}</a></td>
        `.trim();

        return viewSummaryData.map((entityData, index) => {
            const viewCellInRow = (index === 0) ? viewCellValue : '';

            const ruleColumnHtml = buildRuleValueColumnHtml(entityData, index, shouldFoldExpectedColumn);

            const actualValue = entityData.actual > NO_COVERAGE
                ? `<img src="${buildProgressImgLink(entityData)}" />`
                : '';
            const toolTipText = TOOLTIPS.get(entityData.entity) || '';

            return `<tr>
                ${viewCellInRow}
                <td><span title="${toolTipText}">${entityData.entity}</span></td>
                ${ruleColumnHtml}
                <td>${actualValue}</td>
            </tr>`.trim().replace(/^ +/gm, '');
        }).join('\n');
    }

    const checkRuns = JSON.parse(ctx.checkRunsContent);
    let summaryBuffer = ctx.core.summary
        .addHeading(ctx.commentTitle, '2')
        .addRaw(ctx.commentMarker, true)
        .addEOL()
        .addRaw(`<table><tbody>`);

    checkRuns.forEach(checkRun => {
        const runText = buildCheckRunForViewText(checkRun);
        summaryBuffer = summaryBuffer.addRaw(runText, true);
    });
    return summaryBuffer
        .addRaw(`</tbody></table>`)
        .stringify()
};
