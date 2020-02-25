import java.awt.*;
import java.awt.event.*;

public class ChatClient extends Frame implements ActionListener, MouseListener
{
   
   public TextField cc_tfLogon; // �α׿� �Է� �ؽ�Ʈ �ʵ�
   public Button cc_btLogon; // �α׿� ���� ��ư
   public Button cc_roomEnter; // ��ȭ�� ���� �� ���� ��ư
   public Button cc_btLogout; // �α׾ƿ� ��ư
   public Button cc_roomcreate; // �氳�� ��ư 

   public TextField cc_tfStatus; // �α׿� ���� �ȳ�
   public TextField cc_tfDate; // �����ð�
   public List cc_lstMember; // ��ȭ�� ������
   public List cc_roomlist; // ������ ��ȭ�� ���

   public static ClientThread cc_thread;
   public static ChatClient client;
   public String msg_logon="";
   public String cc_roomid = null;
   
   public ChatClient(String str){
      super(str);
      setLayout(new BorderLayout());

      // �α׿�, ��ȭ�� ���� �� ���� ��ư�� �����Ѵ�.
      Panel bt_panel = new Panel();
      bt_panel.setLayout(new FlowLayout());
      cc_btLogon = new Button("�α׿½���");
      cc_btLogon.addActionListener(this);
      bt_panel.add(cc_btLogon);
      
      cc_tfLogon = new TextField(10);
      bt_panel.add(cc_tfLogon);
            
      cc_btLogout = new Button("�α׾ƿ�");
      cc_btLogout.addActionListener(this);
      cc_btLogout.setEnabled(false);
      bt_panel.add(cc_btLogout);
      
      cc_roomEnter = new Button("��ȭ������");
      cc_roomEnter.addActionListener(this);
      cc_roomEnter.setEnabled(false);
      bt_panel.add(cc_roomEnter);
      
      cc_roomcreate = new Button("��ȭ�氳��");
      cc_roomcreate.addActionListener(this);
      cc_roomcreate.setEnabled(false);
      bt_panel.add(cc_roomcreate);
      
      add("Center", bt_panel);

      // 4���� Panel ��ü�� ����Ͽ� ��ȭ�� ������ ����Ѵ�.
      Panel roompanel = new Panel(); // 3���� �г��� ���� �гΰ�ü
      roompanel.setLayout(new BorderLayout());

      Panel northpanel = new Panel();
      northpanel.setLayout(new FlowLayout());
      cc_tfStatus = new TextField("�ϴ��� �ؽ�Ʈ �ʵ忡  ID�� �Է��Ͻʽÿ�,",60); 
      													// ��ȭ���� �������� �˸�
      cc_tfStatus.setEditable(false);
      northpanel.add(cc_tfStatus);
      
      Panel centerpanel = new Panel();
      centerpanel.setLayout(new FlowLayout());
      centerpanel.add(new Label("�α׿� �ð� : "));
      cc_tfDate = new TextField("�α��� �ð��� ǥ�õ˴ϴ�.", 47);
      cc_tfDate.setEditable(false);
      centerpanel.add(cc_tfDate);

      Panel southpanel = new Panel();
      southpanel.setLayout(new FlowLayout());
      southpanel.add(new Label("�α׿� �����"));
      cc_lstMember = new List(10);
      southpanel.add(cc_lstMember);
      southpanel.add(new Label("������ ��ȭ��"));
      cc_roomlist = new List(10);
      cc_roomlist.addMouseListener(this);
      southpanel.add(cc_roomlist);

      roompanel.add("North", northpanel);
      roompanel.add("Center", centerpanel);
      roompanel.add("South", southpanel);
      add("North", roompanel);
      pack();

      // �α׿� �ؽ�Ʈ �ʵ忡 ��Ŀ���� ���ߴ� �޼ҵ� �߰�
      addWindowListener(new WindowAdapter( ) {
     	 public void windowOpened(WindowEvent e) {
     		cc_tfLogon.requestFocus();
     	 }
       });

      addWindowListener(new WinListener());
   }

   class WinListener extends WindowAdapter
   {
      public void windowClosing(WindowEvent we){
    	 if(!msg_logon.equals(""))
    		 cc_thread.requestLogout(msg_logon);
         System.exit(0); // ���߿� �α׾ƿ���ƾ���� ����
      }
   }

