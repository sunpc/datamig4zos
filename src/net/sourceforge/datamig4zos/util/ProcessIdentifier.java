/**
 *  Class: util.ProcessIdentifier
 *  Description: Generate a 8-char identifier for the process based on the system time.
 * 
 * 	Author: Peng Cheng Sun
 * 
 * 	Format:  
 *  C 1 B P 3 A 1 2
 *  | | | | | | | |
 *  | | | | | | | --------- item seq# digit 2
 *  | | | | | | ----------- item seq# digit 1
 *  | | | | | ------------- second or second-30: 0 - 9, A - T
 *  | | | | --------------- minute or minute-30: 0 - 9, A - T
 *  | | | ----------------- minute/second flag: P (min+sec), Q (min+[sec-30]), X ([min-30]+sec), Y ([min-30]+sec)
 *  | | ------------------- hour: A - X
 *  | --------------------- day: 1 - 9, A - V
 *  ----------------------- month: A - L
 *  
 *  Examples:
 *  Mar 05, 15:38:21 #05 - C5OX8K05
 *  Dec 31, 02:14:59 #37 - LVBQDS37
 * 
 *  Modification History
 *  1. 03/05/2011: Code baseline. (V6.0 phase 1)
 *  2. 04/05/2011: Have the thread sleeping for 1 second when seq # reaches 99. (V6.0 beta 1)
 *  3. 11/12/2012: Reset the sequence number to 1 for each new process id. (V6.1.2)
 *  4. 11/16/2012: Fix the sequence number generation. (V6.1.3)
 */
package net.sourceforge.datamig4zos.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @author SunPC
 *
 */
public class ProcessIdentifier {
	
	private static String id = "";					// id to be generated
	
	private static int seq_no = 0;					// current seq no
	
	// generate a new id
	private static void initIdentifier() {
		// check sequence number - v6.1.3
		if(seq_no == 99) {
			// wait for 1 second - v6.0 beta 1
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			// reset seq # to 1
			seq_no = 0;
		}
		
		// initiate the calendar object
		Calendar cal = Calendar.getInstance();
		Date curr_time = cal.getTime();
		SimpleDateFormat formatter = null;
		
		// build first 3 chars
		formatter = new SimpleDateFormat("MM");
		String char1 = convertNum2Char(Integer.parseInt(formatter.format(curr_time)), false);

		formatter = new SimpleDateFormat("dd");
		String char2 = convertNum2Char(Integer.parseInt(formatter.format(curr_time)), true);

		formatter = new SimpleDateFormat("HH");
		String char3 = convertNum2Char(Integer.parseInt(formatter.format(curr_time)), false);

		// build minute second flag - char4
		String char4 = "";
		formatter = new SimpleDateFormat("mm");
		int min = Integer.parseInt(formatter.format(curr_time));
		formatter = new SimpleDateFormat("ss");
		int sec = Integer.parseInt(formatter.format(curr_time));
		
		if(min < 30 && sec < 30)
			char4 = "P";
		else if(min < 30)
			char4 = "Q";
		else if(sec < 30)
			char4 = "X";
		else
			char4 = "Y";
		
		// build char5 and char6
		if(min >= 30) min = min - 30;
		if(sec >= 30) sec = sec - 30;
		
		String char5 = convertNum2Char(min, true);
		String char6 = convertNum2Char(sec, true);
		
		// verify the first 6 chars of the id to determine the seq no - v6.1.2
		String prefix = char1 + char2 + char3 + char4 + char5 + char6;
		
		if (!id.isEmpty() && prefix.equals(id.substring(0, 6))) {	// v6.1.3
			// seq no +1
			seq_no ++;
		} else {
			// reset seq no
			seq_no = 1;
		}
		
		// build char7 and char8
		String suffix = Integer.toString(seq_no);
		
		if(seq_no < 10)
			suffix = "0" + suffix;
		
		// build id
		id = prefix + suffix;
	}
	
	// convert a number to an ascii char 
	private static String convertNum2Char(int num, boolean numericFlag) {
		// convert a number 1~26 to a char of A~Z when numeric_flag = false
		// or keep 0~9 as is and convert the rest to a char 10~35 to a char of A~Z when numeric_flag = true 
		int int_ascii = 0;
		String str = "";
		
		if(numericFlag) {
			int_ascii = num + 55;		
			
			if(num > 35) 
				int_ascii = 65;
			
			if (num > 9)
				str = Character.toString((char)int_ascii);
			else
				str = Integer.toString(num);
			
		} else {
			int_ascii = num + 64;		
			
			if(num > 26 || num == 0)
				int_ascii = 65;
			
			str = Character.toString((char)int_ascii);
		}		
		
		return str;
	}

	// return new id
	public static String getNewIdentifier() {
		initIdentifier();
		return id;
	}

	// return current id
	public static String getCurrentIdentifier() {
		return id;
	}

}
