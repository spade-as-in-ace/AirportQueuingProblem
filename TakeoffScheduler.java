import java.io.PrintStream;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Vector;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JTextArea;

public class TakeoffScheduler implements Runnable {

	Vector<Aircraft> outgoingFlights;
	ArrayBlockingQueue<Aircraft> runwayQueue;
	LinkedList<Pair<Instant, Aircraft>> delays = new LinkedList<>();
	LinkedList<Aircraft> alreadyDelayed = new LinkedList<>();
	Queue<Aircraft> recentCancellations;
	double minute = 60;

	PrintStream[] outputStream;
	JTextArea[] textArea;
	static final int MAXLINES = 14;
	static final String[] TITLE = { " - Cancelled - ", "   -  Delays  -   ", "       - Queue Log -       " };

	private static final int ONTIME = 78;
	private static final int DELAYED = 20;

	AtomicBoolean pause;
	AtomicBoolean terminate;

	public TakeoffScheduler(ArrayBlockingQueue<Aircraft> runwayQueue, Vector<Aircraft> outgoingFlights,
			double timeScale, PrintStream[] outputStream, JTextArea[] textArea, AtomicBoolean pause,
			AtomicBoolean terminate) {
		this.runwayQueue = runwayQueue;
		this.outgoingFlights = outgoingFlights;
		this.minute *= timeScale;
		this.outputStream = outputStream;
		this.textArea = textArea;
		this.pause = pause;
		this.terminate = terminate;
		this.recentCancellations = new ArrayDeque<>(14);
	}

	private int randInt(int min, int max) {
		return ThreadLocalRandom.current().nextInt(min, max + 1);
	}

	private void addToQueue(Aircraft a) {
		while (runwayQueue.remainingCapacity() <= 0)
			;
		runwayQueue.add(a);
		String message = a.getName();
		message += " " + Instant.now().toString().substring(11, 23);
		if (runwayQueue.remainingCapacity() > 0) {
			message += '-';
		} else {
			message += ' ';
		}
		if (alreadyDelayed.contains(a)) {
			message += '+';
		} else {
			message += ' ';
		}
		alreadyDelayed.remove(a);
		print(2, message);
	}

	private void addToDelay(Aircraft a) {
		double delay = randInt(30, 45);
		print(1, a.getName() + " " + Double.toString(delay * minute).substring(0, 4));
		delays.add(new Pair<>(Instant.now().plusSeconds(Math.round(delay * minute)), a));
	}

	private void cancelFlight(Aircraft a) {
		print(0, a.getName() + " " + a.getPassengers());
		if (!recentCancellations.add(a)) {
			recentCancellations.poll();
			recentCancellations.add(a);
		}
	}
	
	private void sortDelays() {
		Instant now = Instant.now();
		for (int i = 0; i < delays.size(); i++) {
			if (now.compareTo(delays.get(i).getKey()) >= 0) {
				alreadyDelayed.add(delays.remove(i).getValue());
				outgoingFlights.add(alreadyDelayed.getLast());
				print(1, alreadyDelayed.getLast().getName() + " NLD +");
			}
		}
	}

	private Aircraft getMax() {
		double maxCost = 0;
		int index = -1;
		for (int i = 0; i < outgoingFlights.size(); i++) {
			if (outgoingFlights.get(i).updateCost() > maxCost) {
				maxCost = outgoingFlights.get(i).updateCost();
				index = i;
			}
		}
		if (index < 0) {
			return null;
		}
		return outgoingFlights.get(index);
	}

	private void print(int i, String s) {
		outputStream[i].println(s);
	}

	private void checkLines() {
		for (int i = 0; i < outputStream.length; i++) {
			if (textArea[i].getLineCount() >= MAXLINES) {
				textArea[i].setText("");
				print(i, TITLE[i]);
			}
		}
	}

	public List<Pair<Instant, Aircraft>> getDelays() {
		return delays;
	}

	public Queue<Aircraft> getCancellations() {
		return recentCancellations;
	}

	@Override
	public void run() {
		Aircraft a;
		for (int i = 0; i < outputStream.length; i++) {
			outputStream[i].println(TITLE[i]);
		}
		for (;;) {
			while (pause.get());
			checkLines();
			try {
				a = getMax();
				if (a != null && a.isReady()) {
					outgoingFlights.remove(a);
					if (randInt(0, 100) < ONTIME || alreadyDelayed.contains(a)) {
						addToQueue(a);
					} else if (randInt(0, 100 - ONTIME) < DELAYED) {
						addToDelay(a);
					} else {
						cancelFlight(a);
					}
				}
				sortDelays();
			} catch (Exception e) {
				System.out.println(e.toString());
				e.printStackTrace();
			}
			if (terminate.get()) return;
		}
	}
}