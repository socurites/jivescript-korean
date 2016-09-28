package com.socurites.jive.bot;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.socurites.jive.AbstractJiveTestCase;
import com.socurites.jive.core.bot.JiveScriptBot;
import com.socurites.jive.core.bot.builder.JiveScriptBotBuilder;
import com.socurites.jive.core.bot.builder.JiveScriptReplyBuilder;
import com.socurites.jive.core.bot.builder.JiveScriptRequestBuilder;

public class JiveScriptBotTest extends AbstractJiveTestCase {
	private static final boolean enableAnalyze = false;
	
	private JiveScriptBot getBot(String tempateDirPath, boolean enableAnalyze) {
		JiveScriptBot bot = (new JiveScriptBotBuilder())
				.parse(tempateDirPath, null)
				.analyze(enableAnalyze)
				.build()
		;
		
		return bot;
	}
	
	/**
	 * trigger / reply 기본
	 */
	@Test
	public void firstSteps_HelloHuman() {
		String templateDirResourcePath = "script/first_steps";
		String message = "봇 안녕!";
		
		JiveScriptBot bot = getBot(getPathFromResource(templateDirResourcePath), enableAnalyze);
		
		JiveScriptReplyBuilder replyBuilder = bot.reply("localuser", message);
		String replyAsText = replyBuilder.getReplyAsText();
		
		assertEquals("인간, 방가!", replyAsText);
	}
	
	/**
	 * 랜덤 응답
	 */
	@Test
	public void firstSteps_RandomReplies() {
		String templateDirResourcePath = "script/first_steps";
		String message = "봇 오늘 기분은 어때";
		
		JiveScriptBot bot = getBot(getPathFromResource(templateDirResourcePath), enableAnalyze);
		
		JiveScriptReplyBuilder replyBuilder = bot.reply("localuser", message);
		String replyAsText = replyBuilder.getReplyAsText();
		
		List<String> candidateReplies = new ArrayList<String>();
		candidateReplies.add("나는 오늘 기분이 무척 좋아, 넌?");
		candidateReplies.add("나는 기분이 좋아, 넌?");
		candidateReplies.add("좋아 :) 넌?");
		candidateReplies.add("완전 좋아! 넌?");
		candidateReplies.add("그럭저럭");
		
		boolean matched = false;
		for ( String candidateReply : candidateReplies ) {
			if ( candidateReply.equals(replyAsText) ) {
				matched = true;
			}
		}
		
		// candidateReplies중 하나와는 매칭이 되어야 한다.
		assertEquals(true, matched);
	}
	
	/**
	 * 랜덤 응답 > {random} 태그
	 */
	@Test
	public void firstSteps_RandomReplies_random_tag() {
		String templateDirResourcePath = "script/first_steps";
		String message = "내 기분이 어떻게";
		
		JiveScriptBot bot = getBot(getPathFromResource(templateDirResourcePath), enableAnalyze);
		
		JiveScriptReplyBuilder replyBuilder = bot.reply("localuser", message);
		String replyAsText = replyBuilder.getReplyAsText();
		
		List<String> candidateReplies = new ArrayList<String>();
		candidateReplies.add("글쎄, 아마 기분이 좋을 것 같아");
		candidateReplies.add("글쎄, 아마 기분이 별로일 것 같아");
		
		boolean matched = false;
		for ( String candidateReply : candidateReplies ) {
			if ( candidateReply.equals(replyAsText) ) {
				matched = true;
			}
		}
		
		// candidateReplies중 하나와는 매칭이 되어야 한다.
		assertEquals(true, matched);
	}
	
	/**
	 * 랜덤 응답 > {weight=#} 태그
	 */
	@Test
	public void firstSteps_RandomReplies_weight_tag() {
		String templateDirResourcePath = "script/first_steps";
		String message = "안녕";
		
		JiveScriptBot bot = getBot(getPathFromResource(templateDirResourcePath), enableAnalyze);
		
		
		Map<String, Integer> candidateReplies = new HashMap<String, Integer>();
		candidateReplies.put("방가!", 0);
		candidateReplies.put("어서옵쇼", 0);
		candidateReplies.put("할레루야다", 0);
		
		JiveScriptReplyBuilder replyBuilder = null;
		String replyAsText = null;
		
		for ( int i = 0; i < 16; i++ ) {
			replyBuilder = bot.reply("localuser", message);
			replyAsText = replyBuilder.getReplyAsText();
			
			candidateReplies.put(replyAsText, candidateReplies.get(replyAsText) + 1);
		}
		
		assertEquals(10.0, candidateReplies.get("방가!").doubleValue(), 5.0);
		assertEquals(5.0, candidateReplies.get("어서옵쇼").doubleValue(), 5.0);
		assertEquals(1.0, candidateReplies.get("할레루야다").doubleValue(), 5.0);
	}
	
