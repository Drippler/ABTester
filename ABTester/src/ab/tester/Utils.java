package ab.tester;

import android.content.SharedPreferences;

public class Utils {

	public static void applySP(SharedPreferences.Editor sp){
		if ( android.os.Build.VERSION.SDK_INT >= 9 )
			sp.apply();
		else
			sp.commit();
	}
	
}
