package com.networks.hnyp.lab2;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * @author Tetiana_Mokhnenko
 * @version 1.0
 */
public class Client {
    static BufferedReader bufReader;

    public static void main(String[] args) {

        System.out.println("This is client, it can speak to server");
        ClientWorker clientWorker;
        bufReader = new BufferedReader(new InputStreamReader(System.in));

        int port;
        String address;

        boolean restart = false;

        do {
            try {

                System.out.print("Please enter server host : ");
                address = bufReader.readLine();
                System.out.print("Please enter server port : ");
                String input = bufReader.readLine();
                try {
                    port = Integer.parseInt(input);
                } catch (NumberFormatException e) {
                    System.out.println("error, port must be number");
                    return;
                }
                clientWorker = new ClientWorker(address, port);
                clientWorker.work();

            } catch (IOException e) {
                System.err.println(e.getMessage());
                restart = true;
            }

        } while (restart);
        try {
            bufReader.close();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    public static class ClientWorker {

        String host;
        int serverPort;
        Socket clientSocket;
        boolean responded = false;

        public ClientWorker(String host, int serverPort) {

            this.host = host;
            this.serverPort = serverPort;
        }

        public void work() throws IOException {
            clientSocket = new Socket(host, serverPort);
            try {
                System.out.println("Connection with the server is set.");

                final DataInputStream inputClientStream
                        = new DataInputStream(clientSocket.getInputStream());
                final DataOutputStream outputClientStream
                        = new DataOutputStream(clientSocket.getOutputStream());
                Pulsation pulsation = new Pulsation();
                pulsation.setDaemon(true);
                pulsation.start();

                new Thread() {
                    @Override
                    public void run() {
                        while (true) {
                            try {
                                if (!clientSocket.isClosed()) {
                                    String data = inputClientStream.readUTF();
                                    responded = true;
                                    if (data.indexOf(Const.HEARTBEAT_RESPONSE) != 0) {
                                        String dataResponse
                                                = data.substring(data.indexOf(Const.SEPARATOR) + 1);
                                        System.out.println(
                                                "The server sent me this: " + dataResponse);
                                    }
                                }
                            } catch (Exception e) {
                                System.out.println("Conn RESET");
                                System.err.println(e.getMessage());
                            }
                        }
                    }
                }.start();

                while (true) {

                    System.out.println(
                            "Type in something and press enter. We'll send it to the server:");
                    String userData = bufReader.readLine();
                    System.out.println("Sending this message to the server...");
                    outputClientStream.writeUTF(Const.USER_DATA + Const.SEPARATOR + userData);
                    outputClientStream.flush();
                }

            } catch (UnknownHostException e) {
                System.err.println("Unknown host " + host + ".");
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        }

        private class Pulsation extends Thread {
            @Override
            public void run() {
                try {
                    while (true) {
                        if (!clientSocket.isClosed()) {
                            DataOutputStream outputStream = new DataOutputStream(clientSocket.getOutputStream());
                            outputStream.writeUTF(
                                    Const.HEARTBEAT_REQUEST + Const.SEPARATOR);
                            sleep(5000);

                            if (!responded) {
                                System.err.println("Heartbeat error!");
                                clientSocket.close();
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
}
