import java.io.*;
import java.net.*;
import java.util.*;

public class ServerThread extends Thread 
{
   private Socket st_sock;
   private DataInputStream st_in;
   private DataOutputStream st_out;
   private StringBuffer st_buffer;
   private String roomname="";
   /* �α׿µ� ����� ���� */
   private static Hashtable<String,ServerThread> logonHash; 
   private static Vector<String> logonVector;
   /* ��ȭ�� ������ ���� */
   private static Hashtable<String,ServerThread> roomHash; 
   private static Vector<String> roomVector;
   
   /* ��ȭ�� ����  */
   private static Vector<String> userRoomVector;

   private static final String SEPARATOR = "|"; // �޽����� ������
   private static final String DELIMETER = "`"; // �Ҹ޽����� ������
   private static final String COMMA = ",";
   private static Date starttime;  	// �α׿� �ð�

   public String st_ID; 			// ID ����

   // �޽��� ��Ŷ �ڵ� �� ������ ����

   // Ŭ���̾�Ʈ�κ��� ���޵Ǵ� �޽��� �ڵ�
   private static final int REQ_LOGON = 1001;
   private static final int REQ_CREATEROOM = 1010;
   private static final int REQ_ENTERROOM = 1011;
   private static final int REQ_UESRLIST = 1012;
   private static final int REQ_SENDWORDS = 1021;
   private static final int REQ_WHISPER = 1022;
   private static final int REQ_LOGOUT = 1031;
   private static final int REQ_QUITROOM = 1041;
   
   // Ŭ���̾�Ʈ�� �����ϴ� �޽��� �ڵ�
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


   static{	
      logonHash = new Hashtable<String,ServerThread>(ChatServer.cs_maxclient);
      logonVector = new Vector<String>(ChatServer.cs_maxclient); 
      roomHash = new Hashtable<String,ServerThread>(ChatServer.cs_maxclient);
      roomVector = new Vector<String>(ChatServer.cs_maxclient); 
      userRoomVector = new Vector<String>(ChatServer.cs_maxclient);
   }

   public ServerThread(Socket sock){
      try{
         st_sock = sock;
         st_in = new DataInputStream(sock.getInputStream()); 
         st_out = new DataOutputStream(sock.getOutputStream());
         st_buffer = new StringBuffer(2048);
      }catch(IOException e){
         System.out.println(e);
      }
   }

