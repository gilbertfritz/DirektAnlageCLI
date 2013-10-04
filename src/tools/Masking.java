package tools;


public class Masking implements Runnable {
	private boolean stop;

	/**
	 * @param The
	 *            prompt displayed to the user
	 */
	public Masking(String prompt) {
		System.out.print(prompt);
	}

	/**
	 * Begin masking...display asterisks (*)
	 */
	@SuppressWarnings("static-access")
	public void run() {
		stop = true;
		while (stop) {
			System.out.print("\010*");
			try {
				Thread.currentThread().sleep(1);
			} catch (InterruptedException ie) {
				ie.printStackTrace();
			}
		}
	}

	/**
	 * Instruct the thread to stop masking
	 */
	public void stopMasking() {
		this.stop = false;
	}
}
