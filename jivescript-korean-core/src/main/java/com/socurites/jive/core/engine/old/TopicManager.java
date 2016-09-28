package com.socurites.jive.core.engine.old;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;


public class TopicManager {
	/** topics. */
	private Map<String, Topic> topics = new HashMap<String, Topic>();
	
	private List<String> topicIds = new ArrayList<String>(); // A vector of topics

	public TopicManager () {
	}
	
	public int getTopicSize() {
		return this.topicIds.size();
	}
	
	public List<String> getTopicIds() {
		return this.topicIds;
	}

	public Topic addTopic(String topic) {
		if ( topics.containsKey(topic) == false ) {
			Topic newTopic = new Topic(topic);
			topics.put(topic, newTopic);
			topicIds.add(topic);
		}

		return topics.get(topic);
	}

	public boolean exists(String topicId) {
		return topics.containsKey(topicId);
	}

	/**
	 * Sort the replies in all the topics. This will build trigger lists of
	 * the topics (taking into account topic inheritence/includes) and sending
	 * the final trigger list into each topic's individual sortTriggers() method.
	 */
	public void sortReplies () {
		// Get trigger lists for all the topics.
		
		for ( String topicId : this.topicIds ) {
			String[] alltrig = this.topicTriggers(topicId, 0, 0, false);

			// Make this topic sort using this trigger list.
			this.addTopic(topicId).sortTriggers(alltrig);

			// Make the topic update its %Previous buffer.
			this.addTopic(topicId).sortPrevious();
		}
	}

	/**
	 * Walk the inherit/include trees and return a list of unsorted triggers.
	 *
	 * @param topic The name of the topic to start at.
	 * @param depth A recursion depth limit (can't recurse more than 50 levels)
	 * @param inheritence The current inheritence level (starts at 0)
	 * @param inherited   Whether the topic is inherited
	 */
	private String[] topicTriggers (String topic, int depth, int inheritance, boolean inherited) {
		// Break if we're too deep.
		if (depth > 50) {
			System.err.println("Deep recursion while scanning topic inheritance (topic " + topic + " was involved)");
			return new String[0];
		}

		/*
			Important info about the depth vs inheritance params to this function:
			depth increments by 1 every time this function recursively calls itself.
			inheritance increments by 1 only when this topic inherits another topic.

			This way, '>topic alpha includes beta inherits gamma' will have this effect:
				alpha and beta's triggers are combined together into one pool, and then
				these triggers have higher matching priority than gamma's.

			The inherited option is true if this is a recursive call, from a topic
			that inherits other topics. This forces the {inherits} tag to be added to
			the triggers, for the topic's sortTriggers() to deal with. This only applies
			when the top topic "includes" another topic.
		*/

		// Collect an array of triggers to return.
		Vector<String> triggers = new Vector<String>();

		// Does this topic include others?
		String[] includes = this.addTopic(topic).includes();
		if (includes.length > 0) {
			for (int i = 0; i < includes.length; i++) {
				// Recurse.
				String[] recursive = this.topicTriggers (includes[i], (depth+1), inheritance, false);
				for (int j = 0; j < recursive.length; j++) {
					triggers.add(recursive[j]);
				}
			}
		}

		// Does this topic inherit others?
		String[] inherits = this.addTopic(topic).inherits();
		if (inherits.length > 0) {
			for (int i = 0; i < inherits.length; i++) {
				// Recurse.
				String[] recursive = this.topicTriggers (inherits[i], (depth+1), (inheritance+1), true);
				for (int j = 0; j < recursive.length; j++) {
					triggers.add(recursive[j]);
				}
			}
		}

		// Collect the triggers for *this* topic. If this topic inherits any other
		// topics, it means that this topic's triggers have higher priority than
		// those in any inherited topics. Enforce this with an {inherits} tag.
		String[] localTriggers = this.addTopic(topic).listTriggers(true);
		if (inherits.length > 0 || inherited) {
			// Get the raw unsorted triggers.
			for (int i = 0; i < localTriggers.length; i++) {
				// Skip any trigger with a {previous} tag, these are for %Previous
				// and don't go in the general population.
				if (localTriggers[i].indexOf("{previous}") > -1) {
					continue;
				}

				// Prefix it with an {inherits} tag.
				triggers.add("{inherits=" + inheritance + "}" + localTriggers[i]);
			}
		}
		else {
			// No need for an inherits tag here.
			for (int i = 0; i < localTriggers.length; i++) {
				// Skip any trigger with a {previous} tag, these are for %Previous
				// and don't go in the general population.
				if (localTriggers[i].indexOf("{previous}") > -1) {
					continue;
				}

				triggers.add(localTriggers[i]);
			}
		}

		// Return it as an array.
		return Util.Sv2s(triggers);
	}

