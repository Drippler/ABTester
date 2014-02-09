package ab.tester;

import java.util.Random;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.util.Log;

import com.amazon.insights.AmazonInsights;
import com.amazon.insights.EventClient;
import com.amazon.insights.InsightsCallback;
import com.amazon.insights.InsightsCredentials;
import com.amazon.insights.InsightsOptions;
import com.amazon.insights.Variation;
import com.amazon.insights.VariationSet;
import com.amazon.insights.error.InsightsError;

public class ABTester {

	// used for running multiple segments
	private static final String RANDOM_INT = "RANDOM_INT";
	
	private static final String TAG = "ABTester";
	private static final String EVENT_SENT_KEY = "EVENT_SENT___";
	private static final String EVENT_COUNTER_KEY = "EVENT_COUNTER___";
	private static final String FIRST_TIME_READY_KEY = "FIRST_TIME_READY_KEY____";
	private static final String SP_FILE_NAME_TESTS = "ABTester_experiments_values_tests";
	private static final String SP_FILE_NAME_EVENTS = "ABTester_experiments_values_events";
	private static final int ASYNC_TIMEOUT = 60000;
	
	private static ABTester instance = null;
	
	private static ABTester get(){
		if(instance != null){
			return instance;
		} else {
			initWasNotCalled();
			return null;
		}
	}

	private static void initWasNotCalled() {
		for (int i = 0; i < 30; i++) // make sure the user notice this error message
			Log.e(TAG, "You must call ABTester.init(); before using the wrapper");
	}
	
	/**
	 * Must be called before usage of any other methods
	 * @param ctx 
	 * @param publicKey - provided by amazon
	 * @param privateKey - provided by amazon
	 * @param logger - implementation of the loggerInterface, will be used to log things
	 */
	public static void init(Context ctx,String publicKey,String privateKey,LoggerInterface logger){
		instance = new ABTester(ctx.getApplicationContext(),publicKey,privateKey,logger);
		// used for for running multiple sagments
		addDimensionAsNumber(RANDOM_INT, new Random().nextInt(100));
	}

	
	private AmazonInsights insightsInstance;
	private Context context;
	private LoggerInterface logger;
	
	private ABTester(Context ctx,String publicKey, String privateKey, LoggerInterface logger) {
		this.context = ctx;
		// Create a credentials object using the values from the
		// Amazon Mobile App Distribution Portal A/B Testing site.
		InsightsCredentials credentials = AmazonInsights.newCredentials(publicKey, privateKey);
		 
		// Create an options object with event collection enabled and WAN delivery enabled
		InsightsOptions options = AmazonInsights.newOptions(true, true);
		 
		 
		// Initialize a new instance of AmazonInsights specifically for your application.
		// The AmazonInsights library requires the Android context in order
		// to access Android services (i.e. SharedPrefs, etc)
		insightsInstance = AmazonInsights.newInstance(credentials, ctx, options);
		
		this.logger = logger;
	}
	
	/**
	 * Must be called before fetching
	 * @param name - the name of the dimension
	 * @param value - the dimension value as a string
	 */
	public static void addDimensionAsString(String name,String value){
		get().insightsInstance.getUserProfile().addDimensionAsString(name, value);
		if(get().logger != null) get().logger.v(TAG, "adding dimmension:" + name + "value:" + value);
	}
	
	/**
	 * Must be called before fetching
	 * @param name - the name of the dimension
	 * @param value - the dimension value as a Number (float,int,double...)
	 */
	public static void addDimensionAsNumber(String name,Number value){
		get().insightsInstance.getUserProfile().addDimensionAsNumber(name, value);
		if(get().logger != null) get().logger.v(TAG, "adding dimmension:" + name + "value:" + value);
	}
	
