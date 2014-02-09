AB Tester for Amazon AB Testing
==============
AB testing wrapper library for the Amazon AB Testing SDK. Adds interface improvements and new features to the existing sdk. The primamry interface improvements allow a much easier integration in existing code as it doesn't require implementation of callbacks and dealing with waiting for variation values.
Features:
- Pre-fetching of experiments + non blocking methods for getting the pre-fetched variable values later in the code.
- Support for tracking of unique events and goals
- Sync fetching of experiments with configurable timeout and fallback to a default value
- Ability to lock variable values so that current users won't be affected by launch configurations of winning variants
- Ability to segment percentage of the users to each project.

Setup
==============

- Download the latest *AmazonInsights-android-sdk* jar to the bin folder (tested with AmazonInsights-android-sdk-2.1.16.jar)
- Import the project to eclipse
- Add this project as a library to your project
- There are classes *ABTester* and *SafeABTester*, the safe class contains try-catch for all the method calls, an error will be logged to the instance logger

Code samples
==============

Initialization 
--------------
must be called first (first Activity or Application)

	ABTesterSafe.init(this, "publickey", "privatekey", new DefualtLogger());
	
Prefetching
--------------
Fetch the test and the desired variables

sync fetching with timeout:

	try {
		ABTesterSafe.syncPreFetch(msTimeout, new ABTest("SampleProject", false, "TITLE","RED_BTN","BLUE_BTN"));
	} catch (TimeoutException e) {
	}
	
If the timeout is reached before the values are fetched, the user won't be part of the test (won't report events and goals)
	
async:

	ABTesterSafe.preFetch( new ABTest("SampleProject", false, "TITLE","RED_BTN","BLUE_BTN") );
	
The Async method can be used in a splash screen or early in the App's launch sequence to pre fetch the tests and use it later in the code without blocking. 
	
Getting the value
--------------

	String s = ABTesterSafe.getString("SampleProject","WANT_BTN","want");
	Long s = ABTesterSafe.getLong("SampleProject","SPEED",90);
	
Events
--------------
Store non unique event:

	ABTesterSafe.recordEvent("SampleProject","click_on_red",false);

	
Submit events:

	@Override
	protected void onPause() {
		super.onPause();
		ABTesterSafe.submitEvents();
	}
	
User Dimension
--------------
Must be done before fetching

	ABTesterSafe.addDimensionAsString("Name","Bob")
	ABTesterSafe.addDimensionAsNumber("Age",20)
	
License
==============
MIT
