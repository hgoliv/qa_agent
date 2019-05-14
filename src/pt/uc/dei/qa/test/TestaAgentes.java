import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.queryparser.classic.ParseException;

import pt.uc.dei.qa.agents.AbstractAgent;
import pt.uc.dei.qa.agents.LemmaSynsQuestionPlusAnswerAgent;
import pt.uc.dei.qa.agents.StemQuestionPlusAnswerAgent;
import pt.uc.dei.qa.agents.VanillaQuestionAgent;
import pt.uc.dei.qa.agents.VanillaQuestionPlusAnswerAgent;
import pt.uc.dei.qa.answers.BordaCountDocs;
import pt.uc.dei.qa.answers.Hit;
import pt.uc.dei.qa.answers.HitScore;

public class TestaAgentes {

	private static final String FILE_FAQS_TODAS = "resources/faqs_todas.txt";
	private static final String FILE_FAQS_RJACSR_AL = "resources/faqs_rjacsr_al.txt";
	private static final String FICHEIRO_PERGUNTAS = "resources/faqs_bde_variantes_hugo.txt";
	private static final int PRIMEIROS_CARS = 250;

	private static final boolean IGNORA_PRIMEIRA_PERGUNTA = true;
	private static final boolean PRINTS_BORDA = false;
	
	private static final int TOP_DOCUMENTS = 5;

