package com.socurites.jive.core.bot.builder;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.text.WordUtils;
import org.apache.log4j.Logger;

import com.socurites.jive.core.bot.conts.JiveScriptConstants;
import com.socurites.jive.core.engine.old.Client;
import com.socurites.jive.core.engine.old.Trigger;
import com.socurites.jive.core.engine.old.Util;
import com.socurites.jive.core.parser.entity.JiveScriptEntity;

public class JiveScriptReplyBuilder {
	private final static Logger logger = Logger.getLogger(JiveScriptReplyBuilder.class);
	
	protected JiveScriptEntity entityBuilder;
	
	protected static Random rand  = new Random(); // A random number generator
	
	protected String username;

	protected String reply;
	
	protected String request;
	
	public JiveScriptReplyBuilder() {
	}
	
	public JiveScriptReplyBuilder entityBuilder(JiveScriptEntity entityBuilder) {
		this.entityBuilder = entityBuilder;
		
		return this;
	}
	
	public JiveScriptReplyBuilder user(String username) {
		this.username = username;
		return this;
	}
	
	public JiveScriptReplyBuilder request(String request) {
		this.request = request;
		this.clearReply();
		return this;
	}
	
	protected JiveScriptRequestBuilder requestBuilder;
	public JiveScriptReplyBuilder requestBuilder(JiveScriptRequestBuilder requestBuilder) {
		this.requestBuilder = requestBuilder;
		return this;
	}
	
	public JiveScriptReplyBuilder reply(String reply) {
		this.reply = reply;
		return this;
	}
	
	private void clearReply() {
		this.reply = null;
	}
	
	public String getReplyAsText() {
		return this.reply;
	}
	
	public boolean isBeginReplyOK() {
		if (this.getReplyAsText().indexOf(JiveScriptConstants.REPLY_OK) > -1) {
			return true;
		}
		
		return false;
	}
	
	public JiveScriptRequestBuilder getRequestBuilder() {
		return this.requestBuilder;
	}

