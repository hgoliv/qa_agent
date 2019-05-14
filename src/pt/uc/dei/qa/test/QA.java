import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

import org.apache.lucene.queryparser.classic.ParseException;

import pt.uc.dei.qa.agents.AbstractAgent;
import pt.uc.dei.qa.agents.LemmaQuestionAgent;
import pt.uc.dei.qa.agents.LemmaQuestionPlusAnswerAgent;
import pt.uc.dei.qa.agents.LemmaSynsQuestionAgent;
import pt.uc.dei.qa.agents.LemmaSynsQuestionPlusAnswerAgent;
import pt.uc.dei.qa.agents.MultiSearcher;
import pt.uc.dei.qa.agents.SearchInterface;
import pt.uc.dei.qa.agents.StemQuestionAgent;
import pt.uc.dei.qa.agents.StemQuestionPlusAnswerAgent;
import pt.uc.dei.qa.agents.VanillaQuestionAgent;
import pt.uc.dei.qa.agents.VanillaQuestionPlusAnswerAgent;
import pt.uc.dei.qa.answers.HitScore;

public class QA {

	private static final String FILE_FAQS_TODAS = "resources/faqs_todas.txt";
	//private static final String FILE_FAQS_RJACSR_AL = "resources/faqs_rjacsr_al.txt";
	//private static final String FILE_PORQUES = "resources/osporques.txt";

	private static final String FAQS_USAR = FILE_FAQS_TODAS;
	
	private static final String FILE_CONFIG = "config.properties";
	//private static final int NUM_AGENTES = 3;
	
	public static void main(String args[]) {

		//SearchInterface searcher = new StemQuestionAgent();
		//SearchInterface searcher = new StemQuestionPlusAnswerAgent();
		//SearchInterface searcher = new LemmaQuestionAgent();
		//SearchInterface searcher = new LemmaQuestionPlusAnswerAgent();
		//SearchInterface searcher = new LemmaSynsQuestionAgent();
		//SearchInterface searcher = new LemmaSynsQuestionPlusAnswerAgent();
		//SearchInterface searcher = new VanillaQuestionAgent();
		//SearchInterface searcher = new VanillaQuestionPlusAnswerAgent();
		
		String fileFaqs = null;
		int numResults = 5;
		boolean overrideIndex = true;
		SearchInterface searcher = null;
		boolean fuzzy = false;
		
		Properties props = new Properties();
		try {
			props.load(new FileReader(FILE_CONFIG));
			
			fileFaqs = props.getProperty("faqs");
			overrideIndex = Integer.parseInt(props.getProperty("over")) == 1;
			numResults = Integer.parseInt(props.getProperty("res"));
			searcher = searcherFromString(props.getProperty("agent"));
			fuzzy = Integer.parseInt(props.getProperty("fuzzy")) == 1;
			
			System.out.println("FAQs File =\t"+fileFaqs);
			System.out.println("Override Index =\t"+overrideIndex);
			System.out.println("Num Results =\t"+numResults);
			System.out.println("Searcher =\t"+searcher);
			System.out.println("Fuzzy =\t"+fuzzy);
			
		} catch (IOException e1) {
			System.err.println("Problema com o ficheiro de configuração!");
			//e1.printStackTrace();
		}
		
		
		try {
			searcher.createIndex(fileFaqs, overrideIndex);

			Scanner keyboard = new Scanner(System.in);
			String text = null;

			while(true) {
				System.out.print("> ");
				text = keyboard.nextLine();

				if(text.trim().isEmpty()) {
					keyboard.close();
					break;
				}
				
				List<HitScore> hits = searcher.search(text, numResults, fuzzy);
				for(HitScore hs : hits) {
					System.out.println(
						hs.getScore()+": ["+hs.getHit().getService()+"] "+hs.getHit().getQuestion()+"\n"+hs.getHit().getAnswer()+"\n"
						);
				}
			}

		} catch (IOException | ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();	
		}
	}

	private static SearchInterface searcherFromString(String s) {
		
		String[] agents = s.split(",");
		MultiSearcher multi = new MultiSearcher(agents.length);
		for(String sa : agents) {
			
			AbstractAgent aa = null;
			if(sa.equals("vanillaq"))
				aa = new VanillaQuestionAgent();
			else if(sa.equals("vanillaqa"))
				aa = new VanillaQuestionPlusAnswerAgent();
			else if(sa.equals("stemq"))
				aa = new StemQuestionAgent();
			else if(sa.equals("stemqa"))
				aa = new StemQuestionPlusAnswerAgent();
			else if(sa.equals("lemq"))
				aa = new LemmaQuestionAgent();
			else if(sa.equals("lemqa"))
				aa = new LemmaQuestionPlusAnswerAgent();
			else if(sa.equals("lemsynq"))
				aa = new LemmaSynsQuestionAgent();
			else if(sa.equals("lemsynqa"))
				aa = new LemmaSynsQuestionPlusAnswerAgent();
			else
				System.out.println("Tipo de agente desconhecido: "+sa);
			
			if(aa != null)
				multi.addAgent(aa);
			
		}
		
		return multi;
	}
}
