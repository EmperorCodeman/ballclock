package ballclock.ballclock;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.awt.datatransfer.StringSelection;
import java.util.*;

class Util {
	public static boolean equals(ArrayDeque<Integer> a, ArrayDeque b) {
		if(a.size() != b.size())
			return false;
		Iterator<Integer> iterA = a.iterator();
		Iterator<Integer> iterB = b.iterator();
		while (iterA.hasNext() && iterB.hasNext()) {
			if (iterA.next().equals(iterB.next())) {
				continue;
			} else {
				return false;
			}
		}
		return true;
	}
}

public class BallClock {
	/*
	 * Gain a understanding of what a ball clock is by watching the video below
	 * before digesting program https://www.youtube.com/watch?v=UHBHCsrqYMw In my
	 * code the que is order specific unlike in video Min return to que first, then
	 * 5min, then hour. Last used first into que
	 * 
	 * Operation of the Ball Clock -Every minute, the least recently used ball is
	 * removed from the queue of balls at the bottom of the clock, elevated, then
	 * deposited on the minute indicator track, which is able to hold four balls.
	 * When a fifth ball rolls on to the minute indicator track, its weight causes
	 * the track to tilt. The four balls already on the track run back down to join
	 * the queue of balls waiting at the bottom in reverse order of their original
	 * addition to the minutes track. The fifth ball, which caused the tilt, rolls
	 * on down to the five-minute indicator track. This track holds eleven balls.
	 * The twelfth ball carried over from the minutes causes the five-minute track
	 * to tilt, returning the eleven balls to the queue, again in reverse order of
	 * their addition. The twelfth ball rolls down to the hour indicator. The hour
	 * indicator also holds eleven balls, but has one extra fixed ball which is
	 * always present so that counting the balls in the hour indicator will yield an
	 * hour in the range one to twelve. The twelfth ball carried over from the
	 * five-minute indicator causes the hour indicator to tilt, returning the eleven
	 * free balls to the queue, in reverse order, before the twelfth ball itself
	 * also returns to the queue.
	 */
	private ArrayList<Integer> min = new ArrayList<Integer>(); // Minute Track, 5 values possible
	private ArrayList<Integer> fiveMin = new ArrayList<Integer>(); // Five Minute Track, 12 Values possible
	private ArrayList<Integer> hour = new ArrayList<Integer>(); // Hour Track, 11 Values possible
	private int cycleCount = 0; // counting cycles is a efficient way of storing time delta
	private ArrayDeque<Integer> que; // balls awaiting use in the tracks
	private ArrayDeque<Integer> cycleHash; // when the que becomes identical to its starting configuration a cycle is complete
	private int[] cycleDuration = null; // [days,hour,minutes]
	private int hoursSinceStartOfACycle = 0; // resent at each start of a cycle
	private int numberOfBalls; 
	public BallClock(int numberOfBalls) {
		assert (27 <= numberOfBalls && numberOfBalls <= 127);
		this.numberOfBalls = numberOfBalls;
		que = new ArrayDeque<Integer>();
		for (int i = 0; i < numberOfBalls; i++) {
			que.add(i);
		}
		cycleHash = que.clone(); // when the que becomes identical to its starting configuration a cycle is
									// complete
	}
	public static void main(String[] args) {
		switch(args.length) {
		case 1 :{ 
			BallClock b = new BallClock(Integer.parseInt(args[0]));
			b.setCycleDuration();
			b.display("days to complete cycle");
			break;
		}
		case 2:{
			BallClock b = new BallClock(Integer.parseInt(args[0]));
			b.run(Integer.parseInt(args[1]));
			b.display("state of the tracks");
			break;
		}
		default:{
			BallClock b = new BallClock(123);
			b.setCycleDuration();
			b.display("days to complete cycle");
			b.display("state of the tracks");
		}
		}
		//Test Set
		//30 balls cycle after 15 days.
		//45 balls cycle after 378 days.
	}

	private boolean checkForCycleCompletion() {
		/*
		 * check if cycle is completed by comparing original que to current que each
		 * time they are the same after start a cycle is complete
		 */
		if (Util.equals(que, cycleHash)) {
			// defensive programming practice, would be handled if customer facing
			// logically a cycle should result in all the tracks being empty
			assert ((hour.size() + fiveMin.size() + min.size()) == 0);
			cycleCount += 1;
			if (cycleCount > 1) // CycleDuration requires hours since start of cycle to init, therefore hold off
								// setting it to zero
				hoursSinceStartOfACycle = 0; // Stores time since start of cycle. Prevents hitting max int by reseting
												// each cycle
			return true;
		}
		return false;
	}