	public JiveScriptReplyBuilder build(boolean begin, int step) {
		String topic          = JiveScriptConstants.TOPIC_RANDOM;             	// Default topic = random
		List<String> stars    = new ArrayList<String>(); 						// Wildcard matches
		List<String> botstars = new ArrayList<String>(); 						// Wildcards in %Previous
		List<String> originStars = new ArrayList<String>();
		
		Client profile = this.entityBuilder.getClients().client(this.username);

		// Update their topic.
		topic = profile.get("topic");

		// Avoid letting the user fall into a missing topic.
		if (this.entityBuilder.hasTopic(topic) == false) {
			logger.error("<RS> User " + this.username + " was in a missing topic named \"" + topic + "\"!");
			topic = JiveScriptConstants.TOPIC_RANDOM;
			profile.set("topic", JiveScriptConstants.TOPIC_RANDOM);
		}

		if (step > entityBuilder.getDepthLimit()) {
			this.reply("ERR: Deep Recursion Detected!");
			logger.error("<RS> " + this.getReplyAsText());
			return this;
		}

		// Are we in the BEGIN statement?
		if (begin) {
//		if ( begin && topic.equals(JiveScriptConstants.TOPIC_RANDOM) ) {
			// This implies the begin topic.
			topic = JiveScriptConstants.TOPIC_BEGIN;
		}

		// Create a pointer for the matched data.
		Trigger matched = null;
		boolean foundMatch     = false;
		String  matchedTrigger = "";

		// See if there are any %previous's in this topic, or any topic related to it. This
		// should only be done the first time -- not during a recursive redirection.
		if (step == 0) {
			logger.debug("Looking for a %Previous");
			String[] allTopics = { topic };
			if (this.entityBuilder.getTopics().addTopic(topic).includes().length > 0 || this.entityBuilder.getTopics().addTopic(topic).inherits().length > 0) {
				// We need to walk the topic tree.
				allTopics = this.entityBuilder.getTopics().getTopicTree(topic, 0);
			}
			for (int i = 0; i < allTopics.length; i++) {
				// Does this topic have a %Previous anywhere?
				logger.debug("Seeing if " + allTopics[i] + " has a %Previous");
				if (this.entityBuilder.getTopics().addTopic(allTopics[i]).hasPrevious()) {
					logger.debug("Topic " + allTopics[i] + " has at least one %Previous");

					// Get them.
					String[] previous = this.entityBuilder.getTopics().addTopic(allTopics[i]).listPrevious();
					for (int j = 0; j < previous.length; j++) {
						logger.debug("Candidate: " + previous[j]);

						// Try to match the bot's last reply against this.
						String lastReply = profile.getReply(1);
						String regexp    = triggerRegexp(this.username, profile, previous[j]);
						logger.debug("Compare " + lastReply + " <=> " + previous[j] + " (" + regexp + ")");

						// Does it match?
						Pattern re = Pattern.compile("^" + regexp + "$");
						Matcher m  = re.matcher(lastReply);
						while (m.find() == true) {
							logger.debug("OMFG the lastReply matches!");

							// Harvest the botstars.
							for (int s = 1; s <= m.groupCount(); s++) {
								logger.debug("Add botstar: " + m.group(s));
								botstars.add(m.group(s));
							}

							// Now see if the user matched this trigger too!
							String[] candidates = this.entityBuilder.getTopics().addTopic(allTopics[i]).listPreviousTriggers(previous[j]);
							for (int k = 0; k < candidates.length; k++) {
								logger.debug("Does the user's message match " + candidates[k] + "?");
								String humanside = triggerRegexp(this.username, profile, candidates[k]);
								logger.debug("Compare " + this.request + " <=> " + candidates[k] + " (" + humanside + ")");

								Pattern reH = Pattern.compile("^" + humanside + "$");
								Matcher mH  = reH.matcher(this.request);
								while (mH.find() == true) {
									logger.debug("It's a match!!!");

									// Make sure it's all valid.
									String realTrigger = candidates[k] + "{previous}" + previous[j];
									if (this.entityBuilder.getTopics().addTopic(allTopics[i]).triggerExists(realTrigger)) {
										// Seems to be! Collect the stars.
										for (int s = 1; s <= mH.groupCount(); s++) {
											logger.debug("Add star: " + mH.group(s));
											stars.add(mH.group(s));
										}

										foundMatch = true;
										matchedTrigger = candidates[k];
										matched = this.entityBuilder.getTopics().addTopic(allTopics[i]).addTrigger(realTrigger);
									}

									break;
								}

								if (foundMatch) {
									break;
								}
							}
							if (foundMatch) {
								break;
							}
						}
					}
				}
			}
		}

		// Search their topic for a match to their trigger.
		if (foundMatch == false) {
			// Go through the sort buffer for their topic.
			String[] triggers = this.entityBuilder.getTopics().addTopic(topic).listTriggers();
			for (int a = 0; a < triggers.length; a++) {
				String trigger = triggers[a];

				// Prepare the trigger for the regular expression engine.
				String regexp = triggerRegexp(this.username, profile, trigger);
				logger.debug("Try to match \"" + this.request + "\" against \"" + trigger + "\" (" + regexp + ")");

				// Is it a match?
				Pattern re = Pattern.compile("^" + regexp + "$");
				Matcher m  = re.matcher(this.request);
				if (m.find() == true) {
					logger.debug("The trigger matches! Star count: " + m.groupCount());

					// Harvest the stars.
					int starcount = m.groupCount();
					for (int s = 1; s <= starcount; s++) {
						logger.debug("Add star: " + m.group(s));
						stars.add(m.group(s));
					}

					// We found a match, but what if the trigger we matched belongs to
					// an inherited topic? Check for that.
					if (this.entityBuilder.getTopics().addTopic(topic).triggerExists(trigger)) {
						// No, the trigger does belong to us.
						matched = this.entityBuilder.getTopics().addTopic(topic).addTrigger(trigger);
					}
					else {
						logger.debug("Trigger doesn't exist under this topic, trying to find it!");
						matched = this.entityBuilder.getTopics().findTriggerByInheritance(topic, trigger, 0);
					}

					foundMatch = true;
					matchedTrigger = trigger;
					break;
				}
			}
		}

		// Store what trigger they matched on (matchedTrigger can be blank if they didn't match).
		profile.set("__lastmatch__", matchedTrigger);

		// Did they match anything?
		if (foundMatch) {
			logger.debug("They were successfully matched to a trigger!");

			/*---------------------------------*/
			/*-- Process Their Matched Reply --*/
			/*---------------------------------*/

			// Make a dummy once loop so we can break out anytime.
			for (int n = 0; n < 1; n++) {
				// Exists?
				if (matched == null) {
					logger.error("<RS> Unknown error: they matched trigger " + matchedTrigger + ", but it doesn't exist?");
					foundMatch = false;
					break;
				}

				// Get the trigger object.
				Trigger trigger = matched;
				logger.debug("The trigger matched belongs to topic " + trigger.inTopic());

				// Check for conditions.
				List<String> conditialReplies = trigger.getConditionalReplies();
				if (conditialReplies.size() > 0) {
					logger.debug("This trigger has some conditions!");

					// See if any conditions are true.
					boolean truth = false;
					for ( String conditionalReply : conditialReplies ) {
						// Separate the condition from the potential reply.
						String[] halves = conditionalReply.split("\\s*=>\\s*");
						String condition = halves[0].trim();
						String potreply  = halves[1].trim();

						// Split up the condition.
						Pattern reCond = Pattern.compile("^(.+?)\\s+(==|eq|\\!=|ne|<>|<|<=|>|>=)\\s+(.+?)$");
						Matcher mCond  = reCond.matcher(condition);
						while (mCond.find()) {
							String left  = mCond.group(1).trim();
							String eq    = mCond.group(2).trim();
							String right = mCond.group(3).trim();

							// Process tags on both halves.
							left = this.processTags(this.username, profile, this.request, left, stars, botstars, step+1);
							right = this.processTags(this.username, profile, this.request, right, stars, botstars, step+1);
							logger.debug("Compare: " + left + " " + eq + " " + right);

							// Defaults
							if (left.length() == 0) {
								left = "undefined";
							}
							if (right.length() == 0) {
								right = "undefined";
							}

							// Validate the expression.
							if (eq.equals("eq") || eq.equals("ne") || eq.equals("==") || eq.equals("!=") || eq.equals("<>")) {
								// String equality comparing.
								if ((eq.equals("eq") || eq.equals("==")) && left.equals(right)) {
									truth = true;
									break;
								}
								else if ((eq.equals("ne") || eq.equals("!=") || eq.equals("<>")) && !left.equals(right)) {
									truth = true;
									break;
								}
							}

							// Numeric comparing.
							int lt = 0;
							int rt = 0;

							// Turn the two sides into numbers.
							try {
								lt = Integer.parseInt(left);
								rt = Integer.parseInt(right);
							} catch (NumberFormatException e) {
								// Oh well!
								break;
							}

							// Run the remaining equality checks.
							if (eq.equals("==") || eq.equals("!=") || eq.equals("<>")) {
								// Equality checks.
								if (eq.equals("==") && lt == rt) {
									truth = true;
									break;
								}
								else if ((eq.equals("!=") || eq.equals("<>")) && lt != rt) {
									truth = true;
									break;
								}
							}
							else if (eq.equals("<") && lt < rt) {
								truth = true;
								break;
							}
							else if (eq.equals("<=") && lt <= rt) {
								truth = true;
								break;
							}
							else if (eq.equals(">") && lt > rt) {
								truth = true;
								break;
							}
							else if (eq.equals(">=") && lt >= rt) {
								truth = true;
								break;
							}
						}

						// True condition?
						if (truth) {
							this.reply(potreply);
							break;
						}
					}
				}

				// Break if we got a reply from the conditions.
				if (this.getReplyAsText() != null && this.getReplyAsText().length() > 0) {
					break;
				}

				// Return one of the replies at random. We lump any redirects in as well.
				List<String> redirects = trigger.getRedirects();
				List<String> replies   = trigger.getReplies();

				// Take into account their weights.
				Vector<Integer> bucket = new Vector<Integer>();
				Pattern reWeight = Pattern.compile("\\{weight=(\\d+?)\\}");

				// Look at weights on redirects.
				int i = 0;
				for ( String redirect : redirects ) {
					if (redirect.indexOf("{weight=") > -1) {
						Matcher mWeight = reWeight.matcher(redirect);
						while (mWeight.find()) {
							int weight = Integer.parseInt(mWeight.group(1));

							// Add to the bucket this many times.
							if (weight > 1) {
								for (int j = 0; j < weight; j++) {
									logger.debug("Trigger has a redirect (weight " + weight + "): " + redirect);
									bucket.add(i);
								}
							}
							else {
								logger.debug("Trigger has a redirect (weight " + weight + "): " + redirect);
								bucket.add(i);
							}

							// Only one weight is supported.
							break;
						}
					}
					else {
						logger.debug("Trigger has a redirect: " + redirect);
						bucket.add(i);
					}
					i++;
				}

				// Look at weights on replies.
				i = 0;
				int currWeight = Integer.MIN_VALUE;
				for ( String reply : replies ) {
					if (reply.indexOf("{weight=") > -1) {
						Matcher mWeight = reWeight.matcher(reply);
						while (mWeight.find()) {
							int weight = Integer.parseInt(mWeight.group(1));
							
							if ( weight > currWeight ) {
								currWeight = weight;
							}

							// Add to the bucket this many times.
							if (weight > 1) {
								for (int j = 0; j < weight; j++) {
									logger.debug("Trigger has a reply (weight " + weight + "): " + reply);
									bucket.add(redirects.size() + i);
								}
							}
							else {
								logger.debug("Trigger has a reply (weight " + weight + "): " + reply);
								bucket.add(redirects.size() + i);
							}
//							 Only one weight is supported.
							break;
						}
						
//						System.out.println("currWeight=" + currWeight);
//						for (int j = 0; j < currWeight; j++) {
//							logger.debug("Trigger has a reply (weight " + currWeight + "): " + reply);
//							bucket.add(redirects.size() + i);
//						}
						
					}
					else {
						logger.debug("Trigger has a reply: " + reply);
						bucket.add(redirects.size() + i);
					}
					i++;
				}

				// Pull a random value out.
				int[] choices = Util.Iv2s(bucket);
				if (choices.length > 0) {
					int choice = choices [ rand.nextInt(choices.length) ];
					logger.debug("Possible choices: " + choices.length + "; chosen: " + choice);
					if (choice < redirects.size()) {
						// The choice was a redirect!
						String redirect = redirects.get(choice).replaceAll("\\{weight=\\d+\\}","");
						redirect = this.processTags (this.username, profile, this.request, redirect, stars, botstars, step);
						logger.debug("Chosen a redirect to " + redirect + "!");
						this.request(redirect)
							.build(begin, step+1)
						;
					}
					else {
						// The choice was a reply!
						choice -= redirects.size();
						if (choice < replies.size()) {
							logger.debug("Chosen a reply: " + replies.get(choice));
							this.reply(replies.get(choice));
						}
					}
				}
			}
		}

		// Still no reply?
		if (!foundMatch) {
			this.reply("ERR: No Reply Matched");
		}
		else if (this.getReplyAsText().length() == 0) {
			this.reply("ERR: No Reply Found");
		}

		logger.debug("Final reply: " + this.getReplyAsText() + " (begin: " + begin + ")");

		// Special tag processing for the BEGIN statement.
		if (begin) {
			// The BEGIN block may have {topic} or <set> tags and that's all.
			// <set> tag
			if (this.getReplyAsText().indexOf("<set") > -1) {
				Pattern reSet = Pattern.compile("<set (.+?)=(.+?)>");
				Matcher mSet  = reSet.matcher(this.getReplyAsText());
				while (mSet.find()) {
					String tag   = mSet.group(0);
					String var   = mSet.group(1);
					String value = mSet.group(2);

					// Set the uservar.
					profile.set(var, value);
					this.reply(this.getReplyAsText().replace(tag, ""));
				}
			}

			// {topic} tag
			if (this.getReplyAsText().indexOf("{topic=") > -1) {
				Pattern reTopic = Pattern.compile("\\{topic=(.+?)\\}");
				Matcher mTopic  = reTopic.matcher(this.getReplyAsText());
				while (mTopic.find()) {
					String tag = mTopic.group(0);
					topic      = mTopic.group(1);
					logger.debug("Set user's topic to: " + topic);
					profile.set("topic", topic);
					this.reply(this.getReplyAsText().replace(tag, ""));
				}
			}
		} else {
			// Process tags.
			this.reply(this.processTags (this.username, profile, this.request, this.getReplyAsText(), stars, botstars, step));
		}

		return this;
	}
	
