package com.socurites.jive.core.parser.line;

import org.apache.log4j.Logger;

import com.socurites.jive.core.parser.JiveScriptParseState;

public class ReplyLineParserBuilder extends LineParserBuilder {
	private final static Logger logger = Logger.getLogger(ReplyLineParserBuilder.class);
	
	
	
	public ReplyLineParserBuilder() {
		super();
	}
	
	@Override
	public boolean build(JiveScriptParseState parseState) {
		logger.debug("\t- REPLY: " + parseState.getCommandText());

		// This can't come before a trigger!
		if ( parseState.isCurrentTriggerEmpty() ) {
			logger.error("Reply found before trigger at " + this.getErrorLocation(parseState));
			return false;
		}

		// Add the reply to the trigger.
		parseState.getEntityBuilder().getTopics().addTopic(parseState.getCurrentTopic()).addTrigger(parseState.getCurrentTrigger()).addReply(parseState.getCommandText());
		
		return true;
	}
}
