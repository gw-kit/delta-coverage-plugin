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

    const reportContent = fs.readFileSync('build/reports/coverage-reports/summary.json');
    const summaryArray = JSON.parse(reportContent);
    for (const view of summaryArray) {
        const conclusion = viewHasViolations(view) ? 'failure' : 'success';
        ctx.github.rest.checks.create({
            owner: ctx.context.repo.owner,
            repo: ctx.context.repo.repo,
            name: `Delta Coverage Check ${view.view}`,
            head_sha: ctx.headSha,
            status: 'completed',
            conclusion: conclusion,
            output: {
                title: view.view,
                summary: readViewMarkdownReport(view),
                // text: readViewMarkdownReport(view),
                images: [
                    {
                        alt: conclusion,
                        image_url: 'https://s3.amazonaws.com/pix.iemoji.com/images/emoji/apple/ios-12/256/red-circle.png'
                    }
                ]
            }
        });
    }
};
