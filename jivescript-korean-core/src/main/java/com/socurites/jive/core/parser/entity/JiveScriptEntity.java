package com.socurites.jive.core.parser.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.socurites.jive.core.engine.old.ClientManager;
import com.socurites.jive.core.engine.old.TopicManager;

/**
 * Script Entity class parsed from script
 * 
 * @author socurites
 *
 */
public class JiveScriptEntity {
	private final static Logger logger = Logger.getLogger(JiveScriptEntity.class);
	
	private List<String> analyzerKeywords = new ArrayList<String>();
	public void addAnalyzerKeyword(String word) {
		this.analyzerKeywords.add(word);
	}
	
	
	/**
	 * @return the analyzerKeywords
	 */
	public List<String> getAnalyzerKeywords() {
		return analyzerKeywords;
	}

	/**
	 * 전역 변수
	 */
	private GlobalVar globalVar = new GlobalVar();
	
	/** ! var. */
	private Map<String, String> VARS = new HashMap<String, String>();
	
	/** ! array. */
	private Map<String, List<String>> ARRAYS = new HashMap<String, List<String>>();
	
	/** ! sub. */
	private Map<String, String> SUBS = new HashMap<String, String>();
	
	/** ! person. */
	private Map<String, String> PERSONS = new HashMap<String, String>();
	
	/** sorted sub. */
	private String[] SORTED_SUBS;
	
	/** sorted person. */
	private String[] SORTED_PERSONS;
	
	private TopicManager topics = new TopicManager();
	public TopicManager getTopics() {
		return this.topics;
	}
	public boolean hasTopic(String topicId) {
		if ( this.topics.exists(topicId) ) {
			return true;
		}
		
		return false;
	}
	
	private ClientManager clients = new ClientManager();
	public ClientManager getClients() {
		return this.clients;
	}
	
	public String getGlobal(String key) {
		return this.globalVar.get(key);
	}
	
	public String getVariable(String name) {
		return this.VARS.get(name);
	}
	
	public List<String> getArray(String name) {
		return this.ARRAYS.get(name);
	}
	
	public Map<String, String> getSubs() {
		return this.SUBS;
	}
	
	public String[] getSortedSubs() {
		return this.SORTED_SUBS;
	}
	
	public Map<String, String> getPersons() {
		return this.PERSONS;
	}
	
	public String[] getSortedPersons() {
		return this.SORTED_PERSONS;
	}
	
	public int getDepthLimit() {
		return this.globalVar.getDepthLimit();
	}
	
	public boolean hasGlobal(String key) {
		return this.globalVar.has(key);
	}
	
	public boolean hasVariable(String name) {
		return this.VARS.containsKey(name);
	}
	
	public boolean hasArray(String name) {
		return this.ARRAYS.containsKey(name);
	}
	
	/**
	 * 전역 변수 추가/삭제
	 * @param key
	 * @param value
	 */
	// TODO: 시스템 전역변수/사용자 전역변수 분리 필요
	public void setGlobal(String key, String value) {
		boolean delete = false;
		if (value == null || value == "<undef>") {
			delete = true;
		}

		if (key.equals("debug")) {
			if (value.equals("true") || value.equals("1") || value.equals("yes") || value.equals("false") || value.equals("0") || value.equals("no") || delete) {
			} else {
				logger.error("Global variable \"debug\" needs a boolean value");
			}
		} else if (key.equals("depth")) {
			try {
				Integer.parseInt(value);
			} catch (NumberFormatException e) {
				logger.error("Global variable \"depth\" needs an integer value");
			}
		}

		if (delete) {
			globalVar.removeUserDefinedVar(key);
		} else {
			globalVar.addUserDefinedVar(key, value);
		}
	}
	
	public boolean setVariable(String name, String value) {
		if (value == null || value == "<undef>") {
			this.VARS.remove(name);
		}
		else {
			this.VARS.put(name, value);
		}

		return true;
	}
	
	public boolean setArray(String name, List<String> items) {
		this.ARRAYS.put(name, items);
		
		return true;
	}
	
	public boolean setSubstitution (String pattern, String output) {
		if (output == null || output == "<undef>") {
			this.SUBS.remove(pattern);
		}
		else {
			this.SUBS.put(pattern, output);
		}

		return true;
	}
	
	public boolean setPersonSubstitution (String pattern, String output) {
		if (output == null || output == "<undef>") {
			this.PERSONS.remove(pattern);
		}
		else {
			this.PERSONS.put(pattern, output);
		}

		return true;
	}
	
	public boolean setSortedSubs(String[] sortedSubs) {
		this.SORTED_SUBS = sortedSubs;
		return true;
	}
	
	public boolean setSortedPersons(String[] sortedPersons) {
		this.SORTED_PERSONS = sortedPersons;
		return true;
	}
	
	/** delete. */
	public boolean removeArray(String name) {
		this.ARRAYS.remove(name);
		return true;
	}
}
