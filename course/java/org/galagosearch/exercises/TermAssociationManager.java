package org.galagosearch.exercises;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map.Entry;

import org.galagosearch.core.parse.Document;
import org.galagosearch.core.parse.DocumentIndexReader;
import org.galagosearch.core.parse.TagTokenizer;

public class TermAssociationManager {

	private static TermAssociationManager singleton = new TermAssociationManager();
	public static TermAssociationManager get() {
		return singleton;
	}

	private boolean inited = false;

	private ArrayList<String> documentList = null;
	private StringBuffer corpusStr = null;
	private ArrayList<HashMap<String, Integer>> termFreqList = null;
	private String[] topTerms = null;
    private HashMap<String, String> stopWordByTopTerms = new HashMap<String, String>();
	private int[] docFreq = null;

	//    private ArrayList<String> documentList = new ArrayList<String>();
	//    private StringBuffer corpusStr = new StringBuffer();

	String stopwordFile="";
	String corpusFile="";
	private HashMap<String, String> stopwordList = null;
	private static final int MINiNUM_TERM_LENGTH =  3;
	private static final int NUM_TOP_TERM_FREQ   =  3;
	private static final int NUM_ASSO_TERM       = 10;

	TagTokenizer tn = new TagTokenizer();

	public TermAssociationManager() {
	}

	public synchronized void init() throws IOException {
		if(inited) {
			return;
		}


		//here, set path of stopword file
		stopwordFile="stopwords.txt";
		//here, set path of corpus file assigned to you
		corpusFile="doc/reuter.corpus";


		if(stopwordList == null)
			stopwordList=makeStopWordList(stopwordFile);

		if(documentList == null) {
			// 캐싱된게 없다면 가져오기
			documentList = new ArrayList<String>();
			corpusStr = new StringBuffer();

			// galago 함수 통해서 직접 읽는다.
			DocumentIndexReader reader = new DocumentIndexReader(corpusFile);
			DocumentIndexReader.Iterator iterator = reader.getIterator();


			while (!iterator.isDone()) {
				Document document = iterator.getDocument();
				documentList.add(document.text);
				corpusStr.append(" " + document.text);
				iterator.nextDocument();
			}

			//to get global tf list
			topTerms=getTopTermFreq(corpusStr.toString());
			docFreq=new int[topTerms.length];

			termFreqList = makeTermFreqList(documentList);
		}

		// to get df per term
		for(int index=0;index<topTerms.length;index++){
			int docFreqTerm=0;
			for(int cnt=0;cnt<termFreqList.size();cnt++){
				HashMap<String, Integer> termFreq=termFreqList.get(cnt);
				if(termFreq.containsKey(topTerms[index]))docFreqTerm++;
			}
			docFreq[index]=docFreqTerm;

		}
		inited = true;
	}

	public class TermFreq implements Comparable<TermFreq> {
		public String term;
		public float frequency;

		@Override
		public int compareTo(TermFreq o) {
			if(this.frequency == o.frequency) return 0;

			if(this.frequency > o.frequency) return -1;
			else return 1;
		}
	}

