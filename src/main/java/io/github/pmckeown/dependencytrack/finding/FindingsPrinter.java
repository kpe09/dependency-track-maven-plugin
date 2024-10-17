package io.github.pmckeown.dependencytrack.finding;

import static io.github.pmckeown.dependencytrack.Constants.DELIMITER;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.joinWith;

import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;

import io.github.pmckeown.dependencytrack.project.Project;
import io.github.pmckeown.util.Logger;

@Singleton
class FindingsPrinter {

	private Logger logger;

	@Inject
	public FindingsPrinter(final Logger logger) {
		this.logger = logger;
	}

	void printFindings(final Project project, final List<Finding> findings) {
		if (findings == null || findings.isEmpty()) {
			logger.info("No findings were retrieved for project: %s", project.getName());
			return;
		}
		logger.info("%d finding(s) were retrieved for project: %s", findings.size(), project.getName());
		logger.info("Printing findings for project %s-%s", project.getName(), project.getVersion());
		findings.forEach(finding -> {
			final Vulnerability vulnerability = finding.getVulnerability();
			logger.info(DELIMITER);
			logger.info("%s (%s)", vulnerability.getVulnId(), vulnerability.getSource());
			logger.info("%s: %s", vulnerability.getSeverity().name(), getComponentDetails(finding));
			logger.info(""); // Spacer
			final List<String> wrappedDescriptionParts = splitString(vulnerability.getDescription());
			if (wrappedDescriptionParts != null && !wrappedDescriptionParts.isEmpty()) {
				wrappedDescriptionParts.forEach(s -> logger.info(s));
			}
			if (finding.getAnalysis().isSuppressed()) {
				logger.info("");
				logger.info("Suppressed - %s", finding.getAnalysis().getAnalysisState().name());
			}
		});
	}

	int getPrintWidth() {
		// We wrap printed lines to match the delimiter string width
		return DELIMITER.length();
	}

	private List<String> splitString(final String string) {
		if (StringUtils.isEmpty(string)) {
			return Collections.emptyList();
		}

		final String percentEscaped = StringUtils.replace(string, "%", "%%");
		final String cleaned = StringUtils.replace(percentEscaped, "\n", "");
		final int chunkSize = getPrintWidth();
		final int numberOfChunks = (cleaned.length() + chunkSize - 1) / chunkSize;
		return IntStream.range(0, numberOfChunks)
				.mapToObj(i -> cleaned.substring(i * chunkSize, Math.min((i + 1) * chunkSize, cleaned.length())))
				.collect(toList());
	}

	private String getComponentDetails(final Finding finding) {
		final Component component = finding.getComponent();
		return joinWith(":", component.getGroup(), component.getName(), component.getVersion());
	}

}
