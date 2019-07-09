package pt.uc.dei.qa.analyzers;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.pt.PortugueseAnalyzer;
import org.apache.lucene.analysis.pt.PortugueseLightStemFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.synonym.SynonymGraphFilter;
import org.apache.lucene.analysis.synonym.SynonymMap;
import org.apache.lucene.util.CharsRef;

import pt.uc.dei.qa.filters.AccentStripFilter;

public class CustomPortugueseAnalyzer extends Analyzer{

	protected SynonymMap synonymMap;
	private boolean stemming;
	
	//private static String synonymsFile;
	//private static String acronymsFile;

	public CustomPortugueseAnalyzer(boolean stemming) {
		this(true, null);
	}

	public CustomPortugueseAnalyzer(boolean stemming, SynonymsConfig synConfig) {
		this.stemming = stemming;

		if(synConfig != null) {
			setSynonymMap(synConfig);
		}
	}

	@Override
	protected TokenStreamComponents createComponents(java.lang.String fieldName) {

		// does not does not use the Unicode standard annex UAX#29 word boundary rules
		//ClassicTokenizer source = new ClassicTokenizer();
		StandardTokenizer source = new StandardTokenizer();
		TokenFilter filter = new LowerCaseFilter(source);
		filter = new StopFilter(filter, PortugueseAnalyzer.getDefaultStopSet());

		if(synonymMap != null)
			filter = new SynonymGraphFilter(filter, synonymMap, false);

		if(stemming) 
			filter = new PortugueseLightStemFilter(filter);
		else  //para ignorar acentos, mas não parece estar a funcionar! Tentar copiar tudo do PortugueseLightStem... ver quando deixa de funcionar?
			filter = new AccentStripFilter(filter);
			//filter = new ASCIIFoldingFilter(filter);
			//filter = new ICUFoldingFilter(filter);

		return new TokenStreamComponents(source, filter);
	}

	/* 
	 * Acronyms file where each line has an acronym=full name
	 * Synonyms file where each line contains a relation between a and b plus a number n: a RELATION b n
	 */
	protected void setSynonymMap(SynonymsConfig synConfig) {

		SynonymMap.Builder builder = new SynonymMap.Builder(true);
		BufferedReader reader;
		
		try {
			
			if(synConfig.getAcronymsFile() != null) {
				
				reader = new BufferedReader(new FileReader(synConfig.getAcronymsFile()));
				String line = null;
				while((line = reader.readLine()) != null) {

					String[] cols = line.split("=");
					String esc1 = cols[0].replace(' ', SynonymMap.WORD_SEPARATOR).toLowerCase();
					String esc2 = cols[1].replace(' ', SynonymMap.WORD_SEPARATOR).toLowerCase();
					
					//String esc0 = cols[0].replace(' ', SynonymMap.WORD_SEPARATOR);
					//String esc1 = cols[1].replace(' ', SynonymMap.WORD_SEPARATOR);
					
					//System.err.println(cols[0]+"<->"+cols[1]);
					//System.err.println(esc0+"<->"+esc1);
					//System.err.println(Arrays.asList(esc0.split(SynonymMap.WORD_SEPARATOR+""))+"<->"+Arrays.asList(esc1.split(SynonymMap.WORD_SEPARATOR+"")));
					
					builder.add(new CharsRef(esc1), new CharsRef(esc2), true);
					builder.add(new CharsRef(esc2), new CharsRef(esc1), true);
					
					//CharsRef cr0 = Builder.join(cols[0].trim().split(" "), new CharsRefBuilder());
					//CharsRef cr1 = Builder.join(cols[1].trim().split(" "), new CharsRefBuilder());
					//builder.add(cr0, cr1, true);
					//builder.add(cr1, cr0, true);
				}
			}

			if(synConfig.getSynonymsFile() != null) {
				
				reader = new BufferedReader(new FileReader(synConfig.getSynonymsFile()));
				String line = null;
				while((line = reader.readLine()) != null) {

					String[] cols = line.split(" ");
					for(String p : synConfig.getPrefixes()) {
						
						if(cols[1].startsWith(p)) {

							int r = java.lang.Integer.parseInt(cols[3]);
							if(r >= synConfig.getMinConfidence()) {
								//System.err.println(cols[0]+"<->"+cols[2]);
								
								String esc1 = cols[0].replace('_', SynonymMap.WORD_SEPARATOR).toLowerCase();
								String esc2 = cols[2].replace('_', SynonymMap.WORD_SEPARATOR).toLowerCase();
								
								builder.add(new CharsRef(esc1), new CharsRef(esc2), true);
								builder.add(new CharsRef(esc2), new CharsRef(esc1), true);
							}
						}
						
					}
					
				}
			}

			synonymMap = builder.build();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public SynonymMap getSynonymMap() {
		return synonymMap;
	}

	public boolean appliesStemming() {
		return stemming;
	}
}
