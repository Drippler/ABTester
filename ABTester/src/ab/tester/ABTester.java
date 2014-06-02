package ab.tester;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import ab.tester.ABTest.ABVariable;
import ab.tester.prefs.ABEventsSharedPrefs;
import ab.tester.prefs.ABGeneralSharedPrefs;
import ab.tester.prefs.ABTestsSharedPrefs;
import android.content.Context;
import android.os.SystemClock;

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
	private static final String TAG = "ABTester";
	private static final long ASYNC_TIMEOUT = TimeUnit.SECONDS.toMillis(60);
	
	private AmazonInsights insightsInstance;
	private LoggerInterface logger;
	private ABTestsSharedPrefs testsPrefs;
	private ABGeneralSharedPrefs generalPrefs;
	private ABEventsSharedPrefs eventsPrefs;
	
	private static ABTester instance = null;
	
	private static ABTester get(){
		if (instance != null){
			return instance;
		} else {
			initWasNotCalled();
			return null;
		}
	}

	private static void initWasNotCalled() {
		throw new RuntimeException("You must call ABTester.init(); before using the wrapper");
	}
	
	public static EventClient getEventClient(){
		return get().insightsInstance.getEventClient();
	}
	
	/**
	 * Must be called before usage of any other methods
	 * @param ctx 
	 * @param publicKey - provided by amazon
	 * @param privateKey - provided by amazon
	 * @param logger - implementation of the loggerInterface, will be used to log things
	 */
	public synchronized static void init(Context ctx, String publicKey, String privateKey, LoggerInterface logger){
		if (instance == null)
			instance = new ABTester(ctx.getApplicationContext(), publicKey, privateKey, logger);
	}

	/**
	 * Must be called before usage of any other methods
	 * @param ctx 
	 * @param publicKey - provided by amazon
	 * @param privateKey - provided by amazon
	 */
	public static void init(Context ctx, String publicKey, String privateKey){
		ABTester.init(ctx, publicKey, privateKey, false);
	}

	/**
	 * Must be called before usage of any other methods
	 * @param ctx 
	 * @param publicKey - provided by amazon
	 * @param privateKey - provided by amazon
	 * @param silence - whether this library will log out events to the LogCat or not
	 */
	public static void init(Context ctx, String publicKey, String privateKey, boolean silence) {
		ABTester.init(ctx, publicKey, privateKey, silence ? null : new DefualtLogger());
	}
	
	private ABTester(Context ctx, String publicKey, String privateKey, LoggerInterface logger) {
		ctx = ctx.getApplicationContext();
		this.testsPrefs = new ABTestsSharedPrefs(ctx);
		this.generalPrefs = new ABGeneralSharedPrefs(ctx);
		this.eventsPrefs = new ABEventsSharedPrefs(ctx);
		
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
		
		setupRandomIntDimension();
	}
	
	private void setupRandomIntDimension() {
		int percentile = generalPrefs.getPercentile();
		insightsInstance.getUserProfile().addDimensionAsNumber(ABGeneralSharedPrefs.PERCENTILE, percentile);
	}
	
	/**
	 * Must be called before fetching
	 * @param name - the name of the dimension
	 * @param value - the dimension value as a string
	 */
	public static void addDimension(String name, String value){
		ABTester tester = get();
		tester.insightsInstance.getUserProfile().addDimensionAsString(name, value);
		if (tester.logger != null) 
			tester.logger.v(TAG, "adding dimmension:" + name + "value:" + value);
	}
	
	/**
	 * Must be called before fetching
	 * @param name - the name of the dimension
	 * @param value - the dimension value as a Number (float,int,double...)
	 */
	public static void addDimension(String name, Number value){
		ABTester tester = get();
		tester.insightsInstance.getUserProfile().addDimensionAsNumber(name, value);
		if (tester.logger != null) 
			tester.logger.v(TAG, "adding dimmension:" + name + "value:" + value);
	}
	
	/**
	 * Stores the event locally, will be sent by calling submitEvents()
	 * @param testName - checks if the user is part of the experiment
	 * @param eventName
	 * @param onlyOnce - should report this once only
	 */
	public static void recordEvent(String eventName, boolean onlyOnce) {
		ABTester tester = get();
		ABEventsSharedPrefs prefs = tester.eventsPrefs;
		boolean wasReported = prefs.wasEventReported(eventName);
		if (onlyOnce == false || (onlyOnce == true && wasReported == false)) {
			EventClient eventClient = tester.insightsInstance.getEventClient();
		    eventClient.recordEvent(eventClient.createEvent(eventName));
		    prefs.reportEvent(eventName);
		    if (tester.logger != null) 
		    	tester.logger.v(TAG, "Event sent: " + eventName);
		} else {
			if (tester.logger != null) 
				tester.logger.v(TAG, "Event FILTERED: " + eventName);
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
	 */
	public static void preFetch(final ABTest... tests) {
		preFetch(ASYNC_TIMEOUT, tests);
	}
	
	/**
	 * ASync preFetching of the experiments. running in a background thread.
	 */
	public static void preFetch(final long timeout, final ABTest... tests) {
		ABTester tester = get();
		if (tester.logger != null) 
			tester.logger.v(TAG, "prefetching async...");
		
		ExecutorService ex = Executors.newSingleThreadExecutor();
		ex.execute(new Runnable() {
			@Override
			public void run() {
					try {
						syncPreFetch(timeout, tests);
					} catch (TimeoutException e) {
						// async fetching will not throw an exception on failure
					}
			}
		});
		ex.shutdown();
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
		final ABTester tester = get();
		String[] testsToFetch = new String[tests.length];
		
		for (int i = 0; i < tests.length; i++)
			testsToFetch[i] = tests[i].getName();

		long begin = SystemClock.uptimeMillis();
		
		final Object lock = new Object();
		final AtomicBoolean isReady = new AtomicBoolean(false); 
		final AtomicBoolean isTimedout = new AtomicBoolean(false);
		final Thread waitingThread = Thread.currentThread();
		
		if (tester.logger != null) 
			tester.logger.v(TAG, "fetching...");
		
		tester.insightsInstance.getABTestClient().getVariations(testsToFetch).setCallback(new InsightsCallback<VariationSet>() {
			
			@Override
			public void onComplete(VariationSet vars) {
				synchronized (lock) {
					if (isTimedout.get() == false) {
						ABTestsSharedPrefs prefs = tester.testsPrefs;
						for (int i = 0; i < tests.length; i++){
							Variation var = vars.getVariation(tests[i].getName());
							if (tester.logger != null)
								tester.logger.v(TAG, "fetched: " + var.getProjectName());
							
							ABVariable[] variables = tests[i].getVariables();
							int length = variables.length;
							for (int j = 0; j < length ; j++) {
								String value = var.getVariableAsString(variables[j].getName(), null);
								variables[j].setValue(value);
							}
							prefs.save(tests[i]);
						}
						isReady.set(true);
						waitingThread.interrupt();
					}
				}
			}
			
			@Override
			public void onError(InsightsError error) {
				super.onError(error);
				if (tester.logger != null) 
					tester.logger.v(TAG, "error: " + error.getMessage());
			}
		});

		// wait until ready or timed out
		while (isReady.get() == false) {
			try { 
				Thread.sleep(msTimeout); 
			} catch (InterruptedException e) {}
			
			if (SystemClock.uptimeMillis() - begin > msTimeout) {
				synchronized (lock) {
					// don't throw exception unless we won't save the data
					if(isReady.get() == false) {
						if (tester.logger != null)
							tester.logger.v(TAG, "fetching timedout");
						
						isTimedout.set(true);
						throw new TimeoutException();
					}
				}
			}
		}
	}
	
	/**
	 * This method used to determine if the user is part of the test, in other words,
	 * if the data for the test was ready at the time this method was called, it will return the same
	 * value forever. for instance: if the data was ready at the time, it will forever return true (since the data is always ready and saved in SP)
	 * otherwise it will always return false, since we don't want to change the functionality of the user based on his network availability  at first run
	 * @param testName - the name of the test
	 * @return - true or false (see above)
	 */
	protected static boolean wasReadyAtFirstRequest(String testName) {
		ABTester tester = get();
		ABTestsSharedPrefs prefs = tester.testsPrefs;
		boolean wasReady = prefs.wasReadyAtFirstRequest(testName);
		if (tester.logger != null) {
			if (wasReady) {
				tester.logger.v(TAG, "test is ready at first request");
			} else {
				tester.logger.v(TAG, "test is not ready at first request");
			}
		}
		return wasReady;
	}

	/**
	 * Return the String for the requested variable in the specified test
	 * @param testName - the name of the test (project name)
	 * @param variable - the name of the variable you want to receive
	 * @param defaultValue - in the user should not be part of the test, this will return this value
	 * @return the value of the variable as string
	 */
	public static String getString(String testName, String variable, String defaultValue) {
		String result;
		ABTester tester = get();
		if (wasReadyAtFirstRequest(testName)) {
			if (tester.logger != null) 
				tester.logger.v(TAG, "Returning variable variation for: " + testName);
			ABTestsSharedPrefs prefs = tester.testsPrefs;
			result = prefs.getVariable(testName, variable, defaultValue);
		} else {
			if (tester.logger != null) 
				tester.logger.v(TAG, "Returning default variable value for: " + testName);
			result = defaultValue;
		}
		return result;
	}
	
	/**
	 * Same as getString with casting, will throw casting exception
	 * @param testName - the name of the test (project name)
	 * @param variable - the name of the variable you want to receive
	 * @param defaultValue - in the user should not be part of the test, this will return this value
	 * @return the value of the variable as boolean
	 */
	public static boolean getBoolean(String testName, String variable, boolean defaultValue) {
		return Boolean.parseBoolean(getString(testName, variable, Boolean.toString(defaultValue)));
	}
	
	/**
	 * Same as getString with casting, will throw casting exception
	 * @param testName - the name of the test (project name)
	 * @param variable - the name of the  variable you want to receive
	 * @param defaultValue - in the user should not be part of the test, this will return this value
	 * @return the value of the variable as long
	 */
	public static long getLong(String testName, String variable, long defaultValue) {
		return Long.parseLong(getString(testName, variable, Long.toString(defaultValue)));
	}
	
	/**
	 * Same as getString with casting, will throw casting exception
	 * @param testName - the name of the test (project name)
	 * @param variable - the name of the  variable you want to receive
	 * @param defaultValue - in the user should not be part of the test, this will return this value
	 * @return the value of the variable as int
	 */
	public static int getInt(String testName, String variable, long defaultValue) {
		return Integer.parseInt(getString(testName, variable, Long.toString(defaultValue)));
	}
	
	/**
	 * Same as getString with casting, will throw casting exception
	 * @param testName - the name of the test (project name)
	 * @param variable - the name of the variable you want to receive
	 * @param defaultValue - in the user should not be part of the test, this will return this value
	 * @return the value of the variable as float
	 */
	public static float getFloat(String testName,String variable, float defaultValue) {
		return Float.parseFloat(getString(testName, variable, Float.toString(defaultValue)));
	}
}