	public String processTags(String user, Client profile, String message, String reply, List<String> vstars, List<String> vbotstars, int step) {
		// Pad the stars.
		vstars.add(0, "");
		vbotstars.add(0, "");

		// Set a default first star.
		if (vstars.size() == 1) {
			vstars.add("undefined");
		}
		if (vbotstars.size() == 1) {
			vbotstars.add("undefined");
		}

		// Convert the stars into simple arrays.
		String[] stars    = vstars.toArray(new String[] {});
		String[] botstars = vbotstars.toArray(new String[] {});

		// Shortcut tags.
		reply = reply.replaceAll("<person>",    "{person}<star>{/person}");
		reply = reply.replaceAll("<@>",         "{@<star>}");
		reply = reply.replaceAll("<formal>",    "{formal}<star>{/formal}");
		reply = reply.replaceAll("<sentence>",  "{sentence}<star>{/sentence}");
		reply = reply.replaceAll("<uppercase>", "{uppercase}<star>{/uppercase}");
		reply = reply.replaceAll("<lowercase>", "{lowercase}<star>{/lowercase}");

		// Quick tags.
		reply = reply.replaceAll("\\{weight=\\d+\\}", ""); // Remove {weight}s
		reply = reply.replaceAll("<input>", "<input1>");
		reply = reply.replaceAll("<reply>", "<reply1>");
		reply = reply.replaceAll("<id>", user);
		reply = reply.replaceAll("\\\\s", " ");
		reply = reply.replaceAll("\\\\n", "\n");
		reply = reply.replaceAll("\\\\", "\\");
		reply = reply.replaceAll("\\#", "#");

		// Stars
		reply = reply.replaceAll("<star>", stars[1]);
		reply = reply.replaceAll("<botstar>", botstars[1]);
		for (int i = 1; i < stars.length; i++) {
			reply = reply.replaceAll("<star" + i + ">", stars[i]);
		}
		for (int i = 1; i < botstars.length; i++) {
			reply = reply.replaceAll("<botstar" + i + ">", botstars[i]);
		}
		reply = reply.replaceAll("<(star|botstar)\\d+>", "");
		reply = reply.replaceAll("<(origin)\\d+>", "");

		// Input and reply tags.
		if (reply.indexOf("<input") > -1) {
			Pattern reInput = Pattern.compile("<input([0-9])>");
			Matcher mInput  = reInput.matcher(reply);
			while (mInput.find()) {
				String tag   = mInput.group(0);
				int    index = Integer.parseInt(mInput.group(1));
				String text  = profile.getInput(index).toLowerCase().replaceAll("[^a-z0-9 ]+","");
				reply = reply.replace(tag, text);
			}
		}
		if (reply.indexOf("<reply") > -1) {
			Pattern reReply = Pattern.compile("<reply([0-9])>");
			Matcher mReply  = reReply.matcher(reply);
			while (mReply.find()) {
				String tag   = mReply.group(0);
				int    index = Integer.parseInt(mReply.group(1));
				String text  = profile.getReply(index).toLowerCase().replaceAll("[^a-z0-9 ]+","");
				reply = reply.replace(tag, text);
			}
		}

		// {random} tag
		if (reply.indexOf("{random}") > -1) {
			Pattern reRandom = Pattern.compile("\\{random\\}(.+?)\\{\\/random\\}");
			Matcher mRandom  = reRandom.matcher(reply);
			while (mRandom.find()) {
				String tag          = mRandom.group(0);
				String[] candidates = mRandom.group(1).split("\\|");
				String chosen = candidates [ rand.nextInt(candidates.length) ];
				reply = reply.replace(tag, chosen);
			}
		}

		// <bot> tag
		if (reply.indexOf("<bot") > -1) {
			Pattern reBot = Pattern.compile("<bot (.+?)>");
			Matcher mBot  = reBot.matcher(reply);
			while (mBot.find()) {
				String tag = mBot.group(0);
				String var = mBot.group(1);

				// Setting the variable?
				if (var.indexOf("=") > -1) {
					String[] parts = var.split("\\s*=\\s*", 2);
					var = parts[0];
					String val = parts[1];
					entityBuilder.setVariable(var, val);
					reply = reply.replace(tag, "");
					continue;
				}

				if (entityBuilder.hasVariable(var)) {
					reply = reply.replace(tag, entityBuilder.getVariable(var));
				} else {
					reply = reply.replace(tag, "undefined");
				}
			}
		}

		// <env> tag
		if (reply.indexOf("<env") > -1) {
			Pattern reEnv = Pattern.compile("<env (.+?)>");
			Matcher mEnv  = reEnv.matcher(reply);
			while (mEnv.find()) {
				String tag = mEnv.group(0);
				String var = mEnv.group(1);

				// Setting the variable?
				if (var.indexOf("=") > -1) {
					String[] parts = var.split("\\s*=\\s*", 2);
					var = parts[0];
					String val = parts[1];
					entityBuilder.setGlobal(var, val);
					reply = reply.replace(tag, "");
					continue;
				}

				// Have this?
				if (entityBuilder.hasGlobal(var)) {
					reply = reply.replace(tag, entityBuilder.getGlobal(var));
				}
				else {
					reply = reply.replace(tag, "undefined");
				}
			}
		}

		// {person}
		if (reply.indexOf("{person}") > -1) {
			Pattern rePerson = Pattern.compile("\\{person\\}(.+?)\\{\\/person\\}");
			Matcher mPerson  = rePerson.matcher(reply);
			while (mPerson.find()) {
				String tag  = mPerson.group(0);
				String text = mPerson.group(1);

				// Run person substitutions.
				logger.debug("Run person substitutions: before: " + text);
				text = Util.substitute(entityBuilder.getSortedPersons(), entityBuilder.getPersons(), text);
				logger.debug("After: " + text);
				reply = reply.replace(tag, text);
			}
		}
		
		// {formal,uppercase,lowercase,sentence} tags
		if (reply.indexOf("{formal}") > -1 || reply.indexOf("{sentence}") > -1 ||
		reply.indexOf("{uppercase}") > -1 || reply.indexOf("{lowercase}") > -1) {
			String[] tags = { "formal", "sentence", "uppercase", "lowercase" };
			for (int i = 0; i < tags.length; i++) {
				Pattern reTag = Pattern.compile("\\{" + tags[i] + "\\}(.+?)\\{\\/" + tags[i] + "\\}");
				Matcher mTag  = reTag.matcher(reply);
				while (mTag.find()) {
					String tag  = mTag.group(0);
					String text = mTag.group(1);

					// String transform.
					text = formatReply(tags[i], text);
					reply = reply.replace(tag, text);
				}
			}
		}

		// <set> tag
		if (reply.indexOf("<set") > -1) {
			Pattern reSet = Pattern.compile("<set (.+?)=(.+?)>");
			Matcher mSet  = reSet.matcher(reply);
			while (mSet.find()) {
				String tag   = mSet.group(0);
				String var   = mSet.group(1);
				String value = mSet.group(2);

				// Set the uservar.
				profile.set(var, value);
				reply = reply.replace(tag, "");
				logger.debug("Set user var " + var + "=" + value);
			}
		}

		// <add, sub, mult, div> tags
		if (reply.indexOf("<add") > -1 || reply.indexOf("<sub") > -1 ||
		reply.indexOf("<mult") > -1 || reply.indexOf("<div") > -1) {
			String[] tags = { "add", "sub", "mult", "div" };
			for (int i = 0; i < tags.length; i++) {
				Pattern reTag = Pattern.compile("<" + tags[i] + " (.+?)=(.+?)>");
				Matcher mTag  = reTag.matcher(reply);
				while (mTag.find()) {
					String tag   = mTag.group(0);
					String var   = mTag.group(1);
					String value = mTag.group(2);

					// Get the user var.
					String curvalue = profile.get(var);
					int current = 0;
					if (!curvalue.equals("undefined")) {
						// Convert it to a int.
						try {
							current = Integer.parseInt(curvalue);
						} catch (NumberFormatException e) {
							// Current value isn't a number!
							reply = reply.replace(tag, "[ERR: Can't \"" + tags[i] + "\" non-numeric variable " + var + "]");
							continue;
						}
					}

					// Value must be a number too.
					int modifier = 0;
					try {
						modifier = Integer.parseInt(value);
					} catch (NumberFormatException e) {
						reply = reply.replace(tag, "[ERR: Can't \"" + tags[i] + "\" non-numeric value " + value + "]");
						continue;
					}

					// Run the operation.
					if (tags[i].equals("add")) {
						current += modifier;
					}
					else if (tags[i].equals("sub")) {
						current -= modifier;
					}
					else if (tags[i].equals("mult")) {
						current *= modifier;
					}
					else {
						// Don't divide by zero.
						if (modifier == 0) {
							reply.replace(tag, "[ERR: Can't divide by zero!]");
							continue;
						}
						current /= modifier;
					}

					// Store the new value.
					profile.set(var, Integer.toString(current));
					reply = reply.replace(tag, "");
				}
			}
		}

		// <get> tag
		if (reply.indexOf("<get") > -1) {
			Pattern reGet = Pattern.compile("<get (.+?)>");
			Matcher mGet  = reGet.matcher(reply);
			while (mGet.find()) {
				String tag = mGet.group(0);
				String var = mGet.group(1);

				// Get the user var.
				reply = reply.replace(tag, profile.get(var));
			}
		}

		// {topic} tag
		if (reply.indexOf("{topic=") > -1) {
			Pattern reTopic = Pattern.compile("\\{topic=(.+?)\\}");
			Matcher mTopic  = reTopic.matcher(reply);
			while (mTopic.find()) {
				String tag   = mTopic.group(0);
				String topic = mTopic.group(1);
				logger.debug("Set user's topic to: " + topic);
				profile.set("topic", topic);
				reply = reply.replace(tag, "");
			}
		}

		// {@redirect} tag
		if (reply.indexOf("{@") > -1) {
			Pattern reRed = Pattern.compile("\\{@([^\\}]*?)\\}");
			Matcher mRed  = reRed.matcher(reply);
			
			JiveScriptReplyBuilder replyBuilder = (new JiveScriptReplyBuilder())
					.entityBuilder(this.entityBuilder)
					.user(username)
			;
			while (mRed.find()) {
				String tag    = mRed.group(0);
				String target = mRed.group(1).trim();
				
				String subreply = replyBuilder.request(target).build(false, step + 1).getReplyAsText();
				reply = reply.replace(tag, subreply);
			}
		}
		
		// unPad the stars.
		vstars.remove(0);
		vbotstars.remove(0);

		return reply;
	}
	
	
	/**
	 * formats the text
	 * 
	 * @param format	formal: 	makes the first letter of each word uppercase. This is useful for names and other proper nouns.
	 * 					sentence: 	makes the first word of each sentence uppercase.
	 * 					uppercase:	makes the entire string upper case
	 * 					lowercase:	makes the entire string upper case
	 * @param text
	 * @return
	 */
	private String formatReply(String format, String text) {
		if (JiveScriptConstants.TAG_FORMAT_UPPERCASE.equals(format) ) {
			return text.toUpperCase();
		} else if ( JiveScriptConstants.TAG_FORMAT_LOWERCASE.equals(format) ) {
			return text.toLowerCase();
		}
		else if ( JiveScriptConstants.TAG_FORMAT_FORMAL.equals(format) ) {
			return WordUtils.capitalizeFully(text);
		}
		else if ( JiveScriptConstants.TAG_FORMAT_SENTENCE.equals(format) ) {
			return text.substring(0, 1).toUpperCase() + text.substring(1);
		} else {
			return "[ERR: Unknown String Transform " + format + "]";
		}
	}
	
