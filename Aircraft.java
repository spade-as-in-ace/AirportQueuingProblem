import java.time.Instant;

public class Aircraft implements Comparable<Aircraft> {

	private static final double TIMESCALE = 0.08;
	private static final int BASECOST = 4;

	private String name = "";
	private String destination = "";
	private int passengerCount;
	private int passengerConnect;
	private long eta;

	private Instant scheduledPushback;
	private Instant scheduledConnection;

	private double delay;
	private double cost;

	private static final double[] WEIGHTS = { 0.3, 1, 0.06 };

	Aircraft(Instant scheduledPushback, int passengerCount, int passengerConnect, Instant scheduledConnection,
			long eta) {
		this.scheduledPushback = scheduledPushback;
		this.passengerCount = passengerCount;
		this.passengerConnect = passengerConnect;
		this.scheduledConnection = scheduledConnection;
		this.eta = eta;
		updateCost();
	}

	Aircraft(String name, Instant scheduledPushback, int passengerCount, int passengerConnect,
			Instant scheduledConnection, long eta) {
		this.name = name;
		this.scheduledPushback = scheduledPushback;
		this.passengerCount = passengerCount;
		this.passengerConnect = passengerConnect;
		this.scheduledConnection = scheduledConnection;
		this.eta = eta;
	}

	Aircraft(String name, Instant scheduledPushback, int passengerCount, int passengerConnect,
			Instant scheduledConnection, long eta, String destination) {
		this.name = name;
		this.scheduledPushback = scheduledPushback;
		this.passengerCount = passengerCount;
		this.passengerConnect = passengerConnect;
		this.scheduledConnection = scheduledConnection;
		this.eta = eta;
		this.destination = destination;
	}

	public double getDelay() {
		delay = (Instant.now().getEpochSecond() - scheduledPushback.getEpochSecond());
		if (delay <= 0) {
			return (Instant.now().getNano() - scheduledPushback.getNano()) * 0.000000001;
		}
		return delay;
	}
	
	double updateCost() {
		if (!isReady()) {
			return 0;
		}
		getDelay();
		cost = 0;
		if (passengerConnect > 0) {
			cost += WEIGHTS[1] * passengerConnect
					* (1.0 / difference(Instant.now().plusSeconds(eta * 60), scheduledConnection));
		}
		cost *= cost;
		cost += WEIGHTS[0] * passengerCount + BASECOST;
		double d4 = TIMESCALE * delay;
		d4 *= d4;
		cost *= WEIGHTS[2] * d4 * d4;
		return cost;
	}

	private long difference(Instant a, Instant b) {
		return a.getEpochSecond() - b.getEpochSecond();
	}

	public String getCost() {
		return Double.toString(cost).substring(0, 4);
	}

	@Override
	public int compareTo(Aircraft arg0) {
		return (int) Math.round(this.updateCost() - arg0.updateCost());
	}

	public String getName() {
		return name;
	}

	public String getTime() {
		return scheduledPushback.toString().substring(11, 22);
	}

	public boolean isReady() {
		return Instant.now().compareTo(scheduledPushback) > 0;
	}

	public String toString() {
		updateCost();
		String s = name + "\nDestination: " + destination + "\nPassenger Count: " + passengerCount
				+ "\nScheduled Pushback: " + scheduledPushback.toString().substring(11, 22);
		if (scheduledConnection != null) {
			s += "\nConnection: " + passengerConnect + " " + scheduledConnection.toString().substring(10, 22);
		}
		if (delay != 0) {
			s += '\n' + "Delayed: " + delay + " minutes";
		}
		s += "\nCost: " + getCost();
		return s;
	}

	public String getDestination() {
		return this.destination;
	}

	public long getETA() {
		return this.eta;
	}

	public int getPassengers() {
		return passengerCount;
	}

}
