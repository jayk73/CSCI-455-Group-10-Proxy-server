package Proxy;

import java.io.BufferedReader;
import java.util.logging.FileHandler;
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

	String logFileName = "proxy.log.txt";
        FileWriter logFileWriter;
        BufferedWriter bw;
        

	public static void main(String[] args) {
		new ProxyServer().startServer(1234);
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
                System.out.println("starting server");
            try {
                
                DataOutputStream os;
                DataInputStream is;
                proxySocket = new ServerSocket (1234);

                while(true){
                    Socket client = proxySocket.accept();
                    is = new DataInputStream (client.getInputStream());
                    os = new DataOutputStream(client.getOutputStream());
                    
                    RequestHandler newHandler = new RequestHandler(client, this);
                    System.out.println("created new thread");
                    newHandler.run();
                    
                    //change this
                    //String line = is.readLine();
                    //os.writeBytes("Hello\n");
                    is.close();
                    os.close();
                    proxySocket.close();
                    client.close();
                }
                
            } 
            catch (IOException ex) {
		
            }
        }



	public String getCache(String hashcode) {
		return cache.get(hashcode);
	}

	public void putCache(String hashcode, String fileName) {
		cache.put(hashcode, fileName);
	}

	public synchronized void writeLog(String info) throws IOException {

			/**
			 * To do
			 * write string (info) to the log file, and add the current time stamp 
			 * e.g. String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
			 *
			*/
                        
                        System.out.println("writing log");
                       
                        logFileWriter = new FileWriter(logFileName, true);
                        bw = new BufferedWriter(logFileWriter);
                        String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
                        
                        bw.write( timeStamp + " "+ info + "\n");
                        bw.newLine();
                        bw.close();
                        System.out.println(timeStamp + " "+ info );
                        //System.out.println("finished writing log");
                                              
	}

}