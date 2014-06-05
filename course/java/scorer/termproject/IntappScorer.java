// BSD License (http://www.galagosearch.org/license)

package scorer.termproject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.galagosearch.core.index.PositionIndexReader;
import org.galagosearch.core.retrieval.structured.CountIterator;
import org.galagosearch.core.retrieval.structured.RequiredStatistics;
import org.galagosearch.core.retrieval.structured.ScoringFunctionIterator;
import org.galagosearch.core.types.DocumentFeature;
import org.galagosearch.core.util.ExtentArray;
import org.galagosearch.tupleflow.Parameters;

@RequiredStatistics(statistics = {"collectionLength", "documentCount"})
public class IntappScorer extends ScoringFunctionIterator {
    double k_1;
    double b;
    double inverseDocumentFrequency;
    double documentFrequency;
    double documentCount;
    double avgDocumentLength;
    double collectionLength;
    HashMap<Integer, ArrayList<Integer>> termPositionsMap = new HashMap<Integer, ArrayList<Integer>>();
    HashMap<Integer, Long> byteLengthMap = new HashMap<Integer, Long>();
    HashMap<Integer, ArrayList<String>> termListMap = new HashMap<Integer, ArrayList<String>>();

    public IntappScorer(Parameters parameters, CountIterator iterator) throws IOException {
        super(iterator);

        // here you write your scoring function needed for a query

//        = new int[extentArray.getPosition()];
//        k_1 = parameters.get("k_1", 1);
//        b = parameters.get("b", 1);

        documentFrequency = 0;
        documentCount = parameters.get("documentCount", 100000);
        collectionLength = parameters.get("collectionLength", 1000000);
        avgDocumentLength = collectionLength / documentCount;
//        System.out.println(((PositionIndexReader.Iterator) iterator).getRecordString());



        if(iterator instanceof PositionIndexReader.Iterator) {
            while(!iterator.isDone()) {

                ArrayList<String> termList = new ArrayList<String>();

                int document = iterator.document();
                ExtentArray extentArray = ((PositionIndexReader.Iterator) iterator).extents();
                ArrayList<Integer> termPositions = new ArrayList<Integer>();

                for (int i = 0; i < extentArray.getPosition(); ++i) {
                    termPositions.add(extentArray.getBuffer()[i].begin);
                }
                termPositionsMap.put(document, termPositions);
                byteLengthMap.put(document, ((PositionIndexReader.Iterator) iterator).getDocumentByteLength());

                iterator.nextDocument();

            }
        }
        iterator.reset();

        while (!iterator.isDone()) {
            documentFrequency += 1;
            iterator.nextDocument();
        }

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
        double score=0;
        int document = iterator.document();
        ArrayList<Integer> termPositions = termPositionsMap.get(document);
        ///////////////////////////////////////////////////////////////////////
        // POSSIBLE FEATURES
        // documentFrequency: number of documents that contains the query term
        // documentCount: total number of documents in the collection
        // collectionLength: length of collection
        // avgDocumentLength: average document length
        // termPositions: positions in the document for the query term
        // count: number of term occurrence in the document for the query term
        // length: length of current document

        double termPosWeightSum = 0;
        if(termPositions != null && count != 0) {
            for(int i = 0; i < termPositions.size(); i++) {

                double value = Math.pow(((double)(length - termPositions.get(i)) / (double)length + 0.5), 2.0);

                if (length > avgDocumentLength) {
                    if(termPositions.get(i)/(double)length < 0.15) {
                        value += value * 0.5;
                    } if(termPositions.get(i)/(double)length < 0.30) {
                        value += value * 0.3;
                    } else if(termPositions.get(i)/(double)length > 0.80) {
                        value += value * 0.3;
                    }
                } else {
                    if(termPositions.get(i)/(double)length < 0.15) {
                        value += value * 0.5;
                    } else if(termPositions.get(i)/(double)length < 0.30) {
                        value += value * 0.3;
                    } else if(termPositions.get(i)/(double)length > 0.80) {
                        value += value * 0.3;
                    }
                }

                termPosWeightSum += value;
            }

            termPosWeightSum = termPosWeightSum * Math.pow(count, 2.0);
        }

        double numerator = termPosWeightSum * (1 + 1);
        double denominator = termPosWeightSum + (1 * (1.0 - 1 + 1 * (length/avgDocumentLength)));
        score = Math.pow(inverseDocumentFrequency, 2.0) * numerator / denominator;

        return score;
    }
}