   // �α׿�, ��ȭ�� ���� �� ���� ��ư ���� �̺�Ʈ�� ó���Ѵ�.
   public void actionPerformed(ActionEvent ae){
      Button b = (Button)ae.getSource();
      if(b.getLabel().equals("�α׿½���")) {
         // �α׿� ó�� ��ƾ
         msg_logon = cc_tfLogon.getText(); // �α׿� ID�� �д´�.
         if(!msg_logon.equals("")){
            cc_thread.requestLogon(msg_logon); // ClientThread�� �޼ҵ带 ȣ��
            cc_tfLogon.setEditable(false);
            cc_btLogon.setEnabled(false);
            cc_roomEnter.setEnabled(true);
            cc_btLogout.setEnabled(true);
            cc_roomcreate.setEnabled(true);
         }else{
            MessageBox msgBox = new  MessageBox(this, "�α׿�", "�α׿� id�� �Է��ϼ���.");
            msgBox.show();
         }
      }
      else if(b.getLabel().equals("��ȭ������")) {
         // ��ȭ�� ���� �� ���� ó�� ��ƾ
         msg_logon = cc_tfLogon.getText(); // �α׿� ID�� �д´�.
         if(cc_roomid != null) {
        	 if(!msg_logon.equals("")){
        		 cc_thread.requestEnterRoom(msg_logon, cc_roomid); // ClientThread�� �޼ҵ带 ȣ��
        		 cc_roomid=null;
        	 }else{
        		 MessageBox msgBox = new MessageBox(this, "�α׿�", "�α׿��� ���� �Ͻʽÿ�.");
        		 msgBox.show();
           	 }
         }
         else {
        	 MessageBox nullmsgBox = new MessageBox(this, "���¹�",  "���� ������ ���̾����ϴ�.");
        	 nullmsgBox.show();
         }
      }
      else if(b.getLabel().equals("�α׾ƿ�")) {
      // �α׾ƿ� ó�� ��ƾ
    	  msg_logon = cc_tfLogon.getText();
    	  if(!msg_logon.equals("")) {
    		  cc_thread.requestLogout(msg_logon);
    		  cc_tfLogon.setEditable(true);
    		  cc_btLogon.setEnabled(true);
    		  cc_roomEnter.setEnabled(false);
              cc_btLogout.setEnabled(false);
              cc_roomcreate.setEnabled(false);
    		  msg_logon="";
    	  }
    	  else {
    		  MessageBox msgBox = new MessageBox(this, "����α׾ƿ�", "���� �α����� �������� �����Դϴ�.");
    		  msgBox.show();
    	  }
      }
      else if(b.getLabel().equals("��ȭ�氳��")) {
      // ��ȭ�� ������ƾ
    	  msg_logon = cc_tfLogon.getText();
    	  if(!msg_logon.equals("")) {
    		  cc_thread.requestCreateRoom(msg_logon);
    	  }
      }
   }

   public static void main(String args[]) {
      client = new ChatClient("��ȭ�� ���� �� ����");
      client.setSize(350, 400);
      client.setVisible(true);
      client.pack();

      // ������ �����ϰ� ������ ����� �����带 ȣ���Ѵ�.
      
      // ������ Ŭ���̾�Ʈ�� �ٸ� �ý������� ����ϴ� ���
      // ���� : java ChatClient [ȣ��Ʈ�̸��� ��Ʈ��ȣ�� �ʿ��ϴ�.]
      // To DO
      
      // ������ Ŭ���̾�Ʈ�� ���� �ý������� ����ϴ� ���
      // ���� : java ChatClient [ȣ��Ʈ�̸��� ��Ʈ��ȣ�� �ʿ����.]
      try{
         cc_thread = new ClientThread(client); // ���� ȣ��Ʈ�� ������
         cc_thread.start(); // Ŭ���̾�Ʈ�� �����带 �����Ѵ�.
      }catch(Exception e){
         System.out.println(e);
      }
   }

@Override
public void mouseClicked(MouseEvent arg0) {
	cc_roomid = cc_roomlist.getSelectedItem();
	if(cc_roomid != null) {
		cc_thread.requestAlreadyUsers(cc_roomid);
	}
	System.out.println(cc_roomid);	
}

@Override
public void mouseEntered(MouseEvent e) {
	// TODO Auto-generated method stub
	
}

@Override
public void mouseExited(MouseEvent e) {
	// TODO Auto-generated method stub
	
}

@Override
public void mousePressed(MouseEvent e) {
	
	
}

@Override
public void mouseReleased(MouseEvent e) {
	// TODO Auto-generated method stub
	
}
}
