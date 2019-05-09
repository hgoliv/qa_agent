package pt.uc.dei.qa.agents;
public class VanillaQuestionAgent extends AbstractAgent{

	protected static final String[] SEARCH_FIELDS = {QUESTION_FIELD};
	protected static final boolean LEMMATIZE_QUESTION = false;
	protected static final boolean LEMMATIZE_ANSWER = false;
	
	public VanillaQuestionAgent() {
		super(SEARCH_FIELDS, LEMMATIZE_QUESTION, LEMMATIZE_ANSWER);
	}
	
	@Override
	protected void initTools() {
		//analyzer = getAnalyzerPT();
		analyzer = getAnalyzerStandard();
	}
	
/*	@Override
	public void createIndex(String fileFaqs, boolean override) throws IOException{
		createFAQIndex(fileFaqs, lemmatizeQuestion, lemmatizeAnswer, override);
	}*/
}