   public void run(){
      try{
         while(true){
            String recvData = st_in.readUTF();
            System.out.println(recvData);
            StringTokenizer st = new StringTokenizer(recvData, SEPARATOR);
            int command = Integer.parseInt(st.nextToken());
            switch(command){

               // �α׿� �õ� �޽��� PACKET : REQ_LOGON|ID
               case REQ_LOGON:{
                  int result;
                  String id = st.nextToken(); // Ŭ���̾�Ʈ�� ID�� ��´�.
                  result = addUser(id, this);
                  st_buffer.setLength(0);
                  starttime = new Date();
                  if(result ==0){  // ������ ����� ����
                     st_buffer.append(YES_LOGON); 
                     					// YES_LOGON|�����ð�|ID1`ID2`..
                     st_buffer.append(SEPARATOR);
                     st_buffer.append(starttime);
                     st_buffer.append(SEPARATOR);
                     String userIDs = getUsers(); //��ȭ�� ���� �����ID�� ���Ѵ�
                     st_buffer.append(userIDs);
                     send(st_buffer.toString());
                     modifyWaitUsers();
                     if(!userRoomVector.isEmpty())
                    	 modifyWaitRooms();
                  }else{  // ���ӺҰ� ����
                     st_buffer.append(NO_LOGON);  // NO_LOGON|errCode
                     st_buffer.append(SEPARATOR);
                     st_buffer.append(result); // ���ӺҰ� �����ڵ� ����
                     send(st_buffer.toString());
                     
                  }
                  break;
               }

               // ��ȭ�� ���� �õ��޽��� PACKET : REQ_CREATEROOM|ID
               case REQ_CREATEROOM : {
            	   st_buffer.setLength(0);
                   String id = st.nextToken(); // Ŭ���̾�Ʈ�� ID�� ��´�.
                   
                   if(checkUserID(id) == null){

                   // NO_ENTERROOM PACKET : NO_ENTERROOM|errCode
                      st_buffer.append(NO_CREATEROOM);
                      st_buffer.append(SEPARATOR);
                      st_buffer.append(MSG_CANNOTOPEN);
                      send(st_buffer.toString());  // NO_CREATEROOM ��Ŷ�� �����Ѵ�.
                      break;
                   }
                   String roomid = id+".room";
                   roomname = roomid;
                   //System.out.println(roomid);
                   roomVector.addElement(id);  // ����� ID �߰�
                   roomHash.put(id, this); //����� ID �� Ŭ���̾�Ʈ�� �����  ������ ����
                   userRoomVector.addElement(roomid); // ������� ���̸� ����

                   
                   // YES_CREATEROOM PACKET : YES_CREATEROOM|ROOM|ROOMLIST
                   st_buffer.append(YES_CREATEROOM); 
                   st_buffer.append(SEPARATOR);
                   st_buffer.append(roomid);
                   st_buffer.append(SEPARATOR);
                   String roomlist = getRoomlist();
                   st_buffer.append(roomlist);
                   send(st_buffer.toString()); // YES_CREATEROOM ��Ŷ�� �����Ѵ�.
                   modifyWaitRooms();
                   
                   //MDY_USERIDS PACKET : MDY_USERID|id
                   st_buffer.setLength(0);
                   st_buffer.append(MDY_USERID);
                   st_buffer.append(SEPARATOR);                
                   st_buffer.append(id);
                   send(st_buffer.toString());
                   break;
               }
               
               // ��ȭ�� ���� �õ� �޽���  PACKET : REQ_ENTERROOM|ID|ROOMID
               case REQ_ENTERROOM:{
                  st_buffer.setLength(0);
                  String id = st.nextToken(); // Ŭ���̾�Ʈ�� ID�� ��´�.
                  roomname = st.nextToken();
                  if(checkUserID(id) == null){

                  // NO_ENTERROOM PACKET : NO_ENTERROOM|errCode
                     st_buffer.append(NO_ENTERROOM);
                     st_buffer.append(SEPARATOR);
                     st_buffer.append(MSG_CANNOTOPEN);
                     send(st_buffer.toString());  // NO_ENTERROOM ��Ŷ�� �����Ѵ�.
                     break;
                  }
                  roomVector.addElement(id);  // ����� ID �߰�
                  roomHash.put(id, this); //����� ID �� Ŭ���̾�Ʈ�� �����  ������ ����
                  if(userRoomVector.contains(roomname)) {
                	  // YES_ENTERROOM PACKET : YES_ENTERROOM|ROOMID
                	  st_buffer.append(YES_ENTERROOM); 
                	  st_buffer.append(SEPARATOR);
                	  st_buffer.append(roomname);
                	  send(st_buffer.toString()); // YES_ENTERROOM ��Ŷ�� �����Ѵ�.

                	  //MDY_ROOMUSERS PACKET : MDY_ROOMUSERS|ROOOMID|���濡�ִ�IDid1'id2' ....
                	  st_buffer.setLength(0);
                	  st_buffer.append(MDY_ROOMUSERS);
                	  st_buffer.append(SEPARATOR);               	
                	  String userIDs = getRoomUsers(); // ��ȭ�� ���� ����� ID�� ���Ѵ�
                	  st_buffer.append(userIDs);
                	  System.out.println(st_buffer);
                	  broadcast(st_buffer.toString(),MDY_USERID); // MDY_USERIDS ��Ŷ�� �����Ѵ�.               	 
                  }
                  break;
               }
               
               // ��ȭ�� ������� ��û �޽��� PACKET : REQ_ALREADYUSER|ROOMID
               case REQ_UESRLIST : {
            	  String roomname = st.nextToken();
            	  String chatlist = getRoomUsers(roomname);
            	  
            	  // YES_USERLIST|CHATLIST
            	  st_buffer.setLength(0);
            	  st_buffer.append(YES_USERLIST); 
            	  st_buffer.append(SEPARATOR);
            	  st_buffer.append(chatlist);
            	  send(st_buffer.toString());   
            	  break;
               }

               // ��ȭ�� ���� �õ� �޽��� PACKET : REQ_SENDWORDS|ID|��ȭ��
               case REQ_SENDWORDS:{
                  st_buffer.setLength(0);
                  st_buffer.append(YES_SENDWORDS);
                  st_buffer.append(SEPARATOR);
                  String id = st.nextToken(); // ������ ������� ID�� ���Ѵ�.
                  st_buffer.append(id);
                  st_buffer.append(SEPARATOR);
                  try{
                     String data = st.nextToken(); // ��ȭ���� ���Ѵ�.
                     st_buffer.append(data);
                  }catch(NoSuchElementException e){}
                  broadcast(st_buffer.toString(), YES_SENDWORDS); // YES_SENDWORDS ��Ŷ  ����
                  break;
               }
               
               // �ӼӸ� ���� �õ� �޽��� PACKET : REQ_WHISPHER|ID|WID|��ȭ��
               case REQ_WHISPER : {
            	   String id = st.nextToken();
            	   String wid = st.nextToken();
            	   String whisperwords = st.nextToken();
            	   // �۽��� Ŭ���̾�Ʈ�� �ؽþ����带 ����.
            	   ServerThread SendThread = (ServerThread)roomHash.get(id);
            	   if(wid != null) {           		   
            		    st_buffer.setLength(0);
            		    st_buffer.append(YES_WHISPERSEND);
            		    st_buffer.append(SEPARATOR);
            		    st_buffer.append(id);
            		    st_buffer.append(SEPARATOR);
            		    st_buffer.append(wid);
            		    st_buffer.append(SEPARATOR);
            		    st_buffer.append(whisperwords);            		
            		    // �ӼӸ� �޽����� �۽��� Ŭ���̾�Ʈ�� ������
            		    SendThread.send(st_buffer.toString());                 
            		    
            		    // ������ Ŭ���̾�Ʈ�� �ؽþ����带 ����.
            		    ServerThread RecvThread = (ServerThread)roomHash.get(wid);                  
            		    st_buffer.setLength(0);
            		    st_buffer.append(YES_WHISPERSEND);
            		    st_buffer.append(SEPARATOR);
            		    st_buffer.append(id);
            		    st_buffer.append(SEPARATOR);
            		    st_buffer.append(wid);
            		    st_buffer.append(SEPARATOR);
            		    st_buffer.append(whisperwords); 
            		    // �ӼӸ� �޽����� ������ Ŭ���̾�Ʈ�� ������
            		    RecvThread.send(st_buffer.toString());
            	  }
                  else {                	                	  	
                	  	st_buffer.setLength(0);
                	 	st_buffer.append(NO_WHISPERSEND);
                	 	st_buffer.append(SEPARATOR);
                	 	st_buffer.append("���� �� ����ڴ� �����ϴ�.\r\n");
                	 	SendThread.send(st_buffer.toString());
                 }	
                 break;
               }
               
               
               // LOGOUT ���� �õ� �޽���  
               // PACKET : YES_LOGOUT|Ż����ID|Ż���� �̿��� ids
               case REQ_LOGOUT:{
            	  String id = st.nextToken();           	  
            	  logonVector.remove(id);
            	  logonHash.remove(id,this);
            	  
            	  int vecsize = logonVector.size();
            	  System.out.println(vecsize);
            	  if(vecsize > 0) {
            		  String ids = getUsers();
            		  st_buffer.setLength(0);
            		  st_buffer.append(YES_LOGOUT);
            		  st_buffer.append(SEPARATOR);
            		  st_buffer.append(id);
            		  st_buffer.append(SEPARATOR);
            	  	  st_buffer.append(ids);
            	  	  send(st_buffer.toString());
            	  	  modifyWaitUsers();
            	  }
            	  else {
            		  st_buffer.setLength(0);
            		  st_buffer.append(YES_LOGOUT);
            		  st_buffer.append(SEPARATOR);
            		  st_buffer.append(id);
            		  st_buffer.append(SEPARATOR);
            		  st_buffer.append("zero");
            		  send(st_buffer.toString());
            		  modifyWaitUsers();
            	  }
                  break;
               }

               // �� �������� ����ϱ� ���� �õ� �޽��� PACKET : YES_QUITROOM|EXIST
               // �濡 �ƹ��� ������� ���� �õ� �޽��� PACKET : YES_QUITROOM|ZERO
               case REQ_QUITROOM:{
            	  String id = st.nextToken();
            	  roomVector.remove(id);
            	  roomHash.remove(id, this);
            	  String roomids = getRoomUsers();
            	  if(roomids.equals("")) {
        			  userRoomVector.remove(roomname);          
        		  }            	 
            	  int vecsize = userRoomVector.size();
            	  if(vecsize>0) {
            		  st_buffer.setLength(0);
            		  st_buffer.append(YES_QUITROOM);
            		  st_buffer.append(SEPARATOR);
            		  st_buffer.append("EXIST");
            		  send(st_buffer.toString());
                	  modifyRoomUsers();
                	  modifyWaitRooms();
            	  }
            	  else {           		  
            		  st_buffer.setLength(0);
            		  st_buffer.append(YES_QUITROOM);
            		  st_buffer.append(SEPARATOR);
            		  st_buffer.append("ZERO");           		  
            		  send(st_buffer.toString());
            		  modifyWaitRooms();
            	  }
            	  break;
               }

            } // switch ����

            Thread.sleep(100);
         } //while ����

      }catch(NullPointerException e){ // �α׾ƿ��� st_in�� �� ���ܸ� �߻��ϹǷ�
      }catch(InterruptedException e){
    	  try {
			release();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
      }catch(IOException e){
    	  try {
			release();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
      }
   }

   // �ڿ��� �����Ѵ�.

   public void release() throws IOException{
	   st_sock.close();
	   st_in.close();
	   st_out.close();
   }

   /* �ؽ� ���̺� ������ ��û�� Ŭ���̾�Ʈ�� ID �� ������ ����ϴ� �����带 ���.
          ��, �ؽ� ���̺��� ��ȭ�� �ϴ� Ŭ���̾�Ʈ�� ����Ʈ�� ����. */
    private static synchronized int addUser(String id, ServerThread client){
      if(checkUserID(id) != null){
         return MSG_ALREADYUSER;
      }  
      if(logonHash.size() >= ChatServer.cs_maxclient){
         return MSG_SERVERFULL;
      }
      logonVector.addElement(id);  // ����� ID �߰�
      logonHash.put(id, client); // ����� ID �� Ŭ���̾�Ʈ�� ����� �����带 �����Ѵ�.
      client.st_ID = id;
      return 0; // Ŭ���̾�Ʈ�� ���������� �����ϰ�, ��ȭ���� �̹� ������ ����.
   }

   /* ������ ��û�� ������� ID�� ��ġ�ϴ� ID�� �̹� ���Ǵ� ���� �����Ѵ�.
           ��ȯ���� null�̶�� �䱸�� ID�� ��ȭ�� ������ ������. */
   private static ServerThread checkUserID(String id){
      ServerThread alreadyClient = null;
      alreadyClient = (ServerThread) logonHash.get(id);
      return alreadyClient;
   }
   
   // ���ǿ� �ִ� ������ �����Ѵ�.
   private void modifyWaitUsers() throws IOException {
	   String ids = getUsers();
	   st_buffer.setLength(0);
	   st_buffer.append(MDY_WAITUSERS);
	   st_buffer.append(SEPARATOR);
	   st_buffer.append(ids);
	   broadcast(st_buffer.toString(), WAITROOM);
   }
   
   // ������ ä�ù� ����Ʈ�� �����Ѵ�.
   private void modifyWaitRooms() throws IOException {
	   String rooms = getRoomlist();
	   st_buffer.setLength(0);
	   st_buffer.append(MDY_WAITROOMS);
	   st_buffer.append(SEPARATOR);
	   st_buffer.append(rooms);
	   broadcast(st_buffer.toString(), WAITROOM);
   }
   // ä�ù濡 �ִ� ������ �����Ѵ�.
   private void modifyRoomUsers() throws IOException {
	   String ids = getRoomUsers();
	   st_buffer.setLength(0);
	   st_buffer.append(MDY_ROOMUSERS);
	   st_buffer.append(SEPARATOR);
	   st_buffer.append(ids);
	   broadcast(st_buffer.toString(), CHATROOM);
   }
   
   // �α׿¿� ������ ����� ID�� ���Ѵ�.
   private String getUsers(){
      StringBuffer id = new StringBuffer();
      String ids;
      Enumeration<String> enu = logonVector.elements();
      while(enu.hasMoreElements()){
         id.append(enu.nextElement());
         id.append(DELIMETER); 
      }
      try{
         ids = new String(id);  // ���ڿ��� ��ȯ�Ѵ�.
         ids = ids.substring(0, ids.length()-1); // ������ "`"�� �����Ѵ�.
      }catch(StringIndexOutOfBoundsException e){
         return "";
      }
      return ids;
   }   

   // ��ȭ�濡 ������ ����� ID�� ���Ѵ�.
   private String getRoomUsers(){
      StringBuffer id = new StringBuffer();
      String ids;
      Enumeration<String> enu = roomVector.elements();
      ServerThread client;
      while(enu.hasMoreElements()){
    	 client = (ServerThread)logonHash.get(enu.nextElement());
    	 if(client.roomname.equals(roomname)) {
    		 id.append(client.st_ID);
    		 id.append(DELIMETER); 
    	 }
      }
      try{
         ids = new String(id);
         ids = ids.substring(0, ids.length()-1); // ������ "`"�� �����Ѵ�.
      }catch(StringIndexOutOfBoundsException e){
         return "";
      }
      return ids;
   }
   
   private String getRoomUsers(String roomid){
	      StringBuffer id = new StringBuffer();
	      String ids;
	      Enumeration<String> enu = roomVector.elements();
	      ServerThread client;
	      while(enu.hasMoreElements()){
	    	 client = (ServerThread)logonHash.get(enu.nextElement());
	    	 if(client.roomname.equals(roomid)) {
	    		 id.append(client.st_ID);
	    		 id.append(COMMA); 
	    	 }
	      }
	      try{
	         ids = new String(id);
	         ids = ids.substring(0, ids.length()-1); // ������ "`"�� �����Ѵ�.
	      }catch(StringIndexOutOfBoundsException e){
	         return "";
	      }
	      return ids;
	   }
   
   // ��ȭ���� ����Ʈ�� ���Ѵ�.
   private String getRoomlist(){
	   StringBuffer room = new StringBuffer();
	   String rooms;
	   Enumeration<String> enu = userRoomVector.elements();
	   while(enu.hasMoreElements()){
		   room.append(enu.nextElement());
		   room.append(DELIMETER); 
	   }
	   try{
		   rooms = new String(room);
		   rooms = rooms.substring(0, rooms.length()-1); // ������ "`"�� �����Ѵ�.
	   }catch(StringIndexOutOfBoundsException e){
		   return " ";
	   }
	   return rooms;
   }

   // ��ȭ�濡 ������ ��� �����(��ε��ɽ���)���� �����͸� �����Ѵ�.
   public synchronized void broadcast(String sendData, int room) throws IOException{
      ServerThread client;
      Enumeration<String> enu = roomVector.elements();
      if(room == WAITROOM ) {
    	enu = logonVector.elements();  
    	while(enu.hasMoreElements()){
    		client = (ServerThread)logonHash.get(enu.nextElement());
    		client.send(sendData);
    	}
      }
      else {
    	  enu = roomVector.elements();
    	  while(enu.hasMoreElements()){
      		client = (ServerThread) roomHash.get(enu.nextElement());
      		if(client.roomname.equals(roomname)) {
      			client.send(sendData);
      		}
      	}
      }
   }

   // �����͸� �����Ѵ�.
   public void send(String sendData) throws IOException{
      synchronized(st_out){
         st_out.writeUTF(sendData);
         st_out.flush();
      }
   }
}   
