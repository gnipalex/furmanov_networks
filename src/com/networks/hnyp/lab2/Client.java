package com.networks.hnyp.lab2;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {
	public static Scanner scanner = new Scanner(System.in);

	private String host;
	private int port;
    private Socket socket;
    private BufferedReader bufReader;
    private BufferedWriter bufWriter;
    private  boolean responded = false;
    private static final String HEARTBEAT_REQUEST = "Heartbeat ping request!";
    private static final String HEARTBEAT_RESPONSE = "Heartbeat ping response!";
    private static final String USER_DATA = "USER_DATA";
    private static final String SEPARATOR = "@";


    private Client(String host, int port) {
		this.host = host;
		this.port = port;
	}

	public void run() throws IOException {
		socket = new Socket(host, port);
		try {
			bufReader = new BufferedReader(
					new InputStreamReader(socket.getInputStream()));
			bufWriter = new BufferedWriter(
					new OutputStreamWriter(socket.getOutputStream()));

            Pulsation pulsation = new Pulsation();
            pulsation.setDaemon(true);
            pulsation.start();
            String response = bufReader.readLine();
			responded = true;
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

    private class Pulsation extends Thread{
        @Override
        public void run() {
            try {
                while (true) {
                    if (!socket.isClosed()) {
                        bufWriter.write(HEARTBEAT_REQUEST + SEPARATOR);
                        sleep(5000);
                        if (!responded) {
                            System.err.println("Heartbeat error!");
                            socket.close();
                        } else {
                            responded = false;
                        }
                    }
                }
            } catch (Exception e) {

                System.err.println(e.getMessage());
            }
        }
    }
}
