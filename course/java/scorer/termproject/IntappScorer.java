// BSD License (http://www.galagosearch.org/license)

package scorer.termproject;

import org.galagosearch.core.index.PositionIndexReader;
import org.galagosearch.core.retrieval.structured.CountIterator;
import org.galagosearch.core.retrieval.structured.RequiredStatistics;
import org.galagosearch.core.retrieval.structured.ScoringFunctionIterator;
import org.galagosearch.core.util.ExtentArray;
import org.galagosearch.tupleflow.Parameters;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

@RequiredStatistics(statistics = {"collectionLength", "documentCount"})
public class IntappScorer extends ScoringFunctionIterator {
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
        documentFrequency = 0;
        documentCount = parameters.get("documentCount", 100000);
        collectionLength = parameters.get("collectionLength", 1000000);
        avgDocumentLength = collectionLength / documentCount;

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

        double termPosWeightSum = 0;

        if(termPositions != null && count != 0) {
            for(int i = 0; i < termPositions.size(); i++) {

                //기존의 term위치 가중치를 제곱하여 가중치를 더욱 강조
                double value = Math.pow(((double)(length - termPositions.get(i)) / (double)length + 0.5), 2.0);

                if (length < avgDocumentLength) {
                    if(termPositions.get(i)/(double)length < 0.25) {
                        value += value * 0.8;
                    } else if(termPositions.get(i)/(double)length < 0.35) {
                        value += value * 0.5;
                    } else if(termPositions.get(i)/(double)length > 0.70) {
                        value += value * 0.5;
                    }
                } else {
                    if(termPositions.get(i)/(double)length < 0.15) {
                        value += value * 0.75;
                    } if(termPositions.get(i)/(double)length < 0.30) {
                        value += value * 0.5;
                    } else if(termPositions.get(i)/(double)length > 0.80) {
                        value += value * 0.5;
                    }
                }

                termPosWeightSum += value;
            }
            //문서에 나오는 쿼리의 개수를 가중하여 더함
            termPosWeightSum = termPosWeightSum * Math.pow(count, 1.5);
        }

        //foundation_bm25_review.pdf 30p 참고하여 b, k_1값 발견
        double k_1 = 1.6;
        double b = 0.8;

        double numerator = termPosWeightSum * (k_1 + 1);
        double denominator = termPosWeightSum + (k_1 * (1.0 - b + b * (length/avgDocumentLength)));

        //스코어에 IDF값에 대한 가중치를 부과하여 계산
        score = Math.pow(inverseDocumentFrequency, 2.0) * numerator / denominator;


        return score;
    }
}
