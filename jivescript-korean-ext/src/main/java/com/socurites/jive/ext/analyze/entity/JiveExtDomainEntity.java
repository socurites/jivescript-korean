package com.socurites.jive.ext.analyze.entity;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;


public class JiveExtDomainEntity {
	private Map<String, String> props = new HashMap<String, String>();
	
	public void addProp(String key, String value) {
		this.props.put(key, value);
	}
	
	public String getProp(String key) {
		if ( this.props.containsKey(key) ) {
			return this.props.get(key);
		} else {
			throw new RuntimeException("key doesn't defined: " + key);
		}
	}
	
	public boolean hasProp(String key) {
		if ( this.props.containsKey(key) ) {
			return true;
		} else {
			return false;
		}
	}
	
	public boolean isEmpty() {
		if ( props.isEmpty() ) {
			return true;
		}
		
		return false;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		
		Set<String> keys = props.keySet();
		for ( String key : keys ) {
			sb.append(key + "=" + props.get(key) + ", ");
		}
		
		if ( sb.length() > 0 ) {
			return sb.toString().substring(0, sb.length()-2);
		} else {
			return sb.toString();
		}
	}
}
