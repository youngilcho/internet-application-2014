package org.galagosearch.exercises;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.galagosearch.core.parse.Document;
import org.galagosearch.core.parse.DocumentIndexReader;
import org.galagosearch.core.parse.TagTokenizer;

public class TermAssoDemo {
    static boolean isdebug = true;
    String inputTerm = "";
    String stopwordFile="";
    String corpusFile="";
    String rawCorpusFile="";
    HashMap<String, String> stopwordList;
    private static final int MINiNUM_TERM_LENGTH =  4;
    private static final int NUM_TOP_TERM_FREQ   =  2;
    private static final int NUM_ASSO_TERM       = 5;

    TagTokenizer tn = new TagTokenizer();

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

    public TermAssoDemo(String objectTerm) {
        this.inputTerm = objectTerm;
    }

    public String ReadCorpus(String filePath) throws IOException {
        File corpusFile = new File(filePath);
        StringBuffer htmlcode = new StringBuffer();

        try {
            BufferedReader corpusFileBr = new BufferedReader(new FileReader(corpusFile));

            String htmlCodeLine = null;
            while ((htmlCodeLine=corpusFileBr.readLine())!=null) {
                htmlcode.append(htmlCodeLine);
            }

            if(corpusFileBr!=null)corpusFileBr.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return htmlcode.toString();
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

    public ArrayList<String> fetchBlocks(String htmlCode, String openBlockID, String closeBlockID) throws Exception{

        ArrayList<String> stringBlocks = new ArrayList<String>();

        Pattern pattern = Pattern.compile(openBlockID+".*?"+closeBlockID);
        Matcher match = pattern.matcher(htmlCode);

        while(match.find()){
            String stringBlock = match.group();
            stringBlock = stringBlock.substring(stringBlock.indexOf(openBlockID)+openBlockID.length(), stringBlock.length()-closeBlockID.length()).trim();
            stringBlocks.add(stringBlock);
        }
        return stringBlocks;
    }


    public HashMap<String, Integer> makeTermFreq(String docStr) throws IOException{
        Document tokenizedResult=tn.tokenize(docStr);

        HashMap<String, Integer> termFreqPair = new HashMap<String, Integer>();

        for(int index=0;index<tokenizedResult.terms.size();index++){
            String term = tokenizedResult.terms.get(index);
            // if term is in stopword list, bypass this term.
            if(stopwordList.containsKey(term)) continue;
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

    public String[] MakeAssoTermList() throws IOException {
        //here, set path of stopword file
        stopwordFile="../stopwords.txt";
        //here, set path of corpus file assigned to you
        corpusFile="../doc/reuter.corpus";

        DocumentIndexReader reader = new DocumentIndexReader(corpusFile);
        DocumentIndexReader.Iterator iterator = reader.getIterator();

        ArrayList<String> documentList = new ArrayList<String>();

        StringBuffer corpusStr = new StringBuffer();

        while (!iterator.isDone()) {
            Document document = iterator.getDocument();
            documentList.add(document.text);
            corpusStr.append(" " + document.text);
            iterator.nextDocument();
        }

        stopwordList=makeStopWordList(stopwordFile);

        //String corpusStr=ReadCorpus(rawCorpusFile);

        // Object Term 은?
        String objectTerm = inputTerm;

        // 모든 term 들의 df 구하기.. 뒤에서 loop을 돌기 위한 밑걸음. TopTerms 을 가지고 loop을 돌거임..
        ArrayList<HashMap<String, Integer>> termFreqList = makeTermFreqList(documentList);
        // 위에 2줄은 document 별로 tf 구하기

        if (isdebug) System.out.println("termFreqList는 만들어졌음....");
        String[] topTerms = getTopTermFreq(corpusStr.toString());
        int[] docFreq=new int[topTerms.length]; // 각 term 별 df
        for(int index=0;index<topTerms.length;index++){
            int docFreqTerm=0;
            for(int cnt=0;cnt<termFreqList.size();cnt++){
                HashMap<String, Integer> termFreq=termFreqList.get(cnt);
                if(termFreq.containsKey(topTerms[index])) docFreqTerm++;
            }
            docFreq[index]=docFreqTerm;
        } // 여기까지 docFreq에는 모든 단어들의 df 가 소팅된 순서로 들어있음
        // termFreqList는 [ <#첫번째 document; "단어" => "갯수"> , <#두번째 document... ] 형식으로 정리되어있음

        String termA = objectTerm; // String을 String[]으로 바꿔야할 듯
        int docFreqTermA = 0;
        for(int cnt=0; cnt < termFreqList.size(); cnt++){
            HashMap<String, Integer> termFreq = termFreqList.get(cnt);
            if(termFreq.containsKey(termA)) docFreqTermA++;
        } // Object Term 의 freq을 구함

        if (docFreqTermA == 0) {
            // 이런 쿼리도 있네.
            return new String[]{termA};
//			throw new IOException("이 단어 없슴");
//			if (isdebug) System.out.println("이 단어 없습니다 " + termA);
        } // DEBUG

        HashMap<String, Float> assoValueList = new HashMap<String, Float>();
        for(int index2=0;index2<topTerms.length;index2++) {
            String termB="";
            int docFreqTermB=0;
            int docFreqTermAB=0;

            if (termA.equals(topTerms[index2])) continue; // 같은 단어에 대해선 무시

            termB = topTerms[index2];
            docFreqTermB = docFreq[index2];
            if (isdebug) System.out.println(index2 + ": topTerm "+ termB +" 의 freq은? " + docFreqTermB);
            docFreqTermAB=0;
            for (int cnt=0;cnt<termFreqList.size();cnt++) {
                HashMap<String, Integer> termFreq=termFreqList.get(cnt);
                if(termFreq.containsKey(termA) && termFreq.containsKey(termB)) docFreqTermAB++;
            }

            //	  term association measures by using docFreqTermA, docFreqTermB, docFreqTermAB
            float assoValue=0;
            assoValue= (float) docFreqTermAB / (float) (docFreqTermA + docFreqTermB) ; // 다이스 계수 <이게 제일 그나마 눈에 보기에 좋음>
//			assoValue= (float) docFreqTermAB / (float) (docFreqTermA * docFreqTermB) ; // MI . 눈으로 보기에 매우 부정확함.
//			assoValue= (float) docFreqTermAB; // 맨 처음에 했던 시도
            assoValueList.put(termB,assoValue);

        }

        if (isdebug) System.out.println("그리고 정렬 중.. ");

        String[] result = new String[NUM_ASSO_TERM]; // FOR RETURN

        ArrayList<TermFreq> assoValueSorted=getSortedList2(assoValueList);
        for (int cnt=0; cnt< NUM_ASSO_TERM ;cnt++){ // NUM_ASSO_TERM에 보여줄 단어들을 적음
            if (isdebug) System.out.println(termA+", "+assoValueSorted.get(cnt).term+", "+assoValueSorted.get(cnt).frequency);
            result[cnt] = assoValueSorted.get(cnt).term ;
        }
        return result;

    }

    public static void main(String[] args) throws IOException {
        TermAssoDemo tad = new TermAssoDemo("chung");
        tad.MakeAssoTermList();
    }
}



//		// to get global tf list 
//		String[] topTerms = getTopTermFreq(corpusStr);
//		int[] docFreq=new int[topTerms.length]; // 각 term 별 df
//
//		// to get tf per document
//		ArrayList<String> documentList=makeDocList(corpusStr);
//		ArrayList<HashMap<String, Integer>> termFreqList=makeTermFreqList(documentList);
//				
//		// to get df per term
//		for(int index=0;index<topTerms.length;index++){
//			int docFreqTerm=0;
//			for(int cnt=0;cnt<termFreqList.size();cnt++){
//				HashMap<String, Integer> termFreq=termFreqList.get(cnt);
//				if(termFreq.containsKey(topTerms[index])) docFreqTerm++;
//			}
//			docFreq[index]=docFreqTerm;
//		}
//		
//		// to get association measure
//		for(int index=0;index<NUM_TOP_TERM_FREQ;index++){
//			
//			HashMap<String, Float> assoValueList = new HashMap<String, Float>();
//			String termA=topTerms[index];
//			String termB="";
//			int docFreqTermA=docFreq[index];
//			int docFreqTermB=0;
//			int docFreqTermAB=0;
//			for(int index2=0;index2<topTerms.length;index2++) {
//				
//				if(topTerms[index]==topTerms[index2])continue;
//				
//				termB=topTerms[index2];
//				docFreqTermB=docFreq[index2];
//				
//				docFreqTermAB=0;
//				for (int cnt=0;cnt<termFreqList.size();cnt++) {
//					HashMap<String, Integer> termFreq=termFreqList.get(cnt);
//					if(termFreq.containsKey(termA) && termFreq.containsKey(termB)) docFreqTermAB++;
//				}
//				
//				
//				// here you make term association measures by using docFreqTermA, docFreqTermB, docFreqTermAB
//				float assoValue=0;
//
//				assoValue= (float) docFreqTermAB; // / (float) (docFreqTermA * docFreqTermB) ;
//				
//				assoValueList.put(termB,assoValue);
//			}
//			
//			ArrayList<TermFreq> assoValueSorted=getSortedList2(assoValueList);
//			
//			for (int cnt=0; cnt<NUM_ASSO_TERM;cnt++){
//				System.out.println(termA+", "+assoValueSorted.get(cnt).term+", "+assoValueSorted.get(cnt).frequency);			
//			}
//		}