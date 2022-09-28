package Proxy;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.SecureRandom;


// RequestHandler is thread that process requests of one client connection
public class RequestHandler extends Thread {

	
	Socket clientSocket;

	BufferedReader inFromClient;

	BufferedWriter outToClient;
	
	byte[] request = new byte[1024];
        
        FileWriter writer;
        BufferedWriter bw;
	
	private ProxyServer server;


	public RequestHandler(Socket clientSocket, ProxyServer proxyServer) {

		
		this.clientSocket = clientSocket;
		

		this.server = proxyServer;

		try {
			clientSocket.setSoTimeout(2000);
			inFromClient = new BufferedReader(new InputStreamReader (clientSocket.getInputStream()));
			outToClient = new BufferedWriter( new OutputStreamWriter (clientSocket.getOutputStream()));

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	
	@Override
	
	public void run() {

		/**
			 * To do
			 * Process the requests from a client. In particular, 
			 * (1) Check the request type, only process GET request and ignore others
                         * (2) Write log.
			 * (3) If the url of GET request has been cached, respond with cached content
			 * (4) Otherwise, call method proxyServertoClient to process the GET request
			 *
		*/
                
                //Check the request type, only process GET request and ignore others
                System.out.println("Processing GET");
                try{
                    String request = inFromClient.readLine();
                    System.out.println("request: " + request);
                    
                    //write log Browser IP URL 
                    server.writeLog(clientSocket.getInetAddress().getHostAddress() + " " + request.substring(4));

                    //if cahced respond with chached content
                    if(server.getCache(request)!=null){
                        System.out.println("sending cached content to client");
                        sendCachedInfoToClient(server.getCache(request));
                    }
                    else{
                        //if not call proxyServerToClient to process GET request
                        //might have to change the request var
                        proxyServertoClient(request.getBytes());
                        }
                    
                    }
                catch(Exception ex){
                    
                }
                
                

	}

	
	private void proxyServertoClient(byte[] clientRequest) {

		// Create Buffered output stream to write to cached copy of file
		String fileName = "cached/" + generateRandomFileName() + ".dat";
		
		// to handle binary content, byte is used
		byte[] serverReply = new byte[4096];
		
			
		/**
		 * To do
		 * (1) Create a socket to connect to the web server (default port 80)
		 * (2) Send client's request (clientRequest) to the web server, you may want to use flush() after writing.
		 * (3) Use a while loop to read all responses from web server and send back to client
		 * (4) Write the web server's response to a cache file, put the request URL and cache file name to the cache Map
		 * (5) close file, and sockets.
		*/
                
                System.out.println("Sending request to webserver");
		try{
                    //send client request use clientRequest
                    Socket toWebServerSocket = new Socket(clientRequest.toString(), 80);
                                  
                    PrintStream out = new PrintStream( toWebServerSocket.getOutputStream());
                    BufferedReader in = new BufferedReader( new InputStreamReader( toWebServerSocket.getInputStream() ) );
                    
                    out.println("GET" + clientRequest.toString() + " HTTP/1.0");
                    out.println();
                    
                    String response = in.readLine();
                    //loop for response
                    while(response != null){
                        response = in.readLine();
                    }
                    //send to client
                    System.out.println("Sending response to client");
                    serverReply = response.getBytes();
                    System.out.println(response);
                    
                    
                    //write response to cache file
                    
                    writer = new FileWriter(fileName);
                    bw = new BufferedWriter(writer);
                    server.putCache(clientRequest.toString(), fileName);
                    //close file/socket
                    toWebServerSocket.close();
                    writer.close();
                    
                    
                    
                } catch(IOException ex){
                    
                }
                
	}      
	
	
	
	// Sends the cached content stored in the cache file to the client
	private void sendCachedInfoToClient(String fileName) {

		try {

			byte[] bytes = Files.readAllBytes(Paths.get(fileName));

			outToClient.write(bytes.toString());
			outToClient.flush();

		} catch (Exception e) {
			e.printStackTrace();
		}

		try {

			if (clientSocket != null) {
				clientSocket.close();
			}

		} catch (Exception e) {
			e.printStackTrace();

		}
	}
	
	
	// Generates a random file name  
	public String generateRandomFileName() {

		String ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ-_";
		SecureRandom RANDOM = new SecureRandom();
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < 10; ++i) {
			sb.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
		}
		return sb.toString();
	}

}