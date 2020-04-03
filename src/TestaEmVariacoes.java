import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.lucene.queryparser.classic.ParseException;

import pt.uc.dei.qa.agents.AbstractAgent;
import pt.uc.dei.qa.agents.StemQuestionAgent;
import pt.uc.dei.qa.agents.VanillaQuestionAgent;
import pt.uc.dei.qa.answers.HitScore;
import pt.uc.dei.qa.variations.QuestionVariationsProcessor;

public class TestaEmVariacoes {

	private static final String FILE_FAQS_VARS = "resources/AIA-BDE_3vars_3classes.txt";

	private static final int TOP_DOCUMENTS = 5;
	private static final boolean COMPARE_ANSWERS = false;

	private static final boolean IGNORE_ACCENTS = true;
	private static final Collator COLLATOR = Collator.getInstance();

	private static final List<String> FOR_VARS = Arrays.asList(QuestionVariationsProcessor.ORIGINAL, "VG1", "VG2", "VUC");

	public static void main (String args[]) {

		COLLATOR.setStrength(Collator.NO_DECOMPOSITION);

		AbstractAgent[] agentes = {
				new VanillaQuestionAgent(),
				//				new VanillaQuestionPlusAnswerAgent(),
				new StemQuestionAgent(),
				//				new StemQuestionPlusAnswerAgent(),
				//				new StemSynsQuestionAgent(),
				//				new StemSynsQuestionPlusAnswerAgent(),
				//				new LemmaQuestionAgent(),
				//				new LemmaQuestionPlusAnswerAgent(),
				//				new LemmaSynsQuestionAgent(),
				//				new LemmaSynsQuestionPlusAnswerAgent()
		};

		List<Map<String, List<String>>> lista = carregaPerguntas(FILE_FAQS_VARS);
		Map<String, Resultados[]> resultados = new HashMap<>();
		//Map<String, int[]> resultadosFuzzy = new HashMap<>();

		Map<String, Integer> totais = new HashMap<>();

		try {

			for(int i = 0; i < agentes.length; i++)
				agentes[i].createIndex(FILE_FAQS_VARS, true);

			for(Map<String, List<String>> mapa : lista) {
				//for(String var : mapa.keySet()) {
				for(String var : FOR_VARS) { //P, VG1, VG2, VUC

					if(!resultados.containsKey(var)) {
						resultados.put(var, new Resultados[agentes.length]);
						for(int r = 0; r < resultados.get(var).length; r++)
							resultados.get(var)[r] = new Resultados();
						totais.put(var, 1);
					}
					else {
						totais.put(var, totais.get(var)+mapa.get(var).size());
					}

					for(String vv : mapa.get(var)) { //vars para cada entrada

						for(int i = 0; i < agentes.length; i++) {

							List<HitScore> hits = agentes[i].search(vv, TOP_DOCUMENTS);
							if(!hits.isEmpty()) {

								for(int j = 0; j < hits.size(); j++) {
									HitScore hitDoc = hits.get(j);
									String resp = hitDoc.getHit().getAnswer();
									String perg = hitDoc.getHit().getQuestion();

									if((COMPARE_ANSWERS && COLLATOR.compare(resp, mapa.get(QuestionVariationsProcessor.ANSWER).get(0)) == 0)
											|| (!COMPARE_ANSWERS && COLLATOR.compare(perg, mapa.get(QuestionVariationsProcessor.ORIGINAL).get(0)) == 0)) {
										//|| (!COMPARE_ANSWERS && perg.equals(mapa.get(QuestionVariationsProcessor.ORIGINAL).get(0)))) {
										if(j < 5) resultados.get(var)[i].incTop5Normal();
										if(j < 3) resultados.get(var)[i].incTop3Normal();
										if(j == 0) resultados.get(var)[i].incTopNormal();

										break; //se resposta está na posição j, não vai estar noutra
									}
									//perguntas que não mapeadas nelas próprias
									else if(j==0 && var.equals(QuestionVariationsProcessor.ORIGINAL)) {
										System.out.println("Question not matched with question: "+perg+" --- should be --- "+mapa.get(QuestionVariationsProcessor.ORIGINAL).get(0));
									}
								}

								List<HitScore> hitsFuzzy = agentes[i].search(vv, TOP_DOCUMENTS, true);
								if(!hitsFuzzy.isEmpty()) {

									for(int j = 0; j < hitsFuzzy.size(); j++) {
										HitScore hitDoc = hitsFuzzy.get(j);
										String resp = hitDoc.getHit().getAnswer();

										if(resp.equals(mapa.get(QuestionVariationsProcessor.ANSWER).get(0))) {
											if(j < 5) resultados.get(var)[i].incTop5Fuzzy();
											if(j < 3) resultados.get(var)[i].incTop3Fuzzy();
											if(j == 0) resultados.get(var)[i].incTopFuzzy();

											break;
										}
									}
								}
							}
						}
					}
				}
			}

		} catch (IOException | ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		for(String var : resultados.keySet()) {
			System.out.println(var+" ----------");
			for(int i = 0; i < resultados.get(var).length; i++) {
				System.out.println("\t"+agentes[i].getClass()+":");

				System.out.print("\t\tNormal: "+resultados.get(var)[i].getTopNormal()+" -> "+((double)resultados.get(var)[i].getTopNormal()) / totais.get(var));
				System.out.print("\t"+resultados.get(var)[i].getTop3Normal()+" -> "+((double)resultados.get(var)[i].getTop3Normal()) / totais.get(var));
				System.out.print("\t"+resultados.get(var)[i].getTop5Normal()+" -> "+((double)resultados.get(var)[i].getTop5Normal()) / totais.get(var));
				System.out.println();

				System.out.print("\t\tFuzzy: "+resultados.get(var)[i].getTopFuzzy()+" -> "+((double)resultados.get(var)[i].getTopFuzzy()) / totais.get(var));
				System.out.print("\t"+resultados.get(var)[i].getTop3Fuzzy()+" -> "+((double)resultados.get(var)[i].getTop3Fuzzy()) / totais.get(var));
				System.out.print("\t"+resultados.get(var)[i].getTop5Fuzzy()+" -> "+((double)resultados.get(var)[i].getTop5Fuzzy()) / totais.get(var));
				System.out.println();
			}
		}
	}

	private static LinkedList<Map<String, List<String>>> carregaPerguntas(String ficheiro){

		LinkedList<Map<String, List<String>>> lista = new LinkedList<>();

		try {
			BufferedReader reader = new BufferedReader(new FileReader(ficheiro));
			String line = null;

			while((line = reader.readLine()) != null) {

				if(line.startsWith(QuestionVariationsProcessor.ORIGINAL)) {
					lista.add(new HashMap<>());
				}

				int ix = line.indexOf(":");
				String var = line.substring(0, ix);

				//System.err.println(var+" .. "+line.substring(ix+1));
				String txt = line.substring(ix+1);
				if(ix > 0 && !txt.trim().isEmpty() && !var.equals(QuestionVariationsProcessor.SECTION)) {
					if(!lista.getLast().containsKey(var))
						lista.getLast().put(var, new ArrayList<>());
					lista.getLast().get(var).add(txt);
				}
			}
			reader.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return lista;

	}
}

class Resultados{

	private int[] soma; //1, top-3, top-5
	private int[] somaFuzzy; //1, top-3, top-5

	public Resultados() {
		super();
		this.soma = new int[3];
		this.somaFuzzy = new int[3];
	}

	public void incTopNormal() {
		soma[0] += 1;
	}

	public void incTopFuzzy() {
		somaFuzzy[0] += 1;
	}

	public void incTop3Normal() {
		soma[1] += 1;
	}

	public void incTop3Fuzzy() {
		somaFuzzy[1] += 1;
	}

	public void incTop5Normal() {
		soma[2] += 1;
	}

	public void incTop5Fuzzy() {
		somaFuzzy[2] += 1;
	}

	public int getTopNormal() {
		return soma[0];
	}

	public int getTopFuzzy() {
		return somaFuzzy[0];
	}

	public int getTop3Normal() {
		return soma[1];
	}

	public int getTop3Fuzzy() {
		return somaFuzzy[1];
	}

	public int getTop5Normal() {
		return soma[2];
	}

	public int getTop5Fuzzy() {
		return somaFuzzy[2];
	}
}

/*class Variante{

	String id;
	String texto;
	public Variante(String id, String texto) {
		this.id = id;
		this.texto = texto;
	}
}*/
