package Proxy;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.SecureRandom;


// RequestHandler is thread that process requests of one client connection
public class RequestHandler extends Thread {

	
	Socket clientSocket;

        InputStream inFromClient;

	OutputStream outToClient;
        
        BufferedWriter proxyToClient;
	
	byte[] request = new byte[1024];
        
        FileWriter writer;
        BufferedWriter bw;
	
	private ProxyServer server;


	public RequestHandler(Socket clientSocket, ProxyServer proxyServer) {

		
		this.clientSocket = clientSocket;
		

		this.server = proxyServer;

		try {
			clientSocket.setSoTimeout(2000);
			inFromClient = clientSocket.getInputStream();
			outToClient = clientSocket.getOutputStream();
                        proxyToClient = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));

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
                    
                    inFromClient.read(request);
//                    for(int i = 0; i < request.length; i ++){
//                        System.out.print(request[i] + " " );
//                    }
                    
                    String requestString = new String(request, StandardCharsets.UTF_8);
                    String[] URL = requestString.split("\n");
                    requestString = URL[0];
                    
                    String URLString = new String(request, StandardCharsets.UTF_8);
                    String[] log = URLString.split(" HTTP/1.1");
                    URLString = log[0];
                    URLString = URLString.substring(4);
                    
                    System.out.println("request: " + requestString);
                    
                    //write log Browser IP URL 
                    server.writeLog(clientSocket.getInetAddress().getHostAddress() + " " + URLString);

                    //if cahced respond with chached content
                    if(server.getCache(requestString.substring(4))!=null){
                        System.out.println("sending cached content to client");
                        sendCachedInfoToClient(server.getCache(requestString));
                    }
                    else{
                        //if not call proxyServerToClient to process GET request
                        //might have to change the request var
                        proxyServertoClient(request);
                        }
                    inFromClient.close();
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
                    
                    
                    //seperate URL from request
                    String URLString = new String(clientRequest, StandardCharsets.UTF_8);
                    String[] URL = URLString.split(" HTTP");
                    URLString = URL[0];
                    URLString = URLString.substring(4);
                    System.out.println("URL: " + URLString);
                    
                    URL remoteURL = new URL(URLString);
                    try ( //send client request use clientRequest
                            Socket toWebServerSocket = new Socket(remoteURL.getHost(), 80)) {
                        //System.out.println("Past Socket");
                        
                        OutputStream outToWebServer;
                        InputStream inFromWebServer;
                        
                        
                        outToWebServer = (toWebServerSocket.getOutputStream());
                        outToWebServer.write(clientRequest);
                        outToWebServer.flush();
                        
                        inFromWebServer = (toWebServerSocket.getInputStream());
                        
                        BufferedReader reader = new BufferedReader(new InputStreamReader(inFromWebServer));
                        String line;
                        //loop for response
                        //send to client
                        System.out.println("Sending response to client");
                        
                        inFromWebServer.transferTo(outToClient);
                        outToClient.write(serverReply);
//                        while((line = reader.readLine()) != null){
//                            proxyToClient.write(line);
//                            //System.out.println("stuck");
//                            System.out.println(line);
//                        }
                        //System.out.println(line);
                        proxyToClient.flush();
                        
                        //write response to cache file
                        File newCacheFile = new File(fileName);
                        writer = new FileWriter(fileName);
                        bw = new BufferedWriter(writer);
                        System.out.println("writing to cache");
                        bw.write(serverReply.toString());
                        server.putCache(URLString, fileName);
                        //close file/socket
                        writer.close();
                        outToWebServer.close();
                        inFromWebServer.close();
                        bw.close();
                        //close file
                        //newCacheFile.close();
                    }
                    
                    
                } catch(IOException ex){
                    
                }
                
	}      
	
	
	
	// Sends the cached content stored in the cache file to the client
	private void sendCachedInfoToClient(String fileName) {

		try {

			byte[] bytes = Files.readAllBytes(Paths.get(fileName));

			outToClient.write(bytes);
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