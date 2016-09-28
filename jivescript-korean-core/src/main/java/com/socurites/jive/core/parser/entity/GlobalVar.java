package com.socurites.jive.core.parser.entity;

import java.util.HashMap;
import java.util.Map;

/**
 * 전역 변수
 * @author socurites
 *
 */
public class GlobalVar {
	/**
	 * execution mode 
	 */
	private boolean debug = false;
	
	/**
	 * depth limit for recursion
	 */
	private int depthLimit = 50;
	
	private Map<String, String> userDefinedVar = new HashMap<String, String>();
	
	public GlobalVar() {
		this.userDefinedVar.put("debug", "false");
	}
	
	public void addUserDefinedVar(String key, String value) {
		this.userDefinedVar.put(key, value);
	}
	
	public void removeUserDefinedVar(String key) {
		this.userDefinedVar.remove(key);
	}
	
	public boolean has(String key) {
		return this.userDefinedVar.containsKey(key);
	}
	
	public String get(String key) {
		return this.userDefinedVar.get(key);
	}

	/**
	 * @return the debug
	 */
	public boolean isDebug() {
		return debug;
	}

	/**
	 * @param debug
	 *            the debug to set
	 */
	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	/**
	 * @return the depthLimit
	 */
	public int getDepthLimit() {
		return depthLimit;
	}

	/**
	 * @param depthLimit
	 *            the depthLimit to set
	 */
	public void setDepthLimit(int depthLimit) {
		this.depthLimit = depthLimit;
	}

}
