package com.socurites.jive.core.bot;

import java.util.List;

import org.apache.log4j.Logger;

import com.socurites.jive.core.bot.builder.JiveScriptReplyBuilder;
import com.socurites.jive.core.bot.builder.JiveScriptRequestBuilder;
import com.socurites.jive.core.bot.conts.JiveScriptConstants;
import com.socurites.jive.core.core.analyze.JiveScriptKoreanAnalyzer;
import com.socurites.jive.core.core.analyze.JiveScriptKoreanDefaultAnalyzer;
import com.socurites.jive.core.engine.old.TopicManager;
import com.socurites.jive.core.parser.entity.JiveScriptEntity;

public class JiveScriptBot {
	private final static Logger logger = Logger.getLogger(JiveScriptBot.class);
	
	protected JiveScriptEntity entityBuilder;
	
	protected boolean enableAnalyze = false;
	
	protected JiveScriptKoreanAnalyzer analyzer;
	
	public JiveScriptBot(boolean enableAnalyze, JiveScriptEntity entityBuilder) {
		this.enableAnalyze = enableAnalyze;
		this.entityBuilder = entityBuilder;
		
		if ( this.enableAnalyze ) {
			this.analyzer = new JiveScriptKoreanDefaultAnalyzer();
			
			List<String> analyzerKeywords = this.entityBuilder.getAnalyzerKeywords();
			this.analyzer.addKewords(analyzerKeywords);
		}
	}
	
	public TopicManager getTopics() {
		return this.entityBuilder.getTopics();
	}
	
	/**
	 * Retrieve a single variable from a user's profile.
	 *
	 * Returns null if the user doesn't exist. Returns the string "undefined"
	 * if the variable doesn't exist.
	 *
	 * @param user The user ID to get data from.
	 * @param name The name of the variable to get.
	 */
	public String getUservar (String user, String name) {
		if (this.entityBuilder.getClients().clientExists(user)) {
			return this.entityBuilder.getClients().client(user).get(name);
		}
		else {
			return null;
		}
	}

	public String lastMatch (String user) {
		return this.getUservar(user, "__lastmatch__");
	}
	
	public JiveScriptReplyBuilder reply(String username, String message) {
		logger.debug("Get reply to [" + username + "] " + message);
		JiveScriptRequestBuilder requestBuilder = (new JiveScriptRequestBuilder())
				.entityBuilder(this.entityBuilder)
				.analyze(this.enableAnalyze)
				.analyzer(this.analyzer)
		;
		
		JiveScriptReplyBuilder replyBuilder = (new JiveScriptReplyBuilder())
				.entityBuilder(this.entityBuilder)
				.requestBuilder(requestBuilder)
				.user(username);

		String analyzedRequestMessage = requestBuilder.build(message);
		int step = 0;
		if (this.entityBuilder.hasTopic(JiveScriptConstants.TOPIC_BEGIN)) {
			replyBuilder = replyBuilder.request(JiveScriptConstants.TRIGGER_REQUEST)
					.build(true, step);
			;
			
			if ( replyBuilder.isBeginReplyOK() ) {
				replyBuilder = replyBuilder.request(analyzedRequestMessage)
						.build(false, step);
				;
			}

		} else {
			replyBuilder = replyBuilder.request(analyzedRequestMessage)
					.build(false, step)
			;
		}

		//this.replyBuilder.reply(this.replyBuilder.processTags(username, this.entityBuilder.getClients().client(username), analyzedRequestMessage, this.replyBuilder.getReplyAsText(), new ArrayList<String>(), new ArrayList<String>(), 0));

		// Save their chat history.
		this.entityBuilder.getClients().client(username).addInput(analyzedRequestMessage);
		this.entityBuilder.getClients().client(username).addReply(replyBuilder.getReplyAsText());

		// Return their reply.
		return replyBuilder;
	}

	/**
	 * @return the entityBuilder
	 */
	public JiveScriptEntity getEntityBuilder() {
		return entityBuilder;
	}

//	public String getReplyAsText() {
//		return this.replyBuilder.getReplyAsText();
//	}
}
