module.exports = async (ctx) => {
    const fs = require('fs');

    const buildPathToReport = (viewName) => {
        return `build/reports/coverage-reports/delta-coverage/${viewName}/report.md`;
    };

    const viewHasViolations = (view) => {
        return view.violations.length > 0;
    };

    const readViewMarkdownReport = (view) => {
        const reportPath = buildPathToReport(view.view);
        try {
            return fs.readFileSync(reportPath, 'utf8');
        } catch (e) {
            return `NO REPORT by path: ${reportPath}`;
        }
    }

    const capitalize = (s) => {
        return s.charAt(0).toUpperCase() + s.slice(1);
    }

    const createCheckRun = async (view) => {
        const hasViolations = viewHasViolations(view);
        const conclusion = hasViolations ? 'failure' : 'success';
        const viewName = capitalize(view.view);
        const response = await ctx.github.rest.checks.create({
            owner: ctx.context.repo.owner,
            repo: ctx.context.repo.repo,
            name: `Coverage ${viewName}`,
            head_sha: ctx.headSha,
            status: 'completed',
            conclusion: conclusion,
            output: {
                title: `Delta Coverage Check '${viewName}'`,
                summary: readViewMarkdownReport(view),
            }
        });
        return {
            checkRunId: response.data.id,
            url: response.data.html_url,
            conclusion: conclusion
        }
    }

    const createAnnotations = (view) => {
        const hasViolations = viewHasViolations(view);
        if (hasViolations) {
            const viewName = capitalize(view.view);
            const violations = view.violations.join(';\n');
            const msg = `[${viewName}]: Code Coverage check failed:\n${violations}`;
            ctx.core.error(msg);
            ctx.core.notice(msg);
        }
    }

    const reportContent = fs.readFileSync(ctx.summaryReportPath);
    const summaryArray = JSON.parse(reportContent);
    const checkRuns = [];
    for (const view of summaryArray) {
        createAnnotations(view);
        const checkRun = await createCheckRun(view);
        checkRuns.push(checkRun);
    }
    return checkRuns;
};
