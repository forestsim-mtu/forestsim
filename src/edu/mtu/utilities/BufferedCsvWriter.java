package edu.mtu.utilities;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class BufferedCsvWriter {

	private BufferedWriter writer;
	
	/**
	 * Open the file for writing.
	 * 
	 * @param fileName The file name and path.
	 * @param append True if the file should be appended to, false otherwise.
	 */
	public BufferedCsvWriter(String fileName, boolean append) throws IOException {
		open(fileName, append);
	}
	
	/**
	 * Finalize the line, flush any buffered contents, and close the file.
	 */
	public void close() throws IOException {
		writer.newLine();
		writer.flush();
		writer.close();
	}
	
	/**
	 * Flushes the buffer to the file.
	 */
	public void flush() throws IOException {
		writer.flush();
	}
	
	/**
	 * Writes the system defined new line operator to the file.
	 */
	public void newLine() throws IOException {
		writer.newLine();
	}
	
	/**
	 * Open the file for writing.
	 * 
	 * @param fileName The file name and path.
	 * @param append True if the file should be appended to, false otherwise.
	 */
	public void open(String fileName, boolean append) throws IOException {
		FileWriter file = new FileWriter(fileName, append);
		writer = new BufferedWriter(file);
	}

	/**
	 * Write the indicated value to the file as a cell.
	 */
	public void write(int value) throws IOException {
		writer.write(value + ",");
	}
	
	/**
	 * Write the indicated value to the file as a cell.
	 */
	public void write(double value) throws IOException {
		writer.write(value + ",");
	}	
	
	/**
	 * Write the indicated value to the file as a cell, note that it is NOT escaped.
	 */
	public void write(String value) throws IOException {
		writer.write(value + ",");
	}
}
