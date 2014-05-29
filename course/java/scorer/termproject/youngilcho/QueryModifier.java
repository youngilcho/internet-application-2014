package scorer.termproject.youngilcho;

import java.io.IOException;
import java.util.*;

import org.galagosearch.core.parse.Document;
import org.galagosearch.core.parse.TagTokenizer;
import org.galagosearch.core.store.SnippetGenerator; // TODO snippet 볼 수 없을까?
//import org.galagosearch.exercises.TermAssoDemo;
//import scorer.termproject.beomjunshin.TermAssociationManager;
import org.galagosearch.exercises.TermAssociationManager; // 예전걸로 돌아옴.

public class QueryModifier {
    final static String CLOSER = " )";
    final static String DIRICHLET_SCORER = "#feature:class=scorer.dirichlet.DirichletScorer( ";
    final static String BM25_SCORER = "#feature:class=scorer.bm25.BM25Iterator( ";
    final static String INTAPP_SCORER = "#feature:class=scorer.termproject.youngilcho.IntappScorer( ";

    public static String modifyQuery(String query) {
        TagTokenizer tokenizer = new TagTokenizer();
        String modQuery = query;
        StringBuffer sbuff = new StringBuffer();
        SnippetGenerator s = new SnippetGenerator(); // DEBUG How to use it?

        // first, check out that inputed query is not a complex query by checking "#" modifier in the text
        if (query.contains("#")) {
            return query;
        }

        try {
            Document tokenizeResult = tokenizer.tokenize(query);
            List<String> tokens = new ArrayList<String>();
            if (query.contains("-") || query.contains("/")) {
                tokens.add(query);
            } else {
                tokens = tokenizeResult.terms;
            }

            TermAssociationManager termAssociationManager = TermAssociationManager.get();
            termAssociationManager.init();
            HashMap<String, Float> expandTokens = termAssociationManager.MakeAssoTermList(tokens.get(0));
            // original query with weight 1


            for (int i = 1; i < tokens.size(); i++) {
                HashMap<String, Float> expanded = termAssociationManager.MakeAssoTermList(tokens.get(i));
                if (expanded != null) {
                    if (expandTokens != null) {
                        expandTokens.putAll(expanded);
                    } else {
                        expandTokens = expanded;
                    }
                }


            }

            if(expandTokens != null) {
                Iterator<Map.Entry<String, Float>> it = expandTokens.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<String, Float> entry = it.next();
                    if(termAssociationManager.getTermFreq(entry.getKey()) > 2500) {
                        it.remove();
                    }
                }
            }



            for (String token : tokens) {
                sbuff.append("#scale:weight=");
                sbuff.append("1");
                sbuff.append("( ");
                sbuff.append(INTAPP_SCORER);
                sbuff.append(token);
                sbuff.append(CLOSER);
                sbuff.append(CLOSER);
                sbuff.append(" ");
                //System.out.print(token + " ");
            } //System.out.println();

            if (expandTokens != null) {
                // calculate whole frequency
                float freqDenominator = 0;
                for (String expandTokenKey : expandTokens.keySet()) {
                    freqDenominator += expandTokens.get(expandTokenKey);
                }
                // expand tokens with weight by assoValue
                for (String expandTokenKey : expandTokens.keySet()) {
                    sbuff.append("#scale:weight=");
                    float expandTokenAssoValue = expandTokens.get(expandTokenKey);
                    sbuff.append(expandTokenAssoValue / freqDenominator);
                    sbuff.append("( ");
                    sbuff.append(INTAPP_SCORER);
                    sbuff.append(expandTokenKey);
                    sbuff.append(CLOSER);
                    sbuff.append(CLOSER);
                    sbuff.append(" ");
                }
            }
            if (sbuff.length() > 0) modQuery = sbuff.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return modQuery;
    }
}