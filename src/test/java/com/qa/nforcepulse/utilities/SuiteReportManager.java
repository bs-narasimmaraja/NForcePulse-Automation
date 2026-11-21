package com.qa.nforcepulse.utilities;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Builds the single NforcePulse execution report (replaces ExtentReports for the HTML side).
 * Data is collected from Hooks (scenario start/end) and StepListener (per-step result) as the
 * suite runs, then rendered to one static HTML file when the suite finishes.
 */
public class SuiteReportManager {

	public static class StepRecord {
		final String text;
		final String status;
		final String screenshotBase64;

		StepRecord(String text, String status, String screenshotBase64) {
			this.text = text;
			this.status = status;
			this.screenshotBase64 = screenshotBase64;
		}
	}

	public static class ScenarioRecord {
		String name;
		String tags;
		String status = "PASSED";
		long durationMs;
		final List<StepRecord> steps = new ArrayList<>();
	}

	private static final List<ScenarioRecord> scenarios = new CopyOnWriteArrayList<>();
	private static final ThreadLocal<ScenarioRecord> current = new ThreadLocal<>();
	private static final String executionId = UUID.randomUUID().toString();

	private static long suiteStart;
	private static long suiteEnd;

	public static void suiteStarted() {
		suiteStart = System.currentTimeMillis();
	}

	public static void startScenario(String name, String tags) {
		ScenarioRecord record = new ScenarioRecord();
		record.name = name;
		record.tags = tags;
		current.set(record);
	}

	public static void recordStep(String stepText, String status, String screenshotBase64) {
		ScenarioRecord record = current.get();
		if (record != null) {
			record.steps.add(new StepRecord(stepText, status, screenshotBase64));
		}
	}

	public static void endScenario(String status, long durationMs) {
		ScenarioRecord record = current.get();
		if (record != null) {
			record.status = status;
			record.durationMs = durationMs;
			scenarios.add(record);
			current.remove();
		}
	}

