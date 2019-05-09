package pt.uc.dei.qa.agents;
public class LemmaQuestionAgent extends AbstractAgent{

	protected static final String[] SEARCH_FIELDS = {LEM_QUESTION_FIELD};
	protected static final boolean LEMMATIZE_QUESTION = true;
	protected static final boolean LEMMATIZE_ANSWER = false;
	protected static final boolean STEMMING = false;
	protected static final boolean SYNONYMS = false;
	
	public LemmaQuestionAgent() {
		super(SEARCH_FIELDS, LEMMATIZE_QUESTION, LEMMATIZE_ANSWER);
	}
	
	@Override
	protected void initTools() {
		//analyzer = getAnalyzerStandard();
		analyzer = getCustomAnalyzer(STEMMING, SYNONYMS);
		initNLP();
	}
	
/*	@Override
	public void createIndex(String fileFaqs, boolean override) throws IOException{
		createFAQIndex(fileFaqs, true, false, override);
	}*/
}