	@Test
	public void firstSteps_priority_trigger() {
		String templateDirResourcePath = "script/first_steps";
		String message = "about perl script";
		
		JiveScriptBot bot = getBot(getPathFromResource(templateDirResourcePath), enableAnalyze);
		JiveScriptReplyBuilder replyBuilder = bot.reply("localuser", message);
		String replyAsText = replyBuilder.getReplyAsText();
		assertEquals("about perl script", replyAsText);
	}
	
	/**
	 * multi line reply
	 */
	@Test
	public void firstSteps_LineBreaking() {
		String templateDirResourcePath = "script/first_steps";
		String message = "멋진 노래 가사 좀 알려줄래";
		
		JiveScriptBot bot = getBot(getPathFromResource(templateDirResourcePath), enableAnalyze);
		JiveScriptReplyBuilder replyBuilder = bot.reply("localuser", message);
		String replyAsText = replyBuilder.getReplyAsText();
		System.out.println(replyAsText);
		
		message = "멋진 노래 가사 좀 알려줘";
		replyBuilder = bot.reply("localuser", message);
		replyAsText = replyBuilder.getReplyAsText();
		System.out.println(replyAsText);
	}
	
	/**
	 * multi line reply
	 * local parser option > concat > newline, space
	 */
	@Test
	public void localParserOption_concat_newline() {
		String templateDirResourcePath = "script/local_parser_option/concat";
		String message = "멋진 노래 가사 좀 알려줄래 newline";
		
		JiveScriptBot bot = getBot(getPathFromResource(templateDirResourcePath), enableAnalyze);
		JiveScriptReplyBuilder replyBuilder = bot.reply("localuser", message);
		String replyAsText = replyBuilder.getReplyAsText();
		System.out.println(replyAsText);
		
		message = "멋진 노래 가사 좀 알려줄래 space";
		
		replyBuilder = bot.reply("localuser", message);
		replyAsText = replyBuilder.getReplyAsText();
		System.out.println(replyAsText);
	}
	
	@Test
	public void beginFile_var() {
		String templateDirResourcePath = "script/begin_file";
		String message = "너 이름이 뭐니";
		
		JiveScriptBot bot = getBot(getPathFromResource(templateDirResourcePath), enableAnalyze);
		JiveScriptReplyBuilder replyBuilder = bot.reply("localuser", message);
		String replyAsText = replyBuilder.getReplyAsText();
		assertEquals("마이봇이라고 불러줘", replyAsText);
		
		message = "너 몇살이야";
		replyBuilder = bot.reply("localuser", message);
		replyAsText = replyBuilder.getReplyAsText();
		assertEquals("난 36살이야", replyAsText);
	}
	
	@Test
	public void beginFile_substitution() {
		String templateDirResourcePath = "script/begin_file";
		String message = "난 엑소가 좋아 넌";
		
		JiveScriptBot bot = getBot(getPathFromResource(templateDirResourcePath), enableAnalyze);
		JiveScriptReplyBuilder replyBuilder = bot.reply("localuser", message);
		String replyAsText = replyBuilder.getReplyAsText();
		assertEquals("난 bigbang이 좋아", replyAsText);
		
		// substitution은 reply에는 적용되지 않는다
		assertNotEquals("난 빅뱅이 좋아", replyAsText);
	}
	
	@Test
	public void trigger_wildcard() {
		String templateDirResourcePath = "script/trigger/wildcard";
		String star1 = "효리";
		String message = "내 이름은 " + star1 + "야";
		
		JiveScriptBot bot = getBot(getPathFromResource(templateDirResourcePath), enableAnalyze);
		JiveScriptReplyBuilder replyBuilder = bot.reply("localuser", message);
		String replyAsText = replyBuilder.getReplyAsText();
		assertEquals(star1 + ", 만나서 반가워", replyAsText);
		
		star1 = "축구";
		String star2 = "야구";
		message = "내 취미는 " + star1 + " " + star2 + "야";
		replyBuilder = bot.reply("localuser", message);
		replyAsText = replyBuilder.getReplyAsText();
		assertEquals("네 취미가 " + star1 + ", " + star2 + "라고?", replyAsText);
		
		String star = "야구";
		message = "내 취미는 " + star + "야";
		replyBuilder = bot.reply("localuser", message);
		replyAsText = replyBuilder.getReplyAsText();
		assertEquals("네 취미가 " + star + "라고?", replyAsText);
	}
	
