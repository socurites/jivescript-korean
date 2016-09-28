package com.socurites.jive.core.parser.line;

import com.socurites.jive.core.parser.JiveScriptParseState;

public abstract class LineParserBuilder {
	abstract public boolean build(JiveScriptParseState parseState);
	
	
	/*
	 * define command specific
	 */
	protected String commandText;
	
	public LineParserBuilder commandText(String commandText) {
		this.commandText = commandText;
		return this;
	}
	
	protected String getErrorLocation(JiveScriptParseState parseState) {
		return parseState.getFileName() + " line " + parseState.getLineNum();
	}
}
