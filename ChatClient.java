import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;

import java.awt.*;

import java.awt.event.*;

import javax.swing.*;

import javax.swing.border.*;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

/**
 * A simple Swing-based client for the chat server. Graphically it is a frame
 * with a text field for entering messages and a textarea to see the whole
 * dialog.
 *
 * The client follows the following Chat Protocol. When the server sends
 * "SUBMITNAME" the client replies with the desired screen name. The server will
 * keep sending "SUBMITNAME" requests as long as the client submits screen names
 * that are already in use. When the server sends a line beginning with
 * "NAMEACCEPTED" the client is now allowed to start sending the server
 * arbitrary strings to be broadcast to all chatters connected to the server.
 * When the server sends a line beginning with "MESSAGE" then all characters
 * following this string should be displayed in its message area.
 */
public class ChatClient {

    String serverAddress;
    Scanner in;
    PrintWriter out;
    JFrame frame = new JFrame("Chatter");
    JTextField textField = new JTextField(50);
    private JTextPane messageArea = new JTextPane();
    private JPanel topPanel = new JPanel();

    private int rows = 20;
    private int cols = 30;

    /**
     * Constructs the client by laying out the GUI and registering a listener with
     * the textfield so that pressing Return in the listener sends the textfield
     * contents to the server. Note however that the textfield is initially NOT
     * editable, and only becomes editable AFTER the client receives the
     * NAMEACCEPTED message from the server.
     */
    public ChatClient(String serverAddress) {
        this.serverAddress = serverAddress;

        textField.setEditable(false);
        messageArea.setEditable(false);
        messageArea.setMargin(new Insets(5, 5, 100, 5));
        messageArea.setBackground(Color.WHITE);

        topPanel.setLayout(new BorderLayout());
        topPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        topPanel.add(new JScrollPane(messageArea), BorderLayout.CENTER);

        frame.getContentPane().add(textField, BorderLayout.SOUTH);
        frame.getContentPane().add(topPanel, BorderLayout.CENTER);
        frame.pack();

        // Send on enter then clear to prepare for next message
        textField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                out.println(textField.getText());
                textField.setText("");
            }
        });
    }

    private String getName() {
        return JOptionPane.showInputDialog(frame, "Choose a screen name:", "Screen name selection",
                JOptionPane.PLAIN_MESSAGE);
    }

    public void appendToPane(JTextPane tp, String msg, Color c) throws BadLocationException
    {
        StyleContext sc = StyleContext.getDefaultStyleContext();
        AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, c);
        StyledDocument doc = tp.getStyledDocument();

        aset = sc.addAttribute(aset, StyleConstants.FontFamily, "Lucida Console");
        aset = sc.addAttribute(aset, StyleConstants.Alignment, StyleConstants.ALIGN_JUSTIFIED);

        int len = tp.getDocument().getLength();
        doc.insertString(len, msg, aset);
    }

    private void run() throws IOException, BadLocationException {
        try {
            var socket = new Socket(serverAddress, 59001);
            in = new Scanner(socket.getInputStream());
            out = new PrintWriter(socket.getOutputStream(), true);

            while (in.hasNextLine()) {
                var line = in.nextLine();
                if (line.startsWith("SUBMITNAME")) 
                {
                    out.println(getName());
                } 
                else if (line.startsWith("NAMEACCEPTED")) 
                {
                    this.frame.setTitle("Chatter - " + line.substring(13));
                    textField.setEditable(true);
                } 
                else if (line.startsWith("MESSAGE")) 
                {
                    //messageArea.append(line.substring(8) + "\n");
                    appendToPane(messageArea, line.substring(8) + "\n", Color.BLACK);
                }
                else if (line.startsWith("SYSTEM"))
                {
                    //colored hylighted message
                    //messageArea.append(line.substring(7) + "\n");
                    appendToPane(messageArea, line.substring(7) + "\n", Color.RED);

                }
            }
        } finally {
            frame.setVisible(false);
            frame.dispose();
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("Pass the server IP as the sole command line argument");
            return;
        }
        var client = new ChatClient(args[0]);
        client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        client.frame.setVisible(true);
        client.run();
    }
}