	@Test
	public void trigger_wildcard_catch_all() {
		String templateDirResourcePath = "script/trigger/wildcard";
		String star1 = "효리";
		String message = "내 이름은 " + star1 + "야";
		
		JiveScriptBot bot = getBot(getPathFromResource(templateDirResourcePath), enableAnalyze);
		JiveScriptReplyBuilder replyBuilder = bot.reply("localuser", message);
		String replyAsText = replyBuilder.getReplyAsText();
		assertEquals(star1 + ", 만나서 반가워", replyAsText);
		
		message = "내 특기는 글쓰기야";
		replyBuilder = bot.reply("localuser", message);
		replyAsText = replyBuilder.getReplyAsText();
		assertEquals("무슨 말인지 모르겠어", replyAsText);
	}
	
	@Test
	public void trigger_wildcard_special() {
		String templateDirResourcePath = "script/trigger/wildcard";
		String star = "19";
		String message = "나는 " + star + "살이야";
		
		JiveScriptBot bot = getBot(getPathFromResource(templateDirResourcePath), enableAnalyze);
		JiveScriptReplyBuilder replyBuilder = bot.reply("localuser", message);
		String replyAsText = replyBuilder.getReplyAsText();
		assertEquals(star + "라면 인생을 즐기기 좋은 나이지", replyAsText);
		
		star = "열아홉";
		message = "나는 " + star + "살이야";
		
		bot = getBot(getPathFromResource(templateDirResourcePath), enableAnalyze);
		replyBuilder = bot.reply("localuser", message);
		replyAsText = replyBuilder.getReplyAsText();
		assertEquals(star + "라면 인생을 즐기기 좋은 나이지", replyAsText);
	}
	
	@Test
	public void trigger_wildcard_array_alternative() {
		String templateDirResourcePath = "script/trigger/array";
		JiveScriptBot bot = getBot(getPathFromResource(templateDirResourcePath), enableAnalyze);
		
		List<String> alternatives = new ArrayList<String>();
		alternatives.add("전화번호");
		alternatives.add("휴대폰번호");
		alternatives.add("사무실 전화번호");
		String message = null;
		JiveScriptReplyBuilder replyBuilder = null;
		String replyAsText = null;
		for ( String alternative : alternatives ) {
			message = alternative + "가 뭐야";
			replyBuilder = bot.reply("localuser", message);
			replyAsText = replyBuilder.getReplyAsText();
			assertEquals("여기로 전화해 줄래? 010-1111-2345", replyAsText);
		}
		
		alternatives = new ArrayList<String>();
		alternatives.add("정말");
		alternatives.add("매우");
		alternatives.add("몹시");
		for ( String alternative : alternatives ) {
			message = "나 지금 " + alternative + " 피곤해";
			replyBuilder = bot.reply("localuser", message);
			replyAsText = replyBuilder.getReplyAsText();
			assertEquals("네가 " + alternative + " 피곤하다니 정말 안됬다", replyAsText);
		}
		
		alternatives = new ArrayList<String>();
		alternatives.add("좋아");
		alternatives.add("싫어");
		String star = "오이";
		for ( String alternative : alternatives ) {
			message = "나는 " + star + " 너무 " + alternative;
			replyBuilder = bot.reply("localuser", message);
			replyAsText = replyBuilder.getReplyAsText();
			assertEquals("놀라운 걸! 나도 " + star + " 너무 " + alternative, replyAsText);
		}
	}
		
	@Test
	public void trigger_wildcard_array_optional() {
		String templateDirResourcePath = "script/trigger/array";
		JiveScriptBot bot = getBot(getPathFromResource(templateDirResourcePath), enableAnalyze);

		String message = "너 지금 기분이 어때";
		
		JiveScriptReplyBuilder replyBuilder = bot.reply("localuser", message);
		String replyAsText = replyBuilder.getReplyAsText();
		assertEquals("완전 좋은 걸", replyAsText);
		
		message = "너 지금 어때";
		
		replyBuilder = bot.reply("localuser", message);
		replyAsText = replyBuilder.getReplyAsText();
		assertEquals("완전 좋은 걸", replyAsText);
		
		message = "배고파";
		
		replyBuilder = bot.reply("localuser", message);
		replyAsText = replyBuilder.getReplyAsText();
		assertEquals("나도 슬슬 허기가 지는 걸", replyAsText);
		
		message = "나 완전 배고파";
		
		replyBuilder = bot.reply("localuser", message);
		replyAsText = replyBuilder.getReplyAsText();
		assertEquals("나도 슬슬 허기가 지는 걸", replyAsText);
		
		message = "나 완전 배고파 뒤지겠다";
		
		replyBuilder = bot.reply("localuser", message);
		replyAsText = replyBuilder.getReplyAsText();
		assertEquals("나도 슬슬 허기가 지는 걸", replyAsText);
	}
	
