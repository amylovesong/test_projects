package com.sun.rental;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class RentalTestDrive {

	public static void main(String[] args) {
		Customer customer = new Customer("Tom");

		Movie movieA = new Movie("aaa", Movie.NEW_RELEASE);
		Movie movieB = new Movie("bbb", Movie.REGULAR);
		Rental rentalA = new Rental(movieA, 3);
		customer.addRental(rentalA);
		Rental rentalB = new Rental(movieB, 5);
		customer.addRental(rentalB);

		String originResult = readOriginResult();

		String refactorString = customer.statement();
		System.out.println("originResult:\n" + originResult + "\n");
		System.out.println("refactorString:\n" + refactorString + "\n");

		if (refactorString.equals(originResult)) {
			System.out.println("Refactor Success~");
		} else {
			System.err.println("Refactor Fail!!");
		}
	}

	static void writeResult(String str) {
		File file = new File("result.txt");
		try {
			OutputStreamWriter writer = new OutputStreamWriter(
					new FileOutputStream(file));
			writer.write(str);
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	static String readOriginResult() {
		StringBuilder result = new StringBuilder();
		File file = new File("result.txt");
		try {
			char[] buffer = new char[256];
			InputStreamReader reader = new InputStreamReader(
					new FileInputStream(file));
			int len = reader.read(buffer);
			while (len != -1) {
				result.append(buffer);
				len = reader.read(buffer);
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return result.toString().trim();
	}

}
