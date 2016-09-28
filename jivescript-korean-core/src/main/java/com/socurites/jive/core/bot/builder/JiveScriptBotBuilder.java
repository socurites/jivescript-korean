package com.socurites.jive.core.bot.builder;

import com.socurites.jive.core.bot.JiveScriptBot;
import com.socurites.jive.core.parser.JiveScriptEntityBuilder;
import com.socurites.jive.core.parser.entity.JiveScriptEntity;

/**
 * Chat Bot Builder
 * @author socurites
 *
 */
public class JiveScriptBotBuilder {
	/**
	 * whether analyzing user request message before bot matches replies. 
	 */
	protected boolean enableAnalyze = false;
	
	/**
	 * Script parser.
	 * extension of rive script parser.
	 */
	protected JiveScriptEntity entityBuilder;
	
	public JiveScriptBotBuilder() {
	}
	
	/**
	 * parse scripts
	 * 
	 * @param templateDirPath
	 * @param keywordDirPath		optional
	 * @return
	 */
	public JiveScriptBotBuilder parse(String templateDirPath, String keywordDirPath) {
		this.entityBuilder = (new JiveScriptEntityBuilder())
				.templateDirectory(templateDirPath)
				.keywordDirectory(keywordDirPath)
				.build();
		
		
		return this;
	}
	
	/**
	 * build a chat bot
	 * 
	 * @return
	 */
	public JiveScriptBot build() {
		JiveScriptBot bot = new JiveScriptBot(this.enableAnalyze, this.entityBuilder);
		
		return bot;
	}
	
	
	/**
	 * 사용자가 입력한 메시지를 trigger 매칭 전에 형태소 분석 수행 여부. 
	 * 
	 * @param enableAnalyze		형태소 분석 수행함.
	 * @return
	 */
	public JiveScriptBotBuilder analyze(boolean enableAnalyze) {
		this.enableAnalyze = enableAnalyze;
		
		return this;
	}
	
}