	/**
	 * Formats a trigger for the regular expression engine.
	 *
	 * @param user    The user ID of the caller.
	 * @param trigger The raw trigger text.
	 */
	private String triggerRegexp (String user, Client profile, String trigger) {
		// If the trigger is simply '*', it needs to become (.*?) so it catches the empty string.
		String regexp = trigger.replaceAll("^\\*$", "<zerowidthstar>");

		// Simple regexps are simple.
		regexp = regexp.replaceAll("\\*", "(.+?)");             // *  ->  (.+?)
		regexp = regexp.replaceAll("#",   "(\\\\d+?)");         // #  ->  (\d+?)
		regexp = regexp.replaceAll("_",   "(\\\\w+?)");     // _  ->  ([A-Za-z ]+?)
		regexp = regexp.replaceAll("\\{weight=\\d+\\}", "");    // Remove {weight} tags
		regexp = regexp.replaceAll("<zerowidthstar>", "(.*?)"); // *  ->  (.*?)

		// Handle optionals.
		if (regexp.indexOf("[") > -1) {
			Pattern reOpts = Pattern.compile("\\s*\\[(.+?)\\]\\s*");
			Matcher mOpts  = reOpts.matcher(regexp);
			while (mOpts.find() == true) {
				String optional = mOpts.group(0);
				String contents = mOpts.group(1);

				// Split them at the pipes.
				String[] parts = contents.split("\\|");

				// Construct a regexp part.
				StringBuffer re = new StringBuffer();
				for (int i = 0; i < parts.length; i++) {
					// See: https://github.com/aichaos/rivescript-js/commit/02f236e78c5d237cb046d2347fe704f5f70231c9
					re.append("(?:\\s|\\b)+" + parts[i] + "(?:\\s|\\b)+");
					if (i < parts.length - 1) {
						re.append("|");
					}
				}
				String pipes = re.toString();

				// If this optional had a star or anything in it, e.g. [*],
				// make it non-matching.
				pipes = pipes.replaceAll("\\(\\.\\+\\?\\)", "(?:.+?)");
				pipes = pipes.replaceAll("\\(\\d\\+\\?\\)", "(?:\\\\d+?)");
				pipes = pipes.replaceAll("\\(\\w\\+\\?\\)", "(?:\\\\w+?)");

				// Put the new text in.
				pipes = "(?:" + pipes + "|(?:\\b|\\s)+)";
				regexp = regexp.replace(optional, pipes);
			}
		}

		// Make \w more accurate for our purposes.
		regexp = regexp.replaceAll("\\\\w", "[가-힣|A-Za-z]");

		// Filter in arrays.
		if (regexp.indexOf("@") > -1) {
			// Match the array's name.
			Pattern reArray = Pattern.compile("\\@(.+?)\\b");
			Matcher mArray  = reArray.matcher(regexp);
			while (mArray.find() == true) {
				String array = mArray.group(0);
				String name  = mArray.group(1);

				if (entityBuilder.hasArray(name)) {
					String[] values = entityBuilder.getArray(name).toArray(new String[] {});
					StringBuffer joined = new StringBuffer();

					// Join the array.
					for (int i = 0; i < values.length; i++) {
						joined.append(values[i]);
						if (i < values.length - 1) {
							joined.append("|");
						}
					}

					// Final contents...
					String rep = "(?:" + joined.toString() + ")";
					regexp = regexp.replace(array, rep);
				}
				else {
					// No array by this name.
					regexp = regexp.replace(array, "");
				}
			}
		}

		// Filter in bot variables.
		if (regexp.indexOf("<bot") > -1) {
			Pattern reBot = Pattern.compile("<bot (.+?)>");
			Matcher mBot  = reBot.matcher(regexp);
			while (mBot.find()) {
				String tag = mBot.group(0);
				String var = mBot.group(1);
				String value = entityBuilder.getVariable(var).toLowerCase().replace("[^a-z0-9 ]+","");

				// Have this?
				if (entityBuilder.hasVariable(var)) {
					regexp = regexp.replace(tag, value);
				}
				else {
					regexp = regexp.replace(tag, "undefined");
				}
			}
		}

		// Filter in user variables.
		if (regexp.indexOf("<get") > -1) {
			Pattern reGet = Pattern.compile("<get (.+?)>");
			Matcher mGet  = reGet.matcher(regexp);
			while (mGet.find()) {
				String tag = mGet.group(0);
				String var = mGet.group(1);
				String value = profile.get(var).toLowerCase().replaceAll("[^a-z0-9 ]+","");

				// Have this?
				regexp = regexp.replace(tag, value);
			}
		}

		// Input and reply tags.
		regexp = regexp.replaceAll("<input>", "<input1>");
		regexp = regexp.replaceAll("<reply>", "<reply1>");
		if (regexp.indexOf("<input") > -1) {
			Pattern reInput = Pattern.compile("<input([0-9])>");
			Matcher mInput  = reInput.matcher(regexp);
			while (mInput.find()) {
				String tag   = mInput.group(0);
				int    index = Integer.parseInt(mInput.group(1));
				String text  = profile.getInput(index).toLowerCase().replaceAll("[^a-z0-9 ]+","");
				regexp       = regexp.replace(tag, text);
			}
		}
		if (regexp.indexOf("<reply") > -1) {
			Pattern reReply = Pattern.compile("<reply([0-9])>");
			Matcher mReply  = reReply.matcher(regexp);
			while (mReply.find()) {
				String tag   = mReply.group(0);
				int    index = Integer.parseInt(mReply.group(1));
				String text  = profile.getReply(index).toLowerCase().replaceAll("[^a-z0-9 ]+","");
				regexp       = regexp.replace(tag, text);
			}
		}

		return regexp;
	}
}
