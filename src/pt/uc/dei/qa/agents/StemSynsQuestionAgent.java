package pt.uc.dei.qa.agents;
import java.io.IOException;

public class StemSynsQuestionAgent extends AbstractAgent{

	protected static final String[] SEARCH_FIELDS = {QUESTION_FIELD};
	protected static final boolean LEMMATIZE_QUESTION = false;
	protected static final boolean LEMMATIZE_ANSWER = false;
	protected static final boolean STEMMING = true;
	protected static final boolean SYNONYMS = true;
	
	public StemSynsQuestionAgent() {
		super(SEARCH_FIELDS, LEMMATIZE_QUESTION, LEMMATIZE_ANSWER);
	}
	
	@Override
	protected void initTools() {
		//analyzer = getAnalyzerPT();
		analyzer = getCustomAnalyzer(STEMMING, SYNONYMS);
	}
	
/*	@Override
	public void createIndex(String fileFaqs, boolean override) throws IOException{
		createFAQIndex(fileFaqs, false, false, override);
	}*/
}