	@Test
	public void trigger_wildcard_array_definition_alternative() {
		String templateDirResourcePath = "script/trigger/array_definition";
		JiveScriptBot bot = getBot(getPathFromResource(templateDirResourcePath), enableAnalyze);
		
		List<String> alternatives = new ArrayList<String>();
		alternatives.add("전화번호");
		alternatives.add("휴대폰번호");
		alternatives.add("사무실 전화번호");
		String message = null;
		JiveScriptReplyBuilder replyBuilder = null;
		String replyAsText = null;
		for ( String alternative : alternatives ) {
			message = alternative + "가 뭐야";
			replyBuilder = bot.reply("localuser", message);
			replyAsText = replyBuilder.getReplyAsText();
			assertEquals("여기로 전화해 줄래? 010-1111-2345", replyAsText);
		}
		
		alternatives = new ArrayList<String>();
		alternatives.add("정말");
		alternatives.add("매우");
		alternatives.add("몹시");
		for ( String alternative : alternatives ) {
			message = "나 지금 " + alternative + " 피곤해";
			replyBuilder = bot.reply("localuser", message);
			replyAsText = replyBuilder.getReplyAsText();
			assertEquals("네가 " + alternative + " 피곤하다니 정말 안됬다", replyAsText);
		}
		
		alternatives = new ArrayList<String>();
		alternatives.add("좋아");
		alternatives.add("싫어");
		String star = "오이";
		for ( String alternative : alternatives ) {
			message = "나는 " + star + " 너무 " + alternative;
			replyBuilder = bot.reply("localuser", message);
			replyAsText = replyBuilder.getReplyAsText();
			assertEquals("놀라운 걸! 나도 " + star + " 너무 " + alternative, replyAsText);
		}
	}
	
	@Test
	public void trigger_wildcard_array_definition_optional() {
		String templateDirResourcePath = "script/trigger/array_definition";
		JiveScriptBot bot = getBot(getPathFromResource(templateDirResourcePath), enableAnalyze);

		String message = "너 지금 기분이 어때";
		
		JiveScriptReplyBuilder replyBuilder = bot.reply("localuser", message);
		String replyAsText = replyBuilder.getReplyAsText();
		assertEquals("완전 좋은 걸", replyAsText);
		
		message = "너 지금 어때";
		
		replyBuilder = bot.reply("localuser", message);
		replyAsText = replyBuilder.getReplyAsText();
		assertEquals("완전 좋은 걸", replyAsText);
		
		message = "너 지금 느낌이 어때";
		
		replyBuilder = bot.reply("localuser", message);
		replyAsText = replyBuilder.getReplyAsText();
		assertEquals("완전 좋은 걸", replyAsText);
	}
	
	@Test
	public void trigger_priority_tirgger() {
		String templateDirResourcePath = "script/trigger/priority_trigger";
		String message = "최신곡 검색";
		
		JiveScriptBot bot = getBot(getPathFromResource(templateDirResourcePath), enableAnalyze);
		
		
		Map<String, Integer> candidateReplies = new HashMap<String, Integer>();
		candidateReplies.put("최신곡 검색해 달라고?", 0);
		candidateReplies.put("최신곡 들려줄게", 0);
		
		JiveScriptReplyBuilder replyBuilder = null;
		String replyAsText = null;
		
		for ( int i = 0; i < 16; i++ ) {
			replyBuilder = bot.reply("localuser", message);
			replyAsText = replyBuilder.getReplyAsText();
			
			candidateReplies.put(replyAsText, candidateReplies.get(replyAsText) + 1);
		}
		
		assertEquals(20.0, candidateReplies.get("최신곡 검색해 달라고?").doubleValue(), 5.0);
		assertEquals(5.0, candidateReplies.get("최신곡 들려줄게").doubleValue(), 5.0);
	}
	