	public static void suiteFinished() {
		suiteEnd = System.currentTimeMillis();
		try {
			generateReport();
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			generatePdf();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// ---- report generation ----

	private static void generateReport() throws IOException {
		File dir = new File("./NforcePulseHtmlAndPdfReports/HTMLReports/");
		if (!dir.exists()) {
			dir.mkdirs();
		}
		SimpleDateFormat fileStamp = new SimpleDateFormat("d-MMM-yy HH-mm-ss");
		File out = new File(dir, "NforcePulseExecutionReport_" + fileStamp.format(new Date()) + ".html");
		try (FileWriter writer = new FileWriter(out)) {
			writer.write(buildHtml());
		}
		System.out.println("Execution report generated: " + out.getPath());
	}

	private static void generatePdf() throws IOException {
		File dir = new File("./NforcePulseHtmlAndPdfReports/PDFReports/");
		if (!dir.exists()) {
			dir.mkdirs();
		}
		SimpleDateFormat fileStamp = new SimpleDateFormat("d-MMM-yy HH-mm-ss");
		File out = new File(dir, "NforcePulseExecutionReport_" + fileStamp.format(new Date()) + ".pdf");

		int scenarioPass = count("PASSED", false);
		int scenarioFail = count("FAILED", false);
		int scenarioSkip = scenarios.size() - scenarioPass - scenarioFail;
		int stepPass = count("PASSED", true);
		int stepFail = count("FAILED", true);
		int stepSkip = totalSteps() - stepPass - stepFail;

		com.itextpdf.kernel.pdf.PdfWriter writer = new com.itextpdf.kernel.pdf.PdfWriter(out.getPath());
		com.itextpdf.kernel.pdf.PdfDocument pdf = new com.itextpdf.kernel.pdf.PdfDocument(writer);
		com.itextpdf.layout.Document doc = new com.itextpdf.layout.Document(pdf);

		com.itextpdf.kernel.font.PdfFont bold = com.itextpdf.kernel.font.PdfFontFactory.createFont(
				com.itextpdf.io.font.constants.StandardFonts.HELVETICA_BOLD);

		doc.add(new com.itextpdf.layout.element.Paragraph("NforcePulse Automation Execution Report")
				.setFont(bold).setFontSize(18)
				.setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER));

		String browser = ConfigurationUtilities.getProperty("browser");
		String env = ConfigurationUtilities.getProperty("Environment");
		doc.add(new com.itextpdf.layout.element.Paragraph("Execution ID : " + executionId));
		doc.add(new com.itextpdf.layout.element.Paragraph("Browser : " + browser + "   Environment : " + env));
		doc.add(new com.itextpdf.layout.element.Paragraph("Start : " + formatDateTime(suiteStart)
				+ "   End : " + formatDateTime(suiteEnd) + "   Duration : " + formatDuration(suiteEnd - suiteStart)));
		doc.add(new com.itextpdf.layout.element.Paragraph("\n"));

		doc.add(new com.itextpdf.layout.element.Paragraph("Execution Overview").setFont(bold).setFontSize(13));
		com.itextpdf.layout.element.Table ringRow = new com.itextpdf.layout.element.Table(
				com.itextpdf.layout.properties.UnitValue.createPercentArray(new float[]{1, 1, 1})).useAllAvailableWidth();
		ringRow.addCell(ringCell("Features / Scripts", scenarioPass, scenarioFail, scenarioSkip, pdf));
		ringRow.addCell(ringCell("Steps", stepPass, stepFail, stepSkip, pdf));
		ringRow.addCell(ringCell("Pass Rate", stepPass, stepFail, stepSkip, pdf));
		doc.add(ringRow);
		doc.add(new com.itextpdf.layout.element.Paragraph("\n"));

		doc.add(new com.itextpdf.layout.element.Paragraph(
				"Scenarios - Passed: " + scenarioPass + "  Failed: " + scenarioFail + "  Skipped: " + scenarioSkip
						+ "     Steps - Passed: " + stepPass + "  Failed: " + stepFail + "  Skipped: " + stepSkip));
		doc.add(new com.itextpdf.layout.element.Paragraph("\n"));

		doc.add(new com.itextpdf.layout.element.Paragraph("Test Result Summary").setFont(bold).setFontSize(13));
		com.itextpdf.layout.element.Table summaryTable = new com.itextpdf.layout.element.Table(
				com.itextpdf.layout.properties.UnitValue.createPercentArray(new float[]{4, 1, 1, 1, 1, 1.3f}))
				.useAllAvailableWidth();
		for (String header : new String[]{"FEATURE / SCRIPT NAME", "STATUS", "PASSED", "FAILED", "STEPS", "DURATION"}) {
			summaryTable.addHeaderCell(new com.itextpdf.layout.element.Cell()
					.add(new com.itextpdf.layout.element.Paragraph(header).setFont(bold).setFontSize(9)));
		}
		for (ScenarioRecord s : scenarios) {
			int sp = 0, sf = 0;
			for (StepRecord st : s.steps) {
				if (st.status.equalsIgnoreCase("PASSED")) sp++;
				else if (st.status.equalsIgnoreCase("FAILED")) sf++;
			}
			summaryTable.addCell(new com.itextpdf.layout.element.Cell()
					.add(new com.itextpdf.layout.element.Paragraph(s.name).setFontSize(9)));
			summaryTable.addCell(new com.itextpdf.layout.element.Cell()
					.add(new com.itextpdf.layout.element.Paragraph(s.status).setFontSize(9)));
			summaryTable.addCell(new com.itextpdf.layout.element.Cell()
					.add(new com.itextpdf.layout.element.Paragraph(String.valueOf(sp)).setFontSize(9)));
			summaryTable.addCell(new com.itextpdf.layout.element.Cell()
					.add(new com.itextpdf.layout.element.Paragraph(String.valueOf(sf)).setFontSize(9)));
			summaryTable.addCell(new com.itextpdf.layout.element.Cell()
					.add(new com.itextpdf.layout.element.Paragraph(String.valueOf(s.steps.size())).setFontSize(9)));
			summaryTable.addCell(new com.itextpdf.layout.element.Cell()
					.add(new com.itextpdf.layout.element.Paragraph(formatDuration(s.durationMs)).setFontSize(9)));
		}
		doc.add(summaryTable);
		doc.add(new com.itextpdf.layout.element.Paragraph("\n"));

		doc.add(new com.itextpdf.layout.element.Paragraph("Detailed Test Steps").setFont(bold).setFontSize(13));

		for (ScenarioRecord s : scenarios) {
			doc.add(new com.itextpdf.layout.element.Paragraph(s.name + "   [" + s.status + "]")
					.setFont(bold).setFontSize(13));
			doc.add(new com.itextpdf.layout.element.Paragraph(
					"Tags: " + s.tags + "   Duration: " + formatDuration(s.durationMs)).setFontSize(10));

			for (StepRecord step : s.steps) {
				com.itextpdf.kernel.colors.Color color = step.status.equalsIgnoreCase("PASSED")
						? com.itextpdf.kernel.colors.ColorConstants.GREEN
						: step.status.equalsIgnoreCase("FAILED")
								? com.itextpdf.kernel.colors.ColorConstants.RED
								: com.itextpdf.kernel.colors.ColorConstants.ORANGE;
				doc.add(new com.itextpdf.layout.element.Paragraph("  [" + step.status + "]  " + step.text)
						.setFontColor(color).setFontSize(10).setMarginBottom(2));
				if (step.screenshotBase64 != null) {
					try {
						byte[] imgBytes = Base64.getDecoder().decode(step.screenshotBase64);
						com.itextpdf.layout.element.Image screenshot = new com.itextpdf.layout.element.Image(
								com.itextpdf.io.image.ImageDataFactory.create(imgBytes));
						screenshot.setMaxWidth(350);
						screenshot.setMarginLeft(14);
						screenshot.setMarginBottom(6);
						doc.add(screenshot);
					} catch (Exception ignored) {
					}
				}
			}
			doc.add(new com.itextpdf.layout.element.Paragraph("\n"));
		}

		doc.close();
		System.out.println("PDF report generated: " + out.getPath());
	}

	private static com.itextpdf.layout.element.Cell ringCell(String label, int passed, int failed, int skipped,
			com.itextpdf.kernel.pdf.PdfDocument pdf) {
		com.itextpdf.layout.element.Cell cell = new com.itextpdf.layout.element.Cell()
				.setBorder(com.itextpdf.layout.borders.Border.NO_BORDER)
				.setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER);
		try {
			String svg = ringSvgStandalone(label, passed, failed, skipped);
			com.itextpdf.layout.element.Image ringImage = com.itextpdf.svg.converter.SvgConverter.convertToImage(
					new java.io.ByteArrayInputStream(svg.getBytes(java.nio.charset.StandardCharsets.UTF_8)), pdf);
			ringImage.setHorizontalAlignment(com.itextpdf.layout.properties.HorizontalAlignment.CENTER);
			cell.add(ringImage);
		} catch (Exception ignored) {
		}
		cell.add(new com.itextpdf.layout.element.Paragraph(
				"Passed " + passed + "   Failed " + failed + "   Skipped " + skipped)
				.setFontSize(9).setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER));
		return cell;
	}

	private static int count(String status, boolean steps) {
		int total = 0;
		for (ScenarioRecord s : scenarios) {
			if (steps) {
				for (StepRecord st : s.steps) {
					if (st.status.equalsIgnoreCase(status)) total++;
				}
			} else if (s.status.equalsIgnoreCase(status)) {
				total++;
			}
		}
		return total;
	}

	private static int totalSteps() {
		int total = 0;
		for (ScenarioRecord s : scenarios) total += s.steps.size();
		return total;
	}

	private static String formatDateTime(long millis) {
		return new SimpleDateFormat("d/M/yyyy HH:mm:ss").format(new Date(millis));
	}

	private static String formatDuration(long millis) {
		long h = millis / 3600000;
		long m = (millis % 3600000) / 60000;
		long s = (millis % 60000) / 1000;
		return String.format("%02d:%02d:%02d", h, m, s);
	}

	private static String escape(String s) {
		if (s == null) return "";
		return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
	}

	private static String logoDataUri() {
		try {
			byte[] bytes = Files.readAllBytes(new File("src/test/resources/ReportAssets/nforcepulse-logo.jpg").toPath());
			return "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(bytes);
		} catch (IOException e) {
			return "";
		}
	}

	private static String ring(String label, int passed, int failed, int skipped) {
		int total = passed + failed + skipped;
		double percent = total == 0 ? 0 : (passed * 100.0 / total);
		double radius = 46;
		double circumference = 2 * Math.PI * radius;

		double passLen = total == 0 ? 0 : circumference * passed / total;
		double failLen = total == 0 ? 0 : circumference * failed / total;
		double skipLen = total == 0 ? 0 : circumference * skipped / total;

		StringBuilder sb = new StringBuilder();
		sb.append("<div class=\"card ring-card\">");
		sb.append("<div class=\"card-title\">").append(escape(label)).append("</div>");
		sb.append("<svg viewBox=\"0 0 110 110\" width=\"110\" height=\"110\">");
		sb.append("<circle cx=\"55\" cy=\"55\" r=\"").append(radius).append("\" stroke=\"#ececec\" stroke-width=\"11\" fill=\"none\"/>");
		if (passLen > 0) {
			sb.append(ringSegment(radius, "#2ecc71", passLen, circumference, 0));
		}
		if (failLen > 0) {
			sb.append(ringSegment(radius, "#e0263f", failLen, circumference, passLen));
		}
		if (skipLen > 0) {
			sb.append(ringSegment(radius, "#e0a800", skipLen, circumference, passLen + failLen));
		}
		sb.append("<text x=\"55\" y=\"52\" text-anchor=\"middle\" class=\"ring-value\">")
				.append(label.equals("Pass Rate") ? (int) percent + "%" : total).append("</text>");
		sb.append("<text x=\"55\" y=\"68\" text-anchor=\"middle\" class=\"ring-label\">").append(escape(label)).append("</text>");
		sb.append("</svg>");
		sb.append("<div class=\"legend\">")
				.append("<span class=\"dot pass\"></span>").append(passed).append(" &nbsp; ")
				.append("<span class=\"dot fail\"></span>").append(failed).append(" &nbsp; ")
				.append("<span class=\"dot skip\"></span>").append(skipped)
				.append("</div>");
		sb.append("</div>");
		return sb.toString();
	}

	// Standalone version of ring() for the PDF: no external CSS classes (the HTML version's
	// ring-value/ring-label/dot classes only exist in the page's stylesheet), so colors and
	// fonts are set inline. Reuses the same ringSegment() arc math so both reports draw the
	// identical donut shape.
	private static String ringSvgStandalone(String label, int passed, int failed, int skipped) {
		int total = passed + failed + skipped;
		double percent = total == 0 ? 0 : (passed * 100.0 / total);
		double radius = 46;
		double circumference = 2 * Math.PI * radius;

		double passLen = total == 0 ? 0 : circumference * passed / total;
		double failLen = total == 0 ? 0 : circumference * failed / total;
		double skipLen = total == 0 ? 0 : circumference * skipped / total;

		StringBuilder sb = new StringBuilder();
		sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 110 110\" width=\"150\" height=\"150\">");
		sb.append("<circle cx=\"55\" cy=\"55\" r=\"").append(radius).append("\" stroke=\"#ececec\" stroke-width=\"11\" fill=\"none\"/>");
		if (passLen > 0) sb.append(ringSegment(radius, "#2ecc71", passLen, circumference, 0));
		if (failLen > 0) sb.append(ringSegment(radius, "#e0263f", failLen, circumference, passLen));
		if (skipLen > 0) sb.append(ringSegment(radius, "#e0a800", skipLen, circumference, passLen + failLen));
		sb.append("<text x=\"55\" y=\"52\" text-anchor=\"middle\" font-size=\"20\" font-family=\"Helvetica\" font-weight=\"bold\" fill=\"#1a1a1a\">")
				.append(label.equals("Pass Rate") ? (int) percent + "%" : total).append("</text>");
		sb.append("<text x=\"55\" y=\"68\" text-anchor=\"middle\" font-size=\"9\" font-family=\"Helvetica\" fill=\"#888888\">")
				.append(escape(label)).append("</text>");
		sb.append("</svg>");
		return sb.toString();
	}

	private static String ringSegment(double radius, String color, double length, double circumference, double precedingLength) {
		return "<circle cx=\"55\" cy=\"55\" r=\"" + radius + "\" stroke=\"" + color + "\" stroke-width=\"11\" fill=\"none\" "
				+ "stroke-linecap=\"butt\" transform=\"rotate(-90 55 55)\" "
				+ "stroke-dasharray=\"" + String.format("%.2f", length) + " " + String.format("%.2f", circumference - length) + "\" "
				+ "stroke-dashoffset=\"" + String.format("%.2f", -precedingLength) + "\"/>";
	}

	private static String badge(String status) {
		String cls = status.equalsIgnoreCase("PASSED") ? "pass" : status.equalsIgnoreCase("FAILED") ? "fail" : "skip";
		return "<span class=\"badge " + cls + "\">" + escape(status) + "</span>";
	}

	private static String buildHtml() {
		int scenarioPass = count("PASSED", false);
		int scenarioFail = count("FAILED", false);
		int scenarioSkip = scenarios.size() - scenarioPass - scenarioFail;

		int stepPass = count("PASSED", true);
		int stepFail = count("FAILED", true);
		int stepSkip = totalSteps() - stepPass - stepFail;

		String overallStatus = scenarioFail > 0 ? "FAILED" : "PASSED";
		String browser = ConfigurationUtilities.getProperty("browser");
		String env = ConfigurationUtilities.getProperty("Environment");
		String baseUrl = ConfigurationUtilities.getProperty(env + "URL");

		StringBuilder html = new StringBuilder();
		html.append("<!DOCTYPE html><html><head><meta charset=\"UTF-8\">");
		html.append("<title>NforcePulse Execution Report</title>");
		html.append("<style>").append(css()).append("</style></head><body>");

		html.append("<div class=\"topbar\">");
		html.append("<div class=\"brand-block\"><img src=\"").append(logoDataUri()).append("\"/>");
		html.append("<div class=\"brand-text\"><span class=\"brand-title\">NForce Pulse</span><span class=\"brand-sub\">Reports</span></div></div>");
		html.append("<div class=\"suite-block\">");
		html.append("<span class=\"suite-title\">NForce Pulse Automation Suite</span>");
		html.append("<span class=\"suite-sub\">NForcePulse_Sanity_Suite - ").append(escape(browser)).append(" &mdash; Pulse</span>");
		html.append("</div></div>");

		html.append("<div class=\"layout\">");

		// sidebar
		html.append("<nav class=\"sidebar\">");
		html.append("<div class=\"nav-title\">OVERVIEW</div>");
		html.append("<a href=\"#overview\">Execution Overview</a>");
		html.append("<a href=\"#summary\">Execution Summary</a>");
		html.append("<a href=\"#resultsummary\">Test Result Summary</a>");
		html.append("<a href=\"#results\">Test Results</a>");
		html.append("<a href=\"#sysinfo\">System Information</a>");
		html.append("<div class=\"nav-title\">TEST SUITE</div>");
		for (int i = 0; i < scenarios.size(); i++) {
			ScenarioRecord s = scenarios.get(i);
			html.append("<a href=\"#scenario-").append(i).append("\" class=\"nav-scenario\">")
					.append("<span>").append(escape(s.name)).append("</span>")
					.append(badge(s.status))
					.append("</a>");
		}
		html.append("</nav>");

		// main
		html.append("<main>");

		html.append("<section class=\"card meta-card\">");
		html.append("<div class=\"meta-top\"><div>");
		html.append("<div class=\"eyebrow\">EXECUTION REPORT</div>");
		html.append("<h2>NforcePulse Automation</h2>");
		html.append("<div class=\"subtitle\">NforcePulse Cucumber Suite - ").append(escape(browser)).append("</div>");
		html.append("</div>").append(badge(overallStatus)).append("</div>");
		html.append("<div class=\"meta-grid\">");
		html.append(metaItem("PROJECT", "NforcePulse Automation"));
		html.append(metaItem("SUITE / SCRIPT", "NforcePulse Cucumber Suite - " + browser));
		html.append(metaItem("EXECUTION ID", executionId));
		html.append(metaItem("BROWSER", browser));
		html.append(metaItem("ENVIRONMENT", env + " (" + baseUrl + ")"));
		html.append(metaItem("START TIME", formatDateTime(suiteStart)));
		html.append(metaItem("END TIME", formatDateTime(suiteEnd)));
		html.append(metaItem("DURATION", formatDuration(suiteEnd - suiteStart)));
		html.append(metaItem("GENERATED", formatDateTime(System.currentTimeMillis())));
		html.append("</div></section>");

		html.append("<section id=\"overview\"><h3><span class=\"dot-red\"></span> Execution Overview</h3>");
		html.append("<div class=\"cards-row\">");
		html.append(ring("Features / Scripts", scenarioPass, scenarioFail, scenarioSkip));
		html.append(ring("Steps", stepPass, stepFail, stepSkip));
		html.append(ring("Pass Rate", stepPass, stepFail, stepSkip));
		html.append("</div></section>");

		html.append("<section id=\"summary\"><h3><span class=\"dot-red\"></span> Execution Summary</h3>");
		html.append("<table><tr><th>METRIC</th><th>FEATURES / SCRIPTS</th><th>STEPS</th></tr>");
		html.append("<tr><td>Passed</td><td>").append(scenarioPass).append("</td><td>").append(stepPass).append("</td></tr>");
		html.append("<tr><td>Failed</td><td>").append(scenarioFail).append("</td><td>").append(stepFail).append("</td></tr>");
		html.append("<tr><td>Skipped</td><td>").append(scenarioSkip).append("</td><td>").append(stepSkip).append("</td></tr>");
		html.append("<tr class=\"total-row\"><td>Total</td><td>").append(scenarios.size()).append("</td><td>").append(totalSteps()).append("</td></tr>");
		html.append("</table></section>");

		html.append("<section id=\"resultsummary\"><h3><span class=\"dot-red\"></span> Test Result Summary</h3>");
		html.append("<table><tr><th>FEATURE / SCRIPT NAME</th><th>STATUS</th><th>PASSED</th><th>FAILED</th><th>STEPS</th><th>DURATION</th></tr>");
		for (int i = 0; i < scenarios.size(); i++) {
			ScenarioRecord s = scenarios.get(i);
			int sp = 0, sf = 0;
			for (StepRecord st : s.steps) {
				if (st.status.equalsIgnoreCase("PASSED")) sp++;
				else if (st.status.equalsIgnoreCase("FAILED")) sf++;
			}
			html.append("<tr><td><a href=\"#scenario-").append(i).append("\">").append(escape(s.name)).append("</a></td><td>")
					.append(badge(s.status)).append("</td><td>").append(sp).append("</td><td>").append(sf).append("</td><td>")
					.append(s.steps.size()).append("</td><td>").append(formatDuration(s.durationMs)).append("</td></tr>");
		}
		html.append("</table></section>");

		html.append("<section id=\"results\"><h3><span class=\"dot-red\"></span> Test Results</h3>");
		for (int i = 0; i < scenarios.size(); i++) {
			ScenarioRecord s = scenarios.get(i);
			html.append("<div class=\"scenario-card\" id=\"scenario-").append(i).append("\">");
			html.append("<div class=\"scenario-head\"><div><strong>").append(escape(s.name)).append("</strong>")
					.append("<div class=\"subtitle\">").append(escape(s.tags)).append(" &middot; ")
					.append(formatDuration(s.durationMs)).append("</div></div>").append(badge(s.status)).append("</div>");

			html.append("<div class=\"step-master-detail\">");
			html.append("<div class=\"step-nav\">");
			for (int j = 0; j < s.steps.size(); j++) {
				StepRecord step = s.steps.get(j);
				String cls = step.status.equalsIgnoreCase("PASSED") ? "pass" : step.status.equalsIgnoreCase("FAILED") ? "fail" : "skip";
				String icon = cls.equals("pass") ? "&#10003;" : cls.equals("fail") ? "&#10007;" : "&#8211;";
				html.append("<div class=\"step-nav-item ").append(cls).append("\" onclick=\"selectStep(this)\"")
						.append(step.screenshotBase64 != null ? " data-img=\"data:image/png;base64," + step.screenshotBase64 + "\"" : "")
						.append(">");
				html.append("<span class=\"step-icon\">").append(icon).append("</span>");
				html.append("<span class=\"step-nav-text\">").append(escape(step.text)).append("</span>");
				html.append("</div>");
			}
			html.append("</div>");
			html.append("<div class=\"step-viewer\">");
			html.append("<img class=\"step-viewer-img\" alt=\"step screenshot\"/>");
			html.append("<div class=\"step-viewer-placeholder\">Click a step on the left to view its screenshot</div>");
			html.append("</div>");
			html.append("</div>");

			html.append("</div>");
		}
		html.append("</section>");

		html.append("<section id=\"sysinfo\"><h3><span class=\"dot-red\"></span> System Information</h3>");
		html.append("<table>");
		html.append("<tr><td>OS</td><td>").append(escape(System.getProperty("os.name"))).append("</td></tr>");
		html.append("<tr><td>Browser</td><td>").append(escape(browser)).append("</td></tr>");
		html.append("<tr><td>Environment</td><td>").append(escape(env)).append("</td></tr>");
		html.append("<tr><td>Base URL</td><td>").append(escape(baseUrl)).append("</td></tr>");
		html.append("<tr><td>User</td><td>").append(escape(ConfigurationUtilities.getProperty("User"))).append("</td></tr>");
		html.append("</table></section>");

		html.append("</main></div>");
		html.append("<script>").append(js()).append("</script>");
		html.append("</body></html>");
		return html.toString();
	}

	private static String metaItem(String label, String value) {
		return "<div><div class=\"label\">" + escape(label) + "</div><div class=\"value\">" + escape(value) + "</div></div>";
	}

	private static String js() {
		return "" +
				"function selectStep(el){" +
				"  var group=el.closest('.step-master-detail');" +
				"  group.querySelectorAll('.step-nav-item').forEach(function(n){n.classList.remove('active');});" +
				"  el.classList.add('active');" +
				"  var img=group.querySelector('.step-viewer-img');" +
				"  var placeholder=group.querySelector('.step-viewer-placeholder');" +
				"  var src=el.getAttribute('data-img');" +
				"  if(src){img.src=src;img.style.display='block';placeholder.style.display='none';}" +
				"  else{img.style.display='none';placeholder.textContent='No screenshot captured for this step';placeholder.style.display='flex';}" +
				"}" +
				"document.querySelectorAll('.step-master-detail').forEach(function(group){" +
				"  var first=group.querySelector('.step-nav-item[data-img]') || group.querySelector('.step-nav-item');" +
				"  if(first){selectStep(first);}" +
				"});";
	}

	private static String css() {
		return "" +
				"*{box-sizing:border-box;font-family:Segoe UI,Arial,sans-serif;}" +
				"body{margin:0;background:#f2f3f5;color:#1a1a1a;}" +
				".topbar{display:flex;justify-content:space-between;align-items:stretch;" +
				"background:linear-gradient(90deg,#0d0d0d 0%,#0d0d0d 30%,#6b0f16 65%,#c4202e 100%);" +
				"border-bottom:3px solid #e0263f;}" +
				".brand-block{display:flex;align-items:center;gap:10px;padding:12px 22px;}" +
				".brand-block img{height:30px;width:30px;border-radius:6px;object-fit:cover;}" +
				".brand-text{display:flex;flex-direction:column;line-height:1.3;}" +
				".brand-title{font-size:16px;font-weight:700;color:#fff;}" +
				".brand-sub{font-size:11px;color:#f0a8ae;letter-spacing:.4px;}" +
				".suite-block{display:flex;flex-direction:column;justify-content:center;align-items:flex-end;padding:12px 22px;text-align:right;}" +
				".suite-title{font-size:14px;font-weight:700;color:#fff;}" +
				".suite-sub{font-size:11px;color:#f3c8cb;margin-top:3px;}" +
				".layout{display:flex;min-height:100vh;align-items:stretch;}" +
				".sidebar{width:210px;background:linear-gradient(180deg,#0d0d0d 0%,#2a0a0e 50%,#6b0f16 100%);padding:16px 0;flex-shrink:0;}" +
				".nav-title{color:#888;font-size:10px;letter-spacing:1px;padding:12px 18px 6px;}" +
				".sidebar a{display:flex;justify-content:space-between;align-items:center;gap:8px;color:#ddd;text-decoration:none;padding:8px 18px;font-size:12px;border-radius:6px;margin:0 8px;}" +
				".sidebar a:hover{background:rgba(255,255,255,.1);}" +
				".nav-scenario span:first-child{overflow:hidden;text-overflow:ellipsis;white-space:nowrap;max-width:110px;}" +
				"main{flex:1;padding:20px 26px;max-width:1150px;}" +
				".card{background:#fff;border:1px solid #e6e6e6;border-radius:10px;padding:18px 20px;margin-bottom:20px;box-shadow:0 1px 2px rgba(0,0,0,.04);}" +
				".meta-top{display:flex;justify-content:space-between;align-items:flex-start;}" +
				".eyebrow{color:#e0263f;font-size:10px;letter-spacing:1px;font-weight:700;}" +
				"h2{margin:4px 0;font-size:20px;}" +
				".subtitle{color:#777;font-size:12px;}" +
				".meta-grid{display:grid;grid-template-columns:repeat(3,1fr);gap:14px;margin-top:16px;}" +
				".label{color:#999;font-size:10px;letter-spacing:.5px;margin-bottom:3px;}" +
				".value{font-size:13px;font-weight:600;word-break:break-word;color:#222;}" +
				"h3{display:flex;align-items:center;gap:8px;font-size:14px;margin:18px 0 10px;color:#222;}" +
				".dot-red{width:7px;height:7px;border-radius:50%;background:#e0263f;display:inline-block;}" +
				".cards-row{display:flex;gap:14px;flex-wrap:wrap;}" +
				".ring-card{flex:1;min-width:170px;text-align:center;padding:14px;}" +
				".card-title{font-size:12px;color:#777;margin-bottom:6px;}" +
				".ring-value{fill:#1a1a1a;font-size:20px;font-weight:700;}" +
				".ring-label{fill:#999;font-size:8px;}" +
				".legend{margin-top:8px;font-size:11px;color:#555;}" +
				".dot{display:inline-block;width:7px;height:7px;border-radius:50%;margin-right:3px;}" +
				".dot.pass{background:#2ecc71;}.dot.fail{background:#e0263f;}.dot.skip{background:#e0a800;}" +
				"table{width:100%;border-collapse:collapse;font-size:12px;}" +
				"th{text-align:left;color:#888;font-size:10px;padding:8px;border-bottom:1px solid #eee;}" +
				"td{padding:8px;border-bottom:1px solid #f0f0f0;color:#333;}" +
				".total-row td{font-weight:700;color:#111;}" +
				".badge{padding:3px 10px;border-radius:12px;font-size:10px;font-weight:700;flex-shrink:0;}" +
				".badge.pass{background:#e6f4ea;color:#1e8e3e;}" +
				".badge.fail{background:#fce8e8;color:#c5221f;}" +
				".badge.skip{background:#fdf3dd;color:#a66a00;}" +
				".scenario-card{background:#fff;border:1px solid #e6e6e6;border-radius:10px;padding:16px;margin-bottom:14px;}" +
				".scenario-head{display:flex;justify-content:space-between;align-items:flex-start;margin-bottom:12px;}" +
				".step-master-detail{display:flex;gap:14px;border-top:1px solid #f0f0f0;padding-top:12px;}" +
				".step-nav{width:280px;flex-shrink:0;display:flex;flex-direction:column;gap:3px;max-height:360px;overflow-y:auto;}" +
				".step-nav-item{display:flex;align-items:center;gap:8px;padding:7px 9px;border-radius:6px;font-size:12px;cursor:pointer;border:1px solid transparent;}" +
				".step-nav-item:hover{background:#f7f7f8;}" +
				".step-nav-item.active{border-color:#e0263f;background:#fdecee;}" +
				".step-icon{font-weight:700;width:14px;flex-shrink:0;text-align:center;}" +
				".step-nav-item.pass .step-icon{color:#1e8e3e;}.step-nav-item.fail .step-icon{color:#c5221f;}.step-nav-item.skip .step-icon{color:#a66a00;}" +
				".step-nav-text{flex:1;}" +
				".step-viewer{flex:1;display:flex;align-items:center;justify-content:center;background:#fafafa;border:1px solid #eee;border-radius:8px;min-height:300px;padding:10px;}" +
				".step-viewer-img{display:none;max-width:100%;max-height:420px;border-radius:6px;border:1px solid #e2e2e2;}" +
				".step-viewer-placeholder{color:#999;font-size:12px;display:flex;align-items:center;justify-content:center;text-align:center;padding:20px;}" +
				"@media(max-width:800px){.layout{flex-direction:column;}.sidebar{width:100%;}.meta-grid{grid-template-columns:1fr 1fr;}.step-master-detail{flex-direction:column;}.step-nav{width:100%;max-height:220px;}}";
	}
}
