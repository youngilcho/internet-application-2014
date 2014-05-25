package scorer.termproject.beomjunshin;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.galagosearch.core.parse.Document;
import org.galagosearch.core.parse.TagTokenizer;
import org.galagosearch.core.store.SnippetGenerator; // TODO snippet 볼 수 없을까? 
//import org.galagosearch.exercises.TermAssoDemo;
import scorer.termproject.beomjunshin.TermAssociationManager;

public class QueryModifier {
	final static String CLOSER = " )";
	final static String DIRICHLET_SCORER = "#feature:class=scorer.dirichlet.DirichletScorer( ";
	final static String BM25_SCORER = "#feature:class=scorer.bm25.BM25Iterator( ";
	final static String INTAPP_SCORER = "#feature:class=scorer.termproject.beomjunshin.IntappScorer( ";

	public static String modifyQuery(String query) {
		TagTokenizer tokenizer = new TagTokenizer();
		String modQuery = query;
		StringBuffer sbuff = new StringBuffer();		
		SnippetGenerator s = new SnippetGenerator(); // DEBUG How to use it? 

		// first, check out that inputed query is not a complex query by checking "#" modifier in the text
		if(query.contains("#")) {
			return query;
		}

		try {
			Document tokenizeResult = tokenizer.tokenize(query);
			List<String> tokens = tokenizeResult.terms;

      TermAssociationManager termAssociationManager = TermAssociationManager.get();
      termAssociationManager.init();
      HashMap<String, Float> expandTokens = termAssociationManager.MakeAssoTermList(tokens);
      
      // original query with weight 1
      for (String token: tokens) {
				sbuff.append("#scale:weight=");
				sbuff.append( "1" );
				sbuff.append("( ");
				sbuff.append(INTAPP_SCORER);
				sbuff.append(token);
				sbuff.append(CLOSER);
				sbuff.append(CLOSER);
				sbuff.append(" ");
				System.out.print(token + " ");
      } System.out.println();

      if (expandTokens != null) {
        // calculate whole frequency
	      float freqDenominator = 0; 
	      for (String expandTokenKey: expandTokens.keySet()) {
	      	freqDenominator += expandTokens.get(expandTokenKey);
	      	System.out.print(expandTokenKey + "(" + expandTokens.get(expandTokenKey) + ") "); // for debug
	      } System.out.println(); // for debug

	      // expand tokens with weight by assoValue
	      for (String expandTokenKey: expandTokens.keySet()) {
					sbuff.append("#scale:weight=");
					float expandTokenAssoValue = expandTokens.get(expandTokenKey); 
					sbuff.append( expandTokenAssoValue/freqDenominator );
					sbuff.append("( ");
					sbuff.append(INTAPP_SCORER);
					sbuff.append(expandTokenKey);
					sbuff.append(CLOSER);
					sbuff.append(CLOSER);
					sbuff.append(" ");
	      }
      }
			if(sbuff.length() > 0) modQuery = sbuff.toString();
		} catch (IOException e) {
			e.printStackTrace();
		} 

		return modQuery;
	}
}