	public HashMap<String, String> makeStopWordList(String filePath) throws IOException {
		HashMap<String, String> stopWordList = new HashMap<String, String>();
		File stopwordfile = new File(filePath);

		try {
			BufferedReader stopwordfileBr = new BufferedReader(new FileReader(stopwordfile));

			String wordline = null;
			while ((wordline=stopwordfileBr.readLine())!=null) {
				if(wordline.length()>0)stopWordList.put(wordline, "");
			}
			if(stopwordfileBr!=null)stopwordfileBr.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return stopWordList;
	}

	public HashMap<String, Integer> makeTermFreq(String docStr) throws IOException{
		Document tokenizedResult=tn.tokenize(docStr);

		HashMap<String, Integer> termFreqPair = new HashMap<String, Integer>();

		for(int index=0;index<tokenizedResult.terms.size();index++){
			String term = tokenizedResult.terms.get(index);
			// if term is in stopword list, bypass this term.
			if(stopwordList.containsKey(term)) continue;
            if(stopWordByTopTerms.containsKey(term)) continue;
			if(term.length() < MINiNUM_TERM_LENGTH) continue;
			if(!termFreqPair.containsKey(term)) termFreqPair.put(term, 1);
			else termFreqPair.put(term, termFreqPair.get(term)+1);
		}

		return termFreqPair;
	}

	public ArrayList<HashMap<String, Integer>> makeTermFreqList(ArrayList<String> documentList){

		ArrayList<HashMap<String, Integer>> termFreqList = new ArrayList<HashMap<String, Integer>>();

		for(int index=0;index<documentList.size();index++){
			try {
				termFreqList.add(makeTermFreq(documentList.get(index)));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return termFreqList;
	}

	public String[] getTopTermFreq(String corpusFile) throws IOException{
		HashMap<String, Integer> termFreqPairCorpus = new HashMap<String, Integer>();
		termFreqPairCorpus=makeTermFreq(corpusFile);
		ArrayList<TermFreq> term_frequency=getSortedList(termFreqPairCorpus);
		String[] topTerms=new String[term_frequency.size()];
		for(int index=0;index<term_frequency.size();index++){
			topTerms[index]=term_frequency.get(index).term;
			//			System.out.println(term_frequency.get(index).term+": "+term_frequency.get(index).frequency);
			//			if(index==2)break;

            if (index < 11) {
                stopWordByTopTerms.put(term_frequency.get(index).term, "");
            }
		}

		return topTerms;
	}

	public ArrayList<TermFreq> getSortedList(HashMap<String, Integer> unSortedList){

		ArrayList<TermFreq> term_frequency = new ArrayList<TermFreq>();

		for(Entry<String, Integer> entry : unSortedList.entrySet()) {
			String key = entry.getKey();
			int value = entry.getValue();
			TermFreq tf = new TermFreq();
			tf.term=key;
			tf.frequency=value;
			term_frequency.add(tf);
		}
		Collections.sort(term_frequency);

		return term_frequency;
	}

	public ArrayList<TermFreq> getSortedList2(HashMap<String, Float> unSortedList){

		ArrayList<TermFreq> term_frequency = new ArrayList<TermFreq>();

		for(Entry<String, Float> entry : unSortedList.entrySet()) {
			String key = entry.getKey();
			float value = entry.getValue();

			TermFreq tf = new TermFreq();
			tf.term=key;
			tf.frequency=value;
			term_frequency.add(tf);
		}

		Collections.sort(term_frequency);

		return term_frequency;
	}

	public HashMap<String, Float> MakeAssoTermList(String originalTerm) {
		int indexOfTermA = 0;

		for(int index=0;index<topTerms.length;index++){
			if (topTerms[index].equals(originalTerm)) {
				indexOfTermA = index;
			}
			break;
		}

		HashMap<String, Float> assoValueList = new HashMap<String, Float>();
		String termA=originalTerm;
		String termB="";
		int docFreqTermA=docFreq[indexOfTermA];
		int docFreqTermB=0;
		int docFreqTermAB=0;
		for(int index2=0;index2<topTerms.length;index2++){

			if(topTerms[indexOfTermA].equals(topTerms[index2]))continue;

			termB=topTerms[index2];
			docFreqTermB=docFreq[index2];

			docFreqTermAB=0;
			for(int cnt=0;cnt<termFreqList.size();cnt++){
				HashMap<String, Integer> termFreq=termFreqList.get(cnt);
				if(termFreq.containsKey(termA) && termFreq.containsKey(termB))docFreqTermAB++;
			}

			if (originalTerm.equals(termB)) continue; // no duplicate(beomjun)
			if ((float) docFreqTermAB == 0) continue;

			// here you make term association measures by using docFreqTermA, docFreqTermB, docFreqTermAB
			float assoValue=0;
			//assoValue= (float) docFreqTermAB / (float) (docFreqTermA + docFreqTermB) ; // 다이스 계수 <이게 제일 그나마 눈에 보기에 좋음>
			//			assoValue= (float) docFreqTermAB / (float) (docFreqTermA * docFreqTermB) ; // MI . 눈으로 보기에 매우 부정확함.
			assoValue= (float) docFreqTermAB; // 맨 처음에 했던 시도
			assoValueList.put(termB,assoValue);
		}

//		String[] result = new String[NUM_ASSO_TERM];
//		ArrayList<TermFreq> assoValueSorted=getSortedList2(assoValueList);
//		for (int cnt=0; cnt< NUM_ASSO_TERM ;cnt++){ // NUM_ASSO_TERM에 보여줄 단어들을 적음
//			result[cnt] = assoValueSorted.get(cnt).term ;
//		}
		
		// result with assoValue (assoValueSorted.get(cnt).frequency <=> assoValue)
		HashMap<String, Float> result = new HashMap<String, Float>();
		ArrayList<TermFreq> assoValueSorted=getSortedList2(assoValueList);
		if (assoValueSorted.size() == 0) 
			return null; // if no expand query then return null
		for (int cnt=0; cnt < NUM_ASSO_TERM; cnt++) {
			result.put(assoValueSorted.get(cnt).term, assoValueSorted.get(cnt).frequency);
		}

		return result;
	}
}
