package com.socurites.jive.core.parser.line;

import org.apache.log4j.Logger;

import com.socurites.jive.core.parser.JiveScriptParseState;

public class TriggerLineParserBuilder extends LineParserBuilder {
	private final static Logger logger = Logger.getLogger(TriggerLineParserBuilder.class);
	
	
	
	public TriggerLineParserBuilder() {
		super();
	}
	
	@Override
	public boolean build(JiveScriptParseState parseState) {
		logger.debug("\t+ TRIGGER: " + parseState.getCommandText());

		if ( parseState.ispreviousCommandTextEmpty() ) {
			// Set the current trigger to this.
			parseState.setCurrentTrigger(parseState.getCommandText());
		} else {
			// This trigger had a %Previous. To prevent conflict, tag the
			// trigger with the "that" text.
			parseState.setCurrentTrigger(parseState.getCommandText() + "{previous}" + parseState.getPreviousCommandText());
			parseState.getEntityBuilder().getTopics().addTopic(parseState.getCurrentTopic()).addTrigger(parseState.getCommandText()).hasPrevious(true);
			parseState.getEntityBuilder().getTopics().addTopic(parseState.getCurrentTopic()).addPrevious(parseState.getCommandText(), parseState.getPreviousCommandText());
		}
		
		return true;
	}
}
