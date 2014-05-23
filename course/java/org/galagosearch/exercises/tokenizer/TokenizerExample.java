
package org.galagosearch.exercises.tokenizer;

import org.galagosearch.core.parse.Document;
import org.galagosearch.core.parse.TagTokenizer;
import org.galagosearch.tupleflow.InputClass;
import org.galagosearch.tupleflow.OutputClass;
import org.galagosearch.tupleflow.execution.Verified;

@Verified
@InputClass(className = "org.galagosearch.core.parse.Document")
@OutputClass(className = "org.galagosearch.core.parse.Document")
public class TokenizerExample extends TagTokenizer {

    @Override
    protected String tokenSimpleFix(String token) {
        char[] chars = token.toCharArray();
        int j = 0;

        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            
            // here write your homework #4
            // HINT: use if-else and break
            if(c=='\'') break;
            else j++;
        }
        // Find JavaDoc and understand the behavior of String(char[], int, int) constructor.
        token = new String(chars, 0, j);
        return token;
    }
    
    @Override
    protected void tokenAcronymProcessing(String token, int start, int end) {
        token = tokenComplexFix(token);

        // remove start and ending periods
        while (token.startsWith(".")) {
            token = token.substring(1);
            start = start + 1;
        }

        while (token.endsWith(".")) {
            token = token.substring(0, token.length() - 1);
            end -= 1;
        }

        // does the token have any periods left?
        if (token.indexOf('.') >= 0) {
            // is this an acronym?  then there will be periods
            // at odd positions:
            boolean isAcronym = token.length() > 0;
            for (int pos = 1; pos < token.length(); pos += 2) {
                if (token.charAt(pos) != '.') {
                    isAcronym = false;
                }
            }

            if (isAcronym) {
                token = token.replace(".", "");
                addToken(token, start, end);
            } else {
            	// here write your homework #4 for bypassing special cases "ph.d" 
            	// HINT: use if-else and String's equals function
            	// addToken(token, start, end); statement add token to tokenizing process. use this statement if necessary.
            	if(token.equals("ph.d")) {
            		addToken(token, start, end);
            	} else {
            		int s = 0;
            		for (int e = 0; e < token.length(); e++) {
            			if (token.charAt(e) == '.') {
            				if (e - s > 1) {
            					String subtoken = token.substring(s, e);
            					addToken(subtoken, start + s, start + e);
            				}
            				s = e + 1;
            			}
            		}

            		if (token.length() - s > 1) {
            			String subtoken = token.substring(s);
            			addToken(subtoken, start + s, end);
            		}
            	}
            }
        } else {
            addToken(token, start, end);
        }
    }
    
    public static void main(String[] args) throws Exception {
    	TokenizerExample tn = new TokenizerExample();
    	
    	Document tokenizedResult=tn.tokenize("yongwook's miyeon jeehoo Shin Park Google shin I.B.M snu.ac.kr ph.d ieee.802.11n ieee.803.99");
//    	Document tokenizedResult=tn.tokenize("ph.d");
    	
    	for(int index=0;index<tokenizedResult.terms.size();index++){
    		System.out.println(tokenizedResult.terms.get(index));
    	}
    }    
}
