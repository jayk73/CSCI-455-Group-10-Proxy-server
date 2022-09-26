package Proxy;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;



public class ProxyServer {

	//cache is a Map: the key is the URL and the value is the file name of the file that stores the cached content
	Map<String, String> cache;
	
	ServerSocket proxySocket;

	String logFileName = "log.txt";

	public static void main(String[] args) {
		new ProxyServer().startServer(Integer.parseInt(args[0]));
	}

	void startServer(int proxyPort) {

		cache = new ConcurrentHashMap<>();

		// create the directory to store cached files. 
		File cacheDir = new File("cached");
		if (!cacheDir.exists() || (cacheDir.exists() && !cacheDir.isDirectory())) {
			cacheDir.mkdirs();
		}

		/**
			 * To do:
			 * create a serverSocket to listen on the port (proxyPort)
			 * Create a thread (RequestHandler) for each new client connection 
			 * remember to catch Exceptions!
			 *
		*/
 
                
            //create a serverSocket to listen on the port
            try{
                proxySocket = new ServerSocket(proxyPort);
                DataOutputStream os;
                DataInputStream is;
                
                while(true){
                    try{
                        Socket clientSocket = proxySocket.accept();
                        is = new DataInputStream (clientSocket.getInputStream());
                        os = new DataOutputStream(clientSocket.getOutputStream());
                        
                        //Request handler 
                        RequestHandler myClient=new RequestHandler(clientSocket, this);
                        myClient.start();
                        
                        
                    }catch (IOException e){
                        
                    }
                       
                }
            }catch(IOException e){
                e.printStackTrace();
            }
            
            
        }



	public String getCache(String hashcode) {
		return cache.get(hashcode);
	}

	public void putCache(String hashcode, String fileName) {
		cache.put(hashcode, fileName);
	}

	public synchronized void writeLog(String info) {
            try{
                FileWriter log = new FileWriter("proxy.log");
                String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
                
                log.write(timestamp);
                log.close();
            }
            catch(IOException e){
                
            }
            
            
            

			/**
			 * To do
			 * write string (info) to the log file, and add the current time stamp 
			 * e.g. String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
			 *
			*/
            
                       
                        
	}

}