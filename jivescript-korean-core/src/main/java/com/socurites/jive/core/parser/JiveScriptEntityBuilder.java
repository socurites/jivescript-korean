package com.socurites.jive.core.parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import com.socurites.jive.core.bot.conts.JiveScriptConstants;
import com.socurites.jive.core.engine.old.Util;
import com.socurites.jive.core.parser.entity.JiveScriptEntity;
import com.socurites.jive.core.parser.line.ConditionLineParserBuilder;
import com.socurites.jive.core.parser.line.DefineLineParserBuilder;
import com.socurites.jive.core.parser.line.LabelLineParserBuilder;
import com.socurites.jive.core.parser.line.LineParserBuilder;
import com.socurites.jive.core.parser.line.RedirectLineParserBuilder;
import com.socurites.jive.core.parser.line.ReplyLineParserBuilder;
import com.socurites.jive.core.parser.line.TriggerLineParserBuilder;
import com.socurites.jive.util.file.JiveFileUtils;

/**
 * Jive Script Parser
 * Extended from Rive Script Parser
 * 
 * @author socurites
 *
 */
public class JiveScriptEntityBuilder {
	private final static Logger logger = Logger.getLogger(JiveScriptEntityBuilder.class);
	public static final String VERSION        = "0.6.0";
	public static final double RS_VERSION    = 2.0;
	
	/** directory path for script file. */
	private String templateDirectoryPath;
	
	/** directory path for keyword file. */
	private String keywordDirectoryPath;
	
	private List<File> templateFiles = new ArrayList<File>();
	
	private List<File> keywordFiles = new ArrayList<File>();
	
	/**
	 * Script object parsed from script file
	 */
	private JiveScriptEntity entityBuilder;
	
	
	
	public JiveScriptEntityBuilder() {
		this.entityBuilder = new JiveScriptEntity();
	}
	
	public JiveScriptEntityBuilder templateDirectory(String templateDirectoryPath) {
		this.templateDirectoryPath = templateDirectoryPath;
		this.loadTemplateDirectory(this.templateDirectoryPath);
		
		return this;
	}
	
	public JiveScriptEntityBuilder keywordDirectory(String keywordDirectoryPath) {
		if ( keywordDirectoryPath != null ) {
			this.keywordDirectoryPath = keywordDirectoryPath;
			this.loadKeywordDirectory(this.keywordDirectoryPath);
		}
		
		return this;
	}
	
	/**
	 * Load template directory
	 * 
	 * @param path
	 * @return
	 */
	private boolean loadTemplateDirectory(String path) {
		logger.debug("Load directory: " + path);
		String[] exts = { ".rive", ".rs" };

		File templateDir = JiveFileUtils.getDir(path);
		
		for (int i = 0; i < exts.length; i++) {
			logger.debug("Searching for files of type: " + exts[i]);
			
			final String type = exts[i];
			File[] templateFiles = templateDir.listFiles(new FilenameFilter() {
				public boolean accept (File file, String name) {
					return name.endsWith(type);
				}
			});

			if (templateFiles == null) {
				logger.error("Couldn't read any files from directory " + path);
				return false;
			}
			
			for (int j = 0; j < templateFiles.length; j++) {
				if (templateFiles[j].exists() == false) {
					logger.error(templateFiles[j].getPath() + ": file not found.");
					return false;
				}
				if (templateFiles[j].isFile() == false) {
					logger.error(templateFiles[j].getPath() + ": not a regular file.");
					return false;
				}
				if (templateFiles[j].canRead() == false) {
					logger.error(templateFiles[j].getPath() + ": can't read from file.");
					return false;
				}
				
				this.templateFiles.add(templateFiles[j]);
			}
		}

		return true;
	}

	private boolean loadKeywordDirectory(String path) {
		String ext = ".jive";

		File keywordDir = JiveFileUtils.getDir(path);
		
		File[] keywordFiles = keywordDir.listFiles(new FilenameFilter() {
			public boolean accept (File file, String name) {
				return name.endsWith(ext);
			}
		});
		
		for (int i = 0; i < keywordFiles.length; i++) {
			this.keywordFiles.add(keywordFiles[i]);
		}
		
		return true;
	}
	
	public JiveScriptEntity build() {
		parseKeyword();
		parseTemplate();
		
		return this.entityBuilder;
	}
	
