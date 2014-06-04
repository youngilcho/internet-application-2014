package scorer.termproject.junheewon;

import java.io.IOException;
import java.util.List;

import org.galagosearch.core.parse.Document;
import org.galagosearch.core.parse.TagTokenizer;

public class QueryModifier {
	final static String CLOSER = " )";
	final static String DIRICHLET_SCORER = "#feature:class=scorer.dirichlet.DirichletScorer( ";
	final static String BM25_SCORER = "#feature:class=scorer.bm25.BM25Iterator( ";
	final static String INTAPP_SCORER = "#feature:class=scorer.termproject.junheewon.IntappScorer( ";
	
	public static String modifyQuery(String query) {
		TagTokenizer tokenizer = new TagTokenizer();
		String modQuery = query;
		StringBuffer sbuff = new StringBuffer();
		// first, check out that inputed query is not a complex query by checking "#" modifier in the text
		if(query.contains("#")) {
			return query;
		}
		
		try {
			Document tokenizeResult = tokenizer.tokenize(query);
			List<String> tokens = tokenizeResult.terms;
			for(int i = 0; i < tokens.size(); i++) {
				// change here if you want to change term score combining policy
//				sbuff.append("#scale:weight=" + 
//						(1.0 * (double)(tokens.size() - i + 1 + tokens.size() + 1) / ((double) (tokens.size() + 1) * 2.0)) + 
//						"( ");
				sbuff.append(INTAPP_SCORER);
//				sbuff.append(BM25_SCORER);
				sbuff.append(tokens.get(i));
				sbuff.append(CLOSER);
				sbuff.append(CLOSER);
				sbuff.append(" ");
			}
			
			if(sbuff.length() > 0) modQuery = sbuff.toString();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		return modQuery;
	}
}
