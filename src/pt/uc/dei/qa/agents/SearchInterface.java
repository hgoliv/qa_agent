package pt.uc.dei.qa.agents;

import java.io.IOException;
import java.util.List;

import org.apache.lucene.queryparser.classic.ParseException;

import pt.uc.dei.qa.answers.HitScore;

public interface SearchInterface {

	public void createIndex(String fileFaqs, boolean override) throws IOException;
	public List<HitScore> search(String text, int n, boolean fuzzy) throws IOException, ParseException;

}