	private void addToQue(ArrayList<Integer> balls) {
		for (Integer ball : balls) {
			que.add(ball);
		}
	}

	public int[] getTimeElapsed() {
		int[] time = new int[3];
		time[0] = hoursSinceStartOfACycle / 24; // truncate for days
		time[1] = ((hoursSinceStartOfACycle % 24) + hour.size()); // remainder for hour of day
		time[2] = (min.size() + 5 * fiveMin.size()); // min should be 0
		if (cycleDuration != null) { // tabulate total time running including past cycles
			time[0] += cycleCount * cycleDuration[0];// days
			time[1] += cycleCount * cycleDuration[1];// hours
			time[2] += cycleCount * cycleDuration[2];// minutes
		}
		return time;
	}

	private void addMinute(int ball) {
		// add ball from que to min track
		min.add(ball);
		/*
		 * if 5 balls in min track then add last ball to 5 min track then add remaining
		 * 4 balls to que
		 */
		if (min.size() >= 5) {
			assert (min.size() == 5);
			int routToFiveMin = min.remove(4);
			Collections.reverse(min); // instruction specify to reverse order
			addToQue(min);
			min.clear();
			add5Min(routToFiveMin);
		}
	}

	private void add5Min(int ball) {
		/*
		 * if 12 balls in 5min track then add last ball to hour track then add remaining
		 * 11 balls to que
		 */
		fiveMin.add(ball);
		if (fiveMin.size() >= 12) {
			assert (fiveMin.size() == 12);
			int routToHourTrack = fiveMin.remove(11);
			Collections.reverse(fiveMin);
			addToQue(fiveMin);
			fiveMin.clear();
			addHour(routToHourTrack);
		}
	}

	private void addHour(int ball) {
		// if 13 balls including virtual fixed ball then drop all balls into que'''
		hour.add(ball);
		if (hour.size() >= 12) {
			assert (hour.size() == 12);
			ArrayList<Integer> hourClone;
			ArrayList<Integer> routToQue = new ArrayList<Integer>();
			routToQue.add(hour.remove(11));
			hourClone = (ArrayList<Integer>) hour.clone(); // so clear can be called before addToQue and Assertion Possible inside inside addToQue
			hour.clear();
			Collections.reverse(hourClone);
			addToQue(hourClone);
			addToQue(routToQue);
			hoursSinceStartOfACycle += 12;
			// Set cycle duration if it is null and a cycle has been completed
			// only check if cycle completed at end of cycle 
						if (null == checkForCycleCompletion() && cycleDuration) {
							// set cycle len
							cycleDuration = getTimeElapsed();
							// A partial day(12 hr) is counted as a full day by criteria
							if (cycleDuration[1] == 12)
								cycleDuration[0] += 1;
							hoursSinceStartOfACycle = 0; // prevent hitting max int by reseting
						}
		}
	}

	public void run(int duration) {
		// duration in minutes
		int days = duration / (60 * 24);
		int hours = (duration % (60 * 24)) / 60;
		int minutes = (duration % (60 * 24)) % 60;
		System.out.println("Running " + String.valueOf(days) + ":Days, " + String.valueOf(hours) + ":Hours, "
				+ String.valueOf(minutes) + ":Minutes");
		for (int min : new int[duration]) {
			addMinute(que.pop());// pop removes and returns first element
		}
	}

	public int[] setCycleDuration() {
		while (cycleDuration == null) {
			addMinute(que.pop())
		}
		return cycleDuration;
	}

	public int[] getCurrentTime() {
		int time[] = getTimeElapsed();
		time[1] += 1; // The clock starts at 1 hour. pm/am not specified
		return time;
	}

	public void display(String arg) {
		if (arg == "time elapsed") {
			int timeRunning[] = getTimeElapsed();
			System.out.println("\nTime Elapsed:\n" + "\tDays: " + String.valueOf(timeRunning[0]) + "\n\tHours: "
					+ String.valueOf(timeRunning[1]) + "\n\tMinutes: " + String.valueOf(timeRunning[2]));
		}
		if (arg == "days to complete cycle") {
			System.out.println("\nDays to Complete Cycle: " + String.valueOf(cycleDuration[0]) + "\t Balls/" + numberOfBalls);
		}
		if (arg == "state of the tracks") { // to be done in jason
			System.out.println("\nState of the Tracks:");
			System.out.println("\tHour Track: " + String.valueOf(hour.size() + 1)); // Add fixed virtual hour
			System.out.println("\tFive Minute Track: " + String.valueOf(fiveMin.size()));
			System.out.println("\tMinute Track: " + String.valueOf(min.size()));
		}
	}
}

