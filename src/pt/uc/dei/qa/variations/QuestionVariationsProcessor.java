package pt.uc.dei.qa.variations;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class QuestionVariationsProcessor {

	public static final String ORIGINAL = "P";
	public static final String ANSWER = "R";
	public static final String SECTION = "S";

	public static VariationsFile loadFileContents(String fileName){

		System.out.println("Loading: "+fileName);
		
		VariationsFile contents = new VariationsFile();

		try {
			BufferedReader reader = new BufferedReader(new FileReader(fileName));

			String line = null;
			String sec = null;
			int li = 0;
			Variations vars = null;
			
			while((line = reader.readLine()) != null) {
				li++;
				
				int ix = line.indexOf(":");
				String tag = line.substring(0, ix);
				String txt = line.substring(ix+1);

				if(tag.equals(QuestionVariationsProcessor.SECTION)) {
					sec = txt;

					if(!contents.hasSection(txt)) {
						contents.addSection(sec);
					}
				}
				else { 

					if(tag.equals(QuestionVariationsProcessor.ORIGINAL)) {
						vars = new Variations(li);
					}

					vars.addVariation(tag, txt);

					if(tag.equals(QuestionVariationsProcessor.ANSWER)) {
						if(vars != null)
							contents.addVariations(sec, vars);
					}
				}
			}
			
			reader.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return contents;
	}
}
