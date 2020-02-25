import java.io.*;
import java.util.*;
import java.net.*;

public class ClientThread extends Thread
{
   
   private ChatClient  ct_client; // ChatClient ��ü
   private Socket ct_sock; // Ŭ���̾�Ʈ ����
   private DataInputStream ct_in; // �Է� ��Ʈ��
   private DataOutputStream ct_out; // ��� ��Ʈ��
   private StringBuffer ct_buffer; // ����
   private Thread thisThread;
   private DisplayRoom room;
   private String roomname;
   public String displayid;
   public String whisperid = null;

   private static final String SEPARATOR = "|";
   private static final String DELIMETER = "`";

   // �޽��� ��Ŷ �ڵ� �� ������ ����

   // ������ �����ϴ� �޽��� �ڵ�
   private static final int REQ_LOGON = 1001;
   private static final int REQ_CREATEROOM = 1010;
   private static final int REQ_ENTERROOM = 1011;
   private static final int REQ_UESRLIST = 1012;
   private static final int REQ_SENDWORDS = 1021;
   private static final int REQ_WHISPER = 1022;
   private static final int REQ_LOGOUT = 1031;
   private static final int REQ_QUITROOM = 1041;

   // �����κ��� ���۵Ǵ� �޽��� �ڵ�
   private static final int YES_LOGON = 2001;
   private static final int NO_LOGON = 2002;
   private static final int YES_CREATEROOM = 2009;
   private static final int NO_CREATEROOM = 2010;
   private static final int YES_ENTERROOM = 2011;
   private static final int NO_ENTERROOM = 2012;
   private static final int MDY_USERID = 2013;
   private static final int MDY_WAITUSERS = 2014;
   private static final int MDY_CHATUSERS = 2015;
   private static final int MDY_ROOMUSERS = 2016;
   private static final int MDY_WAITROOMS = 2017;
   private static final int WAITROOM = 2018;
   private static final int CHATROOM = 2019;
   private static final int YES_SENDWORDS = 2021;
   private static final int NO_SENDWORDS = 2022;
   private static final int YES_WHISPERSEND = 2023;
   private static final int NO_WHISPERSEND = 2024;
   private static final int YES_LOGOUT = 2031;
   private static final int NO_LOGOUT = 2032;
   private static final int YES_QUITROOM = 2041;
   private static final int YES_USERLIST = 2042;

   // ���� �޽��� �ڵ�
   private static final int MSG_ALREADYUSER = 3001;
   private static final int MSG_SERVERFULL = 3002;
   private static final int MSG_CANNOTOPEN = 3011;

   private static MessageBox msgBox, logonbox;

   /* ����ȣ��Ʈ�� ������ ���� ������
          ���� : java ChatClient ȣ��Ʈ�̸� ��Ʈ��ȣ 
   	  To DO .....				*/

   // ����ȣ��Ʈ���� ����ϱ� ���Ͽ� ���� ������
   // ������ Ŭ���̾�Ʈ�� ���� �ý����� ����Ѵ�. 
   public ClientThread(ChatClient client) {
      try{
         ct_sock = new Socket(InetAddress.getLocalHost(), 2777);
         ct_in = new DataInputStream(ct_sock.getInputStream());
         ct_out = new DataOutputStream(ct_sock.getOutputStream());
         ct_buffer = new StringBuffer(4096);
         thisThread = this;
         ct_client = client; // ��ü������ �Ҵ�
      }catch(IOException e){
         MessageBoxLess msgout = new MessageBoxLess(client, "���ῡ��", "������ ������ �� �����ϴ�.");
         msgout.show();
      }
   }

