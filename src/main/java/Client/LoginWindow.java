package Client;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class LoginWindow extends JFrame {
    
    
    JTextField loginField = new JTextField();
    JPasswordField passwordField = new JPasswordField();
    JButton loginButton = new JButton("Login");
    private final ChatClient client;
    
    public LoginWindow()
    {
        super("Login");
        
        this.client = new ChatClient("localhost",8081);
        client.connect();
        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS));
        p.add(loginField);
        p.add(passwordField);
        p.add(loginButton);
        
        loginButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e)
            {
                try {
                    doLogin();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
        
        getContentPane().add(p,BorderLayout.CENTER);
        pack();
        setVisible(true);
    }
    
    public void doLogin() throws IOException
    {
        String login = loginField.getText();
        String password = passwordField.getText();
        
        if(client.login(login,password))
        {
            UserListPane userListPane = new UserListPane(client);
            JFrame frame = new JFrame("User List");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(200,200);

            frame.getContentPane().add(userListPane,BorderLayout.CENTER);
            frame.setVisible(true);
        
            setVisible(false);
        }
        else
        {
            JOptionPane.showMessageDialog(this,"Invalid Username or Password");
        }
    }
    
    public static void main(String ar[])
    {
        LoginWindow loginWindow = new LoginWindow();
        loginWindow.setVisible(true);
    }
    
}
