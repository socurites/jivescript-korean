import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Test {

	public static void main(String[] args) {
		String message = "아이유 노래 추천";
		String regexp = "((.+?) ((?:틀어주다|들려주다|재생|플레이|고고싱|추천|보여주다|듣고싶다)))";
		Pattern re = Pattern.compile("^" + regexp + "$");
		Matcher m  = re.matcher(message);
		
		if (m.find() == true) {
			System.out.println("true");
		}

	}

}