   public void run(){

      try{
         Thread currThread = Thread.currentThread();
         while(currThread == thisThread){ // ����� LOG_OFF���� thisThread=null;�� ���Ͽ�
            String recvData = ct_in.readUTF();
            System.out.println(recvData);
            StringTokenizer st = new StringTokenizer(recvData, SEPARATOR);
            int command = Integer.parseInt(st.nextToken());
            switch(command){

               // �α׿� ���� �޽���  PACKET : YES_LOGON|�α��νð�|ID1`ID2`ID3...
               case YES_LOGON:{
                  logonbox.dispose();   
                  ct_client.cc_tfStatus.setText("�α׿��� �����߽��ϴ�.");
                  String date = st.nextToken(); // �α��� �ð�
                  ct_client.cc_tfDate.setText(date);
                  String ids = st.nextToken(); // ��ȭ�� ������ ����Ʈ
                  StringTokenizer users = new StringTokenizer(ids, DELIMETER);
                  while(users.hasMoreTokens()){
                     ct_client.cc_lstMember.add(users.nextToken());
                  }
                  break;
               }
               
            // �α׿� ���� �Ǵ� �α׿��ϰ� ��ȭ���� �������� ���� ����
               // PACKET : NO_LOGON|errCode
               case NO_LOGON:{
                  int errcode = Integer.parseInt(st.nextToken());             
                  if(errcode == MSG_ALREADYUSER){
                     logonbox.dispose();
                     msgBox = new MessageBox(ct_client, "�α׿�", "�̹� �ٸ� ����ڰ� �ֽ��ϴ�.");
                     ct_client.cc_tfLogon.setEditable(true);
                     ct_client.cc_btLogon.setEnabled(true);
                     ct_client.cc_roomEnter.setEnabled(false);
                     ct_client.cc_btLogout.setEnabled(false);
                     ct_client.cc_roomcreate.setEnabled(false);
                     msgBox.show();
                  }else if(errcode == MSG_SERVERFULL){
                     logonbox.dispose();
                     msgBox = new MessageBox(ct_client, "�α׿�", "��ȭ���� �����Դϴ�.");
                     msgBox.show();
                     ct_client.cc_tfLogon.setEditable(true);
                     ct_client.cc_btLogon.setEnabled(true);
                     ct_client.cc_roomEnter.setEnabled(false);
                     ct_client.cc_btLogout.setEnabled(false);
                     ct_client.cc_roomcreate.setEnabled(false);
                   
                  }
                  break;
               }
               
               // ���ǿ� �ִ� ��������� ����.
               case MDY_WAITUSERS: {
            	   String ids = st.nextToken();
            	   StringTokenizer users = new StringTokenizer(ids, DELIMETER);
            	   ct_client.cc_lstMember.removeAll();
            	   while(users.hasMoreTokens()) {
            		  ct_client.cc_lstMember.add(users.nextToken());            		
            	   }
            	   break;
               }
               
               // ä�ù濡 �ִ� ��������� ����.
               case MDY_ROOMUSERS : {           	   
            	   String ids = st.nextToken();
            	   StringTokenizer roomusers = new StringTokenizer(ids, DELIMETER);           	 
            	   room.dr_lstMember.removeAll();
            	   while(roomusers.hasMoreTokens()) {
            		   room.dr_lstMember.add(roomusers.nextToken());
            	   }
            	   break;
               }
               
               // ���ǿ� �ִ� �븮��Ʈ�� ����.
               case MDY_WAITROOMS : {
            	   String roomlist = st.nextToken();
            	   StringTokenizer roomlists = new StringTokenizer(roomlist, DELIMETER);
            	   ct_client.cc_roomlist.removeAll();
            	   while(roomlists.hasMoreTokens()) {
            		  ct_client.cc_roomlist.add(roomlists.nextToken());            		
            	   }
            	   break;
               }
               
               

               // ��ȭ�� ���� ���� �޽���  PACKET : YES_CREATEROOM|ROOMID|ROOMLIST
               case YES_CREATEROOM:{
            	  roomname = st.nextToken();
            	  String roomlist = st.nextToken();            	 
            	  StringTokenizer roomlists = new StringTokenizer(roomlist, DELIMETER);
            	  while(roomlists.hasMoreTokens()){
                     ct_client.cc_roomlist.add(roomlists.nextToken());
                  }                 
            	  ct_client.dispose(); // �α׿� â�� 
                  room = new DisplayRoom(this, roomname);
                  room.pack();
                  room.show(); // ��ȭ�� â�� ����Ѵ�.
                  break;
               }
               
               // ��ȭ�� ���� ���� �޽���  PACKET : YES_ENTERROOM|ID|ROOMNAME
               case YES_ENTERROOM:{
            	   roomname = st.nextToken();
                   ct_client.dispose(); // �α׿� â�� �����.
                   room = new DisplayRoom(this, roomname);
                   room.pack();
                   room.show(); // ��ȭ�� â�� ����Ѵ�.
                   break;
                }

               // ��ȭ�� ���� �� ���� ���� �޽���  PACKET : NO_ENTERROOM|errCode
               case NO_ENTERROOM:{
                  int roomerrcode = Integer.parseInt(st.nextToken());
                  if(roomerrcode == MSG_CANNOTOPEN){
                     msgBox = new MessageBox(ct_client, "��ȭ������", "�α׿µ� ����ڰ� �ƴմϴ�.");
                     msgBox.show();
                  }   
                  break;
               }

               // ��ȭ�濡 ������ ����� ����Ʈ�� ���׷��̵� �Ѵ�.
               // PACKET : MDY_USERIDS|id
               case MDY_USERID:{
                  room.dr_lstMember.removeAll(); // ��� ID�� �����Ѵ�.
                  String id = st.nextToken(); // ��ȭ�� ������ ����Ʈ           
                  room.dr_lstMember.add(id);                
                  break;
               }
               
               // ��ȭ�濡 ������ ����Ʈ�� �޾ƿɴϴ�.
               // PACKET : YES_USERLIST|CHATUSERS
               case YES_USERLIST : {
            	   String chatusers = st.nextToken();
            	   msgBox = new MessageBox(ct_client, "��ȭ�� ��������Ʈ", "���� ��ȭ�濡 �ִ� ������ "+chatusers+"�Դϴ�.");
                   msgBox.show();  
                   break;
               }

               // ���� �޽��� ���  PACKET : YES_SENDWORDS|ID|��ȭ��
               case YES_SENDWORDS:{
                  String id = st.nextToken(); // ��ȭ�� �������� ID�� ���Ѵ�.
                  try{
                     String data = st.nextToken();
                     room.dr_taContents.append(id+" : "+data+"\n");
                  }catch(NoSuchElementException e){}
                  room.dr_tfInput.setText(""); // ��ȭ�� �Է� �ʵ带 �����.
                  break;
               }
               
               // �ӼӸ� ���� �޽��� ��� PACKET : YES_WHISPERSEND|ID|WID|��ȭ��
               case YES_WHISPERSEND : {
            	   String id = st.nextToken();
            	   try {
            		   String wid = st.nextToken();
            		   String whisperwords = st.nextToken();
            		   room.dr_taContents.append(id + " -> " + wid + " : " + whisperwords + "\r\n");
            	   }
            	   catch(NoSuchElementException e) {
            		   System.out.println("���� �� ���Ҵ� �����ϴ�.");
            	   }
            	   room.dr_tfInput.setText("");
            	   whisperid = null;
            	   break;
               }
               
               // �ӼӸ��� ������ �������� ��������� ��Ŷ PACKET : NO_WHISPERSEND|�����޽���
               case NO_WHISPERSEND : {
            	   String words = st.nextToken();
            	   room.dr_taContents.append(words + "\r\n");
            	   room.dr_tfInput.setText("");
            	   break;
               }

               // LOGOUT �޽��� ó�� 
               // PACKET : YES_LOGOUT ���� ����.
               // ��� ������ ���������� �α׾ƿ� ��  YES_LOGOUT|Ż����id|zero�� ���� ��쿡�� ����Ʈ Ŭ����  
               case YES_LOGOUT:{
            	   ct_client.cc_tfStatus.setText("�α׾ƿ��� �����߽��ϴ�.");                   
                   String id = st.nextToken();                   
            	   String ids = st.nextToken(); // ��ȭ�� ������ ����Ʈ
                   if(ids.equals("zero")) {
                	   ct_client.cc_lstMember.removeAll();
                	   ct_client.cc_roomlist.removeAll();
                   }
                   else {
                	   StringTokenizer users = new StringTokenizer(ids, DELIMETER);
                	   ct_client.cc_lstMember.removeAll();
                	   ct_client.cc_roomlist.removeAll();
                   }
                   break;        
               }

               // ��� �޽���(YES_QUITROOM) ó�� PACKET : YES_QUITROOM|EXIST OR YES_QUITROOM|ZERO        
               case YES_QUITROOM:{
            	  String state = st.nextToken();
            	  if(state.equals("EXIST")) {
            		  room.dispose();
            		  ct_client.pack();
            		  ct_client.show();
            	  }
            	  else {
            		  ct_client.cc_roomlist.removeAll();
            		  room.dispose();
            		  ct_client.pack();
            		  ct_client.show();
            	  }
                  break;
               }

            } // switch ����

            Thread.sleep(200);

         } // while ����(������ ����)


      }catch(InterruptedException e){
         System.out.println(e);
         try {
			release();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

      }catch(IOException e){
         System.out.println(e);
         try {
			release();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
      }
   }

   // ��Ʈ��ũ �ڿ��� �����Ѵ�.
   public void release() throws IOException{
	   ct_sock.close();
	   ct_in.close();
	   ct_out.close();
   };

   // Logon ��Ŷ(REQ_LOGON|ID)�� �����ϰ� �����Ѵ�.
   public void requestLogon(String id) {
      try{
         logonbox = new MessageBox(ct_client, "�α׿�", "������ �α׿� ���Դϴ�.");
         logonbox.show();
         ct_buffer.setLength(0);   // Logon ��Ŷ�� �����Ѵ�.
         ct_buffer.append(REQ_LOGON);
         ct_buffer.append(SEPARATOR);
         ct_buffer.append(id);
         displayid = id; 
         send(ct_buffer.toString());   // Logon ��Ŷ�� �����Ѵ�.
      }catch(IOException e){
         System.out.println(e);
      }
   }
   
   // Logout��Ŷ(REQ_LOGOUT|ID)�� �����ϰ� �����Ѵ�.
   public void requestLogout(String id) {
	   try {
		   ct_buffer.setLength(0);
		   ct_buffer.append(REQ_LOGOUT);
		   ct_buffer.append(SEPARATOR);
		   ct_buffer.append(id);
		   send(ct_buffer.toString());
	   }
	   catch(IOException e) {
		   System.out.println(e);
	   }
   }

   // CreateRoom ��Ŷ(REQ_CREATEROOM|ID)�� �����ϰ� �����Ѵ�.
   public void requestCreateRoom(String id) {
	   try {
		   ct_buffer.setLength(0);
		   ct_buffer.append(REQ_CREATEROOM);
		   ct_buffer.append(SEPARATOR);
		   ct_buffer.append(id);
		   send(ct_buffer.toString());
	   }
	   catch(IOException e) {
		   System.out.println(e);
	   }
   }
   
   // EnterRoom ��Ŷ(REQ_ENTERROOM|ID|id.rooms)�� �����ϰ� �����Ѵ�.
   public void requestEnterRoom(String id, String idroom) {
      try{
         
    	 ct_buffer.setLength(0);   // EnterRoom ��Ŷ�� �����Ѵ�.
         ct_buffer.append(REQ_ENTERROOM);
         ct_buffer.append(SEPARATOR);
         ct_buffer.append(id);
         ct_buffer.append(SEPARATOR);
         ct_buffer.append(idroom);
         send(ct_buffer.toString());   // EnterRoom ��Ŷ�� �����Ѵ�.
      }catch(IOException e){
         System.out.println(e);
      }
   }
   
   // �濡 ������ ����� ��û�ϴ� ��Ŷ�� �����մϴ�. PACKET : (REQ_ALREADYUSER|ROOMID)
   public void requestAlreadyUsers(String roomid) {
	   try{	         
		   ct_buffer.setLength(0);   // EnterRoom ��Ŷ�� �����Ѵ�.
	       ct_buffer.append(REQ_UESRLIST);
	       ct_buffer.append(SEPARATOR);
	       ct_buffer.append(roomid);
	       send(ct_buffer.toString());   // EnterRoom ��Ŷ�� �����Ѵ�.
	   }catch(IOException e){
	       System.out.println(e);
	   }
   }   

   // QuitRoom ��Ŷ(REQ_QUITROOM|ID)�� �����ϰ� �����Ѵ�.
   public void requestQuitRoom(String id) {
	   try {
		   ct_buffer.setLength(0);
		   ct_buffer.append(REQ_QUITROOM);
		   ct_buffer.append(SEPARATOR);
		   ct_buffer.append(id);
		   send(ct_buffer.toString());
	   }
	   catch(IOException e) {
		   System.out.println(e);
	   }	   		
   }
   
   // SendWords ��Ŷ(REQ_SENDWORDS|ID|��ȭ��)�� �����ϰ� �����Ѵ�.
   public void requestSendWords(String words) {
      try{
         ct_buffer.setLength(0);   // SendWords ��Ŷ�� �����Ѵ�.
         ct_buffer.append(REQ_SENDWORDS);
         ct_buffer.append(SEPARATOR);
         ct_buffer.append(ct_client.msg_logon);
         ct_buffer.append(SEPARATOR);
         ct_buffer.append(words);
         send(ct_buffer.toString());   // SendWords ��Ŷ�� �����Ѵ�.
      }catch(IOException e){
         System.out.println(e);
      }
   }
   
   // Whisper ��Ŷ(REQ_WHISPER|ID|WID|��ȭ��)�� �����ϰ� �����Ѵ�.  
   public void requestWhisper(String wid, String whisperwords) {
	   try {
		   ct_buffer.setLength(0);
		   ct_buffer.append(REQ_WHISPER);
		   ct_buffer.append(SEPARATOR);
		   ct_buffer.append(ct_client.msg_logon);
		   ct_buffer.append(SEPARATOR);
		   ct_buffer.append(wid);
		   ct_buffer.append(SEPARATOR);
		   ct_buffer.append(whisperwords);
		   send(ct_buffer.toString());
	   }
	   catch(IOException e) {
		   System.out.println(e);
	   }
   }
   
   // Ŭ���̾�Ʈ���� �޽����� �����Ѵ�.
   private void send(String sendData) throws IOException {
      ct_out.writeUTF(sendData);
      ct_out.flush();
   }
}
