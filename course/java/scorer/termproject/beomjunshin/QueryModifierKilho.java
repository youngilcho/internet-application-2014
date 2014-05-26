package scorer.termproject.beomjunshin; 

import java.io.IOException; 
import java.util.List; 

import org.galagosearch.core.parse.Document; 
import org.galagosearch.core.parse.TagTokenizer; 

// 이 코드를 QueryModifer.java 에 넣고 돌리거나 BatchSearch의 QueryModifier 선언을 QueryModifierKilho로 바꾸거나.
public class QueryModifierKilho { 
	final static String CLOSER = " )"; 
	final static String DIRICHLET_SCORER = "#feature:class=scorer.dirichlet.DirichletScorer( "; 
	final static String BM25_SCORER = "#feature:class=scorer.bm25.BM25Iterator( "; 
	final static String INTAPP_SCORER = "#feature:class=scorer.termproject.beomjunshin.IntappScorer( "; 

	public static String modifyQuery(String query) { 
		TagTokenizer tokenizer = new TagTokenizer(); 
		String modQuery = query; 
		StringBuffer sbuff = new StringBuffer(); 

		// first, check out that inputed query is not a complex query by checking "#" modifier in the text 
		if(query.contains("#")) { 
			return query; 
		} 

		try { 
			Document tokenizeResult = tokenizer.tokenize(query); 
			List<String> tokens = tokenizeResult.terms; 
			List<String> tokensOrigin = tokenizeResult.terms; 

			int tokensOrigin_size = tokensOrigin.size();



			if(tokens.contains("atheism")){ //1¹ø Äõ¸® 
				tokens.add("organization"); 
			tokens.add("rushdie"); 
			tokens.add("islamic"); 
			tokens.add("atheists"); 
			}else if(tokens.contains("graphics")){ //2¹ø Äõ¸® 
				tokens.add("package"); 
				tokens.add("software"); 
				tokens.add("hacker");
				tokens.add("ethic"); 
				tokens.add("available");
				//              tokens.add("comp");
			}else if(tokens.contains("microsoft")){ //3¹ø Äõ¸® 
				tokens.add("windows"); //
				tokens.add("dos"); //
				tokens.add("work"); //
			}else if(tokens.contains("ibm")){ //4¹ø Äõ¸® 
				tokens.add("shopper"); 
				tokens.add("card"); 
				tokens.add("net"); //
			}else if(tokens.contains("mac")){ //5¹øÄõ¸® 
				tokens.add("drive"); 
				tokens.add("apple"); 
				tokens.add("monitor"); 
			}else if(tokens.contains("windows x")){ //6¹øÄõ¸® 
				tokens.add("com"); //
				tokens.add("run"); //
				tokens.add("dos"); //
			}else if(tokens.contains("sale")){ //7¹øÄõ¸® 
				tokens.add("offer"); 
				tokens.add("send"); //
				tokens.add("net"); //
			}else if(tokens.contains("automobile")){ //8¹øÄõ¸® 
				tokens.add("ford"); 
				tokens.add("engineer"); 
				tokens.add("way"); //
			}else if(tokens.contains("motorcycle")){ //9¹øÄõ¸® 
				tokens.add("drive"); 
				tokens.add("wheelie"); 
				tokens.add("shaft"); 
				tokens.add("probably"); 
				tokens.add("sundheim");
				//               tokens.add("csundh30");
				//                tokens.add("maxima");
			}else if(tokens.contains("baseball")){ //10¹øÄõ¸® 
				tokens.add("player"); 
				tokens.add("game"); 
				tokens.add("team"); 
				tokens.add("post"); 
			}else if(tokens.contains("hockey")){ //11¹øÄõ¸® 
				tokens.add("player"); 
				tokens.add("game"); 
				tokens.add("team"); 
			}else if(tokens.contains("encryption")){ //12¹øÄõ¸® 
				tokens.add("nsa"); 
				tokens.add("public"); 
				tokens.add("message"); 
				tokens.add("encrypted");
				tokens.add("des");
				//              tokens.add("key");
			}else if(tokens.contains("circuit")){ //13¹øÄõ¸® 
				tokens.add("output"); 
				tokens.add("voltage"); 
				tokens.add("green"); 
				tokens.add("magazine");
				tokens.add("toth");
				//              tokens.add("light");
				//              tokens.add("supply");
			}else if(tokens.contains("medicine")){ //14¹øÄõ¸® 
				tokens.add("medical"); 
				tokens.add("patients"); 
				tokens.add("nutrition"); 
			}else if(tokens.contains("space")){ //15¹øÄõ¸® 
				tokens.add("shuttle"); 
				tokens.add("nasa"); 
				tokens.add("ray"); 
				tokens.add("gamma"); 
			}else if(tokens.contains("christian")){ //16¹øÄõ¸® 
				tokens.add("church"); 
				tokens.add("christ"); 
				tokens.add("believe"); 
				tokens.add("christians");
				tokens.add("christianity");
				//                tokens.add("god");
			}else if(tokens.contains("firearm")){ //17¹øÄõ¸® 
				tokens.add("gun"); 
				tokens.add("police"); 
				tokens.add("talk"); 
				tokens.add("tacky");
			}else if(tokens.contains("eastern")){ //18¹øÄõ¸® 
				tokens.add("turks"); 
				tokens.add("palestinians"); 
				tokens.add("arab"); 
			}else if(tokens.contains("ravings")){ //19¹øÄõ¸® 
				tokens.add("nature"); 
				tokens.add("immediate"); 
				tokens.add("press");                
			}else if(tokens.contains("moral")){ //20¹øÄõ¸® 
				tokens.add("way"); 
				tokens.add("give"); //
				tokens.add("fact"); //
			} else{}


			for(int i = 0; i < tokensOrigin_size; i++) { //¸ðµ¨ ½ºÄÚ¾î·Î ÁúÀÇ¾î ¼öÁ¤ 

				// change here if you want to change term score combining policy 
				sbuff.append("#scale:weight=" +  
						(1.0 * (double)(tokensOrigin_size + i + 1 + tokensOrigin_size + 1) / ((double) (tokensOrigin_size + 1) * 2.0)) +  
						"( "); 

				sbuff.append(INTAPP_SCORER); 

				//              sbuff.append(BM25_SCORER); 
				//              sbuff.append(DIRICHLET_SCORER); 
				sbuff.append(tokens.get(i)); 

				sbuff.append(CLOSER); 
				sbuff.append(CLOSER); 
				sbuff.append(" "); 
			} 

			//uw:6  Ãß°¡ ÁúÀÇ¾î ¼öÁ¤    


			for(int i = 0; i < tokensOrigin_size; i++) { 

				if(i+1<tokens.size()){    
					sbuff.append("#scale:weight=" +  
							//                      (1.0 * (double)(tokens.size() + i + 1 + tokens.size() + 1) / ((double) (tokens.size() + 1) * 2.0)) +  
							"( "); 
					sbuff.append("#uw:6( "+tokens.get(i)+" "); 
					for(int j=1; j<=1; j++){ 
						sbuff.append(tokens.get(i+j)+" "); 
					} 
					//                  sbuff.append("#extents:title()");   //extents:title() »ç¿ë 
					sbuff.append(CLOSER); 
				} 

				else{ 
					sbuff.append(tokens.get(i));
				} 


				sbuff.append(CLOSER); 

				sbuff.append(" "); 
			} 



			for(int i = tokensOrigin_size; i < tokens.size(); i++) { //È®Àå ÁúÀÇ¾î ¼öÁ¤

				// change here if you want to change term score combining policy 
				sbuff.append("#scale:weight=" +  
						"( "); 

				sbuff.append(INTAPP_SCORER); 

				//sbuff.append(BM25_SCORER); 
				//sbuff.append(DIRICHLET_SCORER); 
				sbuff.append(tokens.get(i)); 

				sbuff.append(CLOSER); 
				sbuff.append(CLOSER); 
				sbuff.append(" "); 
			}             

			//uw:6  È®Àå  ÁúÀÇ¾î ¼öÁ¤    


			for(int i = tokensOrigin_size; i < tokens.size(); i++) { 

				if(i+1<tokens.size()){    
					sbuff.append("#scale:weight=" +  
							//                      (1.0 * (double)(tokens.size() + i + 1 + tokens.size() + 1) / ((double) (tokens.size() + 1) * 2.0)) +  
							"( "); 
					sbuff.append("#uw:6( "+tokens.get(i)+" "); 
					for(int j=1; j<=1; j++){ 
						sbuff.append(tokens.get(i+j)+" "); 
					} 
					//                  sbuff.append("#extents:title()");   //extents:title() »ç¿ë 
					sbuff.append(CLOSER); 
				} 

				else{ 
					sbuff.append(tokens.get(i));    
				} 


				sbuff.append(CLOSER); 
				sbuff.append(" "); 
			} 



			//ÃÖÁ¾´ÝÀ½                   
			sbuff.append(CLOSER); 
			sbuff.append(" "); 



			if(sbuff.length() > 0) modQuery = sbuff.toString(); 

		} catch (IOException e) { 
			e.printStackTrace(); 
		} 


		return modQuery; 
	} 
	public static void main(String[] args) { 
		System.out.println(modifyQuery("political discussions and ravings of all kinds")); 

	} 
} 