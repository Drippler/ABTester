package ab.tester.prefs;

import ab.tester.ABTest;
import ab.tester.ABTest.ABVariable;
import android.content.Context;
import android.content.SharedPreferences.Editor;

public class ABTestsSharedPrefs extends ABSharedPrefs {

	private static final String FIRST_TIME_READY_KEY = "FIRST_TIME_READY_KEY____";
	
	public ABTestsSharedPrefs(Context context) {
		super(context);
	}

	@Override
	protected String getPrefsName() {
		return "ABTester_experiments_values_tests";
	}

	public void save(ABTest test) {
		Editor editor = prefs.edit();
		ABVariable[] variables = test.getVariables();
		String testName = test.getName();
		int length = variables.length;
		for (int i = 0; i < length; i++) {
			String key = testName + SEPARATOR + variables[i].getName();
			
			// save only if not locked, or locked and yet to be set
			boolean lock = test.getLock();
			if ( (variables[i].getValue() != null) && ((lock == false) || (lock == true && prefs.contains(key) == false)) ) {
				editor.putString(key , variables[i].getValue());
			}
		}
		// set it to true, so we can check that that has been downloaded
		editor.putBoolean(testName, true);
		apply(editor);
	}
	
	/**
	 * Will return true in case the test data (variables) has been fetched
	 * @param test the test
	 * @return true if the data is fetched and ready
	 */
	protected boolean isTestReady(ABTest test) {
		return isTestReady(test.getName());
	}
	
	/**
	 * Will return true in case the test data (variables) has been fetched
	 * @param testName the name of the test will be checked
	 * @return true if the data is fetched and ready
	 */
	protected boolean isTestReady(String testName) {
		return prefs.getBoolean(testName, false);
	}
	
	/**
	 * This method used to determine if the user is part of the test, in other words,
	 * if the data for the test was ready at the time this method was called, it will return the same
	 * value forever. for instance: if the data was ready at the time, it will forever return true (since the data is always ready and saved in SP)
	 * otherwise it will always return false, since we don't want to change the functionality of the user based on his network availability  at first run
	 * @param testName - the test
	 * @return - true or false (see above)
	 */
	public boolean wasReadyAtFirstRequest(ABTest test) {
		return wasReadyAtFirstRequest(test.getName());
	}
	
	/**
	 * This method used to determine if the user is part of the test, in other words,
	 * if the data for the test was ready at the time this method was called, it will return the same
	 * value forever. for instance: if the data was ready at the time, it will forever return true (since the data is always ready and saved in SP)
	 * otherwise it will always return false, since we don't want to change the functionality of the user based on his network availability  at first run
	 * @param testName - the test
	 * @return - true or false (see above)
	 */
	public boolean wasReadyAtFirstRequest(String testName) {
		boolean result;
		String key = FIRST_TIME_READY_KEY + testName;
		if (prefs.contains(key)) {
			boolean wasReady = prefs.getBoolean(key, false);
			if (wasReady) {
				result = true;
			} else {
				result = false;
			}
		} else {
			if (isTestReady(testName)) {
				apply(prefs.edit().putBoolean(key, true));
				result = true;
			} else {
				apply(prefs.edit().putBoolean(key, false));
				result = false;
			}
		}
		
		return result;
	}
	
	/**
	 * get the variable synced previously to the SharedPreferences
	 * fetched locally
	 * @param sp - the SharedPreferences
	 * @param abName - the name of the ABtest (project name)
	 * @param variableName - the name of the variable to be fetched
	 * @param defaultValue - the default will be returned when, the value is not synced. you can check if synced via isABTestSynced();
	 * @return
	 */
	public String getVariable(String abName, String variableName, String defaultValue){
		return prefs.getString(abName + ABSharedPrefs.SEPARATOR + variableName, defaultValue);
	}
	
	/**
	 * get the variable synced previously to the SharedPreferences
	 * fetched locally
	 * @param sp - the SharedPreferences
	 * @param test - the test
	 * @param ABVariable - the variable
	 * @param defaultValue - the default will be returned when, the value is not synced. you can check if synced via isABTestSynced();
	 * @return
	 */
	public String getVariable(ABTest test, ABVariable variable, String defaultValue){
		return getVariable(test.getName(), variable.getName(), defaultValue);
	}
}
