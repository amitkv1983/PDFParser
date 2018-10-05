package com.bci.diablo.entity;

public class SummaryRow {
	String severity;
	String subtest;
	public String getSeverity() {
		return severity;
	}
	public void setSeverity(String severity) {
		this.severity = severity;
	}
	public String getSubtest() {
		return subtest;
	}
	public void setSubtest(String subtest) {
		this.subtest = subtest;
	}
	public int getOccurrences() {
		return occurrences;
	}
	public void setOccurrences(int occurrences) {
		this.occurrences = occurrences;
	}
	public String getRecommendations() {
		return recommendations;
	}
	public void setRecommendations(String recommendations) {
		this.recommendations = recommendations;
	}
	int occurrences;
	String recommendations;
	

}
