package ab.tester;

public interface LoggerInterface {

	public void e(String tag,String text);
	public void e(String tag,String text,Throwable e);
	public void d(String tag,String text);
	public void v(String tag,String text);
	
}
