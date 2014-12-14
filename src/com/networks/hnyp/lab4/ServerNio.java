package com.networks.hnyp.lab4;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Scanner;

public class ServerNio {
	public static final int DEFAULT_TIMEOUT = 200;
	private int port;
	private ServerSocketChannel serverSocketChannel;
	private Selector selector;

	public ServerNio(int port) {
		this.port = port;
	}

	public synchronized void start() throws IOException {
		if (serverSocketChannel != null) {
			return;
		}
		selector = Selector.open();
		serverSocketChannel = ServerSocketChannel
				.open();
		serverSocketChannel.configureBlocking(false);
		serverSocketChannel.bind(new InetSocketAddress(port));
		serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
		Thread runner = new Thread(new ServerRunner());
		runner.setDaemon(true);
		runner.start();
		System.err.println("server started at " + InetAddress.getLocalHost() + ":" + port);
	}

	public synchronized void stop() {
		if (serverSocketChannel != null) {
			try {
				serverSocketChannel.close();
				serverSocketChannel = null;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private class ServerRunner implements Runnable {

		@Override
		public void run() {
			try {
				while (serverSocketChannel.isOpen()) {
					selector.select();
					Iterator<SelectionKey> selKeysIterator = selector.selectedKeys().iterator();
					while(selKeysIterator.hasNext()) {
						SelectionKey key = selKeysIterator.next();
						if (key.isAcceptable()) {
							SocketChannel socketChannel = serverSocketChannel.accept();
							if (socketChannel == null) {
								continue;
							}
							socketChannel.configureBlocking(false);
							socketChannel.register(selector, SelectionKey.OP_WRITE);
							System.out.println("#User connected " + socketChannel.getRemoteAddress());
							selKeysIterator.remove();
						} else if (key.isReadable()) {
							SelectableChannel selectableChannel = key.channel();//.read(byteBuffer);
                            if (selectableChannel instanceof SocketChannel) {
                            	SocketChannel socketChannel = (SocketChannel)selectableChannel;
                            	ByteBuffer byteBuffer = ByteBuffer.allocateDirect(512);
                            	StringBuilder inputStringBuilder = new StringBuilder();
                            	socketChannel.read(byteBuffer);
                            	byteBuffer.flip();
                            	while (byteBuffer.hasRemaining()) {
                            		char symbol = (char)byteBuffer.get();
                            		inputStringBuilder.append(symbol);
                            	}
                            	byteBuffer.compact();
                            	String response = "I see your '" + inputStringBuilder.toString().replaceAll("\r\n", "") + "'\n";
                            	byteBuffer.put(response.getBytes());
                            	byteBuffer.flip();
                            	socketChannel.write(byteBuffer);
                            	byteBuffer.flip();
                            	socketChannel.close();
                            	selKeysIterator.remove();
                            }  
						} else if (key.isWritable()) {
							SelectableChannel selectableChannel = key.channel();//.read(byteBuffer);
                            if (selectableChannel instanceof SocketChannel) {
                            	SocketChannel socketChannel = (SocketChannel)selectableChannel;
                            	ByteBuffer byteBuffer = ByteBuffer.allocateDirect(512);
                            	String response = "Hello user!! Say something\n";
                            	byteBuffer.put(response.getBytes());
                            	byteBuffer.flip();
                            	socketChannel.write(byteBuffer);
                            	byteBuffer.flip();
                            	socketChannel.register(selector, SelectionKey.OP_READ);
                            	selKeysIterator.remove();
                            }
						}
					}
				}
				
			} catch (IOException ex) {
				System.err.println("==SERVER IO ERROR==");
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
		ServerNio server = new ServerNio(port);
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
