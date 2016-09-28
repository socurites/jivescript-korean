/*
	RiveScript - The Official Java RiveScript Interpreter

	Copyright (c) 2016 Noah Petherbridge

	Permission is hereby granted, free of charge, to any person obtaining a copy
	of this software and associated documentation files (the "Software"), to deal
	in the Software without restriction, including without limitation the rights
	to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
	copies of the Software, and to permit persons to whom the Software is
	furnished to do so, subject to the following conditions:

	The above copyright notice and this permission notice shall be included in all
	copies or substantial portions of the Software.

	THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
	IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
	FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
	AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
	LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
	OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
	SOFTWARE.
*/

package com.socurites.jive.core.engine.old;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

/**
 * A topic manager class for RiveScript.
 */

public class Topic {
	private final static Logger logger = Logger.getLogger(Topic.class);
	
	/** topic name. */
	private String name;
	
	private boolean hasPrevious  = false;

	/** triggers(topic name / pattern). */
	private Map<String, Trigger> triggers = new HashMap<String, Trigger>();
	private Map<String, Vector<String> > previous =	new HashMap<String, Vector<String> >();
	private List<String> includes = new ArrayList<String>();
	private List<String> inherits = new ArrayList<String>();
	private String[] sortedTriggers  = null;


	public Topic(String name) {
		this.name = name;
	}
	
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}



	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}



	public Trigger addTrigger(String pattern) {
		if ( this.triggers.containsKey(pattern) == false) {
			Trigger newTrigger = new Trigger(this.name, pattern);
			triggers.put(pattern, newTrigger);
		}
		
		return this.triggers.get(pattern);
	}
	
