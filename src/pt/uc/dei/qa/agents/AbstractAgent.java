package pt.uc.dei.qa.agents;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.InvalidPropertiesFormatException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.pt.PortugueseAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.xml.sax.SAXException;

import dictionary.DictionaryLoadException;
import lemma.Lemmatizer;
import pos.POSTagger;
import pt.uc.dei.qa.analyzers.CustomPortugueseAnalyzer;
import pt.uc.dei.qa.answers.Hit;
import pt.uc.dei.qa.answers.HitScore;
import rank.WordRankingLoadException;
import token.Tokenizer;

public abstract class AbstractAgent implements SearchInterface{

	public static final String SERVICE_FIELD = "S";
	public static final String QUESTION_FIELD = "P";
	public static final String ANSWER_FIELD = "R";
	public static final String LEM_QUESTION_FIELD = "PL";
	public static final String LEM_ANSWER_FIELD = "RL";

	protected static StandardAnalyzer analyzerStd;
	protected static PortugueseAnalyzer analyzerPT;
	protected static Analyzer analyzerCustom;
	protected static Tokenizer tokenizerPT;
	protected static POSTagger postaggerPT;
	protected static Lemmatizer lemmatizerPT;

	protected IndexSearcher isearcher;

	protected boolean lemmatizeQuestion, lemmatizeAnswer;
	protected String[] searchFields;

	protected Analyzer analyzer;

	public AbstractAgent(String[] fields, boolean lemQuestion, boolean lemAnswer) {
		System.out.println("Creating agent: "+this.getClass()+" ...");
		System.out.println("\tSearch fields: "+Arrays.asList(fields));
		System.out.println("\tLemmatize question: "+lemQuestion);
		System.out.println("\tLemmatize answer: "+lemAnswer);

		this.searchFields = fields;
		this.lemmatizeQuestion = lemQuestion;
		this.lemmatizeAnswer = lemAnswer;

		initTools();
	}

	protected abstract void initTools();
	//public abstract void createIndex(String file, boolean override) throws IOException;

	protected static PortugueseAnalyzer getAnalyzerPT() {
		if(analyzerPT == null) {
			analyzerPT = new PortugueseAnalyzer();
		}
		return analyzerPT;
	}

	protected static StandardAnalyzer getAnalyzerStandard() {
		if(analyzerStd == null) {
			analyzerStd = new StandardAnalyzer();
		}
		return analyzerStd;
	}

	public CustomPortugueseAnalyzer getCustomAnalyzer(boolean stemming, boolean synonyms) {
		return new CustomPortugueseAnalyzer(stemming, synonyms);
	}

	protected static Tokenizer getTokenizer() throws InvalidPropertiesFormatException, IOException, ParserConfigurationException, SAXException {
		if(tokenizerPT == null)
			tokenizerPT = new Tokenizer();
		return tokenizerPT;
	}

	protected static POSTagger getPOSTagger() throws InvalidPropertiesFormatException, IOException, ParserConfigurationException, SAXException {
		if(postaggerPT == null)
			postaggerPT = new POSTagger();
		return postaggerPT;
	}

	protected static Lemmatizer getLemmatizer() throws NumberFormatException, InvalidPropertiesFormatException, IOException, ParserConfigurationException, SAXException, DictionaryLoadException, WordRankingLoadException {
		if(lemmatizerPT == null)
			lemmatizerPT = new Lemmatizer();
		return lemmatizerPT;
	}