	@Test
	public void commands_redirection_to_trigger() {
		String templateDirResourcePath = "script/commands/redirection";
		JiveScriptBot bot = getBot(getPathFromResource(templateDirResourcePath), enableAnalyze);

		String message = "안녕";
		
		JiveScriptReplyBuilder replyBuilder = bot.reply("localuser", message);
		String replyAsText = replyBuilder.getReplyAsText();
		assertEquals("반가워", replyAsText);
		
		message = "안녕하세요";
		
		replyBuilder = bot.reply("localuser", message);
		replyAsText = replyBuilder.getReplyAsText();
		assertEquals("반가워", replyAsText);
		
		String star2 = "프로도"; 
		message = "안녕 나는 " + star2 +"야";
		
		replyBuilder = bot.reply("localuser", message);
		replyAsText = replyBuilder.getReplyAsText();
		assertEquals(star2 + ", 반가워", replyAsText);
	}
	
	@Test
	public void commands_commands_previous() {
		String templateDirResourcePath = "script/commands/previous";
		JiveScriptBot bot = getBot(getPathFromResource(templateDirResourcePath), enableAnalyze);
		
		String message = "안녕";
		
		JiveScriptReplyBuilder replyBuilder = bot.reply("localuser", message);
		String replyAsText = replyBuilder.getReplyAsText();
		assertEquals("누구야", replyAsText);
		
		message = "나는 철수야";
		
		replyBuilder = bot.reply("localuser", message);
		replyAsText = replyBuilder.getReplyAsText();
		assertEquals("만나서 반가워", replyAsText);
		
		message = "나도 반가워";
		
		replyBuilder = bot.reply("localuser", message);
		replyAsText = replyBuilder.getReplyAsText();
		assertEquals("오늘 기분이 어때", replyAsText);
	}
	
	@Test
	public void commands_commands_get_set_var() {
		String templateDirResourcePath = "script/commands/variable";
		JiveScriptBot bot = getBot(getPathFromResource(templateDirResourcePath), enableAnalyze);
		
		String star = "수호";
		String message = "내 이름은 " + star + "야";
		
		JiveScriptReplyBuilder replyBuilder = bot.reply("localuser", message);
		String replyAsText = replyBuilder.getReplyAsText();
		assertEquals("만나서 반가워, " + star, replyAsText);
		
		message = "내 이름이 뭐라고";
		
		replyBuilder = bot.reply("localuser", message);
		replyAsText = replyBuilder.getReplyAsText();
		assertEquals(star + "라며! 바보같으니라구.", replyAsText);
	}
	
	@Test
	public void commands_commands_get_set_var_formal_tag() {
		String templateDirResourcePath = "script/commands/variable";
		JiveScriptBot bot = getBot(getPathFromResource(templateDirResourcePath), enableAnalyze);
		JiveScriptRequestBuilder requestBuilder = (new JiveScriptRequestBuilder())
				.entityBuilder(bot.getEntityBuilder())
				.analyze(false)
				;
		
		String star = "Richard";
		String message = "내 이름은 " + star + "야";
		
		JiveScriptReplyBuilder replyBuilder = bot.reply("localuser", message);
		String replyAsText = replyBuilder.getReplyAsText();
		
		
		assertEquals("만나서 반가워, " + requestBuilder.build(star), replyAsText);
		
		star = "Richard";
		message = "나는 " + star + "야";
		
		replyBuilder = bot.reply("localuser", message);
		replyAsText = replyBuilder.getReplyAsText();
		
		assertEquals("만나서 반가워, " + star, replyAsText);
	}
	
	@Test
	public void commands_commands_conditional_string() {
		String templateDirResourcePath = "script/commands/conditional";
		JiveScriptBot bot = getBot(getPathFromResource(templateDirResourcePath), enableAnalyze);
		
		String message = "내 이름이 뭐라고";
		
		JiveScriptReplyBuilder replyBuilder = bot.reply("localuser", message);
		String replyAsText = replyBuilder.getReplyAsText();
		assertEquals("알려준적 없잖아", replyAsText);
		
		String star="Exo";
		message = "나는 " + star + "야";
		
		replyBuilder = bot.reply("localuser", message);
		replyAsText = replyBuilder.getReplyAsText();
		assertEquals("만나서 반가워", replyAsText);
		
		message = "내가 " + star + "라고 말했었나";
		
		replyBuilder = bot.reply("localuser", message);
		replyAsText = replyBuilder.getReplyAsText();
		assertEquals("어, 500년 전에, " + star + "라고 말했어", replyAsText);
	}
	
