package pt.uc.dei.qa.agents;
public class LemmaSynsQuestionPlusAnswerAgent extends AbstractAgent{

	protected static final String[] SEARCH_FIELDS = {LEM_QUESTION_FIELD, LEM_ANSWER_FIELD};
	protected static final boolean LEMMATIZE_QUESTION = true;
	protected static final boolean LEMMATIZE_ANSWER = true;
	protected static final boolean STEMMING = false;
	protected static final boolean SYNONYMS = true;
	
	public LemmaSynsQuestionPlusAnswerAgent() {
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
		createFAQIndex(fileFaqs, true, true, override);
	}*/
}