	/**
	 * Stores the event locally, will be sent by calling submitEvents()
	 * @param testName - checks if the user is part of the experiment
	 * @param eventName
	 */
	public static void recordEvent(String testName, String eventName,boolean onlyOnce) {
		if ( wasReadyOnFirstRequest(testName) ){
			SharedPreferences sp = get().getSharedPreferencesForEvents();
			boolean wasReported = sp.getBoolean(EVENT_SENT_KEY + eventName, false);
			if( onlyOnce == true || (onlyOnce == false && wasReported == false) ){
				EventClient eventClient = get().insightsInstance.getEventClient();
			    eventClient.recordEvent(eventClient.createEvent(eventName));
			    sp.edit().putBoolean(EVENT_SENT_KEY + eventName, true);
			    if(get().logger != null) get().logger.v(TAG, "Event sent: " + eventName);
			} else {
				if(get().logger != null) get().logger.v(TAG, "Event FILTERED: " + eventName);
			}
		} else {
			if(get().logger != null) get().logger.v(TAG, "Event FILTERED not part of the test: " + eventName);
		}
	}
	
	/**
	 * Submit the events that were previously stored locally. asynchronously 
	 * call it in the onPause() method of an activity
	 */
	public static void submitEvents() {
		EventClient eventClient = get().insightsInstance.getEventClient();
		eventClient.submitEvents();
	}

	
	/**
	 * ASync preFetching of the experiments. running in a background thread.
	 * @param tests
	 */
	public static void preFetch(final ABTest... tests){
		if(get().logger != null) get().logger.v(TAG, "prefetching async...");
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					syncPreFetch(ASYNC_TIMEOUT, tests);
				} catch (Exception e){
				}
			}
		}, "ABTest-prefetching-thread").start();
	}
	
	/**
	 * Fetching to local storage the AB tests, this function will block you thread, for not longer than the
	 * Specified time out. Make sure to set the user dimension before calling this.
	 * After the timeout, the fetching will be stopped
	 * @param msTimeout - timeout in millis
	 * @param tests.. - ABTest which you want to fetch
	 * @throws TimeoutException - will be thrown in case the method times out
	 */
	public static void syncPreFetch(long msTimeout, final ABTest... tests) throws TimeoutException {
		String[] testsToFetch = new String[tests.length]; 
		for (int i = 0; i < tests.length; i++)
			testsToFetch[i] = tests[i].testName;

		long begin = SystemClock.uptimeMillis();
		
		final AtomicBoolean isReady = new AtomicBoolean(false); 
		final AtomicBoolean isTimedout = new AtomicBoolean(false);
		final Thread waitingThread = Thread.currentThread();
		
		if(get().logger != null) get().logger.v(TAG, "fetching...");
		get().insightsInstance.getABTestClient().getVariations(testsToFetch).setCallback(new InsightsCallback<VariationSet>() {
			@Override
			public void onComplete(VariationSet vars) {
				synchronized (isReady) {
					if(isTimedout.get() == false){
						for (int i = 0; i < tests.length; i++){
							Variation var = vars.getVariation(tests[i].testName);
							if(get().logger != null) get().logger.v(TAG, "fetched: " + var.getProjectName() );
							for (int j = 0; j < tests[i].variables.length ; j++){
								tests[i].variables[j].value = var.getVariableAsString(tests[i].variables[j].name, null);
							}
							tests[i].saveTo(get().getSharedPreferencesForTests());
						}
						isReady.set(true);
						waitingThread.interrupt();
					}
				}
			}
			
			@Override
			public void onError(InsightsError error) {
				super.onError(error);
				if(get().logger != null) get().logger.v(TAG, "error: " + error.getMessage() );
			}
			
		});

		// wait until ready or timed out
		while( isReady.get() == false ) {
			try { 
				Thread.sleep(msTimeout); 
			} catch (InterruptedException e) {}
			if( SystemClock.uptimeMillis() - begin > msTimeout ){
				synchronized (isReady) {
					// don't throw exception unless we won't save the data
					if(isReady.get() == false){
						if(get().logger != null) get().logger.v(TAG, "fetching timedout");
						isTimedout.set(true);
						throw new TimeoutException();
					}
				}
			}
		}
	}
	
	/**
	 * Will return true in case the test data (variables) has been fetched
	 * @param testName the name of the test will be checked
	 * @return true if the data is fetched and ready
	 */
	public static boolean isTestReady(String testName) {
		return ABTest.isABTestSynced(get().getSharedPreferencesForTests(), testName);
	}
	
	
	/**
	 * This method used to determine of the user is part of the test, in other words.
	 * if the data for the test was ready at the time this method was called, it will return the same
	 * value forever. for instance: if the data was ready at the time, it will forever return true (since the data is always ready and saved in SP)
	 * otherwise it will always return false, since we don't want to change the functionality of the user based on his network availability  at first run
	 * @param testName - the name of the test
	 * @return - true or false (see above)
	 */
	protected static boolean wasReadyOnFirstRequest(String testName){
		SharedPreferences sp = get().getSharedPreferencesForTests();
		if(sp.contains(getWasReadySpKey(testName))){
			boolean wasReady = get().getSharedPreferencesForTests().getBoolean(getWasReadySpKey(testName), false);
			if(wasReady){
				return true;
			} else {
				if(get().logger != null) get().logger.v(TAG, "Test was NOT ready at first request, user will never be part of the test: " + testName);
				return false;
			}
		} else {
			if (isTestReady(testName)) {
				if(get().logger != null) get().logger.v(TAG, "test is ready at first request");
				sp.edit().putBoolean(getWasReadySpKey(testName), true).apply();
				return true;
			} else {
				if(get().logger != null) get().logger.v(TAG, "test is not ready at first request");
				sp.edit().putBoolean(getWasReadySpKey(testName), false).apply();
				return false;
			}
		}
	}

	private static String getWasReadySpKey(String testName) {
		return FIRST_TIME_READY_KEY + testName;
	}
	
	/**
	 * Return the String for the requested variable in the specified test
	 * @param testName - the name of the test (project name)
	 * @param variable - the name of the variable you want to receive
	 * @param defaultValue - in the user should not be part of the test, this will return this value
	 * @param participanceEvent - the name of the event that will be submited if you are part of the test (submited once)
	 * @return the value of the variable as string
	 */
	public static String getString(String testName,String variable,String defaultValue) {
		if(wasReadyOnFirstRequest(testName)){
			if(get().logger != null) get().logger.v(TAG, "Returning variable variation for: " + testName);
			return ABTest.getVariable(get().getSharedPreferencesForTests(), testName, variable, defaultValue);
		} else {
			if(get().logger != null) get().logger.v(TAG, "Returning default variable value for: " + testName);
			return defaultValue;
		}
	}
	
	/**
	 * Same as getString with casting, will throw casting exception
	 * @param testName - the name of the test (project name)
	 * @param variable - the name of the variable you want to receive
	 * @param defaultValue - in the user should not be part of the test, this will return this value
	 * @param participanceEvent - the name of the event that will be submited if you are part of the test (submited once)
	 * @return the value of the variable as Boolean
	 */
	public static boolean getBoolean(String testName,String variable, boolean defaultValue) {
		return Boolean.parseBoolean(getString(testName, variable, Boolean.toString(defaultValue)));
	}
	
	/**
	 * Same as getString with casting, will throw casting exception
	 * @param testName - the name of the test (project name)
	 * @param variable - the name of the  variable you want to receive
	 * @param defaultValue - in the user should not be part of the test, this will return this value
	 * @param participanceEvent - the name of the event that will be submited if you are part of the test (submited once)
	 * @return the value of the variable as Long
	 */
	public static long getLong(String testName,String variable, long defaultValue) {
		return Long.parseLong(getString(testName, variable, Long.toString(defaultValue)));
	}
	
	/**
	 * Same as getString with casting, will throw casting exception
	 * @param testName - the name of the test (project name)
	 * @param variable - the name of the variable you want to receive
	 * @param defaultValue - in the user should not be part of the test, this will return this value
	 * @param participanceEvent - the name of the event that will be submited if you are part of the test (submited once)
	 * @return the value of the variable as Float
	 */
	public static float getFloat(String testName,String variable, float defaultValue) {
		return Float.parseFloat(getString(testName, variable, Float.toString(defaultValue)));
	}
	
	/**
	 * @return SharedPreferences used by this wrapper
	 */
	private SharedPreferences getSharedPreferencesForTests(){
		return context.getSharedPreferences(SP_FILE_NAME_TESTS, Context.MODE_PRIVATE);
	}
	
	private SharedPreferences getSharedPreferencesForEvents(){
		return context.getSharedPreferences(SP_FILE_NAME_EVENTS, Context.MODE_PRIVATE);
	}
	
}
