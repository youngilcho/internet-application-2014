package scorer.termproject;

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
    final static String INTAPP_SCORER = "#feature:class=scorer.termproject.IntappScorer( ";

    public static String modifyQuery(String query) {
        TagTokenizer tokenizer = new TagTokenizer();
        String modQuery = query;
        StringBuffer sbuff = new StringBuffer();

        if (query.contains("#")) {
            return query;
        }

        try {
            Document tokenizeResult = tokenizer.tokenize(query);
            List<String> tokens = tokenizeResult.terms;

            TermAssociationManager termAssociationManager = TermAssociationManager.get();
            termAssociationManager.init();
            HashMap<String, Float> expandTokens = termAssociationManager.MakeAssoTermList(tokens.get(0));

            // original query with weight

            if (query.toLowerCase().contains("d-mark"))
                tokens.add("dm");

            if (query.contains("-")) {
                tokens.add(query.trim().replace("-", ""));

                String[] temp = query.trim().split("-");
                String initialAcronym = "";
                for (String x : temp) {
                    initialAcronym += x.charAt(0);
                }
                //tokens.add(initialAcronym);

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
                    sbuff.append((expandTokenAssoValue / freqDenominator) * 0.15f);
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