/*
 * Read files in Excel comma separated value format.
 * Copyright (C) 2001-2003 Stephen Ostermiller
 * http://ostermiller.org/contact.pl?regarding=Java+Utilities
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * See COPYING.TXT for details.
 */

package com.Ostermiller.util;
import java.io.*;
import java.util.*;

/**
 * Read files in comma separated value format as outputted by the Microsoft
 * Excel Spreadsheet program.
 * More information about this class is available from <a target="_top" href=
 * "http://ostermiller.org/utils/ExcelCSV.html">ostermiller.org</a>.
 * <P>
 * Excel CSV is a file format used as a portable representation of a database.
 * Each line is one entry or record and the fields in a record are separated by commas.
 * <P>
 * If field includes a comma or a new line, the whole field must be surrounded with double quotes.
 * When the field is in quotes, any quote literals must be escaped by two quotes ("").
 * Text that comes after quotes that have been closed but come before the next comma will be ignored.
 * <P>
 * Empty fields are returned as as String of length zero: "".  The following line has three empty
 * fields and three non-empty fields in it.  There is an empty field on each end, and one in the
 * middle.  One token is returned as a space.<br>
 * <pre>,second,, ,fifth,</pre>
 * <P>
 * Blank lines are always ignored.	Other lines will be ignored if they start with a
 * comment character as set by the setCommentStart() method.
 * <P>
 * An example of how CVSLexer might be used:
 * <pre>
 * ExcelCSVParser shredder = new ExcelCSVParser(System.in);
 * String t;
 * while ((t = shredder.nextValue()) != null) {
 *	   System.out.println("" + shredder.lastLineNumber() + " " + t);
 * }
 * </pre>
 * <P>
 * The CSV that Excel outputs differs from the
 * <a href="http://ostermiller.org/utils/CSVLexer.html">standard</a>
 * in several respects:
 * <ul><li>Leading and trailing whitespace is significant.</li>
 * <li>A backslash is not a special character and is not used to escape anything.</li>
 * <li>Quotes inside quoted strings are escaped with a double quote rather than a backslash.</li>
 * <li>Excel may convert data before putting it in CSV format:<ul>
 * <li>Tabs are converted to a single space.</li>
 * <li>New lines in the data are always represented as the UNIX new line. ("\n")</li>
 * <li>Numbers that are greater than 12 digits may be represented in truncated
 * scientific notation form.</li></ul>
 * This parser does not attempt to fix these excel conversions, but users should be aware
 * of them.</li></ul>
 *
 * @see com.Ostermiller.util.CSVParser
 */
public class ExcelCSVParser implements CSVParse {

	/**
	 * Does all the dirty work.
	 * Calls for new tokens are routed through
	 * this object.
	 */
	private ExcelCSVLexer lexer;

	/**
	 * Token cache.  Used for when we request a token
	 * from the lexer but can't return it because its
	 * on the next line.
	 */
	private String tokenCache;

	/**
	 * Line cache.	The line number that goes along with
	 * the tokenCache.	Not valid if the tokenCache is
	 * null.
	 */
	private int lineCache;

	/**
	 * The line number the last token came from, or -1 if
	 * no tokens have been returned.
	 */
	private int lastLine = -1;

	/**
	 * Create a parser to parse comma separated values from
	 * an InputStream.
	 *
	 * @param in stream that contains comma separated values.
	 */
	public ExcelCSVParser(java.io.InputStream in) {
		lexer = new ExcelCSVLexer(in);
	}

	/**
	 * Create a parser to parse comma separated values from
	 * a Reader.
	 *
	 * @param in reader that contains comma separated values.
	 */
	public ExcelCSVParser(java.io.Reader in) {
		lexer = new ExcelCSVLexer(in);
	}

	/**
	 * get the next value.
	 *
	 * @return the next value or null if there are no more values.
	 * @throws IOException if an error occurs while reading
	 */
	public String nextValue() throws IOException {
		if (tokenCache == null){
			tokenCache = lexer.getNextToken();
			lineCache = lexer.getLineNumber();
		}
		lastLine = lineCache;
		String result = tokenCache;
		tokenCache = null;
		return result;
	}

