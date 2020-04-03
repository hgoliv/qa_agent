package pt.uc.dei.qa.analyzers;

import java.util.Arrays;

public class SynonymsConfig {

	private String synonymsFile;
	private String[] prefixes;
	private double minConfidence;
	
	private String acronymsFile;
	
	public SynonymsConfig() {
		synonymsFile = null;
		prefixes = new String[]{"SINONIMO"};
		minConfidence = -1;
		acronymsFile = null;
	}
	
	public SynonymsConfig(String synonymsFile, String[] prefixes, double minConfidence, String acronymsFile) {
		this.synonymsFile = synonymsFile;
		this.prefixes = prefixes;
		this.minConfidence = minConfidence;
		this.acronymsFile = acronymsFile;
	}

	public void setSynonymsFile(String synonymsFile) {
		this.synonymsFile = synonymsFile;
	}

	public void setPrefixes(String[] prefix) {
		this.prefixes = prefix;
	}

	public void setMinConfidence(double minConfidence) {
		this.minConfidence = minConfidence;
	}

	public void setAcronymsFile(String acronymsFile) {
		this.acronymsFile = acronymsFile;
	}

	public String getSynonymsFile() {
		return synonymsFile;
	}

	public String[] getPrefixes() {
		return prefixes;
	}

	public double getMinConfidence() {
		return minConfidence;
	}

	public String getAcronymsFile() {
		return acronymsFile;
	}
	
	public String toString() {
		return this.getClass().getSimpleName()+":[Synonyms="+synonymsFile+"; prefix="+Arrays.asList(prefixes)+"; minConfidence="+minConfidence+"; acronyms="+acronymsFile+"]";
	}
}
