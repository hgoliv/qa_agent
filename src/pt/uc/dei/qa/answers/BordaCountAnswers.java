package pt.uc.dei.qa.answers;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.search.ScoreDoc;

import pt.uc.dei.qa.agents.AbstractAgent;

//TODO: implementar como AbstractAgent?
public class BordaCountAnswers {

	private int topDocuments;
	private Map<String,List<Integer>> candidates;
	
	private boolean printInformation;

	public BordaCountAnswers(int top, boolean printInfo) {
		this.topDocuments = top;
		this.candidates= new HashMap<>();
		this.printInformation = printInfo;
	}

	public void updateMap(ScoreDoc[] docs, AbstractAgent aa) throws IOException {
		
		for(int i = 0; i < docs.length && i < topDocuments; i++) {

			int points = topDocuments-i;
			String resp = aa.getDocument(docs[i].doc).getField(AbstractAgent.ANSWER_FIELD).stringValue();

			if(!candidates.containsKey(resp))
				candidates.put(resp, new ArrayList<>());
			candidates.get(resp).add(points);
			
			if(printInformation)
				System.out.println("\t+"+points+" : "+resp);
		}
	}
	
	public String getWinner() {
				
		int maxVotes = 0; 
		String winner = null; 
		for (Map.Entry<String,List<Integer>> entry : candidates.entrySet()) 
		{ 
			String key = entry.getKey(); 
			List<Integer> scores = entry.getValue();
			int val = sumValues(scores);
			if (val > maxVotes) {
				maxVotes = val; 
				winner = key; 
			} 

			//TODO: handle ties?
			
			if(printInformation && val > 5) {
				System.out.println("\t-> "+val+" : "+key);
			}
			
		}
		//System.out.println("--- "+winner);
		return winner;
	}
	
	private static int sumValues(List<Integer> scores) {
		int sum = 0;
		for(Integer i : scores)
			sum += i;
		return sum;
	}
}
