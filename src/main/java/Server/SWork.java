
package Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;

public class SWork extends Thread {                // thread class which contains different values for different users
    
    private final Socket clientSocket;
    private String login;
    private final Serverr server;
    private OutputStream outStream;
    private InputStream inStream;
    private HashSet<String> topicSet = new HashSet<>();         // stores list of topic person is  part of
    
    SWork(Serverr server,Socket socket)
    {
        clientSocket = socket;
        this.server = server;
    }
    
    public void run()
    {
        try 
        { 
            handleClient();    
        } 
        catch (IOException e) {
            if(e.getMessage().equalsIgnoreCase("Connection reset by peer")){
                System.out.println("Client disconnected..Waiting for another connection");
            }
            else if(e.getMessage().equalsIgnoreCase("Connection reset")){
                System.out.println("Client disconnected..Waiting for another connection");
            }
            else{
                e.printStackTrace();
            }
        } 
        catch (InterruptedException ex) {
            Logger.getLogger(SWork.class.getName()).log(Level.SEVERE, null, ex);
        }
     
    }
    
    private void handleClient() throws IOException, InterruptedException
    {
        
         this.outStream = clientSocket.getOutputStream();
         this.inStream = clientSocket.getInputStream();
         
         BufferedReader br = new BufferedReader(new InputStreamReader(inStream));
         String line;
         while((line = br.readLine()) != null)
         {
             String token[] = StringUtils.split(line);    // to  split one line into parts according to space
             System.out.println(line);
             if(token != null && token.length != 0)
             {
                    String cmd = token[0];              // checking what user want login or quit
                    if("logout".equalsIgnoreCase(cmd) || "quit".equalsIgnoreCase(cmd))         
                    {
                        logoffClient();
                        break;
                    }
                    else if(cmd.equals("login"))
                    {
                        loginClient(outStream, token);
                    }
                    else if(cmd.equals("msg"))
                    {
                        String tokens[] = StringUtils.split(line,null,3);
                        handleMessage(tokens);
                    }
                    else if(cmd.equals("join"))
                    {
                        handleGroup(token);
                    }
                    else if(cmd.equals("leave"))
                    {
                        handleLeave(token);
                    }
                    else
                    {
                    outStream.write(("Unknown " + cmd + "\n").getBytes());
                    }
             }
         }
               
         clientSocket.close();                
    }
    
    public String getLogin()       //it will return the login value of working user thread 
    {
        return login;
    }

    private void loginClient(OutputStream outStream, String[] token) throws IOException
    {
        System.out.println("token " + token.length);
        if(token.length == 3)
        {
            String user = token[1];
            String password = token[2];
            
            if((user.equals("guest") && password.equals("guest")) || (user.equals("abc") && user.equals("abc")))
            {
                outStream.write("ok login\n".getBytes());
                outStream.write(("User logged in successfully " + user + "\n").getBytes());
                login = user;               // login storing online user names 
                  
                List<SWork> workerList = server.getWorkerList();
                for(SWork sw : workerList)        // sending current user all other online login
                {
                    if(sw.getLogin() != null)                   //cheking whether particular user is not equals to null
                    {
                        if(!login.equals(sw.getLogin()))            // checking whether user status should not be printed in his console itself
                        {
                            String msg = "Online: " + sw.getLogin() + "\n";
                            send(msg);
                        }
                    }
                }
                
                String onmsg = "Online: " + login + "\n";
                for(SWork sw : workerList)      // all other online users current user status
                {
                    if(sw.getLogin() != null)                   //cheking whether particular user is not equals to null
                    {
                        if(!login.equals(sw.getLogin()))           // checking whether user status should not be printed in his console itself
                        {
                                sw.send(onmsg);
                        }
                    }
                }
            }
            else
            {
                outStream.write("error login".getBytes());
                outStream.write(("error login" + login).getBytes());
            }
            
        }
        
    }

    private void send(String msg) throws IOException {         // sending msg on user console
        outStream.write(msg.getBytes());
    }

    private void logoffClient() throws IOException 
    {
        server.removeWorker(this);                        // use to remove logoff user from list because otherwise it will give socket close error  on last user logout because for printing msg of offline server it wont find any output stream to write as nothing  is present 
        List<SWork> workerList = server.getWorkerList();
        String onmsg = "Offline: " + login + "\n";
        for(SWork sw : workerList)      // all other online users current user status
        {
            if(!login.equals(sw.getLogin()))           // checking whether user status should not be printed in his console itself
            {
                sw.send(onmsg);
            }
        } 
    }

    private void handleMessage(String[] tokens) throws IOException {      // send msg to particular person
        
        String sendto = tokens[1];       //person to send
        String body = tokens[2];         // msg  to send
        
        boolean isTopic = sendto.charAt(0) == '#';
        
        List<SWork> worker = server.getWorkerList();
        for(SWork sw : worker)
        {
            if(isTopic)
            {
                if(sw.memberOfTopic(sendto))            // send group message    receive=msg <topic> : <name of person sending> body......  send= msg topic body......
                {
                    String outmsg = "msg " + sendto + ":" + login + " " + body + "\n";
                    sw.send(outmsg);
                }
            }
            if(sendto.equalsIgnoreCase(sw.getLogin()))          // send one  to one msg
            {
                String outmsg = "msg " + login + " " + body + "\n";                  // send=msg <username> body........   recieve = msg <sending person name> body.... 
                sw.send(outmsg);
            }
        }
        
        
    }
    
    public boolean memberOfTopic(String topic)        //  checks whether user is member or not
    {
        return topicSet.contains(topic);
    }

    private void handleGroup(String token[]) {                  // add member to the group
        
        if(token.length>1)
        {
            String topic = token[1];
            topicSet.add(topic);                            // adding topic to a particular user set
        }
        
    }

    private void handleLeave(String[] token) {
        
        if(token.length>1)
        {
            String topic = token[1];
            topicSet.remove(topic);                            // removing topic to a particular user set
        }
        
    }
  
}
