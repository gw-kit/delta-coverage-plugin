module.exports = (ctx) => {

    const NO_EXPECTED = -1;
    const NO_COVERAGE = -1;

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

        const entities = [ ...entityToExpectedRatio.keys() ];
        return entities.map((entity) => {
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

        const viewSummaryData = buildViewSummaryData(checkRun);
        const hasFailure = viewSummaryData.some(it => it.isFailed);
        const statusSymbol = hasFailure ? '🔴' : '🟢';
        const viewCellValue = `
            <td rowspan=3><a href="${checkRun.url}">${statusSymbol} ${checkRun.viewName}</a></td>
        `.trim();
        return viewSummaryData.map((entityData, index) => {
            const viewCellInRow = (index === 0) ? viewCellValue : '';
            const ruleValue = (entityData.expected > NO_EXPECTED)
                ? `🎯 ${entityData.expected}% 🎯`
                : ``;
            const actualValue = entityData.actual > NO_COVERAGE
                ? `<img src="${buildProgressImgLink(entityData)}" />`
                : '';
            return `<tr>
                ${viewCellInRow}
                <td>${entityData.entity}</td>
                <td>${ruleValue}</td>
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
