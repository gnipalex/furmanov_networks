package com.networks.hnyp.lab1;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client {
	public static Scanner scanner = new Scanner(System.in);

	private String host;
	private int port;

	private Client(String host, int port) {
		this.host = host;
		this.port = port;
	}

	public void run() throws IOException {
		Socket socket = new Socket(host, port);
		try {
			BufferedReader bufReader = new BufferedReader(
					new InputStreamReader(socket.getInputStream()));
			BufferedWriter bufWriter = new BufferedWriter(
					new OutputStreamWriter(socket.getOutputStream()));

			String response = bufReader.readLine();
			System.out.println("server --> " + response);
			String input = scanner.nextLine();
			bufWriter.write(input);
			bufWriter.newLine();
			bufWriter.flush();
			response = bufReader.readLine();
			System.out.println("server --> " + response);
		} finally {
			socket.close();
		}
	}

	public static void main(String[] args) {
		String host;
		int port = 0;

		System.out.println("This is client, it can speak to server");
		System.out.print("Please enter server host : ");
		host = scanner.nextLine();

		System.out.print("Please enter server port : ");
		String input = scanner.nextLine();
		try {
			port = Integer.parseInt(input);
		} catch (NumberFormatException e) {
			System.out.println("error, port must be number");
			return;
		}
		Client client = new Client(host, port);
		do {
			try {
				client.run();
			} catch (IOException e) {
				System.out.println("###connection error###");
			}
			System.out.println("--type not empty line to terminate--");
		} while (scanner.nextLine().isEmpty());
	}
}
