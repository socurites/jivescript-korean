package com.socurites.jive.ext.analyze.entity;

import org.apache.commons.lang3.StringUtils;

import com.socurites.jive.core.bot.builder.JiveScriptReplyBuilder;
import com.socurites.jive.core.bot.builder.JiveScriptRequestBuilder;

public class JiveScriptExtReplyBuilder extends JiveScriptReplyBuilder {
	private JiveScriptReplyBuilder replyBuilder;
	
	public JiveScriptExtReplyBuilder(JiveScriptReplyBuilder reply) {
		this.replyBuilder = reply;
	}

	
	protected JiveExtDomainEntity domainEntity;
	/**
	 * @return the domainEntity
	 */
	public JiveExtDomainEntity getDomainEntity() {
		return domainEntity;
	}
	
	/* (non-Javadoc)
	 * @see com.socurites.jive.bot.builder.JiveScriptReplyBuilder#getReplyAsText()
	 */
	@Override
	public String getReplyAsText() {
		this.domainEntity = new JiveExtDomainEntity();
		String replyAsText = this.replyBuilder.getReplyAsText();
		String[] tokens = StringUtils.split(replyAsText, "|");
		
		replyAsText = tokens[0];
		
		if ( tokens.length > 1 ) {
			String[] domainTokens = StringUtils.split(tokens[1], "/");
			
			for ( String domainToken : domainTokens ) {
				String[] keyVal = StringUtils.split(domainToken, "=");
				
				if ( keyVal.length >= 2) {
					this.domainEntity.addProp(keyVal[0], keyVal[1]);
				} else {
					this.domainEntity.addProp(keyVal[0], null);
				}
			}
		}
		
		
		return replyAsText;
	}

	/* (non-Javadoc)
	 * @see com.socurites.jive.bot.builder.JiveScriptReplyBuilder#getRequestBuilder()
	 */
	@Override
	public JiveScriptRequestBuilder getRequestBuilder() {
		return this.replyBuilder.getRequestBuilder();
	}
	
	
}
