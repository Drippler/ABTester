package ab.tester;

import android.util.Log;

/**
 * Default implementation of the LoggerInterface used by the ABTester wrapper  
 * using the Log.e(), Log.d() .... function
 */
public class DefualtLogger implements LoggerInterface {

	@Override
	public void e(String tag, String text) {
		Log.e(tag, text);
	}

	@Override
	public void e(String tag, String text, Throwable e) {
		Log.e(tag, text, e);
	}
	
	@Override
	public void d(String tag, String text) {
		Log.d(tag, text);
	}

	@Override
	public void v(String tag, String text) {
		Log.v(tag, text);
	}


}
