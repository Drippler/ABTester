package ab.tester;


public class ABTest {

	private String testName;
	private ABVariable[] variables;
	private boolean lock;

	public ABTest(String testName, boolean lockVariables, String... desiredVariables) {
		this.lock = lockVariables;
		this.testName = testName;
		this.variables = new ABVariable[desiredVariables.length];
		for (int i = 0; i < desiredVariables.length; i++)
			this.variables[i] = new ABVariable(desiredVariables[i]);
	}
	
	public ABVariable[] getVariables() {
		return variables;
	}

	public String getName() {
		return testName;
	}
	
	public boolean getLock() {
		return lock;
	}
	
	/**
	 * used to store the data
	 */
	public class ABVariable {
		private String name;
		private String value;
		
		protected ABVariable(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
		
		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}
	}
}
