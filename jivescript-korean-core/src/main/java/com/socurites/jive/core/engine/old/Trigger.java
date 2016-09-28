package com.socurites.jive.core.engine.old;

import java.util.ArrayList;
import java.util.List;

public class Trigger {
	/** 트리거 패턴. */
	private String pattern = "";
	
	/** 트리거가 등록된 토픽명. */
	private String inTopic = "";

	/** @Redirect. */
	private List<String> redirects = new ArrayList<String>();

	/** -Reply. */
	private List<String> replies = new ArrayList<String>();

	/** *Condition. */
	private List<String> conditionalReplies = new ArrayList<String>();

	private boolean previous = false;

	public Trigger(String topic, String pattern) {
		this.inTopic = topic;
		this.pattern = pattern;
	}

	public String inTopic() {
		return this.inTopic;
	}

	public void hasPrevious(boolean paired) {
		this.previous = true;
	}

	public boolean hasPrevious() {
		return this.previous;
	}

	public void addReply(String reply) {
		this.replies.add(reply);
	}

	public List<String> getReplies() {
		return this.replies;
	}

	/**
	 * Add a new redirection to a trigger.
	 *
	 * @param meant
	 *            What the user "meant" to say.
	 */
	public void addRedirect(String meant) {
		this.redirects.add(meant);
	}

	public List<String> getRedirects() {
		return redirects;
	}

	public void addConditionalReply(String conditionalReply) {
		this.conditionalReplies.add(conditionalReply);
	}

	/**
	 * @return the contiditions
	 */
	public List<String> getConditionalReplies() {
		return conditionalReplies;
	}

}
