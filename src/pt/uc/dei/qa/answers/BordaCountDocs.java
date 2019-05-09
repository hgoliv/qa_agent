package pt.uc.dei.qa.answers;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.search.ScoreDoc;

import pt.uc.dei.qa.agents.AbstractAgent;

public class BordaCountDocs {

	private int topDocuments;
	private Map<Hit,List<Integer>> candidates;
	
	private boolean printInformation;

	public BordaCountDocs(int top, boolean printInfo) {
		this.topDocuments = top;
		this.candidates= new HashMap<>();
		this.printInformation = printInfo;
	}

	public void updateMap(List<HitScore> hits, AbstractAgent aa) throws IOException {
		
		for(int i = 0; i < hits.size() && i < topDocuments; i++) {

			int points = topDocuments-i;
			Hit hit = hits.get(i).getHit();

			if(!candidates.containsKey(hit))
				candidates.put(hit, new ArrayList<>());
			candidates.get(hit).add(points);
			
			if(printInformation)
				System.out.println("\t+"+points+" : "+hit.getAnswer());
		}
	}
	
	public Hit getWinner() {
				
		int maxVotes = 0; 
		Hit winner = null; 
		for (Map.Entry<Hit,List<Integer>> entry : candidates.entrySet()) 
		{ 
			Hit key = entry.getKey(); 
			List<Integer> scores = entry.getValue();
			int val = sumValues(scores);
			if (val > maxVotes) {
				maxVotes = val; 
				winner = key; 
			} 

			//TODO: handle ties?
			
			if(printInformation && val > 5) {
				System.out.println("\t-> "+val+" : "+key.getAnswer());
			}
			
		}
		//System.out.println("--- "+winner);
		return winner;
	}
	
	public List<HitScore> getWinners() {
		
		List<HitScore> all = new ArrayList<>();
		
		for (Map.Entry<Hit,List<Integer>> entry : candidates.entrySet()) {

			Hit key = entry.getKey();
			List<Integer> scores = entry.getValue();
			int val = sumValues(scores);
			
			all.add(new HitScore(key, val));
			
			if(printInformation && val > 5) {
				System.out.println("\t-> "+val+" : "+key.getAnswer());
			}
		}
		
		Collections.sort(all);
		
		return all.size() > topDocuments ? all.subList(0, topDocuments) : all;
	}
	
	private static int sumValues(List<Integer> scores) {
		int sum = 0;
		for(Integer i : scores)
			sum += i;
		return sum;
	}
}
