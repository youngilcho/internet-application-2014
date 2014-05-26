// BSD License (http://www.galagosearch.org/license)

package scorer.termproject.beomjunshin;

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
	HashMap<Integer, ArrayList<String>> termListMap = new HashMap<Integer, ArrayList<String>>();

	public IntappScorer(Parameters parameters, CountIterator iterator) throws IOException {
		super(iterator);

		// here you write your scoring function needed for a query
		documentFrequency = 0;
		documentCount = parameters.get("documentCount", 100000);
		collectionLength = parameters.get("collectionLength", 1000000);
		avgDocumentLength = collectionLength / documentCount;

		if(iterator instanceof PositionIndexReader.Iterator) {
			while(!iterator.isDone()) {

				ArrayList<String> termList = new ArrayList<String>(); // NOTE 아직 안 쓰이고 있음

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
		int B = 0;
		///////////////////////////////////////////////////////////////////////
		// POSSIBLE FEATURES
		// documentFrequency: number of documents that contains the query term
		// documentCount: total number of documents in the collection
		// collectionLength: length of collection
		// avgDocumentLength: average document length
		// termPositions: positions in the document for the query term
		// count: number of term occurrence in the document for the query term
		// length: length of current document
		// --------------------------------------
		// here you write your scoring function needed for a document // SAMPLE CODE. replace it.
		if (B == 0) {

			double termPosWeightSum = 0;
			if(termPositions != null && count != 0) {
				for(int i = 0; i < termPositions.size(); i++) {
					double value;

					// TRY1 / TODO : length에 따른 weight 을 어떻게 더 잘 줄까?? 맨앞과 맨끝에? 
					if (length < 30) {
						value = ((double)(length - termPositions.get(i)) / (double)length + 1.0) / 2.0; // NOTE
					} else if (length < 50) {
						value = ((double)(length - termPositions.get(i)) / (double)length + 1.0) / 1.3; // NOTE
					} else if (length < 200){
						value = ((double)(length - termPositions.get(i)) / (double)length + 1.0) / 1.0; // NOTE 
					} else {
						value = ((double)(length - termPositions.get(i)) / (double)length + 1.0) / 1.0; // NOTE 대강봤을 때, 200이상이 롱테일 뒤에 위치할수록 가중치 더 큼 
					}

					if (value > 0) termPosWeightSum += value; // NOTE value가 0보다 작은 경우가 발생할 수 있을까?
					else termPosWeightSum += 1.0;
				}
			}

			double numerator = termPosWeightSum * (1 + 1); // NOTE fi(문서에서 용어 i의 tf)에 위치에 따른 가중치 부과함
			double denominator = termPosWeightSum + (1 * (1.0 - 1 + 1 * (length/avgDocumentLength))); //
			score = Math.log((documentCount - documentFrequency + 0.5) / (documentFrequency + 0.5)) * numerator / denominator;

		} else {
			double termPosWeightSum = 0;
			if(termPositions != null && count != 0) {
				for(int i = 0; i < termPositions.size(); i++) {
					double value;

					// TRY1 / TODO : length에 따른 weight 을 어떻게 더 잘 줄까?? 맨앞과 맨끝에? 
					if (length < 30) {
						value = ((double)(length - termPositions.get(i)) / (double)length + 1.0) / 2.0; // NOTE
					} else if (length < 50) {
						value = ((double)(length - termPositions.get(i)) / (double)length + 1.0) / 1.3; // NOTE
					} else if (length < 200){
						value = ((double)(length - termPositions.get(i)) / (double)length + 1.0) / 1.0; // NOTE 
					} else {
						value = ((double)(length - termPositions.get(i)) / (double)length + 1.0) / 1.0; // NOTE 대강봤을 때, 200이상이 롱테일 뒤에 위치할수록 가중치 더 큼 
					}

					if (value > 0) termPosWeightSum += value; // NOTE value가 0보다 작은 경우가 발생할 수 있을까?
					else termPosWeightSum += 1.0;
				}
			}
			double lambda = 0.1;
			double numerator2 = (1.0 - lambda) * count / length;
			double denominator2 = lambda * documentFrequency / collectionLength;
			double numerator = termPosWeightSum * (1 + 1); // NOTE fi(문서에서 용어 i의 tf)에 위치에 따른 가중치 부과함
			double denominator = termPosWeightSum + (1 * (1.0 - 1 + 1 * (length/avgDocumentLength))); //
			
			score = (Math.log((count+1.0) * avgDocumentLength/length)*Math.log((documentCount+1.0)/(documentFrequency+1.0)))
					*(numerator / denominator)*length/avgDocumentLength + 
					10*Math.log(numerator2/denominator2+1);
//			score = Math.log(numerator2/denominator2+1);
		}
		return score;
	}
}