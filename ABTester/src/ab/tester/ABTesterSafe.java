package ab.tester;

import java.util.concurrent.TimeoutException;

import android.content.Context;

/**
 * Warps the ABTester class, each call made in a try catch, if a throwable
 * is thrown, it will be logged to the logger if not null
 */
public class ABTesterSafe {

	private static final String TAG = "ABTester";
	private static LoggerInterface logger;
	
	/**
	 * Must be called before usage of any other methods
	 * @param ctx 
	 * @param publicKey - provided by amazon
	 * @param privateKey - provided by amazon
	 * @param l - implementation of the loggerInterface, will be used to log things
	 */
	public static void init(Context ctx, String publicKey, String privateKey, LoggerInterface l){
		try {
			logger = l;
			ABTester.init(ctx, publicKey, privateKey, l);
		} catch (Throwable t){
			if (logger != null)
				logger.e(TAG, "ABTester - crashed", t);
		}
	}
	
	/**
	 * Must be called before usage of any other methods
	 * @param ctx 
	 * @param publicKey - provided by amazon
	 * @param privateKey - provided by amazon
	 */
	public static void init(Context ctx, String publicKey, String privateKey){
		try {
			ABTester.init(ctx, publicKey, privateKey, false);
		} catch (Throwable t){
			if (logger != null)
				logger.e(TAG, "ABTester - crashed", t);
		}
	}

	/**
	 * Must be called before usage of any other methods
	 * @param ctx 
	 * @param publicKey - provided by amazon
	 * @param privateKey - provided by amazon
	 * @param silence - whether this library will log out events to the LogCat or not
	 */
	public static void init(Context ctx, String publicKey, String privateKey, boolean silence) {
		try {
			ABTester.init(ctx, publicKey, privateKey, silence );
		} catch (Throwable t){
			if (logger != null)
				logger.e(TAG, "ABTester - crashed", t);
		}
	}
	
	/**
	 * Must be called before fetching
	 * @param name - the name of the dimension
	 * @param value - the dimension value as a string
	 */
	public static void addDimensionAsString(String name, String value){
		try {
			ABTester.addDimensionAsString(name, value);
		} catch (Throwable t){
			if (logger != null)
				logger.e(TAG, "ABTester - crashed", t);
		}
	}
	
	/**
	 * Must be called before fetching
	 * @param name - the name of the dimension
	 * @param value - the dimension value as a Number (float,int,double...)
	 */
	public static void addDimensionAsNumber(String name, Number value){
		try {
			ABTester.addDimensionAsNumber(name, value);
		} catch (Throwable t){
			if (logger != null)
				logger.e(TAG, "ABTester - crashed", t);
		}
	}
	
	/**
	 * Stores the event locally, will be send by calling submitEvents()
	 * @param testName - checks if we part of the experiment
	 * @param eventName
	 * @param onlyOnce - TODO ?
	 */
	public static void recordEvent(String testName, String eventName, boolean onlyOnce){
		try {
			ABTester.recordEvent(testName, eventName, onlyOnce);
		} catch (Throwable t){
			if (logger != null)
				logger.e(TAG, "ABTester - crashed", t);
		}
	}
	
	/**
	 * Submit the events that were previously stored locally.
	 * call it in the onPause() method of an activity
	 */
	public static void submitEvents(){
		try {
			ABTester.submitEvents();
		} catch (Throwable t){
			if (logger != null)
				logger.e(TAG, "ABTester - crashed", t);
		}
	}
	
	/**
	 * ASync preFetching of the experiments. running in a background thread.
	 * @param tests
	 */
	public static void preFetch(final ABTest... tests){
		// TODO are we aware that tests might be null ?
		try {
			ABTester.preFetch(tests);
		} catch (Throwable t){
			if (logger != null)
				logger.e(TAG, "ABTester - crashed", t);
		}
	}
		
	/**
	 * Fetching to local storage the AB tests, this function will block you thread, for not longer than the
	 * Specified time out. Make sure to set the user dimension before calling this.
	 * After the timeout, the fetching will be stopped
	 * @param msTimeout - timeout in milliseconds
	 * @param tests.. - ABTest which you want to fetch
	 * @throws TimeoutException - will be thrown in case the method times out
	 */
	public static void syncPreFetch(long msTimeout, final ABTest... tests) throws TimeoutException {
		// TODO are we aware that tests might be null ?
		try {
			ABTester.syncPreFetch(msTimeout, tests);
		} catch (TimeoutException to){
			throw to; // pass timeout
		} catch (Throwable t){
			if (logger != null)
				logger.e(TAG, "ABTester - crashed", t);
		}
	}
		
		
	/**
	 * Will return true in case the test 
	 * @param testName the name of the test will be checked
	 * @return true if the data is fetched and ready
	 */
	public static boolean isTestReady(String testName){
		try {
			return ABTester.isTestReady(testName);
		} catch (Throwable t){
			if (logger != null)
				logger.e(TAG, "ABTester - crashed", t);
			return false;
		}
	}
	
	/**
	 * Return the String for the requested variable in the specified test
	 * @param testName - the name of the test (project name)
	 * @param variable - the name of the variable you want to receive
	 * @param defaultValue - in the user should not be part of the test, this will return this value
	 * @return the value of the variable as string
	 */
	public static String getString(String testName, String variable, String defaultValue) {
		try {
			return ABTester.getString(testName, variable, defaultValue);
		} catch (Throwable t) {
			if (logger != null)
				logger.e(TAG, "ABTester - crashed", t);
			return defaultValue;
		}
	}
	
	/**
	 * Same as getString with casting, will throw casting exception
	 * @param testName - the name of the test (project name)
	 * @param variable - the name of the variable you want to receive
	 * @param defaultValue - in the user should not be part of the test, this will return this value
	 * @return the value of the variable as Boolean
	 */
	public static boolean getBoolean(String testName, String variable, boolean defaultValue) {
		try {
			return ABTester.getBoolean(testName, variable, defaultValue);
		} catch (Throwable t){
			if (logger != null)
				logger.e(TAG, "ABTester - crashed", t);
			return defaultValue;
		}
	}
	
	/**
	 * Same as getString with casting, will throw casting exception
	 * @param testName - the name of the test (project name)
	 * @param variable - the name of the  variable you want to receive
	 * @param defaultValue - in the user should not be part of the test, this will return this value
	 * @return the value of the variable as Long
	 */
	public static long getLong(String testName, String variable, long defaultValue) {
		try {
			return ABTester.getLong(testName, variable, defaultValue);
		} catch (Throwable t){
			if (logger != null)
				logger.e(TAG, "ABTester - crashed", t);
			return defaultValue;
		}
	}
	
	/**
	 * Same as getString with casting, will throw casting exception
	 * @param testName - the name of the test (project name)
	 * @param variable - the name of the variable you want to receive
	 * @param defaultValue - in the user should not be part of the test, this will return this value
	 * @return the value of the variable as Float
	 */
	public static float getFloat(String testName, String variable, float defaultValue) {
		try {
			return ABTester.getFloat(testName, variable, defaultValue);
		} catch (Throwable t){
			if (logger != null)
				logger.e(TAG, "ABTester - crashed", t);
			return defaultValue;
		}
	}
}
