import java.io.PrintStream;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JTextArea;

public class TakeoffController implements Runnable {

	ArrayBlockingQueue<Aircraft> runwayQueue;

	private int takeoffTime = 600000;
	private int takeoffMin;
	private int takeoffMax;

	String message;

	PrintStream outputStream;
	JTextArea textArea;
	static final String TITLE = "      - Runway Log -      ";
	static final int MAXLINES = 14;
	
	AtomicBoolean pause;
	AtomicBoolean terminate;
	
	Queue<Aircraft> recentDepartures;

	public TakeoffController(ArrayBlockingQueue<Aircraft> runwayQueue, double timeScale, PrintStream outputStream,
			JTextArea textArea, AtomicBoolean pause, AtomicBoolean terminate) {
		this.runwayQueue = runwayQueue;
		this.takeoffTime = (int) Math.round(this.takeoffTime * timeScale);
		this.takeoffMin = (int) Math.round(0.9 * this.takeoffTime);
		this.takeoffMax = (int) Math.round(1.3 * this.takeoffTime);
		this.outputStream = outputStream;
		this.textArea = textArea;
		this.pause = pause;
		this.terminate = terminate;
		this.recentDepartures = new ArrayDeque<>(14);
	}

	private int randInt(int min, int max) {
		return ThreadLocalRandom.current().nextInt(min, max + 1);
	}

	private void checkLines() {
		if (textArea.getLineCount() >= MAXLINES) {
			textArea.replaceRange("\n", TITLE.length(), textArea.getText().length() - message.length() - 2);
		}
	}

	public Queue<Aircraft> getDepartures(){
		return recentDepartures;
	}
	
	@Override
	public void run() {
		Aircraft a;
		outputStream.println(TITLE);
		for (;;) {
			while(pause.get());
			checkLines();
			try {
				//Thread.sleep(randInt(takeoffMin, takeoffMax));
				Thread.sleep(100);
				do {
					a = runwayQueue.poll();
				} while (a == null);
				message = "Takeoff: " + a.getName() + " " + a.getDelay();
				outputStream.println("Takeoff: " + a.getName() + " " + a.getDelay());
				if(!recentDepartures.add(a)) {
					recentDepartures.poll();
					recentDepartures.add(a);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			if(terminate.get()) return;
		}
	}
}