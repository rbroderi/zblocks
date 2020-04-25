import zblocks.EphemeralQueue;

public class tester {

	public static void main(String args[]) {
		EphemeralQueue<String> test = new EphemeralQueue<String>();
		test.enqueue("1");
		test.enqueue("2");
		test.enqueue("3");
		for(String s:test) {
			System.out.println(s);
			if(test.size()==2) {
				test.enqueue("z");
				test.enqueue("z2");
			}
		}
		for(String s:test) {
			System.out.println(s);
		}
	}

}
