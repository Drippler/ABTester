package ab.tester;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;

public class Utils {

	@SuppressLint("NewApi")
	public static void applySP(SharedPreferences.Editor sp){
		if (android.os.Build.VERSION.SDK_INT >= 9)
			sp.apply();
		else
			sp.commit();
	}
	
}
