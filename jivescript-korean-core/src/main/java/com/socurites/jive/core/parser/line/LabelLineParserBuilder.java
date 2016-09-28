package com.socurites.jive.core.parser.line;

import org.apache.log4j.Logger;

import com.socurites.jive.core.bot.conts.JiveScriptConstants;
import com.socurites.jive.core.parser.JiveScriptParseState;
import com.socurites.jive.core.parser.element.JSElement;

public class LabelLineParserBuilder extends LineParserBuilder {
	private final static Logger logger = Logger.getLogger(LabelLineParserBuilder.class);
	
	
	
	public LabelLineParserBuilder() {
		super();
	}
	
	@Override
	public boolean build(JiveScriptParseState parseState) {
		if (parseState.getCommandType().equals(JiveScriptConstants.CMD_LABEL)) {
			return parseStartLabel(parseState);
			
		} else if (parseState.getCommandType().equals(JiveScriptConstants.CMD_ENDLABEL)) {
			return parseEndLabel(parseState);
		}
		
		return false;
	}
	
	private boolean parseEndLabel(JiveScriptParseState parseState) {
		// < ENDLABEL
		logger.debug("\t< ENDLABEL");
		String type = parseState.getCommandText().trim().toLowerCase();

		if (type.equals("begin") || type.equals("topic")) {
			logger.debug("\t\tEnd topic label.");
			parseState.setCurrentTopic(JiveScriptConstants.TOPIC_RANDOM);
			return true;
		}
		else {
			logger.error("Unknown end topic type \"" + type + "\" at " + this.getErrorLocation(parseState));
			return false;
		}
	}
	
	
	private boolean parseStartLabel(JiveScriptParseState parseState) {
		String label[] = parseState.getCommandText().split("\\s+");
		String type    = "";
		String name    = "";
		if (label.length >= 1) {
			type = label[0].trim().toLowerCase();
		}
		if (label.length >= 2) {
			name = label[1].trim();
		}

		// Handle the label types.
		if (type.equals("begin")) {
			// The BEGIN statement.
			logger.debug("\tFound the BEGIN Statement.");

			// A BEGIN is just a special topic.
			type = "topic";
			name = JiveScriptConstants.TOPIC_BEGIN;
		}
		if (type.equals("topic")) {
			// Starting a new topic.
			logger.debug("\tSet topic to " + name);
			parseState.clearCurrentTrigger();
			parseState.setCurrentTopic(name);

			// Does this topic include or inherit another one?
			if (label.length >= 3) {
				final int mode_includes = 1;
				final int mode_inherits = 2;
				int mode = 0;
				for (int a = 2; a < label.length; a++) {
					if (label[a].toLowerCase().equals("includes")) {
						mode = mode_includes;
					}
					else if (label[a].toLowerCase().equals("inherits")) {
						mode = mode_inherits;
					}
					else if (mode > 0) {
						// This topic is either inherited or included.
						if (mode == mode_includes) {
							parseState.getEntityBuilder().getTopics().addTopic(parseState.getCurrentTopic()).includes(label[a]);
						}
						else if (mode == mode_inherits) {
							parseState.getEntityBuilder().getTopics().addTopic(parseState.getCurrentTopic()).inherits(label[a]);
						}
					}
				}
			}
		}
		
		return true;
	}
}
