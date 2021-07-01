package Client;

import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class UserListPane extends JPanel implements UserStatusInterface{
    
    private final ChatClient client;
    private JList<String> userListUi;
    private DefaultListModel<String> userlistModel;
    
    public UserListPane(ChatClient client)
    {
        this.client = client;
        this.client.addUserStatusListener(this);     // adding  user listener
        
        userlistModel = new DefaultListModel<>();     // listmodel
        userListUi = new JList<>(userlistModel);       // list
        setLayout(new BorderLayout());
        add(new JScrollPane(userListUi),BorderLayout.CENTER);
        
        userListUi.addMouseListener(new MouseAdapter(){
            @Override
            public void mouseClicked(MouseEvent e)
            {
                if(e.getClickCount() > 1)
                {
                    String login = userListUi.getSelectedValue();
                    MessagePane messagePane = new MessagePane(client,login);
                    
                    JFrame f = new JFrame("Message: " + login);
                    f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                    f.setSize(500,500);
                    f.getContentPane().add(messagePane,BorderLayout.CENTER);
                    f.setVisible(true);
                }
            }
        });        
    }
    
//    public static void main(String ar[]) throws IOException
//    {
//       ChatClient client = new ChatClient("localhost",8081);
//        
//       UserListPane userListPane = new UserListPane(client);
//       JFrame frame = new JFrame("User List");
//       frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        frame.setSize(200,200);
//        
//        frame.getContentPane().add(userListPane,BorderLayout.CENTER);
//        frame.setVisible(true);
//        
//        if(client.connect())     // connecting with server
//        {
//                client.login("guest", "guest");
//        }
//        
//    }

    @Override
    public void online(String login) {
       userlistModel.addElement(login);
    }

    @Override
    public void offline(String login) {
        userlistModel.removeElement(login);
    }
    
}
