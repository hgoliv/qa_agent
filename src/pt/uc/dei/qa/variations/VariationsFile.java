package pt.uc.dei.qa.variations;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class VariationsFile {

	private int totalQuestions;
	private Map<String, List<Variations>> varsPerSection;
	
	public VariationsFile() {
		varsPerSection = new LinkedHashMap<>();
		totalQuestions = 0;
	}
	
	public Set<String> sections(){
		return varsPerSection.keySet();
	}
	
	public boolean hasSection(String key) {
		return varsPerSection.containsKey(key);
	}
	
	public void addSection(String key) {
		if(!varsPerSection.containsKey(key))
			varsPerSection.put(key, new ArrayList<>());
	}
	
	public void addVariations(String key, Variations vars) {
		totalQuestions++;
		addSection(key);
		varsPerSection.get(key).add(vars);
	}
	
	public List<Variations> getVaritationsInSection(String key) {
		return varsPerSection.get(key);
	}
}
