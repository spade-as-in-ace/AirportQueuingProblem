import java.time.Instant;
import java.util.List;
import java.util.Queue;
import java.util.Scanner;
import java.util.Vector;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class Airport {

	static ExecutorService airportServices;
	static Vector<Aircraft> outgoingFlights = new Vector<>();
	static ArrayBlockingQueue<Aircraft> runwayQueue = new ArrayBlockingQueue<>(4);

	static ConsolePanel console;

	static AtomicBoolean pause;
	static AtomicBoolean terminate;

	static FlightScheduler flightScheduler;
	static TakeoffScheduler takeoffScheduler;
	static TakeoffController takeoffControl;

	public static boolean checkInList(String str, String[] list) {
		for (String cmd : list) {
			if (str.equals(cmd)) {
				return true;
			}
		}
		return false;
	}

	public static Aircraft checkInList(String str, Vector<Aircraft> list) {
		for (Aircraft a : list) {
			if (str.equals(a.getName())) {
				return a;
			}
		}
		return null;
	}

	public static Aircraft checkInList(String str, ArrayBlockingQueue<Aircraft> list) {
		for (Aircraft a : list) {
			if (str.equals(a.getName())) {
				return a;
			}
		}
		return null;
	}

	public static Aircraft checkInList(String str, Queue<Aircraft> list) {
		for (Aircraft a : list) {
			if (str.equals(a.getName())) {
				return a;
			}
		}
		return null;
	}

	public static Aircraft checkInList(String str, List<Aircraft> list) {
		for (Aircraft a : list) {
			if (str.equals(a.getName())) {
				return a;
			}
		}
		return null;
	}

	public static Aircraft checkInListPair(String str, List<Pair<Instant, Aircraft>> list) {
		for (Pair<Instant, Aircraft> a : list) {
			if (str.equals(a.getValue().getName())) {
				return a.getValue();
			}
		}
		return null;
	}

	public static void checkForFlight(String flight) {
		Aircraft a = checkInList(flight, outgoingFlights);
		if (a != null) {
			System.out.println("Currently At Gate");
		} else {
			a = checkInList(flight, runwayQueue);
			if (a != null) {
				System.out.println("Currently on Queued");
			} else {
				a = checkInList(flight, takeoffControl.getDepartures());
				if (a != null) {
					System.out.println("In Transit");
				} else {
					a = checkInListPair(flight, takeoffScheduler.getDelays());
					if (a != null) {
						System.out.println("Currently Delayed");
					} else {
						a = checkInList(flight, takeoffScheduler.getCancellations());
						if (a != null) {
							System.out.println("Cancelled");
						} else {
							System.out.println("Flght" + flight + " not found");
						}
					}
				}
			}
		}
		if (a != null) {
			System.out.println(a.toString());
		}
	}

	public static void handleUserInput() {
		Scanner s = new Scanner(System.in);
		String input;
		String[] pauseCommands = { "pause", "p", "freeze", "stop" };
		String[] resumeCommands = { "resume", "unpause", "play", "p" };
		String[] exitCommands = { "exit", "quit", "close", "q" };
		while (true) {
			input = s.nextLine();
			if (checkInList(input, pauseCommands) && !pause.get()) {
				System.out.println("PAUSED");
				pause.set(true);
			} else if (checkInList(input, resumeCommands) && pause.get()) {
				System.out.println("UNPAUSED");
				pause.set(false);
			} else if (checkInList(input, exitCommands)) {
				System.out.println("EXITING");
				s.close();
				terminate.set(true);
				airportServices.shutdown();
				console.close();
				return;
			} else if (input.equals("t")) {
				System.out.println(Instant.now().toString().substring(11, 26));
			} else if (input.equals("get")) {
				System.out.print("Flight: ");
				if (pause.get()) {
					checkForFlight("Flight" + s.nextLine());
				} else {
					pause.set(true);
					checkForFlight("Flight" + s.nextLine());
					pause.set(false);
				}
			}
		}
	}

	public static void main(String[] args) {
		console = new ConsolePanel(5, "Airport", new String[] { "    - Gate Log -     - ETA -  - Dest -       ",
				" - Cancelled - ", "   -  Delays  -   ", "       - Queue Log -       ", "      - Runway Log -      " });

		pause = new AtomicBoolean();
		pause.set(false);
		terminate = new AtomicBoolean();
		terminate.set(false);

		double timeScale = 0.01;
		flightScheduler = new FlightScheduler(outgoingFlights, timeScale, console.getOutputStream(0),
				console.getTextArea(0), pause, terminate);

		takeoffScheduler = new TakeoffScheduler(runwayQueue, outgoingFlights, timeScale,
				console.getOutputStream(new int[] { 1, 2, 3 }), console.getTextArea(new int[] { 1, 2, 3 }), pause,
				terminate);

		takeoffControl = new TakeoffController(runwayQueue, timeScale, console.getOutputStream(4),
				console.getTextArea(4), pause, terminate);
		airportServices = Executors.newFixedThreadPool(4);
		airportServices.execute(flightScheduler);
		airportServices.execute(takeoffScheduler);
		airportServices.execute(takeoffControl);

		handleUserInput();
	}

}