//	public Trigger getTrigger(String pattern) {
//		return this.triggers.get(pattern);
//	}

	/**
	 * Test if a trigger exists.
	 *
	 * @param trigger The pattern for the trigger.
	 */
	public boolean triggerExists (String trigger) {
		if (triggers.containsKey(trigger) == false) {
			return false;
		}
		return true;
	}

	/**
	 * Fetch a sorted list of all triggers. Note that the results are only accurate if
	 * you called sortTriggers() for this topic after loading new replies into it (the
	 * sortReplies() in RiveScript automagically calls sortTriggers() for all topics,
	 * so just make sure you call sortReplies() after loading new replies).
	 */
	public String[] listTriggers () {
		return listTriggers (false);
	}

	/**
	 * Fetch a list of all triggers. If you provide a true value to this method, it will
	 * return the UNSORTED list (getting the keys of the trigger hash directly). If you
	 * want a SORTED list (which you probably do), use listTriggers() instead, or explicitly
	 * provide a false value to this method.
	 *
	 * @param raw Get a raw unsorted list instead of a sorted one.
	 */
	public String[] listTriggers (boolean raw) {
		// If raw, get the unsorted triggers directly from the hash.
		if (raw) {
			// Turn the trigger keys into a list.
			Vector<String> trigs = new Vector<String>();
			Iterator it = triggers.keySet().iterator();
			while (it.hasNext()) {
				String next = it.next().toString();
				logger.debug("[RS::Topic] RAW TRIGGER: " + next);
				trigs.add (next);
			}

			// Turn the trigger vector into a string array.
			String[] result = new String [ trigs.size() ];
			int i = 0;
			for (Enumeration e = trigs.elements(); e.hasMoreElements(); ) {
				result[i] = e.nextElement().toString();
				i++;
			}

			// Return it.
			return result;
		}

		// Do we have a sort buffer?
		if (sortedTriggers == null) {
			// Um no, that's bad.
			System.err.println("You called listTriggers() for topic " + name + " before its replies have been sorted!");
			return new String [0];
		}
		return sortedTriggers;
	}

	public void sortTriggers (String[] alltrigs) {
		List<String> sorted   = new ArrayList<String>();

		// Do multiple sorts, one for each inheritence level.
		HashMap<Integer, Vector<String> > heritage = new HashMap<Integer, Vector<String> >();
		heritage.put(-1, new Vector<String>());
		int highest = -1;
		Pattern reInherit = Pattern.compile("\\{inherits=(\\d+)\\}");
		for (int i = 0; i < alltrigs.length; i++) {
			int inherits = -1; // Default, when no {inherits} tag.

			// Does it have an inherit level?
			if (alltrigs[i].indexOf("{inherits=") > -1) {
				Matcher m = reInherit.matcher(alltrigs[i]);
				while (m.find()) {
					inherits = Integer.parseInt(m.group(1));
					if (inherits > highest) {
						highest = inherits;
					}
					break;
				}
			}

			alltrigs[i] = alltrigs[i].replaceAll("\\{inherits=\\d+\\}","");

			// Initialize this inherit group?
			if (heritage.containsKey(inherits) == false) {
				heritage.put(inherits, new Vector<String>() );
			}

			// Add it.
			heritage.get(inherits).add(alltrigs[i]);
		}

		// Go on and sort each heritage level. We want to loop from level 0 up,
		// and then do level -1 last.
		for (int h = -1; h <= highest; h++) {
			if (heritage.containsKey(h) == false) {
				continue;
			}

			int inherits = h;
			logger.debug("[RS::Topic] Sorting triggers by heritage level " + inherits);
			String[] triggers = Util.Sv2s(heritage.get(inherits));

			// Sort-priority maps.
			HashMap<Integer, Vector<String> > prior = new HashMap<Integer, Vector<String> >();

			// Assign each trigger to its priority level.
			logger.debug("[RS::Topic] BEGIN sortTriggers in topic " + this.name);
			Pattern rePrior = Pattern.compile("\\{weight=(\\d+?)\\}");
			for (int i = 0; i < triggers.length; i++) {
				int priority = 0;

				// See if this trigger has a {weight}.
				if (triggers[i].indexOf("{weight") > -1) {
					// Try to match the regexp then.
					Matcher m = rePrior.matcher(triggers[i]);
					while (m.find() == true) {
						priority = Integer.parseInt(m.group(1));
					}
				}

				// Initialize its priority group?
				if (prior.containsKey(priority) == false) {
					// Create it.
					prior.put(priority, new Vector<String>() );
				}

				// Add it.
				prior.get(priority).add(triggers[i]);
			}

			/*
				Keep in mind here that there is a difference between includes and
				inherits -- topics that inherit other topics are able to OVERRIDE
				triggers that appear in the inherited topic. This means that if the
				top topic has a trigger of simply *, then NO triggers are capable of
				matching in ANY inherited topic, because even though * has the lowest
				sorting priority, it has an automatic priority over all inherited
				topics.

				The topicTriggers in TopicManager takes this into account. All topics
				that inherit other topics will have their local triggers prefixed
				with a fictional {inherits} tag, which will start at {inherits=0}
				and increment if the topic tree has other inheriting topics. So
				we can use this tag to make sure topics that inherit things will
				have their triggers always be on the top of the stack, from
				inherits=0 to inherits=n.
			*/

			// Sort the priority lists numerically from highest to lowest.
			int[] prior_sorted = Util.sortKeysDesc(prior);
			for (int p = 0; p < prior_sorted.length; p++) {
				logger.debug("[RS::Topic] Sorting triggers w/ priority " + prior_sorted[p]);
				Vector<String> p_list = prior.get(prior_sorted[p]);

				/*
					So, some of these triggers may include {inherits} tags, if
					they came from a topic which inherits another topic. Lower
					inherits values mean higher priority on the stack. Keep this
					in mind when keeping track of how to sort these things.
				*/

				int highest_inherits = inherits; // highest {inherits} we've seen

				// Initialize a sort bucket that will keep inheritance levels'
				// triggers in separate places.
				//InheritanceManager bucket = new InheritanceManager();
				Inheritance bucket = new Inheritance();

				// Loop through the triggers and sort them into their buckets.
				for (Enumeration e = p_list.elements(); e.hasMoreElements(); ) {
					String trigger = e.nextElement().toString();

					// Count the number of whole words it has.
					String[] words = trigger.split("[ |\\*|\\#|\\_]");
					int wc = 0;
					for (int w = 0; w < words.length; w++) {
						if (words[w].length() > 0) {
							wc++;
						}
					}

					logger.debug("[RS::Topic] On trigger: " + trigger + " (it has " + wc + " words) - inherit level: " + inherits);

					// Profile it.
					if (trigger.indexOf("_") > -1) {
						// It has the alpha wildcard, _.
						if (wc > 0) {
							bucket.addAlpha(wc, trigger);
						}
						else {
							bucket.addUnder(trigger);
						}
					}
					else if (trigger.indexOf("#") > -1) {
						// It has the numeric wildcard, #.
						if (wc > 0) {
							bucket.addNumber(wc, trigger);
						}
						else {
							bucket.addPound(trigger);
						}
					}
					else if (trigger.indexOf("*") > -1) {
						// It has the global wildcard, *.
						if (wc > 0) {
							bucket.addWild(wc, trigger);
						}
						else {
							bucket.addStar(trigger);
						}
					}
					else if (trigger.indexOf("[") > -1) {
						// It has optional parts.
						bucket.addOption(wc, trigger);
					}
					else {
						// Totally atomic.
						bucket.addAtomic(wc, trigger);
					}
				}

				// Sort each inheritence level individually.
				logger.debug("[RS::Topic] Dumping sort bucket !");
				Vector<String> subsort = bucket.dump(new Vector<String>());
				for (Enumeration e = subsort.elements(); e.hasMoreElements(); ) {
					String next = e.nextElement().toString();
					logger.debug("[RS::Topic] ADD TO SORT: " + next);
					sorted.add(next);
				}
			}
		}

		// Turn the running sort buffer into a string array and store it.
		this.sortedTriggers = sorted.toArray(new String[] {});
	}

	/**
	 * Add a mapping between a trigger and a %Previous that follows it.
	 *
	 * @param pattern  The trigger pattern.
	 * @param previous The pattern in the %Previous.
	 */
	public void addPrevious (String pattern, String previous) {
		// Add it to the vector.
		if (this.previous.containsKey(previous) == false) {
			this.previous.put(previous, new Vector<String>());
		}
		this.previous.get(previous).add(pattern);
	}

	/**
	 * Check if any trigger in the topic has a %Previous (only good after
	 * sortPrevious, from RiveScript.sortReplies is called).
	 */
	public boolean hasPrevious () {
		return this.hasPrevious;
	}

	/**
	 * Get a list of all the %Previous keys.
	 */
	public String[] listPrevious () {
		Vector<String> vector = new Vector<String>();
		Iterator sit = previous.keySet().iterator();
		while (sit.hasNext()) {
			vector.add((String) sit.next());
		}
		return Util.Sv2s(vector);
	}

	/**
	 * List the triggers associated with a %Previous.
	 *
	 * @param previous The %Previous pattern.
	 */
	public String[] listPreviousTriggers (String previous) {
		// TODO return sorted list
		if (this.previous.containsKey(previous)) {
			return Util.Sv2s(this.previous.get(previous));
		}
		return new String[0];
	}

	/**
	 * Sort the %Previous buffer.
	 */
	public void sortPrevious () {
		// Keep track if ANYTHING has a %Previous.
		this.hasPrevious = false;

		// Find all the triggers that have a %Previous. This hash maps a %Previous
		// label to the list of triggers that are associated with it.
		HashMap<String, Vector<String> > prev2trig = new HashMap<String, Vector<String> >();

		// Loop through the triggers to find those with a %Previous.
		String[] triggers = this.listTriggers(true);
		for (int i = 0; i < triggers.length; i++) {
			String pattern = triggers[i];
			if (pattern.indexOf("{previous}") > -1) {
				// This one has it.
				this.hasPrevious = true;
				String[] parts = pattern.split("\\{previous\\}", 2);
				String previous = parts[1];

				// Keep it under the %Previous.
				if (prev2trig.containsKey(previous) == false) {
					prev2trig.put(previous, new Vector<String>());
				}
				prev2trig.get(previous).add(parts[0]);
			}
		}

		// TODO: we need to sort the triggers but ah well
		this.previous = prev2trig;
	}

	/**
	 * Query whether a %Previous is registered with this topic.
	 *
	 * @param previous The pattern in the %Previous.
	 */
	public boolean previousExists (String previous) {
		if (this.previous.containsKey(previous)) {
			return true;
		}
		return false;
	}

	/**
	 * Retrieve a string array of the +Triggers that are associated with a %Previous.
	 *
	 * @param previous The pattern in the %Previous.
	 */
	public String[] listPrevious (String previous) {
		if (this.previous.containsKey(previous)) {
			return Util.Sv2s (this.previous.get(previous));
		}
		else {
			return new String[0];
		}
	}

	/**
	 * Add a topic that this one includes.
	 *
	 * @param topic The included topic's name.
	 */
	public void includes (String topic) {
		this.includes.add(topic);
	}

	/**
	 * Add a topic that this one inherits.
	 *
	 * @param topic The inherited topic's name.
	 */
	public void inherits (String topic) {
		this.inherits.add(topic);
	}

	/**
	 * Retrieve a list of included topics.
	 */
	public String[] includes () {
		return this.includes.toArray(new String[] {});
	}

	/**
	 * Retrieve a list of inherited topics.
	 */
	public String[] inherits () {
		return this.inherits.toArray(new String[] {});
	}
}
