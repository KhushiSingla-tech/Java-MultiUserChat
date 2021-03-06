package Client;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

public class MessagePane extends JPanel implements messageListener {

    private final ChatClient client;
    private final String login;
    
    private DefaultListModel<String> listModel = new DefaultListModel<>();
    private JList<String> messageList = new JList<>(listModel);
    private JTextField inputField = new JTextField();

    MessagePane(ChatClient client, String login) 
    {
        this.client = client;
        this.login = login;
        
        client.addMessageListener(this);
        
        setLayout(new BorderLayout());
        add(new JScrollPane(messageList),BorderLayout.CENTER);
        add(inputField,BorderLayout.SOUTH);
        
        inputField.addActionListener(new ActionListener(){     // add text to list  model
            @Override
            public void actionPerformed(ActionEvent e)
            {
                String text = inputField.getText();
                try {
                    client.msg(login,text);
                    listModel.addElement("You : " + text);
                    inputField.setText("");
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onMessage(String fromLogin, String msgBody) {    // add recieved msg in list model
        if(login.equalsIgnoreCase(fromLogin))
        {
            String line = fromLogin + ":" + msgBody;
            listModel.addElement(line);
        }
    }
    
    
}
