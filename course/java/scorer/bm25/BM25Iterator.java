// BSD License (http://www.galagosearch.org/license)

package scorer.bm25;

import java.io.IOException;

import org.galagosearch.core.index.PositionIndexReader;
import org.galagosearch.core.retrieval.structured.CountIterator;
import org.galagosearch.core.retrieval.structured.RequiredStatistics;
import org.galagosearch.core.retrieval.structured.ScoringFunctionIterator;
import org.galagosearch.core.util.ExtentArray;
import org.galagosearch.tupleflow.Parameters;

/**
 * Both Exercise 7.5 and 11.3 ask students to write code to perform BM25
 * weighting.  This code is one sample implementation.  Use it like this:
 *    #feature:class=org.galagosearch.exercises.bm25.BM25Iterator( dog )
 *
 * @author trevor
 */
@RequiredStatistics(statistics = {"collectionLength", "documentCount"})
public class BM25Iterator extends ScoringFunctionIterator {
    double k_1;
    double b;
    double documentFrequency;
    double documentCount;
    double avgDocumentLength;
    double inverseDocumentFrequency;

    public BM25Iterator(Parameters parameters, CountIterator iterator) throws IOException {
        super(iterator);
        
        //call count: 1

        k_1 = parameters.get("k_1", 1);
        b = parameters.get("b", 1);
        documentFrequency = 0;
        documentCount = parameters.get("documentCount", 100000);
        double collectionLength = parameters.get("collectionLength", 1000000);
        avgDocumentLength = collectionLength / documentCount;

        while (!iterator.isDone()) {
            documentFrequency += 1;
            iterator.nextDocument();
        }

//        System.out.println("Collection Length:	"+collectionLength);
//        System.out.println("# of Documents:	"+documentCount);
//        System.out.println("Document Frequency:	"+documentFrequency);

        iterator.reset();
        computeInverseDocumentFrequency();
    }

    public void computeInverseDocumentFrequency() {
        double numerator = documentCount - documentFrequency + 0.5;
        double denominator = documentFrequency + 0.5;

        inverseDocumentFrequency = Math.log(numerator / denominator);
    }
    
    public double score(int document, int length) {
        int count = 0;

        if (iterator.document() == document) {
            count = iterator.count();
        }
        return scoreCount(count, length);
    }

    public double scoreCount(int count, int length) {
        //call count: the number of documents containing terms in a query
    	
//        System.out.println("TF:	"+count);
//        System.out.println("Document length:	"+length);

        double numerator = count * (k_1 + 1);
        double denominator = count + (k_1 * (1.0 - b + b * (length/avgDocumentLength)));

        return inverseDocumentFrequency * numerator / denominator;
    }
}

