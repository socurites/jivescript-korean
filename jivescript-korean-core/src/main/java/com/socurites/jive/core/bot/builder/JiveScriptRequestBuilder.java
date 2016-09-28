package com.socurites.jive.core.bot.builder;

import java.util.List;

import com.socurites.jive.core.analyze.entity.JiveToken;
import com.socurites.jive.core.analyze.entity.JiveTokenModel;
import com.socurites.jive.core.core.analyze.JiveScriptKoreanAnalyzer;
import com.socurites.jive.core.engine.old.Util;
import com.socurites.jive.core.parser.entity.JiveScriptEntity;

public class JiveScriptRequestBuilder {
	private JiveScriptEntity entityBuilder;
	private boolean enableAnalyze = false;
	private JiveScriptKoreanAnalyzer analyzer;
	private JiveTokenModel jiveTokenModel;
	
	public JiveScriptRequestBuilder analyze(boolean enableAnalyze) {
		this.enableAnalyze = enableAnalyze;
		
		return this;
	}
	
	public JiveScriptRequestBuilder analyzer(JiveScriptKoreanAnalyzer analyzer) {
		this.analyzer = analyzer;
		
		return this;
	}
	

	public JiveScriptRequestBuilder entityBuilder(JiveScriptEntity entityBuilder) {
		this.entityBuilder = entityBuilder;
		
		return this;
	}
	
	public String build(String message){
		message = message.toLowerCase();
		message = Util.substitute(entityBuilder.getSortedSubs(), entityBuilder.getSubs(), message);
		
		if ( this.enableAnalyze ) {
			message = getAnalyzedMessage(message);
		}
		message = message.replaceAll("[^a-z0-9가-힣 \\?]", "");
		return message;
	}
	
	
	/**
	 * @return the jiveTokenModel
	 */
	public JiveTokenModel getJiveTokenModel() {
		return jiveTokenModel;
	}

	private String getAnalyzedMessage(String message) {
		this.jiveTokenModel = this.analyzer.analyze(message);
		List<JiveToken> tokens = this.jiveTokenModel.getTokens();
		StringBuffer sb = new StringBuffer();
		
		for ( JiveToken token : tokens ) {
			if ( isMeaningfulPos(token.getText(), token.getPos()) )
			sb.append(token.getText() + " ");
		}
		message = sb.toString().trim();
		
		return message;
	}
	
	private boolean isMeaningfulPos(String text, String pos) {
		if ( "noun".equals(pos)
			|| "verb".equals(pos)
			|| "adj".equals(pos)
			|| "alpha".equals(pos)
				) {
			
			return true;
		} 
		/*
		else if ( "punct".equals(pos) && "?".equals(text) ) {
			return true;
		}
		*/
		return false;
			
	}
}
