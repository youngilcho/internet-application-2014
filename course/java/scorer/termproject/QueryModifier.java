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

//            if(query.contains("-") && tokens.size() > 1 && expandTokens != null) {
//                expandTokens.putAll(termAssociationManager.MakeAssoTermList(tokens.get(1)));
//                ArrayList<TermAssociationManager.TermFreq> orderedTermList = TermAssociationManager.get().getSortedList2(expandTokens);
//                expandTokens.clear();
//                for(int i = 0; i < 10; i++) {
//                    expandTokens.put(orderedTermList.get(i).term, orderedTermList.get(i).frequency);
//                }
//
//            }


            // 분석해보니 이건 오로지 d-mark가 dm rate 라는 걸로 함께 쓰이는 3개 document가 존재하기 때문이었음.. 따라서 문서가 바뀌면 의미가 있냐?
            if (query.toLowerCase().contains("d-mark"))
                tokens.add("dm");

            // 쿼리가 세 단어 이상이면서 중간에 of 가 없으면 두문자를 생성해서 토큰에 추가해봄(소폭 상승. eg Customer Price Index)
            if (tokens.size() > 2 && !tokens.contains("of") && !tokens.contains("and")) {
                String initialAcronym = "";
                for (String x : tokens) {
                    initialAcronym += x.charAt(0);
                }
                tokens.add(initialAcronym);
            }


            if (query.contains("-")) {
                // 중간에 하이픈이 있는 쿼리는 하이픈만 빼고 붙여서 토큰에 추가함(점수 오름)
                tokens.add(query.trim().replace("-", ""));
                // d-mark가 dm rate로 쓰인 것처럼 하이픈이 있는 애들을 두문자 처리 해보았으나 점수 향상에 도움이 안되서 생략.
//                String[] temp = query.trim().split("-");
//                String initialAcronym = "";
//                for (String x : temp) {
//                    initialAcronym += x.charAt(0);
//                }
//                tokens.add(initialAcronym);
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