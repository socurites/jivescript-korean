package com.socurites.jive.core.core.analyze;

import java.util.List;

import com.socurites.jive.core.analyze.entity.JiveTokenModel;

public abstract class JiveScriptKoreanAnalyzer {
	public abstract JiveTokenModel analyze(String text);
	
	public abstract void addKewords(List<String> keywords);
}
