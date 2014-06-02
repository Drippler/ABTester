package ab.tester.prefs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

public abstract class ABSharedPrefs {

	public static final String SEPARATOR = "___";
	protected SharedPreferences prefs;
	protected abstract String getPrefsName();
	
	public ABSharedPrefs(Context context) {
		this.prefs = context.getSharedPreferences(getPrefsName(), Context.MODE_PRIVATE);
	}
	
	@SuppressLint("NewApi")
	public void apply(SharedPreferences.Editor editor){
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.GINGERBREAD)
			editor.apply();
		else
			editor.commit();
	}
}
