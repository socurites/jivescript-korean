package com.socurites.jive.core.analyze.entity;

import java.util.List;

public class JiveTokenModel {
	protected List<JiveToken> tokens;
	protected List<String> tags;

	public JiveTokenModel(List<JiveToken> tokens, List<String> tags) {
		this.tokens = tokens;
		this.tags = tags;
	}

	/**
	 * @return the tokens
	 */
	public List<JiveToken> getTokens() {
		return tokens;
	}

	/**
	 * @return the tags
	 */
	public List<String> getTags() {
		return tags;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		
		sb.append("# tokens: ");
		sb.append("\n");
		for (JiveToken token : this.tokens) {
			sb.append(token.getText() + "/" + token.getPos());
			sb.append("\n");
		}
		
		sb.append("# tags: ");
		sb.append("\n");
		for (String tag : this.tags) {
			sb.append(tag);
			sb.append("\n");
		}
		
		return sb.toString();
	}
}
