// BSD License (http://www.galagosearch.org/license)
package org.galagosearch.core.tools;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.galagosearch.core.retrieval.Retrieval;
import org.galagosearch.core.retrieval.ScoredDocument;
import org.galagosearch.core.retrieval.query.Node;
import org.galagosearch.core.retrieval.query.SimpleQuery;
import org.galagosearch.core.retrieval.query.StructuredQuery;
import org.galagosearch.exercises.TermAssociationManager;
import org.galagosearch.tupleflow.Parameters;

import scorer.termproject.youngilcho.QueryModifier;
import scorer.termproject.youngilcho.AsyncTaskService;

/**
 *
 * @author trevor
 */
public class BatchSearch {
    public static Node parseQuery(String query, Parameters parameters) {
        String queryType = parameters.get("queryType", "complex");

        if (queryType.equals("simple")) {
            return SimpleQuery.parseTree(query);
        }

        return StructuredQuery.parse(query);
    }

    public static String formatScore(double score) {
        double difference = Math.abs(score - (int) score);

        if (difference < 0.00001) {
            return Integer.toString((int) score);
        }
        return String.format("%10.8f", score);
    }

    public static void run(String[] args, final PrintStream out) throws Exception {
        // read in parameters
        final Parameters parameters = new Parameters(args);
        final List<Parameters.Value> queries = parameters.list("query");



        // record results requested
        final int requested = (int) parameters.get("count", 1000);

        AsyncTaskService.get().init();
        // TermAssociationManager를 먼저 init 해둬야 함 그래야 thread safe
        TermAssociationManager.get().init();

        final int[] asyncIndex = {0};
        for (final Parameters.Value query : queries) {
            AsyncTaskService.get().runTask(AsyncTaskService.TAG_TERM_ASSO, new Runnable() {
                @Override
                public void run() {
                    try {
                        // open index
                        final Retrieval retrieval = Retrieval.instance(parameters.get("index"), parameters);

                        String queryText = query.get("text");
                        // IntApp modified.
                        String modquery = QueryModifier.modifyQuery(queryText);
                        Node queryRoot = parseQuery(modquery, parameters);
                        queryRoot = retrieval.transformQuery(queryRoot);

                        ScoredDocument[] results = retrieval.runQuery(queryRoot, requested);

                        for (int i = 0; i < results.length; i++) {
                            String document = retrieval.getDocumentName(results[i].document);
                            double score = results[i].score;
                            int rank = i + 1;
                            System.out.print(String.format("%s Q0 %s %d %s galago\n", query.get("number"), document, rank,
                                    formatScore(score)));
                        }
                        asyncIndex[0]++;

                        if(asyncIndex[0] == queries.size())
                            System.exit(0);

                    } catch (Exception ex) {
                        Thread t = Thread.currentThread();
                        t.getUncaughtExceptionHandler().uncaughtException(t, ex);
                    }
                }
            });
        }
    }

    public static void main(String[] args) throws Exception {
        run(args, System.out);
    }
}
