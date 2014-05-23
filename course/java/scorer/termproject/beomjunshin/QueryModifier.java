package scorer.termproject.beomjunshin;

import java.io.IOException;
import java.util.List;

import org.galagosearch.core.parse.Document;
import org.galagosearch.core.parse.TagTokenizer;
import org.galagosearch.exercises.TermAssociationManager;

public class QueryModifier {
	final static String CLOSER = " )";
	final static String DIRICHLET_SCORER = "#feature:class=scorer.dirichlet.DirichletScorer( ";
	final static String BM25_SCORER = "#feature:class=scorer.bm25.BM25Iterator( ";
	final static String INTAPP_SCORER = "#feature:class=scorer.termproject.youngilcho.IntappScorer( ";
    final static String NEW_INTAPP_SCORER = "#feature:class=scorer.termproject.NewIntappScorer( ";


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


            TermAssociationManager termAssociationManager = TermAssociationManager.get();
            termAssociationManager.init();
            String[] expandTokens = termAssociationManager.MakeAssoTermList(tokens.get(0));

			double originalTermMinWeight = 1.0;
            for(int i = 0; i < tokens.size(); i++) {
				double tokenWeight = (1.0 * (double)(tokens.size() - i + 1 + tokens.size() + 1) / ((double) (tokens.size() + 1) * 2.0));

                originalTermMinWeight = Math.min(tokenWeight, originalTermMinWeight);

				// change here if you want to change term score combining policy
				sbuff.append("#scale:weight=" +
						tokenWeight +
						"( ");
				sbuff.append(INTAPP_SCORER);
				//sbuff.append(BM25_SCORER);
				sbuff.append(tokens.get(i));
				sbuff.append(CLOSER);
				sbuff.append(CLOSER);
				sbuff.append(" ");
			}

            // 원래 입력된 term의 위치에 따른 가중치 중 가장 낮은 걸 가중치로 줌
           for(String s : expandTokens) {
                sbuff.append("#scale:weight=" +
                        originalTermMinWeight * 0.4 +
                        "( ");
                sbuff.append(INTAPP_SCORER);
                //sbuff.append(BM25_SCORER);
                sbuff.append(s);
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
