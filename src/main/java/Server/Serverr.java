
package Server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Serverr extends Thread{
    
    private int serverport;
    
    private ArrayList<SWork> workerList = new ArrayList<>();
    
    Serverr(int serverport)
    {
        this.serverport = serverport;   
    }
    
    public List<SWork> getWorkerList()
    {
        return workerList;
    }
    
    @Override
    public void run()
    {
        try
        {
            
            ServerSocket serverSocket = new ServerSocket(serverport);
            while(true)
            {
                
                System.out.println("About to accept connections");
                Socket clientSocket = serverSocket.accept();
                System.out.println("Accepted connection from" + clientSocket);
                
                SWork serverWorker = new SWork(this,clientSocket);
                workerList.add(serverWorker);       // adding online user in the list
                serverWorker.start();    
            }
            
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    void removeWorker(SWork sw) {       // removing offline  user  from list
        workerList.remove(sw);
    }
    
}
