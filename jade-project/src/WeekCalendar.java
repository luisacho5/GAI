package jadeproject;
import java.util.Random;
import java.util.ArrayList;

public class WeekCalendar {
	private boolean[] free;

	private final int WEEKDAYS = 7;
	private final int HOURS    = 24;
	
	WeekCalendar() {//Constructor of the WeekCalendar
	free = new boolean[HOURS * WEEKDAYS];//Array with the lenght of all the spots of one week 24*7=168
		Random random = new Random();
		for (int i = 0; i < free.length; ++i) {
			free[i] = random.nextBoolean(); //We set in all the positions of the array of spots a random boolean; true(available or not) or  false
		}
	}

	@Override
	public String toString() {//Function to print all the days and hours with the disponibility
		String result = "Hours\tMon.\tTue.\tWed.\tThu.\tFri.\tSat.\tSun.\n";

		for (int hour = 0; hour < HOURS; ++hour) {
			result += hour + "H00\t";
			
			for (int day = 0; day < WEEKDAYS; ++day) {
				if (isAvailable(day, hour)) {
				    result += "FREE\t";
				    
				}
				else {
				    result += "OCUPIED\t";
				}
			}

			result += "\n";
		}

		return result;
	}


	public ArrayList<int[]> findSlots() {//This function will look in all the posibles slots and find the free ones
		ArrayList<int[]> slotsFree = new ArrayList<int[]>();

		for (int day = 0; day < WEEKDAYS; ++day) {
			for (int hour = 0; hour < HOURS; ++hour) {
			    
				int[] slot = {day, hour}; //The posible spot in the calendar with hour and day
				
				if (isAvailable(day, hour)) slotsFree.add(slot); //If its free we add to the Arraylist slotsFree
			}
		}

		return slotsFree;//When we finish recorring all the Calendar we return all the free Slots in the calendar
	}

	public boolean isAvailable(int day, int hour) { //The function that checks the Avaliability in the spot we send, hour and day
		return free[day * HOURS + hour];
	}

    public void cancelMeeting(int day, int hour) {//If we want to cancel a meeting we just turn the spot into false
		free[day * HOURS + hour] = false;
	}
	public void scheduleMeeting(int day, int hour) {//If we want to set a Meeting we only need to turn the spot of the array true
	    free[day * HOURS + hour] = true;
	}
	
	public static String getWeekDayName(int day) { //Function simple that if you send a number it will bring you back the day of the week
		String result = "";

		switch (day) {
			case 0:
				result = "Monday";
				break;
			case 1:
				result = "Tuesday";
				break;
			case 2:
				result = "Wednesday";
				break;
			case 3:
				result = "Thursday";
				break;
			case 4:
				result = "Friday";
				break;
			case 5: 
				result = "Saturday";
				break;
			case 6:
				result = "Sunday";
				break;
		}

		return result;
	}
}
