package com.socurites.jive.core.parser.line;

import org.apache.log4j.Logger;

import com.socurites.jive.core.parser.JiveScriptParseState;

public class ConditionLineParserBuilder extends LineParserBuilder {
	private final static Logger logger = Logger.getLogger(ConditionLineParserBuilder.class);
	
	
	
	public ConditionLineParserBuilder() {
		super();
	}
	
	@Override
	public boolean build(JiveScriptParseState parseState) {
		logger.debug("\t* CONDITION: " + parseState.getCommandText());

		// This can't come before a trigger!
		if ( parseState.isCurrentTriggerEmpty() ) {
			logger.error("Redirect found before trigger at " + this.getErrorLocation(parseState));
			return false;
		}

		// Add the condition to the trigger.
		parseState.getEntityBuilder().getTopics().addTopic(parseState.getCurrentTopic()).addTrigger(parseState.getCurrentTrigger()).addConditionalReply(parseState.getCommandText());
		
		return true;
	}
}
