**DEPRECATED - This service is no longer available** AB Tester for Amazon AB Testing SDK
==============
*DroidCon 2014 presentation:* http://www.slideshare.net/NirHartmann/dripplers-ab-test-library

AB testing wrapper library for the Amazon AB Testing SDK for Android. Adds interface improvements and new features to the existing sdk. The primamry interface improvements allow a much easier integration in existing code as it doesn't require implementation of callbacks and dealing with waiting for variation values.
Features:
- Pre-fetching of experiments + non blocking methods for getting the pre-fetched variable values later in the code.
- Support for tracking of unique events and goals
- Support for setting a default value for variables for when the API isn't available, times out, or when a test is over.
- Sync fetching of experiments with configurable timeout and fallback to a default value
- Ability to lock variable values so that current users won't be affected by launch configurations of winning variants
- Ability to segment percentage of the users to each project.

Setup
==============

- Download the latest *AmazonInsights-android-sdk* jar to the bin folder (tested with AmazonInsights-android-sdk-2.1.16.jar) from https://developer.amazon.com/public/apis/manage/ab-testing
- Import the project to eclipse
- Add this project as a library to your project

progard
--------------
if you use progard, add those lines, other wise it won't compile (still don't know why)

	keep class com.amazon.** { *; }
	dontwarn com.amazon.**


Code samples
==============

Initialization 
--------------
must be called first (first Activity or Application)

	ABTester.init(this, "publickey", "privatekey", new DefualtLogger());
	
Prefetching
--------------
Fetch the test and the desired variables

sync fetching with timeout:

	try {
		ABTester.syncPreFetch(msTimeout, new ABTest("SampleProject", false, "TITLE","RED_BTN","BLUE_BTN"));
	} catch (TimeoutException e) {
	}
	
If the timeout is reached before the values are fetched, the user won't be part of the test (won't report events and goals)
	
async:

	ABTester.preFetch( new ABTest("SampleProject", false, "TITLE","RED_BTN","BLUE_BTN") );
	
The Async method can be used in a splash screen or early in the App's launch sequence to pre fetch the tests and use it later in the code without blocking. 
	
Getting the value
--------------

	String s = ABTester.getString("SampleProject","WANT_BTN","want");
	Long s = ABTester.getLong("SampleProject","SPEED",90);
	
Events
--------------
Store non unique event:

	ABTester.recordEvent("SampleProject","click_on_red",false);

	
Submit events:

	@Override
	protected void onPause() {
		super.onPause();
		ABTester.submitEvents();
	}
	
User Dimension
--------------
Must be done before fetching

	ABTester.addDimensionAsString("Name","Bob")
	ABTester.addDimensionAsNumber("Age",20)
	

License
==============
The MIT License (MIT)

Copyright (c) 2014 Drippler (http://drippler.com)

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
