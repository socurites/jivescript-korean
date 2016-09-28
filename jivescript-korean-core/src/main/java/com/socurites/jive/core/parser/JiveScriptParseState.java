package com.socurites.jive.core.parser;

import java.util.HashMap;
import java.util.Map;

import com.socurites.jive.core.bot.conts.JiveScriptConstants;
import com.socurites.jive.core.parser.element.JSElement;
import com.socurites.jive.core.parser.entity.JiveScriptEntity;
import com.socurites.jive.core.parser.line.LineParserBuilder;

public class JiveScriptParseState {
	/** Global State. */
	private JiveScriptEntity entityBuilder;
	
	/** Script File State. */
	protected Map<String, String> localOptions;
	

	/** Line State. */
	private String currentTrigger = "";
	
	private String currentTopic = JiveScriptConstants.TOPIC_RANDOM;
	
	private String previousCommandText = "";
	
	private JSElement currElement;
	private JSElement prevElement;
	
	/** for debugging log. */
	protected String fileName;
	protected int lineNum = 0;
	
	
	public JiveScriptParseState(JiveScriptEntity entityBuilder) {
		this.entityBuilder = entityBuilder;
	}
	
	
	/**
	 * @return the entityBuilder
	 */
	public JiveScriptEntity getEntityBuilder() {
		return entityBuilder;
	}

	/**
	 * @return the localOptions
	 */
	public Map<String, String> getLocalOptions() {
		return localOptions;
	}

	/**
	 * @return the currentParserBuilder
	 */
	public LineParserBuilder getCurrentParserBuilder() {
		return currentParserBuilder;
	}

	/**
	 * @return the fileName
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * @return the lineNum
	 */
	public int getLineNum() {
		return lineNum;
	}

	private LineParserBuilder currentParserBuilder;
	public JiveScriptParseState currentLineParser(LineParserBuilder parserBuilder) {
		this.currentParserBuilder = parserBuilder;
		return this;
	}
	
	public boolean build() {
		return this.currentParserBuilder.build(this);
	}
	
	
	
	public JiveScriptParseState entityBuilder(JiveScriptEntity entityBuilder) {
		this.entityBuilder = entityBuilder;
		return this;
	}
	
	public JiveScriptParseState localOptions() {
		this.localOptions = new HashMap<String, String>();
		this.localOptions.put("concat", "none");
		
		return this;
	}
	
	public JiveScriptParseState fileName(String fileName) {
		this.fileName = fileName;
		this.lineNum = 0;
		return this;
	}
	
	
	
	public void incrementLineNum() {
		this.lineNum++;
	}

	
	/**
	 * @return the currentTrigger
	 */
	public String getCurrentTrigger() {
		return currentTrigger;
	}

	/**
	 * @param currentTrigger the currentTrigger to set
	 */
	public void setCurrentTrigger(String currentTrigger) {
		this.currentTrigger = currentTrigger;
	}
	
	public void clearCurrentTrigger() {
		this.currentTrigger = "";
	}
	
	public boolean isCurrentTriggerEmpty() {
		if ( currentTrigger.length() == 0 ) {
			return true;
		}
		return false;
	}
	
	
	public String getCommandText() {
		return this.currElement.getCommandText();
	}

	public boolean hasCommandText() {
		if ( this.currElement.hasCommandText() ) {
			return true;
		}
		
		return false;
	}
	
	public void appendCommandText(String commandText) {
		this.currElement.appendCommandText(commandText);
	}
	
	public String getCommandType() {
		return this.currElement.getCommandType();
	}
	
	/**
	 * @return the previousCommandText
	 */
	public String getPreviousCommandText() {
		return previousCommandText;
	}
	
	/**
	 * @param previousCommandText the previousCommandText to set
	 */
	public void setPreviousCommandText(String previousCommandText) {
		this.previousCommandText = previousCommandText;
	}
	
	public void clearPreviousCommandText() {
		this.previousCommandText = "";
	}
	
	public boolean ispreviousCommandTextEmpty() {
		if ( this.previousCommandText.length() == 0 ) {
			return true;
		}
		return false;
	}
	
	public void setElementState(String codes) {
		this.prevElement = this.currElement;
		this.currElement = new JSElement(codes, this.prevElement);
	}

	/**
	 * @param currElement the currElement to set
	 */
	public void setCurrElement(JSElement currElement) {
		this.currElement = currElement;
	}

	/**
	 * @param prevElement the prevElement to set
	 */
	public void setPrevElement(JSElement prevElement) {
		this.prevElement = prevElement;
	}

	/**
	 * @return the prevElement
	 */
	public JSElement getPrevElement() {
		return prevElement;
	}




	/**
	 * @return the currentTopic
	 */
	public String getCurrentTopic() {
		return currentTopic;
	}

	/**
	 * @param currentTopic the currentTopic to set
	 */
	public void setCurrentTopic(String currentTopic) {
		this.currentTopic = currentTopic;
	}
	
	/**
	 * current line type check utility method
	 */
	/**
	 * 현재 라인이 주석/공백 등 무시해도 되는 문장인지 검사.
	 * @return
	 */
	public boolean isIgnorable() {
		if ( this.currElement.isComment() || this.currElement.isBlank() ) {
			return true;
		}
		
		return false;
	}
	
	public boolean isTrigger() {
		if ( this.currElement.getCommandType().equals(JiveScriptConstants.CMD_TRIGGER) ) {
			return true;
		}
		
		return false;
	}
	
	public boolean isContinue() {
		if ( this.currElement.getCommandType().equals(JiveScriptConstants.CMD_CONTINUE) ) {
			return true;
		}
		
		return false;
	}
	
	public boolean isPrevious() {
		if ( this.currElement.getCommandType().equals(JiveScriptConstants.CMD_PREVIOUS) ) {
			return true;
		}
		
		return false;
	}
	
	public boolean isDefine() {
		if ( this.currElement.getCommandType().equals(JiveScriptConstants.CMD_DEFINE) ) {
			return true;
		}
		
		return false;
	}
}
