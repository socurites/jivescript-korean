package com.socurites.jive.core.parser.element;

public class JSElement {
	public static String STATE_BLANK = "blank";
	public static String STATE_COMMENT = "comment";
	public static String STATE_MULTI_COMMENT = "multi_comment";

	protected String currState = STATE_BLANK;
	protected String prevState = STATE_BLANK;
	
	protected String originalLine;
	protected String commandType;
	protected String commandText;
	
	protected JSElement prevElement;
	
	public JSElement(String line, JSElement prevElement) {
		this.originalLine = removeInlineComment(line.trim());
		this.prevElement = prevElement;
		
		if ( prevElement == null ) {
			this.prevState = STATE_BLANK;
		} else {
			this.prevState = prevElement.getCurrState();
		}
		
		if ( !this.isBlank() ) {
			this.commandType = this.originalLine.substring(0, 1);
			this.commandText = this.originalLine.substring(1).trim();
		}
	}
	
	public String getOriginalLine() {
		return this.originalLine;
	}
	
	public String getCommandType() {
		return this.commandType;
	}
	
	public String getCommandText() {
		return this.commandText;
	}
	
	public void appendCommandText(String commandText) {
		this.commandText += commandText;
	}
	
	public String getCurrState() {
		return this.currState;
	}
	
	public boolean isBlank() {
		if (this.originalLine.length() < 2) {
			return true;
		}
		
		return false;
	}
	
	public boolean hasCommandText() {
		if ( this.commandText.length() > 0 ) {
			return true;
		}
		
		return false;
	}
	
	public boolean isComment() {
		if (this.originalLine.startsWith("/*")) {
			if (this.originalLine.indexOf("*/") > -1) {
				return true;
			}
			this.currState = STATE_MULTI_COMMENT;
			return true;
		}
		else if (this.originalLine.startsWith("/")) {
			return true;
		}
		else if (this.originalLine.indexOf("*/") > -1) {
			currState = STATE_BLANK;
			return true;
		} else if ( this.prevState.equals(STATE_MULTI_COMMENT) ) {
			return true;
		}
		
		return false;
	}
	
	private String removeInlineComment(String line) {
		if (line.indexOf(" // ") > -1) {
			String[] split = line.split(" // ");
			return split[0];
		} else {
			return line;
		}
	}
}
