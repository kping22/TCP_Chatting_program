import java.awt.*;
import java.awt.event.*;

public class DisplayRoom extends Frame implements ActionListener, KeyListener, MouseListener
{
   
   private Button dr_btClear; // ��ȭ�� â ȭ�� �����
   private Button dr_btLogout; // �α׾ƿ� ���� ��ư
   private Label ID , idarea;  
   public TextArea dr_taContents; // ��ȭ�� ���� ����Ʈâ
   public List dr_lstMember; // ��ȭ�� ������

   public TextField dr_tfInput; // ��ȭ�� �Է��ʵ�

   public static ClientThread dr_thread;

   public DisplayRoom(ClientThread client, String title){
      super(title);
      setLayout(new BorderLayout());

      // ��ȭ�濡�� ����ϴ� ������Ʈ�� ��ġ�Ѵ�.
      Panel northpanel = new Panel();
      northpanel.setLayout(new FlowLayout());
      
      dr_btClear = new Button("ȭ�������"); 
      dr_btClear.addActionListener(this);
      northpanel.add(dr_btClear);
   
      dr_btLogout = new Button("����ϱ�");
      dr_btLogout.addActionListener(this);
      northpanel.add(dr_btLogout);

      Panel centerpanel = new Panel();
      centerpanel.setLayout(new FlowLayout());
      dr_taContents = new TextArea(10, 27);
      dr_taContents.setEditable(false);
      centerpanel.add(dr_taContents);
     
      dr_lstMember = new List(10);
      dr_lstMember.addMouseListener(this);
      centerpanel.add(dr_lstMember);

      Panel southpanel = new Panel();
      southpanel.setLayout(new FlowLayout());
      dr_tfInput = new TextField(41);
      dr_tfInput.addKeyListener(this);
      southpanel.add(dr_tfInput);

      add("North", northpanel);
      add("Center", centerpanel);
      add("South", southpanel);

      dr_thread = client; // ClientThread Ŭ������ �����Ѵ�.
      ID = new Label("ID : ");
      northpanel.add(ID);
      
      idarea = new Label(dr_thread.displayid);
      northpanel.add(idarea);

      // �Է� �ؽ�Ʈ �ʵ忡 ��Ŀ���� ���ߴ� �޼ҵ� 
      addWindowListener(new WindowAdapter( ) {
    	 public void windowOpened(WindowEvent e) {
    		 dr_tfInput.requestFocus();
    	 }
      });
            addWindowListener(new WinListener());
   }

   class WinListener extends WindowAdapter
   {
      public void windowClosing(WindowEvent we){
    	  dr_thread.requestQuitRoom(dr_thread.displayid);
      }
   }

   // ȭ�������, ����ϱ� �̺�Ʈ�� ó���Ѵ�.
   public void actionPerformed(ActionEvent ae){
      Button b = (Button)ae.getSource();
      if(b.getLabel().equals("ȭ�������")) {// ȭ������� ó�� ��ƾ
    	  dr_taContents.setText("");    
      }
      else if(b.getLabel().equals("����ϱ�")) {// ����ϱ� ó�� ��ƾ
    	  dr_thread.requestQuitRoom(dr_thread.displayid);
      }
   }

   // �Է��ʵ忡 �Է��� ��ȭ���� ������ �����Ѵ�.
   public void keyPressed(KeyEvent ke){
      if(ke.getKeyChar() == KeyEvent.VK_ENTER){
    	 if(dr_thread.whisperid != null) {
    		 String whisperwords = dr_tfInput.getText();
    		 dr_thread.requestWhisper(dr_thread.whisperid, whisperwords);
    	 }
    	 else {
    		 String words = dr_tfInput.getText(); // ��ȭ���� ���Ѵ�.
    		 dr_thread.requestSendWords(words); // ��ȭ���� ������ ����ڿ� �����Ѵ�.
    	 }
      }
   }

   public void keyReleased(KeyEvent ke){}
   public void keyTyped(KeyEvent ke){}

@Override
public void mouseClicked(MouseEvent arg0) {
	// TODO Auto-generated method stub
		dr_thread.whisperid = dr_lstMember.getSelectedItem();
		System.out.println(dr_thread.whisperid);
}

@Override
public void mouseEntered(MouseEvent arg0) {
	// TODO Auto-generated method stub
	
}

@Override
public void mouseExited(MouseEvent arg0) {
	// TODO Auto-generated method stub
	
}

@Override
public void mousePressed(MouseEvent e) {

}

@Override
public void mouseReleased(MouseEvent arg0) {
	// TODO Auto-generated method stub
	
}

}