	private void parseKeyword() {
		for ( File keywordFile : this.keywordFiles ) {
			Scanner scanner = null;
			try {
				scanner = new Scanner(keywordFile);
				while ( scanner.hasNextLine() ) {
					this.entityBuilder.addAnalyzerKeyword(scanner.nextLine());
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} finally {
				scanner.close();
				
			}
			
		}
	}
	
	private void parseTemplate() {
		for ( File templateFile : this.templateFiles ) {
			List<String> lines = null;
			try {
				lines = FileUtils.readLines(templateFile);
				String[] templates = lines.toArray(new String[] {});
				
				parse(templateFile.getPath(), templates);
			} catch (FileNotFoundException e) {
				logger.error(templateFile.getPath() + ": file not found exception.");
			} catch (IOException e) {
				logger.error(templateFile.getPath() + ": IOException while reading.");
			}
		}
		
		sortReplies();
	}
	
	/**
	 * parse script codes
	 * 
	 * @param fileName
	 * @param code
	 * @return
	 */
	protected boolean parse(String fileName, String[] code) {
		JiveScriptParseState parseState = (new JiveScriptParseState(this.entityBuilder))
				.localOptions()
				.fileName(fileName)
		;
		
		boolean parseResult = false;
		for ( ; parseState.getLineNum() < code.length; parseState.incrementLineNum()) {
			logger.debug("Line: " + code[parseState.getLineNum()]);
			parseState.setElementState(code[parseState.getLineNum()]);
			
			if ( parseState.isIgnorable() ) {
				continue;
			}

			if ( parseState.isTrigger() ) {	
				parseState.clearPreviousCommandText();
			}

			this.parseLookaheadLoop(fileName, code, parseState);

			if (parseState.getCommandType().equals(JiveScriptConstants.CMD_DEFINE)) {
				LineParserBuilder defineLineParserBuilder = (new DefineLineParserBuilder())
						.commandText(parseState.getCommandText());
				
				parseState = parseState.currentLineParser(defineLineParserBuilder);
				parseResult = parseState.build();
				
				if ( parseResult == true ) {
					continue;
				} else {
					return false;
				}
				
			} else if (parseState.getCommandType().equals(JiveScriptConstants.CMD_LABEL)
					|| parseState.getCommandType().equals(JiveScriptConstants.CMD_ENDLABEL) ) {
				LineParserBuilder labelLineParserBuilder = (new LabelLineParserBuilder());
				
				parseState = parseState.currentLineParser(labelLineParserBuilder);
				parseResult = parseState.build();
				
			} else if (parseState.getCommandType().equals(JiveScriptConstants.CMD_TRIGGER)) {
				TriggerLineParserBuilder triggerLineParserBuilder = (new TriggerLineParserBuilder());
				
				parseState = parseState.currentLineParser(triggerLineParserBuilder);
				parseResult = parseState.build();
			} else if (parseState.getCommandType().equals(JiveScriptConstants.CMD_REPLY)) {
				ReplyLineParserBuilder replyLineParserBuilder = (new ReplyLineParserBuilder());
				
				parseState = parseState.currentLineParser(replyLineParserBuilder);
				parseResult = parseState.build();
				
				if ( parseResult == false ) {
					continue;
				}
			} else if (parseState.getCommandType().equals(JiveScriptConstants.CMD_PREVIOUS)) {
				// % PREVIOUS
				// This was handled above.
			} else if (parseState.getCommandType().equals(JiveScriptConstants.CMD_CONTINUE)) {
				// ^ CONTINUE
				// This was handled above.
			} else if (parseState.getCommandType().equals(JiveScriptConstants.CMD_REDIRECT)) {
				RedirectLineParserBuilder redirectLineParserBuilder = (new RedirectLineParserBuilder());
				
				parseState = parseState.currentLineParser(redirectLineParserBuilder);
				parseResult = parseState.build();
				
				if ( parseResult == false ) {
					continue;
				}
			} else if (parseState.getCommandType().equals(JiveScriptConstants.CMD_CONDITION)) {
				ConditionLineParserBuilder condtionLineParserBuilder = (new ConditionLineParserBuilder());
				
				parseState = parseState.currentLineParser(condtionLineParserBuilder);
				parseResult = parseState.build();
				
				if ( parseResult == false ) {
					continue;
				}
			} else {
				logger.error("Unrecognized currElement.getCommand() \"" + parseState.getCommandType() + "\" at " + fileName + " line " + parseState.getLineNum());
			}
		}

		return true;
	}
	
	private void parseLookaheadLoop(String fileName, String[] code, JiveScriptParseState parseState) {
		JiveScriptParseState lookAhead = (new JiveScriptParseState(this.entityBuilder))
				.localOptions()
				.fileName(fileName)
		;
		
		for (int j = (parseState.getLineNum() + 1); j < code.length; j++) {
			lookAhead.setElementState(code[j]);
			
			if ( lookAhead.isIgnorable() || !lookAhead.hasCommandText() ) {
				continue;
			}

			if ( !lookAhead.isContinue() && !lookAhead.isPrevious() ) {
				break;
			}

			if ( parseState.isTrigger() ) {
				if (lookAhead.isPrevious() ) {
					parseState.setPreviousCommandText(lookAhead.getCommandText());
					break;
				}
				else {
					parseState.clearPreviousCommandText();
				}
			}

			if ( parseState.isDefine() ) {
				if ( lookAhead.isContinue() ) {
					parseState.appendCommandText("<crlf>" + lookAhead.getCommandText());
				}
			}

			if ( !parseState.isContinue() && !parseState.isPrevious() && !parseState.isDefine() ) {
				if ( lookAhead.isContinue() ) {
					// Concatenation character?
					String concat = "";
					if (parseState.getLocalOptions().get("concat").equals("space")) {
						concat = " ";
					} else if (parseState.getLocalOptions().get("concat").equals("newline")) {
						concat = "\n";
					}
					parseState.appendCommandText(concat + lookAhead.getCommandText());
				}
				else {
					break;
				}
			}
		}
	}
	
	public void sortReplies () {
		// We need to make sort buffers under each topic.
		logger.debug("There are " + this.entityBuilder.getTopics().getTopicSize() + " topics to sort replies for.");

		// Tell the topic manager to sort its topics' replies.
		this.entityBuilder.getTopics().sortReplies();

		// Sort the substitutions.
		entityBuilder.setSortedSubs(Util.sortByLength (entityBuilder.getSubs().keySet().toArray(new String[] {})));
		entityBuilder.setSortedPersons(Util.sortByLength (entityBuilder.getPersons().keySet().toArray(new String[] {})));
	}
	
	
	
	
	
	
//	/**
//	 * Set a variable for one of the bot's users. A null value will delete a
//	 * variable.
//	 *
//	 * @param user  The user's ID.
//	 * @param name  The name of the variable to set.
//	 * @param value The value to set.
//	 */
//	public boolean setUservar (String user, String name, String value) {
//		if (value == null || value == "<undef>") {
//			this.entityBuilder.getClients().client(user).delete(name);
//		}
//		else {
//			this.entityBuilder.getClients().client(user).set(name, value);
//		}
//
//		return true;
//	}
//
//	/**
//	 * Set -all- user vars for a user. This will replace the internal hash for
//	 * the user. So your hash should at least contain a key/value pair for the
//	 * user's current "topic". This could be useful if you used getUservars to
//	 * store their entire profile somewhere and want to restore it later.
//	 *
//	 * @param user  The user's ID.
//	 * @param data  The full hash of the user's data.
//	 */
//	public boolean setUservars (String user, HashMap<String, String> data) {
//		// TODO: this should be handled more sanely. ;)
//		this.entityBuilder.getClients().client(user).setData(data);
//		return true;
//	}
//
//	/**
//	 * Get a list of all the user IDs the bot knows about.
//	 */
//	public String[] getUsers () {
//		// Get the user list from the clients object.
//		return this.entityBuilder.getClients().listClients();
//	}
//
//	/**
//	 * Retrieve a listing of all the uservars for a user as a HashMap.
//	 * Returns null if the user doesn't exist.
//	 *
//	 * @param user The user ID to get the vars for.
//	 */
//	public HashMap<String, String> getUservars (String user) {
//		if (this.entityBuilder.getClients().clientExists(user)) {
//			return this.entityBuilder.getClients().client(user).getData();
//		}
//		else {
//			return null;
//		}
//	}
//	
}

