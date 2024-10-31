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
        ctx.github.rest.checks.create({
            owner: ctx.context.repo.owner,
            repo: ctx.context.repo.repo,
            name: `Coverage ${viewName}`,
            head_sha: ctx.headSha,
            status: 'completed',
            conclusion: conclusion,
            output: {
                title: `Delta Coverage Check _${viewName}_`,
                summary: readViewMarkdownReport(view),
            }
        });
    }

    const createAnnotations = (view) => {
        const hasViolations = viewHasViolations(view);
        if (hasViolations) {
            const viewName = capitalize(view.view);
            const violations = view.violations.join('\n    ');
            ctx.core.warning(`
                Code Coverage check failed for '${viewName}':
                    ${violations}
                `);
        }
    }

    const reportContent = fs.readFileSync(ctx.summaryReportPath);
    const summaryArray = JSON.parse(reportContent);
    for (const view of summaryArray) {
        createAnnotations(view);
        createCheckRun(view);
    }
};
