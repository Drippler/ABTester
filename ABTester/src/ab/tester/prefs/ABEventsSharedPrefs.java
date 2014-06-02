package ab.tester.prefs;

import android.content.Context;
import android.content.SharedPreferences;

public class ABEventsSharedPrefs extends ABSharedPrefs {

	private static final String EVENT_SENT_KEY = "EVENT_SENT___";
	
	public ABEventsSharedPrefs(Context context) {
		super(context);
	}

	@Override
	protected String getPrefsName() {
		return "ABTester_experiments_values_events";
	}

	public boolean wasEventReported(String eventName) {
		return prefs.getBoolean(EVENT_SENT_KEY + eventName, false);
	}
	
	public void reportEvent(String eventName) {
		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean(EVENT_SENT_KEY + eventName, true);
		apply(editor);
	}

}