	/**
	 * Get the line number that the last token came from.
	 * <p>
	 * New line breaks that occur in the middle of a token are no
	 * counted in the line number count.
	 *
	 * @return line number or -1 if no tokens have been returned yet.
	 */
	public int lastLineNumber(){
		return lastLine;
	}

	/**
	 * Get all the values from a line.
	 * <p>
	 * If the line has already been partially read, only the
	 * values that have not already been read will be included.
	 *
	 * @return all the values from the line or null if there are no more values.
	 * @throws IOException if an error occurs while reading
	 */
	public String[] getLine() throws IOException{
		int lineNumber = -1;
		Vector v = new Vector();
		if (tokenCache != null){
			v.add(tokenCache);
			lineNumber = lineCache;
		}
		while ((tokenCache = lexer.getNextToken()) != null
				&& (lineNumber == -1 || lexer.getLineNumber() == lineNumber)) {
			v.add(tokenCache);
			lineNumber = lexer.getLineNumber();
		}
		if (v.size() == 0){
			return null;
		}
		lastLine = lineNumber;
		lineCache = lexer.getLineNumber();
		String[] result = new String[v.size()];
		return ((String[])v.toArray(result));
	}

	/**
	 * Get all the values from the file.
	 * <p>
	 * If the file has already been partially read, only the
	 * values that have not already been read will be included.
	 * <p>
	 * Each line of the file that has at least one value will be
	 * represented.  Comments and empty lines are ignored.
	 * <p>
	 * The resulting double array may be jagged.
	 *
	 * @return all the values from the file or null if there are no more values.
	 * @throws IOException if an error occurs while reading
	 */
	public String[][] getAllValues() throws IOException {
		Vector v = new Vector();
		String[] line;
		while((line = getLine()) != null){
			v.add(line);
		}
		if (v.size() == 0){
			return null;
		}
		String[][] result = new String[v.size()][];
		return ((String[][])v.toArray(result));
	}

	/**
	 * Set the characters that indicate a comment at the beginning of the line.
	 * For example if the string "#;!" were passed in, all of the following lines
	 * would be comments:<br>
	 * <pre> # Comment
	 * ; Another Comment
	 * ! Yet another comment</pre>
	 * By default there are no comments in CVS files.  Commas and quotes may not be
	 * used to indicate comment lines.
	 *
	 * @param commentDelims list of characters a comment line may start with.
	 */
	public void setCommentStart(String commentDelims){
		lexer.setCommentStart(commentDelims);
	}

	/**
	 * Get the number of the line from which the last value was retrieved.
	 *
	 * @return line number or -1 if no tokens have been returned.
	 */
	public int getLastLineNumber(){
		return lastLine;
	}

	/**
	 * Parse the given file for comma separated values and print the results
	 * to System.out.
	 *
	 * @param args First argument is the file name.  System.in used if no filename given.
	 */
	private static void main(String[] args) {
		InputStream in;
		try {
			if (args.length > 0){
				File f = new File(args[0]);
				if (f.exists()){
					if (f.canRead()){
						in = new FileInputStream(f);
					} else {
						throw new IOException("Could not open " + args[0]);
					}
				} else {
					throw new IOException("Could not find " + args[0]);
				}
			} else {
				in = System.in;
			}
			ExcelCSVParser p  = new ExcelCSVParser(in);
			String[] t;
			while ((t = p.getLine()) != null) {
				for (int i=0; i<t.length; i++){
					System.out.print('"' + t[i] + '"');
					if (i<t.length-1){
						System.out.print(", ");
					}
				}
				System.out.println();
			}
		} catch (IOException e){
			System.out.println(e.getMessage());
		}
	}

	/**
	 * Parse the comma delimited data from a string.
	 *
	 * @param s string with comma delimited data to parse.
	 */
	public static String[][] parse(String s){
		try {
			return (new ExcelCSVParser(new StringReader(s))).getAllValues();
		} catch (IOException x){
			return null;
		}
	}

	/**
	 * Parse the comma delimited data from a stream.
	 *
	 * @param in Reader with comma delimited data to parse.
	 * @throws IOException if an error occurs while reading
	 */
	public static String[][] parse(Reader in) throws IOException {
		return (new ExcelCSVParser(in)).getAllValues();
	}
}
