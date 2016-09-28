package com.socurites.jive.core.parser.line;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.socurites.jive.core.bot.conts.JiveScriptConstants;
import com.socurites.jive.core.parser.JiveScriptEntityBuilder;
import com.socurites.jive.core.parser.JiveScriptParseState;

public class DefineLineParserBuilder extends LineParserBuilder {
	private final static Logger logger = Logger.getLogger(DefineLineParserBuilder.class);
	
	
	
	public DefineLineParserBuilder() {
		super();
	}
	
	@Override
	public boolean build(JiveScriptParseState parseState) {
		logger.debug("\t! DEFINE");
		
		String[] tokens = this.commandText.split("\\s*=\\s*", 2);
		String[] left = StringUtils.split(tokens[0]);
		String type = left[0];
		String var = "";
		String value = "";
		boolean delete = false;
		if (left.length == 2) {
			var = left[1].trim().toLowerCase();
		}
		if (tokens.length == 2) {
			value = tokens[1].trim();
		}

		if (!type.equals(JiveScriptConstants.CMD_DEFINE_TYPE_ARRAY)) {
			value = value.replaceAll("<crlf>", "");
		}

		if (type.equals(JiveScriptConstants.CMD_DEFINE_TYPE_VERSION)) {
			logger.debug("\tUsing RiveScript version " + value);
			double version = 0;
			try {
				version = Double.valueOf(value).doubleValue();
			} catch (NumberFormatException e) {
				logger.error("RiveScript version \"" + value + "\" not a valid floating number at " + this.getErrorLocation(parseState));
				return true;
			}

			if (version > JiveScriptEntityBuilder.RS_VERSION) {
				logger.error("We can't parse RiveScript v" + value + " documents at " + this.getErrorLocation(parseState));
				return false;
			}

			return true;
		} else {
			if (var.equals("")) {
				logger.error("<RS> Missing a " + type + " variable name at " + this.getErrorLocation(parseState));
				return true;
			}
			if (value.equals("")) {
				logger.error("<RS> Missing a " + type + " value at " + this.getErrorLocation(parseState));
				return true;
			} else if (value.equals("<undef>")) {
				delete = true;
			}
		}

		if (type.equals(JiveScriptConstants.CMD_DEFINE_TYPE_LOCAL)) {
			logger.debug("\tSet local parser option " + var + " = " + value);
			parseState.getLocalOptions().put(var, value);
		} else if (type.equals(JiveScriptConstants.CMD_DEFINE_TYPE_GLOBAL)) {
			logger.debug("\tSet global " + var + " = " + value);
			parseState.getEntityBuilder().setGlobal(var, value);
		} else if (type.equals(JiveScriptConstants.CMD_DEFINE_TYPE_VAR)) {
			logger.debug("\tSet bot variable " + var + " = " + value);
			parseState.getEntityBuilder().setVariable(var, value);
		} else if (type.equals(JiveScriptConstants.CMD_DEFINE_TYPE_ARRAY)) {
			logger.debug("\tSet array " + var);
			if (delete) {
				parseState.getEntityBuilder().removeArray(var);
				return true;
			}

			parseState.getEntityBuilder().setArray(var, getArrayItems(value));
		} else if (type.equals(JiveScriptConstants.CMD_DEFINE_TYPE_SUB)) {
			logger.debug("\tSubstitution " + var + " => " + value);
			parseState.getEntityBuilder().setSubstitution(var, value);
		} else if (type.equals(JiveScriptConstants.CMD_DEFINE_TYPE_PERSON)) {
			logger.debug("\tPerson substitution " + var + " => " + value);
			parseState.getEntityBuilder().setPersonSubstitution(var, value);
		} else {
			logger.error("Unknown definition type \"" + type + "\" at " + this.getErrorLocation(parseState));
			return true;
		}
		
		return true;
	}
	
	private List<String> getArrayItems(String value) {
		String[] parts = value.split("<crlf>");
		List<String> items = new ArrayList<String>();
		for (int i = 0; i < parts.length; i++) {
			String[] pieces = null;
			
			if (parts[i].indexOf(JiveScriptConstants.DELIMETER_ARRAY_ITEM_BAR) > -1) {
				pieces = StringUtils.split(parts[i], JiveScriptConstants.DELIMETER_ARRAY_ITEM_BAR);
			} else {
				pieces = StringUtils.split(parts[i]);
			}
			
			items.addAll(Arrays.asList(pieces));
		}
		
		return items;
	}
}