	/**
	 * Walk the inherit/include trees starting with one topic and find the trigger
	 * object that corresponds to the search trigger. Or rather, if you have a trigger
	 * that was part of a topic's sort list, but that topic itself doesn't manage
	 * that trigger, this function will search the tree to find the topic that does,
	 * and return its Trigger object.
	 *
	 * @param topic   The name of the topic to start at.
	 * @param pattern The trigger pattern text.
	 * @param depth   The current depth limit (should start at 0), for recursion.
	 */
	public Trigger findTriggerByInheritance (String topic, String pattern, int depth) {
		// Break if we're too deep.
		if (depth > 50) {
			System.err.println("Deep recursion while scanning topic inheritance (topic " + topic + " was involved)");
			return null;
		}

		// Inheritance is more important than inclusion.
		String[] inherits = this.addTopic(topic).inherits();
		for (int i = 0; i < inherits.length; i++) {
			// Does this topic have our trigger?
			if (this.addTopic(inherits[i]).triggerExists(pattern)) {
				// Good! Return it!
				return this.addTopic(inherits[i]).addTrigger(pattern);
			}
			else {
				// Recurse.
				Trigger match = this.findTriggerByInheritance (inherits[i], pattern, (depth+1));
				if (match != null) {
					// Found it!
					return match;
				}
			}
		}

		// Now check for "includes".
		String[] includes = this.addTopic(topic).includes();
		for (int i = 0; i < includes.length; i++) {
			// Does this topic have our trigger?
			if (this.addTopic(includes[i]).triggerExists(pattern)) {
				// Good! Return it!
				return this.addTopic(includes[i]).addTrigger(pattern);
			}
			else {
				// Recurse.
				Trigger match = this.findTriggerByInheritance (includes[i], pattern, (depth+1));
				if (match != null) {
					// Found it!
					return match;
				}
			}
		}

		// Don't know what else we can do.
		return null;
	}

	/**
	 * Walk the inherit/include trees starting with one topic and list every topic we find.
	 *
	 * @param topic   The name of the topic to start at.
	 * @param depth   The current depth limit (should start at 0), for recursion.
	 */
	public String[] getTopicTree (String topic, int depth) {
		// Avoid deep recursion.
		if (depth >= 50) {
			System.err.println("Deep recursion while scanning topic inheritance (topic " + topic + " was involved)");
			return new String[0];
		}

		// Collect a vector of topics.
		Vector<String> result = new Vector<String>();
		result.add(topic);

		// Does this topic include others?
		String[] includes = this.addTopic(topic).includes();
		for (int i = 0; i < includes.length; i++) {
			String[] children = this.getTopicTree(includes[i], (depth+1));
			for (int j = 0; j < children.length; j++) {
				result.add(children[j]);
			}
		}

		// Does it inherit?
		String[] inherits = this.addTopic(topic).inherits();
		for (int i = 0; i < inherits.length; i++) {
			String[] children = this.getTopicTree(inherits[i], (depth+1));
			for (int j = 0; j < children.length; j++) {
				result.add(children[j]);
			}
		}

		// Return.
		return Util.Sv2s(result);
	}
}
