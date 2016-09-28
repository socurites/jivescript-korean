package com.socurites.jive.bot;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.socurites.jive.AbstractJiveTestCase;
import com.socurites.jive.core.bot.JiveScriptBot;
import com.socurites.jive.core.bot.builder.JiveScriptBotBuilder;
import com.socurites.jive.core.bot.builder.JiveScriptReplyBuilder;
import com.socurites.jive.core.bot.builder.JiveScriptRequestBuilder;

public class JiveScriptBotAnalyzedTest extends AbstractJiveTestCase {
	private static final boolean enableAnalyze = true;
	
	private JiveScriptBot getBot(String tempateDirPath, String keywordDirResourcePath, boolean enableAnalyze) {
		JiveScriptBot bot = (new JiveScriptBotBuilder())
				.parse(tempateDirPath, keywordDirResourcePath)
				.analyze(enableAnalyze)
				.build()
		;
		
		return bot;
	}
	
	@Test
	public void analyze_test() {
		String templateDirResourcePath = "script/analzye/rive";
		String keywordDirResourcePath = "script/analzye/jive";
		String message = "봇 오늘 기분은 어때";
		
		JiveScriptBot bot = getBot(getPathFromResource(templateDirResourcePath), getPathFromResource( keywordDirResourcePath), enableAnalyze);
		
		JiveScriptReplyBuilder replyBuilder = bot.reply("localuser", message);
		String replyAsText = replyBuilder.getReplyAsText();
		
		assertEquals("나는 오늘 기분이 무척 좋아, 넌?", replyAsText);
	}

	@Test
	public void analyze_stop_test() {
		String templateDirResourcePath = "script/analzye/rive";
		String keywordDirResourcePath = "script/analzye/jive";
		String message = "멜봇";
		
		JiveScriptBot bot = getBot(getPathFromResource(templateDirResourcePath), getPathFromResource(keywordDirResourcePath), enableAnalyze);
		
		JiveScriptReplyBuilder replyBuilder = bot.reply("localuser", message);
		String replyAsText = replyBuilder.getReplyAsText();
		
		assertEquals("왜?", replyAsText);
	}
}
