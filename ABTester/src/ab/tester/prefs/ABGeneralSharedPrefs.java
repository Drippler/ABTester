package ab.tester.prefs;

import java.util.Random;

import android.content.Context;
import android.content.SharedPreferences;

public class ABGeneralSharedPrefs extends ABSharedPrefs {

	public static final String PERCENTILE = "percentile";
	
	public ABGeneralSharedPrefs(Context context) {
		super(context);
	}

	@Override
	protected String getPrefsName() {
		return "ABTester_general";
	}

	public int getPercentile() {
		int random;
		if (prefs.contains(PERCENTILE)) // use old random number
			random = prefs.getInt(PERCENTILE, 0);
		else {
			random = new Random().nextInt(100);
			SharedPreferences.Editor editor = prefs.edit();
			editor.putInt(PERCENTILE, random); // first time, save it
			apply(editor);
		}
		return random;
	}

}