	protected void initNLP() {

		try {
			tokenizerPT = getTokenizer();
			postaggerPT = getPOSTagger();
			lemmatizerPT = getLemmatizer();
		} catch (IOException | ParserConfigurationException | SAXException | NumberFormatException | DictionaryLoadException | WordRankingLoadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void createIndex(String fileFaqs, boolean override) throws IOException{
		createFAQIndex(fileFaqs, lemmatizeQuestion, lemmatizeAnswer, override);
	}

	protected void createFAQIndex(String faqsFile, boolean lemmatizeQuestion, boolean lemmatizeAnswer, boolean override) throws IOException{

		String indexName = indexName(faqsFile, analyzer, lemmatizeQuestion, lemmatizeAnswer);
		//System.err.println(this.getClass()+" -> "+indexName);

		// To store an index on disk, use this instead:
		Directory directory = FSDirectory.open(Paths.get(indexName));
		if(override || !DirectoryReader.indexExists(directory)) {
			System.err.println(this.getClass()+":\tcreating index "+indexName+" ...");

			IndexWriterConfig config = new IndexWriterConfig(analyzer);
			IndexWriter iwriter = new IndexWriter(directory, config);

			BufferedReader reader = new BufferedReader(new FileReader(faqsFile));
			String line = null;
			Document doc = null;
			String service = null;
			while((line = reader.readLine()) != null) {

				if(line.startsWith("S:")) {
					service = line.substring(2);				
				}
				else if(line.startsWith("P:")) {
					doc = new Document();
					String p = line.substring(2);
					doc.add(new TextField(QUESTION_FIELD, p, Field.Store.YES));
					doc.add(new StringField(SERVICE_FIELD, service, Field.Store.YES));

					if(lemmatizeQuestion || lemmatizeAnswer) {
						String l = lemmatize(p);
						doc.add(new TextField(LEM_QUESTION_FIELD, l, Field.Store.YES));
					}
				}
				else if(line.startsWith("R:")) {
					String r = line.substring(2);
					doc.add(new TextField(ANSWER_FIELD, r, Field.Store.YES));
					if(lemmatizeQuestion || lemmatizeAnswer) {
						String l = lemmatize(r);
						doc.add(new TextField(LEM_ANSWER_FIELD, l, Field.Store.YES));
					}
					
					iwriter.addDocument(doc);
				}
			}

			iwriter.close();
			reader.close();
		}

		DirectoryReader ireader = DirectoryReader.open(directory);
		isearcher = new IndexSearcher(ireader);
	}

	private static String indexName(String faqsFile, Analyzer analyzer, boolean lemmatizeQuestion, boolean lemmatizeAnswer) {

		StringBuilder sb = new StringBuilder();

		sb.append("index");
		/*if(faqsFile.equals(FILE_FAQS_TODAS))
			sb.append("_todas");*/

		File file = new File(faqsFile);
		String name = file.getName();
		int ip = name.indexOf(".");
		sb.append("_"+(ip > 0 ? name.substring(0, ip) : name));

		if(analyzer instanceof PortugueseAnalyzer)
			sb.append("_ptan");
		else if(analyzer instanceof StandardAnalyzer)
			sb.append("_san");
		else if(analyzer instanceof CustomPortugueseAnalyzer){

			CustomPortugueseAnalyzer can = (CustomPortugueseAnalyzer)analyzer;

			if(can.appliesStemming())
				sb.append("_stem");
			if(can.getSynonymMap() != null)
				sb.append("_syns");
		}

		if(lemmatizeQuestion || lemmatizeAnswer)
			sb.append("_lempr");

		return sb.toString();
	}

	protected String lemmatize(String texto) {
		String[] tokens = tokenizerPT.tokenize(texto);
		String[] postags = postaggerPT.tag(tokens);

		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < tokens.length; i++) {
			sb.append(lemmatizerPT.lemmatize(tokens[i], postags[i])+" ");
		}
		return sb.toString().trim();
	}

	/*public IndexSearcher getIndexSearcher() {
		return isearcher;
	}*/

	public String[] getSearchFields() {
		return searchFields;
	}

	public List<HitScore> search(String text, int n, boolean fuzzy) throws IOException, ParseException{
		ScoreDoc[] docs = fuzzy ? searchFuzzy(text, n, searchFields) : searchMultiFields(text, n, searchFields);
		
		List<HitScore> list = new ArrayList<>();
		for(int i = 0; i < docs.length; i++) {
			Document d = getDocument(docs[i].doc);
			Hit hit = new Hit(d);
			list.add(new HitScore(hit, docs[i].score));
		}
		return list;
	}
	
	
	public List<HitScore> search(String text, int n) throws IOException, ParseException{
		return search(text, n, false);
	}

	/*public ScoreDoc[] search(String text, int n, boolean fuzzy) throws IOException, ParseException{
		return fuzzy ? searchFuzzy(text, n, searchFields) : searchMultiFields(text, n, searchFields);
	}*/

	protected ScoreDoc[] searchMultiFields(String text, int n, String[] fields) throws IOException, ParseException{

		List<String> listFields = Arrays.asList(fields);
		if(listFields.contains(LEM_QUESTION_FIELD) || listFields.contains(LEM_ANSWER_FIELD))
			text = lemmatize(text);

		text = QueryParser.escape(text);
		MultiFieldQueryParser parser = new MultiFieldQueryParser(fields, analyzer);
		Query query = parser.parse(text);
		//ScoreDoc[] hits = isearcher.search(query, null, 1000).scoreDocs;
		return isearcher.search(query, n).scoreDocs;
	}

	protected ScoreDoc[] searchFuzzy(String text, int n, String[] fields) throws IOException, ParseException{

		List<String> listFields = Arrays.asList(fields);
		if(listFields.contains(LEM_QUESTION_FIELD))
			text = lemmatize(text);

		text = QueryParser.escape(text);

		//TODO: devia ser feito com o tokenizer do Analyzer... ou até com a FuzzyQuery!
		String[] tokens = text.split("[\\p{Punct}\\s]+");
		StringBuilder sb = new StringBuilder();
		for(String t : tokens)
			sb.append(t+"~ ");

		MultiFieldQueryParser parser = new MultiFieldQueryParser(fields, analyzer);
		Query query = parser.parse(sb.toString());

		return isearcher.search(query, n).scoreDocs;
	}

	public Document getDocument(int n) throws IOException{
		return isearcher.doc(n);
	}
	
	public String toString() {
		return this.getClass().getSimpleName();
	}
}
