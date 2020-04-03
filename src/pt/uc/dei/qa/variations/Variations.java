package pt.uc.dei.qa.variations;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Variations {

	private int questionId;
	private Map<String, List<String>> variations;
	
	public Variations(int qid) {
		this.questionId = qid;
		
		variations = new LinkedHashMap<>();
	}
	
	public int getId() {
		return questionId;
	}
	
	public boolean hasType(String key) {
		return variations.containsKey(key);
	}
	
	public void addVariation(String key, String var) {
		
		if(!variations.containsKey(key))
			variations.put(key, new ArrayList<>());
		
		variations.get(key).add(var);
	}
	
	public Set<String> getVarTypes() {
		return variations.keySet();
	}
	
	public List<String> getVariations(String key) {
		return variations.get(key);
	}
}
