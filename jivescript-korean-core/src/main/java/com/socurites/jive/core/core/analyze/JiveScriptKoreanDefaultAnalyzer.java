package com.socurites.jive.core.core.analyze;

import java.util.ArrayList;
import java.util.List;

import scala.collection.Seq;

import com.socurites.jive.core.analyze.entity.JiveToken;
import com.socurites.jive.core.analyze.entity.JiveTokenModel;
import com.twitter.penguin.korean.KoreanPosJava;
import com.twitter.penguin.korean.KoreanTokenJava;
import com.twitter.penguin.korean.TwitterKoreanProcessorJava;
import com.twitter.penguin.korean.phrase_extractor.KoreanPhraseExtractor.KoreanPhrase;
import com.twitter.penguin.korean.tokenizer.KoreanTokenizer.KoreanToken;

public class JiveScriptKoreanDefaultAnalyzer extends JiveScriptKoreanAnalyzer {
	public JiveScriptKoreanDefaultAnalyzer() {
		this.loadAnalyzer();
	}
	
	/**
	 * loading static TwitterKoreanProcessorJava class when creating a bot
	 */
	private void loadAnalyzer() {
//		String text = "우울한 노래 들려줘";
		String text = "알려줘 알려줄래 보여줄래 보여줘";
		
	    // Normalize
	    CharSequence normalized = TwitterKoreanProcessorJava.normalize(text);
	    System.out.println("#normalized: " + normalized);
	    // 한국어를 처리하는 예시입니다ㅋㅋ #한국어


	    // Tokenize
	    Seq<KoreanToken> tokens = TwitterKoreanProcessorJava.tokenize(normalized);
	    System.out.println("#tokenize: " + TwitterKoreanProcessorJava.tokensToJavaKoreanTokenList(tokens));
	    // [한국어(Noun: 0, 3), 를(Josa: 3, 1),  (Space: 4, 1), 처리(Noun: 5, 2), 하는(Verb: 7, 2),  (Space: 9, 1), 예시(Noun: 10, 2), 입니(Adjective: 12, 2), 다(Eomi: 14, 1), ㅋㅋ(KoreanParticle: 15, 2),  (Space: 17, 1), #한국어(Hashtag: 18, 4)]


	    // Stemming
	    Seq<KoreanToken> stemmed = TwitterKoreanProcessorJava.stem(tokens);
	    System.out.println("#stem: " + TwitterKoreanProcessorJava.tokensToJavaKoreanTokenList(stemmed));
	    // [한국어(Noun: 0, 3), 를(Josa: 3, 1),  (Space: 4, 1), 처리(Noun: 5, 2), 하다(Verb: 7, 2),  (Space: 9, 1), 예시(Noun: 10, 2), 이다(Adjective: 12, 3), ㅋㅋ(KoreanParticle: 15, 2),  (Space: 17, 1), #한국어(Hashtag: 18, 4)]
	    

	    // Phrase extraction
	    List<KoreanPhrase> phrases = TwitterKoreanProcessorJava.extractPhrases(tokens, true, true);
	    System.out.println("#phrases: " + phrases);
	    // [한국어(Noun: 0, 3), 처리(Noun: 5, 2), 처리하는 예시(Noun: 5, 7), 예시(Noun: 10, 2), #한국어(Hashtag: 18, 4)]
	}
	
	@Override
	public void addKewords(List<String> keywords) {
		TwitterKoreanProcessorJava.addNounsToDictionary(keywords);
	}
	
	@Override
	public JiveTokenModel analyze(String text) {
		text = text.replaceAll(" ", "");
		
		List<JiveToken> jiveTokens = new ArrayList<JiveToken>();
		List<String> tags = new ArrayList<String>();
		
	    CharSequence normalized = TwitterKoreanProcessorJava.normalize(text);
	    Seq<KoreanToken> tokens = TwitterKoreanProcessorJava.tokenize(normalized);
	    Seq<KoreanToken> stemmedTokens = TwitterKoreanProcessorJava.stem(tokens);
	    List<KoreanTokenJava> koreanTokens = TwitterKoreanProcessorJava.tokensToJavaKoreanTokenList(stemmedTokens);
	    List<KoreanPhrase> phrases = TwitterKoreanProcessorJava.extractPhrases(tokens, true, false);
	    
	    JiveToken jiveToken = null;
	    for ( int i = 0; i < koreanTokens.size(); i++) {
	    	jiveToken = new JiveToken(koreanTokens.get(i).getText(), convertPos(koreanTokens.get(i).getPos()));
	    	jiveTokens.add(jiveToken);
	    }
	    
	    for ( int i = 0; i < phrases.size(); i++ ) {
	    	tags.add(phrases.get(i).text());
	    }
	    
	    JiveTokenModel jiveTokenModel = new JiveTokenModel(jiveTokens, tags);
	    
	    return jiveTokenModel;
	}
	
	public String convertPos(KoreanPosJava koreanPosJava) {
		if ( KoreanPosJava.Noun == koreanPosJava ) {
			return "noun";
		} else if ( KoreanPosJava.ProperNoun == koreanPosJava ) {
			return "noun";
		} else if ( KoreanPosJava.Adjective == koreanPosJava ) {
			return "adj";
		} else if ( KoreanPosJava.Verb == koreanPosJava ) {
			return "verb";
		} else if ( KoreanPosJava.Adverb == koreanPosJava ) {
			return "adv";
		} else if ( KoreanPosJava.Punctuation == koreanPosJava ) {
			return "punct";
		} else if ( KoreanPosJava.Alpha == koreanPosJava ) {
			return "alpha";
		} else {
			return "etc";
		}
	}
	
	public static void main(String[] args) {
		JiveScriptKoreanDefaultAnalyzer test = new JiveScriptKoreanDefaultAnalyzer();
	}
}
