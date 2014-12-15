package com.networks.hnyp.lab2;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author Tetiana_Mokhnenko
 * @version 1.0
 */
public class ServerClient {

	public static void main(String[] args) {
		Server server;
		System.out.println("This is server");
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

		boolean restart = false;
		do {
			try {
				System.out.print("Please enter free port :");
				server = new Server(Integer.parseInt(reader.readLine()));
				server.start();
			} catch (IOException e) {
				System.err.println(e.getMessage());
				restart = true;
			}
		} while (restart);
	}

	public static class Server extends Thread {
		private ServerSocket serverSocket;
		private int clientNumber = 1;

		public Server(int port) {
			try {
				serverSocket = new ServerSocket(port);
			} catch (IOException e) {
				System.err.println(e.getMessage());
			}
		}

		@Override
		public void run() {
			try {
				Socket socket = serverSocket.accept();
				createNewSession(socket);
			} catch (IOException e) {
				System.out.println(e.getMessage());
			}
		}

		public void createNewSession(Socket socket) {
			SessionThread newSessionThread = new SessionThread(socket);
			newSessionThread.start();
		}

		private class SessionThread extends Thread {
			private Socket socket;
			public SessionThread(Socket socket) {
				this.socket = socket;
			}
			int currentClientNumber;

			@Override
			public void run() {
				boolean restart = true;
				do {
					try {
						DataInputStream in = new DataInputStream(socket.getInputStream());
						DataOutputStream out = new DataOutputStream(socket.getOutputStream());
						String data = in.readUTF();
						currentClientNumber = clientNumber++;
						System.out.println("I got new client (#" + currentClientNumber + ")!");
						while (true) {
							if (data.indexOf(Const.HEARTBEAT_REQUEST) == 0) {
								out.writeUTF(Const.HEARTBEAT_RESPONSE + "@");
							} else {

								String dataResponse = data.substring(data.indexOf("@") + 1);
								System.out.println("The client #" + currentClientNumber
										+ " sent me a message: " + dataResponse);
								System.out.println("I'll send him an answer...");
								out.writeUTF("I got your message: '" + dataResponse + "'");
								System.out.println("Waiting for the next message...");
								System.out.println();
							}
							out.flush();
							data = in.readUTF();
						}
					} catch (IOException e) {
						System.out.println(e.getMessage());
						restart = false;
					}
				} while (restart);
			}
		}
	}

}
