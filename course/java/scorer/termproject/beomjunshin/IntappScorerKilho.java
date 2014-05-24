// BSD License (http://www.galagosearch.org/license)

package scorer.beomjunshin.termproject;

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
	double documentFrequency;
    double documentCount;
    double avgDocumentLength;
    double collectionLength;
    HashMap<Integer, ArrayList<Integer>> termPositionsMap = new HashMap<Integer, ArrayList<Integer>>();
    HashMap<Integer, Long> byteLengthMap = new HashMap<Integer, Long>();

    public IntappScorer(Parameters parameters, CountIterator iterator) throws IOException {
        super(iterator);
        
        // here you write your scoring function needed for a query
        
//        = new int[extentArray.getPosition()];
        documentFrequency = 0;
        documentCount = parameters.get("documentCount", 100000);
        collectionLength = parameters.get("collectionLength", 1000000);
        avgDocumentLength = collectionLength / documentCount;
//        System.out.println(((PositionIndexReader.Iterator) iterator).getRecordString());
        if(iterator instanceof PositionIndexReader.Iterator) {
        	while(!iterator.isDone()) {
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
    	
        //here you write your scoring function needed for a document
    	// SAMPLE CODE. replace it.
    	double termPosWeightSum = 0;
    	if(termPositions != null && count != 0) {
    		for(int i = 0; i < termPositions.size(); i++) {
    			int k = termPositions.size()-i-1;
    			double value1 = 1.2*((double)(length - termPositions.get(i)) / (double)length + 1.0) / 2.0;
    			double value2 = ((double)(length - termPositions.get(k)) / (double)length + 1.0) / 2.0;

    			double value = Math.max(value1, value2);
    			if(value > 0) termPosWeightSum += 12*value;	//¹®¼­ ¾Õ,µÚ¿¡ÀÖ´Â ´Ü¾î Áß¿ä!
    			else termPosWeightSum += 1.0;
    		}
    	}
    		

    	double numerator = termPosWeightSum * (1.2 + 1);
    	double denominator = termPosWeightSum + (1.2 * (1.0 - 0.8 + 0.8 * (length/avgDocumentLength)));	//ÆÄ¶ó¹ÌÅÍ º¯°æ
    	score = Math.log((documentCount - documentFrequency + 0.5) / (documentFrequency + 0.5)) * numerator / denominator;
    	
    	
    	
    	
    	

    	// Default baseline : BM25
//    	double numerator = count * (1 + 1);
//    	double denominator = count + (1 * (1.0 - 1 + 1 * (length/avgDocumentLength)));
//   	score = numerator / denominator;


        return score;
    }
}