	@Test
	public void commands_commands_conditional_number() {
		String templateDirResourcePath = "script/commands/conditional";
		JiveScriptBot bot = getBot(getPathFromResource(templateDirResourcePath), enableAnalyze);
		
		int age = 17;
		String message = "나는 " + age + "살이야";
		
		JiveScriptReplyBuilder replyBuilder = bot.reply("localuser", message);
		String replyAsText = replyBuilder.getReplyAsText();
		assertEquals("공부할 나이구나", replyAsText);
		
		age = 27;
		message = "나는 " + age + "살이야";
		
		replyBuilder = bot.reply("localuser", message);
		replyAsText = replyBuilder.getReplyAsText();
		assertEquals("왕창 놀아라", replyAsText);
		
		age = 47;
		message = "나는 " + age + "살이야";
		
		replyBuilder = bot.reply("localuser", message);
		replyAsText = replyBuilder.getReplyAsText();
		assertEquals("불혹이야 ㅜㅜ", replyAsText);
		
		age = 57;
		message = "나는 " + age + "살이야";
		
		replyBuilder = bot.reply("localuser", message);
		replyAsText = replyBuilder.getReplyAsText();
		assertEquals("행복을 추구할 나이야", replyAsText);
	}
	
	@Test
	public void block_topic() {
		String templateDirResourcePath = "script/block/topic";
		JiveScriptBot bot = getBot(getPathFromResource(templateDirResourcePath), enableAnalyze);
		
		String message = "너 왜이리 개떡 같냐";
		
		JiveScriptReplyBuilder replyBuilder = bot.reply("localuser", message);
		String replyAsText = replyBuilder.getReplyAsText();
		assertEquals("무례하군요. 사과하지 않으시면, 절교에요.", replyAsText);
		
		
		
		List<String> candidateReplies = new ArrayList<String>();
		candidateReplies.add("용서를 빌지 않으면, 절대 안돼.");
		candidateReplies.add("미안하다고 말하라고!");
		candidateReplies.add("사과해!");
		
		message = "개똥이다";
		replyBuilder = bot.reply("localuser", message);
		replyAsText = replyBuilder.getReplyAsText();
		
		boolean matched = false;
		for ( String candidateReply : candidateReplies ) {
			if ( candidateReply.equals(replyAsText) ) {
				matched = true;
			}
		}
		
		System.out.println(replyAsText);
		// candidateReplies중 하나와는 매칭이 되어야 한다.
		assertEquals(true, matched);
		
		message = "정말로 미안해";
		replyBuilder = bot.reply("localuser", message);
		replyAsText = replyBuilder.getReplyAsText();
		assertEquals("뭐 그럴수도 있지, 이번만이다!", replyAsText);
	}
	
	@Test
	public void block_begin_basic() {
		String templateDirResourcePath = "script/block/begin_basic";
		String message = "봇 안녕!";
		
		JiveScriptBot bot = getBot(getPathFromResource(templateDirResourcePath), enableAnalyze);
		
		JiveScriptReplyBuilder replyBuilder = bot.reply("localuser", message);
		String replyAsText = replyBuilder.getReplyAsText();
		
		assertEquals("인간, 방가!", replyAsText);
	}

	/**
	 * It's not supported yet.
	 * need debugging
	 */
	@Test
	public void block_begin_basic_check() {
//		String templateDirResourcePath = "script/block/begin_basic_check";
//		JiveScriptBot bot = getBot(getPathFromResource(templateDirResourcePath), enableAnalyze);
//		
//		String message = "hi bot";
//		JiveScriptReplyBuilder replyBuilder = bot.reply("localuser", message);
//		String replyAsText = replyBuilder.getReplyAsText();
//		System.out.println(replyAsText);
//		
//		message = "123";
//		replyBuilder = bot.reply("localuser", message);
//		replyAsText = replyBuilder.getReplyAsText();
//		System.out.println(replyAsText);
		
		/*
		String message = "봇 안녕!";
		JiveScriptReplyBuilder replyBuilder = bot.reply("localuser", message);
		String replyAsText = replyBuilder.getReplyAsText();
		
		assertEquals("안녕 난 봇이야. 넌 이름이 뭐니", replyAsText);
		
		message = "난 철수야";
		replyBuilder = bot.reply("localuser", message);
		replyAsText = replyBuilder.getReplyAsText();
		
		assertEquals("무슨 말인지 모르겠어. 이름만 말해 줄래?", replyAsText);
		*/
	}
}
