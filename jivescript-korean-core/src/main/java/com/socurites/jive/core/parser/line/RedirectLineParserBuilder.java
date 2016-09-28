package com.socurites.jive.core.parser.line;

import org.apache.log4j.Logger;

import com.socurites.jive.core.parser.JiveScriptParseState;

public class RedirectLineParserBuilder extends LineParserBuilder {
	private final static Logger logger = Logger.getLogger(RedirectLineParserBuilder.class);
	
	
	
	public RedirectLineParserBuilder() {
		super();
	}
	
	@Override
	public boolean build(JiveScriptParseState parseState) {
		logger.debug("\t@ REDIRECT: " + parseState.getCommandText());

		// This can't come before a trigger!
		if ( parseState.isCurrentTriggerEmpty() ) {
			logger.error("Redirect found before trigger at " + this.getErrorLocation(parseState));
			return false;
		}

		// Add the redirect to the trigger.
		// TODO: this extends RiveScript, not compat w/ Perl yet
		parseState.getEntityBuilder().getTopics().addTopic(parseState.getCurrentTopic()).addTrigger(parseState.getCurrentTrigger()).addRedirect(parseState.getCommandText());
		
		return true;
	}
}