	public static void main(String args[]) {

		AbstractAgent[] agentes = {
				new VanillaQuestionAgent(),
//				new VanillaQuestionPlusAnswerAgent(),
//				new StemQuestionAgent(),
				new StemQuestionPlusAnswerAgent(),
//				new StemSynsQuestionAgent(),
//				new StemSynsQuestionPlusAnswerAgent(),
//				new LemmaQuestionAgent(),
//				new LemmaQuestionPlusAnswerAgent(),
//				new LemmaSynsQuestionAgent(),
				new LemmaSynsQuestionPlusAnswerAgent()
		};
		
		//agente x [normal, fuzzy]
/*		boolean[][] usarBorda = {
				
		};*/
		
		Map<String,String> mapa = carregaPerguntas(FICHEIRO_PERGUNTAS);

		int[] certasNormal = new int[agentes.length];
		int[] certasFuzzy = new int[agentes.length];
		int certasBorda = 0;
		int certasBordaFuzzy = 0;
		int certasBordaWithFuzzy = 0;

		try {

			for(int i = 0; i < agentes.length; i++)
				agentes[i].createIndex(FILE_FAQS_RJACSR_AL, false);
			
			for(String p : mapa.keySet()) {

				System.out.println("Query: "+p);
				
/*				BordaCountAnswers borda = new BordaCountAnswers(TOP_DOCUMENTS, PRINTS_BORDA);
				BordaCountAnswers bordaFuzzy = new BordaCountAnswers(TOP_DOCUMENTS, PRINTS_BORDA);
				BordaCountAnswers bordaWithFuzzy = new BordaCountAnswers(TOP_DOCUMENTS, false);*/
				
				BordaCountDocs borda = new BordaCountDocs(TOP_DOCUMENTS, PRINTS_BORDA);
				BordaCountDocs bordaFuzzy = new BordaCountDocs(TOP_DOCUMENTS, PRINTS_BORDA);
				BordaCountDocs bordaWithFuzzy = new BordaCountDocs(TOP_DOCUMENTS, false);
				
				for(int i = 0; i < agentes.length; i++) {

					//System.out.println("\t"+agentes[i].getClass());
					List<HitScore> hits = agentes[i].search(p, TOP_DOCUMENTS);
					borda.updateMap(hits, agentes[i]);
					bordaWithFuzzy.updateMap(hits, agentes[i]);

					if(!hits.isEmpty()) {
						HitScore hitDoc = hits.get(0);
						String resp = trataResposta(hitDoc.getHit().getAnswer());

						//if(resp.substring(0, Math.min(resp.length(),50)).equals(mapa.get(p).substring(0, Math.min(mapa.get(p).length(),50))) && !resp.equals(mapa.get(p)))
						//	System.err.println(resp+"\nVS\n"+mapa.get(p));
						
						if(resp.equals(mapa.get(p)))
							certasNormal[i] += 1;
					}
					
					List<HitScore> hitsFuzzy = agentes[i].search(p, TOP_DOCUMENTS, true);
					bordaFuzzy.updateMap(hitsFuzzy, agentes[i]);
					bordaWithFuzzy.updateMap(hitsFuzzy, agentes[i]);
					
					if(!hitsFuzzy.isEmpty()) {
						HitScore hitDoc = hitsFuzzy.get(0);
						String resp = trataResposta(hitDoc.getHit().getAnswer());

						if(resp.equals(mapa.get(p)))
							certasFuzzy[i] += 1;
					}
				}
				
				Hit bordaWinner = borda.getWinner();
				if(bordaWinner != null) {
					String bordaWinnerStr = trataResposta(bordaWinner);
					if(bordaWinnerStr.equals(mapa.get(p)))
						certasBorda++;
				}
				bordaWinner = bordaFuzzy.getWinner();
				if(bordaWinner != null) {
					String bordaWinnerStr = trataResposta(bordaWinner);
					if(bordaWinnerStr.equals(mapa.get(p)))
						certasBordaFuzzy++;
				}
				bordaWinner = bordaWithFuzzy.getWinner();
				if(bordaWinner != null) {
					String bordaWinnerStr = trataResposta(bordaWinner);
					if(bordaWinnerStr.equals(mapa.get(p)))
						certasBordaWithFuzzy++;
				}
				
/*				String bordaWinner = borda.getWinner();
				if(bordaWinner != null) {
					bordaWinner = trataResposta(bordaWinner);
					if(bordaWinner.equals(mapa.get(p)))
						certasBorda++;
				}
				bordaWinner = bordaFuzzy.getWinner();
				if(bordaWinner != null) {
					bordaWinner = trataResposta(bordaWinner);
					if(bordaWinner.equals(mapa.get(p)))
						certasBordaFuzzy++;
				}
				bordaWinner = bordaWithFuzzy.getWinner();
				if(bordaWinner != null) {
					bordaWinner = trataResposta(bordaWinner);
					if(bordaWinner.equals(mapa.get(p)))
						certasBordaWithFuzzy++;
				}*/
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		for(int i = 0; i < certasNormal.length; i++) {
			System.out.println("\t"+agentes[i].getClass()+":");
			System.out.println("\t\tNormal: "+certasNormal[i]+" -> "+((double)certasNormal[i]) / mapa.keySet().size());
			System.out.println("\t\tFuzzy: "+certasFuzzy[i]+" -> "+((double)certasFuzzy[i]) / mapa.keySet().size());
		}
		
		//TODO: usar diretamente a resposta dos agentes, para não ter de repetir as queries!
		
		System.out.println("\tBordaCount: "+certasBorda+" -> "+((double)certasBorda) / mapa.keySet().size());
		System.out.println("\tBordaCountFuzzy: "+certasBordaFuzzy+" -> "+((double)certasBordaFuzzy) / mapa.keySet().size());
		System.out.println("\tBordaCountNormalFuzzy: "+certasBordaWithFuzzy+" -> "+((double)certasBordaWithFuzzy) / mapa.keySet().size());
		
	}

	private static Map<String,String> carregaPerguntas(String ficheiro){

		Map<String,String> mapa = new HashMap<>();

		try {
			BufferedReader reader = new BufferedReader(new FileReader(ficheiro));
			String line = null;

			List<String> perguntas = new ArrayList<String>();
			while((line = reader.readLine()) != null) {

				if(line.startsWith("P:")) {

					perguntas.add(line.substring(2).trim());
				}
				else if(line.startsWith("R:")) {

					String resposta = trataResposta(line.substring(2));

					int i = IGNORA_PRIMEIRA_PERGUNTA ? 1 : 0;
					for(; i < perguntas.size(); i++) {
						mapa.put(perguntas.get(i), resposta);
					}
					perguntas = new ArrayList<String>();
				}
			}
			reader.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return mapa;

	}

	private static String trataResposta(Hit hit) {

		String resposta = hit.getAnswer();
		return trataResposta(resposta);
	}
	
	private static String trataResposta(String resposta) {

		String tratada = resposta.length() > PRIMEIROS_CARS ? resposta.substring(0, PRIMEIROS_CARS) : resposta;
		return tratada.toLowerCase().trim();
	}
}
