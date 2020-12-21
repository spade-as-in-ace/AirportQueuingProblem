import java.io.PrintStream;
import java.time.Instant;
import java.util.Vector;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JTextArea;

public class FlightScheduler implements Runnable {

	Vector<Aircraft> outgoingFlights;

	private int minTime = 120000;
	private int maxTime = 480000;
	private double minute = 60;

	String[] destinations = { "Seoul", "Washington DC", "Dublin", "Orlando", "London", "San Juan", "Los Angeles",
			"Moscow", "Madrid", "Abu Dhabi", "Hong Kong", "Helsinki", "Warsaw", "Memphis", "Amsterdam", "Paris",
			"Boston", "Casablanca", "Rochester", "Las Vegas", "Fort Lauderdale", "Dakar", "Syracuse", "Denver",
			"Santo Domingo", "Tirana", "Rome", "Istanbul", "Athens", "Kiev", "Frankfurt", "Vienna", "Belgrade",
			"Bucharest", "Budapest", "Zagreb", "Sarajevo", "Toronto", "Tel Aviv", "Jerusalem", "Pristina", "Sofia",
			"Skopje", "Podgorica", "Ljubljana", "Thessaloniki", "Munich" };
	int[] etaMinutes = { 338, 60, 690, 165, 720, 280, 200, 780, 790, 1300, 271, 900, 680, 110, 540, 800, 60, 765, 90,
			170, 185, 750, 81, 210, 290, 690, 495, 570, 660, 630, 460, 580, 520, 645, 515, 650, 730, 242, 635, 635, 625,
			660, 665, 660, 695, 750, 580 };

	String message;

	PrintStream outputStream;
	JTextArea textArea;
	static final String TITLE = "    - Gate Log -     - ETA -  - Dest -       ";
	static final int MAXLINES = 14;
	
	AtomicBoolean pause;
	AtomicBoolean terminate;

	public FlightScheduler(Vector<Aircraft> outgoingFlights, double timeScale, PrintStream outputStream,
			JTextArea jTextArea, AtomicBoolean pause, AtomicBoolean terminate) {
		this.outgoingFlights = outgoingFlights;
		this.minTime = (int) Math.round(this.minTime * timeScale);
		this.maxTime = (int) Math.round(this.maxTime * timeScale);
		this.outputStream = outputStream;
		this.textArea = jTextArea;
		this.minute *= timeScale;
		this.pause = pause;
		this.terminate = terminate;
	}

	private int randInt(int min, int max) {
		return ThreadLocalRandom.current().nextInt(min, max + 1);
	}

	private long randLong(long min, long max) {
		return ThreadLocalRandom.current().nextLong(min, max + 1);
	}

	private String buildMessage(Aircraft a, boolean connection) {
		String s = a.getName() + " " + a.getTime() + " " + a.getETA();
		if (connection) {
			s += '+';
		} else if(a.getPassengers() < 200){
			s += '-';
		} else {
			s += ' ';
		}
		s += " " + a.getDestination();
		return s;
	}

	private void checkLines() {
		if (textArea.getLineCount() >= MAXLINES) {
			textArea.replaceRange("\n", TITLE.length(), textArea.getText().length() - message.length()-2);
		}
	}

	@Override
	public void run() {
		Aircraft a;
		int r;
		int passengers;
		int passengerConnect;
		Instant connection;
		outputStream.println(TITLE);
		for (;;) {
			while(pause.get());
			checkLines();
			r = randInt(0, destinations.length - 1);
			passengers = randInt(150, 550);
			if(passengers < 250) {
				passengers = randInt(0, 10);
			}
			if (etaMinutes[r] <= 450 && passengers > 200) {
				passengerConnect = randInt(0, 100);
				if (passengerConnect < 20) {
					passengerConnect = randInt(0, passengers);
					connection = Instant.now().plusSeconds((long) ((etaMinutes[r] + randInt(10, 120)) * 60));
				} else {
					passengerConnect = 0;
					connection = null;
				}
			} else {
				passengerConnect = 0;
				connection = null;
			}
			a = new Aircraft("Flight" + randInt(1000, 9999),
					Instant.now().plusSeconds((long) (randLong(5, 10) * minute)), passengers, passengerConnect,
					connection, etaMinutes[r], destinations[r]);
			this.outgoingFlights.add(a);
			message = buildMessage(a, connection != null);
			outputStream.println(message);
			try {
				Thread.sleep(randInt(minTime, maxTime));
			} catch (InterruptedException e) {
				System.out.println(e.toString());
				e.printStackTrace();
			}
			if(terminate.get()) return;
		}
	}
}