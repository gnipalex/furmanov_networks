package com.networks.hnyp.lab1;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Random;
import java.util.Scanner;

public class Server {
	public static final int DEFAULT_TIMEOUT = 200;
	private int port;
	private ServerSocket serverSocket;

	private Server(int port) {
		this.port = port;
	}

	public synchronized void start() throws IOException {
		if (serverSocket != null) {
			return;
		}
		serverSocket = new ServerSocket(port);
		serverSocket.setSoTimeout(DEFAULT_TIMEOUT);
		System.err.println("==server started at "
				+ InetAddress.getLocalHost() + ":"
				+ serverSocket.getLocalPort() + "==");
		Thread runner = new Thread(new ServerRunner());
		runner.setDaemon(true);
		runner.start();
	}

	public synchronized void stop() {
		if (serverSocket != null) {
			try {
				serverSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private class ServerRunner implements Runnable {

		@Override
		public void run() {
			try {
				while (!serverSocket.isClosed()) {
					try {
						Socket socket = serverSocket.accept();
						Thread handler = new Thread(
								new RequestProcessor(socket));
						handler.start();
					} catch (SocketTimeoutException ex) {
						// do nothing
					}
				}
			} catch (IOException ex) {
				System.err.println("==server runner finished working==");
			}
		}
	}

	private class RequestProcessor implements Runnable {

		private Socket socket;

		private RequestProcessor(Socket socket) {
			this.socket = socket;
		}

		@Override
		public void run() {
			try {
				BufferedReader bufReader = new BufferedReader(
						new InputStreamReader(socket.getInputStream()));
				BufferedWriter bufWriter = new BufferedWriter(
						new OutputStreamWriter(socket.getOutputStream()));
				System.out.println(Thread.currentThread().getName()
						+ " New user connected --> "
						+ socket.getRemoteSocketAddress());
				bufWriter.write("Hello user, tell me something");
				bufWriter.newLine();
				bufWriter.flush();
				String response = bufReader.readLine();
				System.out.println(Thread.currentThread().getName() + "response from client --> " + response);
				bufWriter.write("I see your '" + response
						+ "', please take this random number "
						+ new Random().nextInt());
				bufWriter.newLine();
				bufWriter.flush();
			} catch (IOException ex) {
				System.err.println("##connection error##");
			} finally {
				try {
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);
		System.out.println("This is server");
		System.out.print("Please enter free port :");
		int port = 0;
		String input = scanner.nextLine();
		try {
			port = Integer.parseInt(input);
		} catch (NumberFormatException e) {
			System.out.println("error, port must be number!!");
			return;
		}
		Server server = new Server(port);
		try {
			server.start();
		} catch (IOException e) {
			System.out.println("error, failed to start server");
			return;
		}
		System.out.println("--type any string to stop server--");
		scanner.nextLine();
		server.stop();
	}

}
