package com.socurites.jive.ext.analyze.entity;

import com.socurites.jive.core.bot.JiveScriptBot;
import com.socurites.jive.core.bot.builder.JiveScriptBotBuilder;

/**
 * Chat Bot Builder
 * @author socurites
 *
 */
public class JiveScriptExtBotBuilder extends JiveScriptBotBuilder {
	/**
	 * build a chat bot
	 * 
	 * @return
	 */
	public JiveScriptBot build() {
		JiveScriptBot bot = new JiveScriptExtBot(this.enableAnalyze, this.entityBuilder);
		
		return bot;
	}
}
