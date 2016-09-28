package com.socurites.jive.ext.analyze.entity;

import com.socurites.jive.core.bot.JiveScriptBot;
import com.socurites.jive.core.bot.builder.JiveScriptReplyBuilder;
import com.socurites.jive.core.parser.entity.JiveScriptEntity;

public class JiveScriptExtBot extends JiveScriptBot {
	public JiveScriptExtBot(boolean enableAnalyze, JiveScriptEntity entityBuilder) {
		super(enableAnalyze, entityBuilder);
	}
	
	public JiveScriptExtReplyBuilder reply(String username, String message) {
		JiveScriptReplyBuilder reply = super.reply(username, message);
		
		JiveScriptExtReplyBuilder extReplyBuilder = new JiveScriptExtReplyBuilder(reply);
		;
		
		return extReplyBuilder;
	}
}
