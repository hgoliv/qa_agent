package pt.uc.dei.qa.agents;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.queryparser.classic.ParseException;

import pt.uc.dei.qa.answers.BordaCountDocs;
import pt.uc.dei.qa.answers.HitScore;

public class MultiSearcher implements SearchInterface{

	private int topDocuments;
	private List<AbstractAgent> agents;

	public MultiSearcher(int top) {

		topDocuments = top;
		agents = new ArrayList<>();
	}

	public void addAgent(AbstractAgent a) {
		agents.add(a);
	}

	public void createIndex(String fileFaqs, boolean override) throws IOException{
		for(AbstractAgent ag : agents) {
			ag.createIndex(fileFaqs, override);
		}
	}

	public List<HitScore> search(String text, int n, boolean fuzzy) throws IOException, ParseException{

		BordaCountDocs voting = new BordaCountDocs(topDocuments, false);
		for(AbstractAgent aa : agents) {
			List<HitScore> hits = aa.search(text, topDocuments, fuzzy);
			voting.updateMap(hits, aa);
		}

		List<HitScore> best = voting.getWinners();
		return best.size() > n ? best.subList(0, n) : best;
	}

	public String toString() {
		return this.getClass().getSimpleName()+agents;
	}
}
