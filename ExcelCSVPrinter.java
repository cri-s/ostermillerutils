/*
 * Write files in Excel comma separated value format.
 * Copyright (C) 2001,2002 Stephen Ostermiller
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

/**
 * Print values as a comma separated list that can be read by the
 * Excel spreadsheet.
 * More information about this class is available from <a target="_top" href=
 * "http://ostermiller.org/utils/ExcelCSV.html">ostermiller.org</a>.
 */
public class ExcelCSVPrinter implements CSVPrint {

	/**
	 * The place that the values get written.
	 */
	protected PrintWriter out;

	/**
	 * True iff we just began a new line.
	 */
	protected boolean newLine = true;

	/**
	 * Create a printer that will print values to the given
	 * stream.	 Character to byte conversion is done using
	 * the default character encoding.	Comments will be
	 * written using the default comment character '#'.
	 *
	 * @param out stream to which to print.
	 */
	public ExcelCSVPrinter (OutputStream out){
		this.out = new PrintWriter(out);
	}

	/**
	 * Create a printer that will print values to the given
	 * stream.	Comments will be
	 * written using the default comment character '#'.
	 *
	 * @param out stream to which to print.
	 */
	public ExcelCSVPrinter (Writer out){
		if (out instanceof PrintWriter){
			this.out = (PrintWriter)out;
		} else {
			this.out = new PrintWriter(out);
		}
	}

	/**
	 * Print the string as the last value on the line.	The value
	 * will be quoted if needed.
	 *
	 * @param value value to be outputted.
	 */
	public void println(String value){
		print(value);
		out.println();
		out.flush();
		newLine = true;
	}

	/**
	 * Output a blank line
	 */
	public void println(){
		out.println();
		out.flush();
		newLine = true;
	}

	/**
	 * Print a single line of comma separated values.
	 * The values will be quoted if needed.  Quotes and
	 * newLine characters will be escaped.
	 *
	 * @param values values to be outputted.
	 */
	public void println(String[] values){
		for (int i=0; i<values.length; i++){
			print(values[i]);
		}
		out.println();
		out.flush();
		newLine = true;
	}

	/**
	 * Print several lines of comma separated values.
	 * The values will be quoted if needed.  Quotes and
	 * newLine characters will be escaped.
	 *
	 * @param values values to be outputted.
	 */
	public void println(String[][] values){
		for (int i=0; i<values.length; i++){
			println(values[i]);
		}
		if (values.length == 0){
			out.println();
		}
		out.flush();
		newLine = true;
	}

	/**
	 * Since ExcelCSV format does not support comments,
	 * this method will ignore the comment and star
	 * a new row.
	 *
	 * @param comment the comment to output (ignored)
	 */
	public void printlnComment(String comment){
		println();
	}

	/**
	 * Print the string as the next value on the line.	The value
	 * will be quoted if needed.
	 *
	 * @param value value to be outputted.
	 */
	public void print(String value){
		boolean quote = false;
		if (value.length() > 0){
			for (int i=0; i<value.length(); i++){
				char c = value.charAt(i);
				if (c=='"' || c==',' || c=='\n' || c=='\r'){
					quote = true;
				}
			}
		} else if (newLine) {
			// always quote an empty token that is the firs
			// on the line, as it may be the only thing on the
			// line.  If it were not quoted in that case,
			// an empty line has no tokens.
			quote = true;
		}
		if (newLine){
			newLine = false;
		} else {
			out.print(",");
		}
		if (quote){
			out.print(escapeAndQuote(value));
		} else {
			out.print(value);
		}
		out.flush();
	}

	/**
	 * enclose the value in quotes and escape the quote
	 * and comma characters that are inside.
	 *
	 * @param value needs to be escaped and quoted
	 * @return the value, escaped and quoted.
	 */
	private static String escapeAndQuote(String value){
		String s = StringHelper.replace(value, "\"", "\"\"");
		return (new StringBuffer(2 + s.length())).append("\"").append(s).append("\"").toString();
	}

	/**
	 * Write some test data to the given file.
	 *
	 * @param args First argument is the file name.  System.out used if no filename given.
	 */
	private static void main(String[] args) {
		OutputStream out;
		try {
			if (args.length > 0){
				File f = new File(args[0]);
				if (!f.exists()){
					f.createNewFile();
					if (f.canWrite()){
						out = new FileOutputStream(f);
					} else {
						throw new IOException("Could not open " + args[0]);
					}
				} else {
					throw new IOException("File already exists: " + args[0]);
				}
			} else {
				out = System.out;
			}
			ExcelCSVPrinter p  = new ExcelCSVPrinter(out);
			p.print("unquoted");
			p.print("escaped\"quote");
			p.println("comma,comma");
			p.print("!quoted");
			p.print("!unquoted");
			p.print(" quoted");
			p.println("quoted ");
			p.print("one");
			p.print("");
			p.print("");
			p.print("");
			p.println("");
			p.println("two");
			p.print("\nthree\nline\n");
			p.println("\ttab");
		} catch (IOException e){
			System.out.println(e.getMessage());
		}
	}
}
