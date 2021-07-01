package Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import org.apache.commons.lang3.StringUtils;

public class ChatClient {
    
    private final String serverName;
    private final int serverPort;
    private Socket socket;
    private InputStream serverIn;
    private OutputStream serverOut;
    private BufferedReader bufferedIn;
    
    private ArrayList<UserStatusInterface> userStatusListener = new ArrayList<>();
    private ArrayList<messageListener> messageListener = new ArrayList<>();
    
    ChatClient(String serverName, int serverPort)
    {
        this.serverName = serverName;
        this.serverPort = serverPort;
    }
    
    public static void main(String ar[]) throws IOException
    {
        ChatClient client = new ChatClient("localhost",8081);        //   connecting to  server
       
        client.addUserStatusListener(new UserStatusInterface() {        // print who is online and offline
            @Override
            public void online(String login) {
                System.out.println("Online: " + login);
            }

            @Override
            public void offline(String login) {
                System.out.println("Offline: " + login);
            }
            
        });
        
        client.addMessageListener(new messageListener(){                    // send mesage to particular user
            
            @Override
            public void onMessage(String fromLogin, String msgBody)
            {
                System.out.println("You got a message from " + fromLogin + " ===> " + msgBody);
            }
            
        });
        
        if(!client.connect())
        {
            System.out.println("Connection failed");
        }
        else
        {
            System.out.println("Connected successfully");
                      
            if(client.login("guest","guest"))
            {
                System.out.println("Login successfull");
                
                client.msg("abc" , "Hello World!");
            }
            else
            {
                System.out.println("Login failed");
            }
           // client.logoff();
        }
    }
    
    public boolean login(String user, String password) throws IOException               // sends login credentials to server
    {
        String cmd = "login " + user + " " + password +  "\n";     //  sending login credentials to server
        serverOut.write(cmd.getBytes());

        String response = bufferedIn.readLine();
        System.out.println("Response  Line: " + response);        // getting its response
        
        if("ok login".equalsIgnoreCase(response))
        {
            startMessageReader();
            return true;
        }
        else
        {
            return false;
        }
        
    }
    
    public void  logoff() throws IOException
    {
        String cmd = "logout\n";     //  sending logoff credentials to server
        serverOut.write(cmd.getBytes());
    }
    
    public boolean connect()               // coonecting to the server
    {
        try
        {
            this.socket = new Socket(serverName,serverPort);
            System.out.println("Client pport is " + socket.getLocalPort());
            this.serverOut = socket.getOutputStream();
            this.serverIn = socket.getInputStream();
            this.bufferedIn = new BufferedReader(new InputStreamReader(serverIn));
            return true;
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        
        return false;
        
    }
    
    public void addUserStatusListener(UserStatusInterface listener)    //   add  person that comes online
    {
        userStatusListener.add(listener);
    }
    
    public void removeUserStatusListener(UserStatusInterface listener)   // remove  offline  person
    {
        userStatusListener.remove(listener);
    }
    
    public void addMessageListener(messageListener listener)           // add  message listener
    {
        messageListener.add(listener);
    }
    
    public void  removeMessageListener(messageListener listener)     // remove  message listener
    {
        messageListener.remove(listener);
    }

    public void startMessageReader() {             //  creating thread  to read messages
        
        Thread t = new Thread(){
            @Override
            public void run()
            {
                 readMessageLoop();  
            }
        };
        
        t.start();
        
    }
    
    public void readMessageLoop()      // method in thread used to read online and offline messages
    {
        try
        {
        String line;
        while((line = bufferedIn.readLine()) != null)
        {
            String[] token = StringUtils.split(line);
            if(token != null && token.length > 0)
            {
                String cmd = token[0];
                if("Online:".equalsIgnoreCase(cmd))
                {
                    handleOnline(token);
                }
                else if("Offline:".equalsIgnoreCase(cmd))
                {
                    handleOffline(token);
                }
                else if("msg".equalsIgnoreCase(cmd))
                {
                    String[] tokenMsg = StringUtils.split(line,null,3);
                    handleMessage(tokenMsg);
                }
            }
        }
        }
        catch(Exception e)
        {
            e.printStackTrace();
            try
            {
                socket.close();
            }
            catch(IOException ex)
            {
                ex.printStackTrace();
            }
        }
    }

    public void handleOnline(String[] token) {         // send username of person online
        
        String login = token[1];
        for(UserStatusInterface listener : userStatusListener)
        {
            listener.online(login);
        }
        
    }

    public void handleOffline(String[] token) {                //  send username of person offline
        
         String login = token[1];
        for(UserStatusInterface listener : userStatusListener)
        {
            listener.offline(login);
        }
        
    }

    public void msg(String sendTo, String msgBody) throws IOException {   // format in which msg will be sent
        String cmd = "msg " + sendTo + " " + msgBody + "\n";
        serverOut.write(cmd.getBytes());
    }

    public void handleMessage(String[] token) {          // handle incoming messages
        
        String login = token[1];
        String msgBody = token[2];
        
        for(messageListener listener : messageListener)
        {
            listener.onMessage(login, msgBody);
        }
        
    }
    
}
