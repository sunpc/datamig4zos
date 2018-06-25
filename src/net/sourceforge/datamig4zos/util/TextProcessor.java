/**
 *  Class: util.TextProcessor
 *  Description: This class provides many common functions.
 *	Some code is inherited from Batch Generator Tool v2.3.
 * 
 *	Author: Peng Cheng Sun
 *   
 *  Modification History
 *  01. 02/18/2009: Code baseline. (V5.0)
 *  02. 05/06/2009: replaceStr - allow null. (V5.1)
 *  03. 03/09/2011: Class renamed from CommonFunction to TextProcessor. (V6.0 phase 1)
 *  04. 03/11/2011: Functional enhancements. (V6.0 phase 1)
 *  	1) Change date format from yyyy-MM-dd to MM/dd/yyyy.
 *   	2) Add wrapText() method.
 *   	3) Add a logic to verify the jcl_header.
 *   	4) Change the pass-in parameters name to standard.
 *  05. 04/02/2011: Change the wrapText() size from 60 to 73. (V6.0 phase 6)
 *  06. 11/06/2012: Add formatSql() method. (V6.1)
 *  07. 11/12/2012: Performance tuning. (V6.1.2)
 *  	1) Enhance wrapText() method.
 *  	2) Remove formatSql() method.
 *  	3) Use generateJcl() method to replace generateTemplate(), generateParameter() and generateOutput() methods.
 */
package net.sourceforge.datamig4zos.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Vector;

/**
 * @author SunPC
 * 
 */
public class TextProcessor {

	// read a text file
	public static String readFile(String fileName) throws Exception {
		String return_value = "";

		File file = new File(fileName);

		if (!file.exists()) {
			return return_value;
		}

		BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream(fileName)));

		try {
			String row = br.readLine();
			while (row != null) {
				return_value = return_value + row + "\n";
				row = br.readLine();
			}
			br.close();
		} catch (Exception ex) {
			// e.printStackTrace();
			br.close();
			throw ex;
		}

		return return_value;
	}

	// write the current input to a txt file
	public static void writeFile(String str, String fileName) throws Exception {
		FileWriter outfile = new FileWriter(fileName);
		BufferedWriter buff = new BufferedWriter(outfile);

		try {
			buff.write(str);
			buff.close();
			outfile.close();
		} catch (Exception ex) {
			// e.printStackTrace();
			buff.close();
			outfile.close();
			throw ex;
		}
	}

	// replace old string with new string
	public static String replaceStr(String inputStr, String oldString,
			String newString) throws Exception {
		if (inputStr == null) {
			return null;
		}

		if (oldString == null || oldString.equals("") || newString == null) { // V5.1
			return inputStr;
		}

		int i = 0;

		if ((i = inputStr.indexOf(oldString, i)) >= 0) {
			char[] inputStr2 = inputStr.toCharArray();
			char[] newString2 = newString.toCharArray();
			int oLength = oldString.length();
			StringBuffer buf = new StringBuffer(inputStr2.length);
			buf.append(inputStr2, 0, i).append(newString2);
			i += oLength;
			int j = i;
			while ((i = inputStr.indexOf(oldString, i)) > 0) {
				buf.append(inputStr2, j, i - j).append(newString2);
				i += oLength;
				j = i;
			}
			buf.append(inputStr2, j, inputStr2.length - j);
			return buf.toString();
		}

		return inputStr;
	}

	// wrap a text into a string array
	// from http://progcookbook.blogspot.com/2006/02/text-wrapping-function-for-java.html
	public static String wrapText(String text, int len) throws Exception {
		// return empty array for null text
		if (text == null)
			return "";								// v6.1.2

		// return text if len is zero or less
		if (len <= 0)
			return text;							// v6.1.2

		// return text if less than length
		if (text.length() <= len)
			return text;							// v6.1.2
		
		text = replaceStr(text, "\r\n", "\n");		// v6.1.2
		text = replaceStr(text, "\t", " ");			// v6.1.2

		char[] chars = text.toCharArray();
		Vector<String> lines = new Vector<String>();
		StringBuffer line = new StringBuffer();
		StringBuffer word = new StringBuffer();

		for (int i = 0; i < chars.length; i++) {
			word.append(chars[i]);

			if (chars[i] == ' ') {
				if ((line.length() + word.length()) > len) {
					lines.add(line.toString());
					line.delete(0, line.length());
				}

				line.append(word);
				word.delete(0, word.length());
			}
		}

		// handle any extra chars in current word
		if (word.length() > 0) {
			if ((line.length() + word.length()) > len) {
				lines.add(line.toString());
				line.delete(0, line.length());
			}
			line.append(word);
		}

		// handle extra line
		if (line.length() > 0) {
			lines.add(line.toString());
		}

		String[] words_array = new String[lines.size()];
		int c = 0; // counter
		for (Enumeration<String> e = lines.elements(); e.hasMoreElements(); c++) {
			words_array[c] = (String) e.nextElement();
		}
		
		String new_text = "";									// v6.1.2
		for (int i = 0; i < words_array.length; i++) {			// v6.1.2
			new_text += words_array[i];							// v6.1.2
			if(i < words_array.length - 1) {					// v6.1.2
				new_text += "\n";								// v6.1.2
			}													// v6.1.2
		}														// v6.1.2

		return new_text;										// v6.1.2
	}
	
	// generate the JCL file - v6.1.2
	public static void generateJcl(String outputFileName, 
			String[][] globalParam, String ... jclParts) throws Exception {
		// merge the JCL parts
		String jcl = "";
		
		// verify the JCL parts
		for (int i = 0; i < jclParts.length - 1; i++) {
			// get the last char
			if (!jclParts[i].isEmpty() && jclParts[i].charAt(jclParts[i].length() - 1) != '\n') {
				jclParts[i] += "\n";
			}
			
			jcl += jclParts[i];
		}
		
		jcl += jclParts[jclParts.length-1];
		
		// replace date & time
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy"); // v6.0.phase1
		String mDateTime = formatter.format(cal.getTime());
		jcl = replaceStr(jcl, "#DATE#", mDateTime);
		formatter = new SimpleDateFormat("HH:mm:ss");
		mDateTime = formatter.format(cal.getTime());
		jcl = replaceStr(jcl, "#TIME#", mDateTime);
		
		// replace the parameters
		for (int i = 0; i < globalParam.length; i++) {
			jcl = replaceStr(jcl, globalParam[i][0], globalParam[i][1]);
		}
		
		// write to output
		writeFile(jcl, outputFileName);
		
	}
}
