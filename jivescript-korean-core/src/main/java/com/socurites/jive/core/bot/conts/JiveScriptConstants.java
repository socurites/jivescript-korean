package com.socurites.jive.core.bot.conts;

public class JiveScriptConstants {
	public static final String TOPIC_RANDOM = "random";
	public static final String TOPIC_BEGIN = "__begin__";
	
	
	/** begin topic > request trigger. */
	public static final String TRIGGER_REQUEST = "request";
	
	/** begin topic > ok reply. */
	public static final String REPLY_OK = "{ok}";
	
	/** tags. */
	/** tags > format. */
	public static final String TAG_FORMAT_FORMAL = "formal";
	public static final String TAG_FORMAT_SENTENCE = "sentence";
	public static final String TAG_FORMAT_UPPERCASE = "uppercase";
	public static final String TAG_FORMAT_LOWERCASE= "lowercase";
	
	
	/** pos. */
	public static final String POS_NOUN = "noun";
	public static final String POS_VERB = "verb";
	public static final String POS_ADJECTIVE = "adj";
	public static final String POS_ETC = "etc";
	
	
	/** CMD TYPE. */
	public static final String CMD_DEFINE    		= "!";
	public static final String CMD_TRIGGER   		= "+";
	public static final String CMD_PREVIOUS  		= "%";
	public static final String CMD_REPLY     		= "-";
	public static final String CMD_CONTINUE  		= "^";
	public static final String CMD_REDIRECT  		= "@";
	public static final String CMD_CONDITION 		= "*";
	public static final String CMD_LABEL     		= ">";
	public static final String CMD_ENDLABEL 		 = "<";
	
	/** DEFINE_CMD_TYPE. */
	public static final String CMD_DEFINE_TYPE_VERSION = "version";
	public static final String CMD_DEFINE_TYPE_LOCAL = "local";
	public static final String CMD_DEFINE_TYPE_GLOBAL = "global";
	public static final String CMD_DEFINE_TYPE_VAR = "var";
	public static final String CMD_DEFINE_TYPE_ARRAY = "array";
	public static final String CMD_DEFINE_TYPE_SUB = "sub";
	public static final String CMD_DEFINE_TYPE_PERSON = "person";
	
	
	/** DELIMETER. */
	public static final String DELIMETER_ARRAY_ITEM_BAR = "|";
	
}
