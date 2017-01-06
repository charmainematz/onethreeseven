/*
 * MATIENZO, CHARMAINE T. 
 * 2011-36902
 * CMSC 137 B-1L
 * Project 1: MimickS the behavior of TCP into UDP
 * 			  
*/

import java.io.*;
import java.net.*;
import java.util.concurrent.TimeUnit;
import java.math.BigInteger;
import java.util.*;

class Server{ 

    public static void main(String[] args) throws Exception{
    	ServerThread serverThread = new ServerThread();
    	serverThread.start();
    }

    private static class ServerThread extends Thread{
    	static DatagramSocket socket=null;
    	byte[] seqNumber; 
    	byte[] ackNumber;
    	byte flags;
    	byte[] winSize;
    	byte[] messageData;
    	static byte[] sendData;
    	BigInteger seqNum; 
    	String message;
    	static BigInteger disSeqNum;

    	//long ackNum, seqNum;
		ServerThread() {
			super("Server");
			try{
				socket = new DatagramSocket();
		        System.out.println("Server listening on port: " + socket.getLocalPort());
			}catch(SocketException e){}
		}//end of ServerThread  

		public void run(){
			while(true){
				try{
					System.out.println();
					System.out.println("Listening...");
					byte[] data = new byte[1024];
					DatagramPacket packet = new DatagramPacket(data, data.length);					
						
					boolean noData = true;
						
					try{
						socket.receive(packet);
						noData = false;
					}catch(SocketTimeoutException e){
						socket.receive(packet);	
					}
						
					InetAddress address = packet.getAddress();
					int port = packet.getPort();	

					//dissection of the packet
					byte[] receivedData = new byte[packet.getLength()];
					receivedData = packet.getData();

					//if a new client is trying to connect
					//SYN flag is set
					if(receivedData[8]==2){
						try{								
							TimeUnit.SECONDS.sleep(2);
							System.out.println();
							System.out.println("A New Client is trying to connect.");
						}catch (InterruptedException i) {
							System.out.println("Error in TimeUnit.");
						}		
							
						ackNumber = new byte[4];
						ackNumber[0] = receivedData[0];
						ackNumber[1] = receivedData[1];
						ackNumber[2] = receivedData[2];
						ackNumber[3] = receivedData[3];	
						System.out.println("  SEQ NUMBER FROM CLIENT:"+Math.abs(new BigInteger(ackNumber).longValue()));
						

						seqNumber = new byte[4];
						seqNumber[0] = receivedData[4];
						seqNumber[1] = receivedData[5];
						seqNumber[2] = receivedData[6];
						seqNumber[3] = receivedData[7];	
						System.out.println("  ACK NUMBER FROM CLIENT:"+Math.abs(new BigInteger(seqNumber).longValue()));

						flags = receivedData[8];
						winSize = new byte[2];
						winSize[0] = receivedData[9];
						winSize[1] = receivedData[10];
						messageData = new byte[packet.getLength()-11];	
						
						int u=0;
						for(int i=11; i<packet.getLength();i++){
							messageData[u] = receivedData[i];
							u++;
						}
										
						message = new String(messageData, 0, messageData.length, "UTF-8");
						System.out.println();
	
						//Generate Server sequence number then send ACK+SYN to Client
						seqNumber = new byte[4];
						new Random().nextBytes(seqNumber);
						System.out.println("  SEQ NUMBER SENT TO CLIENT: "+Math.abs(new BigInteger(seqNumber).longValue()));
						seqNum = new BigInteger(seqNumber).abs();
						
						BigInteger ackNum = new BigInteger(ackNumber).abs().add(BigInteger.valueOf(1));
						ackNumber = ackNum.toByteArray();
						System.out.println("  ACK NUMBER SENT TO CLIENT: "+Math.abs(new BigInteger(ackNumber).longValue()));
						flags = 18;

						//integration into RESPONSE packet
						//integrate into 1 packet
						sendData = new byte[messageData.length+11];
						sendData[0] = seqNumber[0];
						sendData[1] = seqNumber[1];
						sendData[2] = seqNumber[2];
						sendData[3] = seqNumber[3];
						sendData[4] = ackNumber[0];
						sendData[5] = ackNumber[1];
						sendData[6] = ackNumber[2];
						sendData[7] = ackNumber[3];
						sendData[8] = flags;
						sendData[9] = winSize[0];
						sendData[10] = winSize[1];
							
						u=11;
						for(int i=0; i<messageData.length;i++){
							sendData[u] = messageData[i];
							u++;
						}

						packet = new DatagramPacket(sendData, sendData.length, address, port);
						socket.send(packet);

						//if not a new client, if ACK flag is set
		
					}
					else if(receivedData[8]==16){	//if not a new client
						ackNumber = new byte[4];
						ackNumber[0] = receivedData[0];
						ackNumber[1] = receivedData[1];
						ackNumber[2] = receivedData[2];
						ackNumber[3] = receivedData[3];	
						System.out.println();
						System.out.println("  ACK NUMBER FROM CLIENT:"+Math.abs(new BigInteger(ackNumber).longValue()));
							
						seqNumber = new byte[4];
						seqNumber[0] = receivedData[4];
						seqNumber[1] = receivedData[5];
						seqNumber[2] = receivedData[6];
						seqNumber[3] = receivedData[7];	
						
						flags = receivedData[8];
						winSize = new byte[2];
						winSize[0] = receivedData[9];
						winSize[1] = receivedData[10];
						messageData = new byte[packet.getLength()-11];	
						
						int u=0;
						for(int i=11; i<packet.getLength();i++){
							messageData[u] = receivedData[i];
							u++;
						}
											
						message = new String(messageData, 0, messageData.length, "UTF-8");
						System.out.println("  ACK NUMBER FROM CLIENT ACCEPTED. CONNECTION ESTABLISHED!");
						System.out.println("  MESSAGE FROM CLIENT:"+message);
					}
					//if FIN is set to 1, disconnected
					//no ACK is sent yet
					else if(receivedData[8]==1){
						try{								
							TimeUnit.SECONDS.sleep(2);
							System.out.println();
							System.out.println("A Client is trying to disconnect.");
						}catch (InterruptedException i) {
							System.out.println("Error in TimeUnit.");
						}		
							
						ackNumber = new byte[4];
						ackNumber[0] = receivedData[0];
						ackNumber[1] = receivedData[1];
						ackNumber[2] = receivedData[2];
						ackNumber[3] = receivedData[3];	
						System.out.println("  DISCONNECTION SEQ NUMBER FROM CLIENT:"+Math.abs(new BigInteger(ackNumber).longValue()));
						
						seqNumber = new byte[4];
						seqNumber[0] = receivedData[4];
						seqNumber[1] = receivedData[5];
						seqNumber[2] = receivedData[6];
						seqNumber[3] = receivedData[7];	
						System.out.println("  DISCONNECTION ACK NUMBER FROM CLIENT:"+Math.abs(new BigInteger(seqNumber).longValue()));

						flags = receivedData[8];					
						winSize = new byte[2];
						winSize[0] = receivedData[9];
						winSize[1] = receivedData[10];
						messageData = new byte[packet.getLength()-11];	
						
						int u=0;
						for(int i=11; i<packet.getLength();i++){
							messageData[u] = receivedData[i];
							u++;
						}					
						
						message = new String(messageData, 0, messageData.length, "UTF-8");
						System.out.println();

						//Generate Server sequence number then send ACK+SYN to Client
						seqNumber = new byte[4];
						new Random().nextBytes(seqNumber);
						System.out.println("  DISCONNECTION SEQ NUMBER SENT TO CLIENT: "+Math.abs(new BigInteger(seqNumber).longValue()));
						seqNum = new BigInteger(seqNumber).abs();
						
						BigInteger ackNum = new BigInteger(ackNumber).abs().add(BigInteger.valueOf(1));
						ackNumber = ackNum.toByteArray();
						System.out.println("  DISCONNECTION ACK NUMBER SENT TO CLIENT: "+Math.abs(new BigInteger(ackNumber).longValue()));
						flags = 17;

						//integration into RESPONSE packet
						//integrate into 1 packet
						sendData = new byte[messageData.length+11];
						sendData[0] = seqNumber[0];
						sendData[1] = seqNumber[1];
						sendData[2] = seqNumber[2];
						sendData[3] = seqNumber[3];
						sendData[4] = ackNumber[0];
						sendData[5] = ackNumber[1];
						sendData[6] = ackNumber[2];
						sendData[7] = ackNumber[3];
						sendData[8] = flags;
						sendData[9] = winSize[0];
						sendData[10] = winSize[1];
							
						u=11;
						for(int i=0; i<messageData.length;i++){
							sendData[u] = messageData[i];
							u++;
						}

						packet = new DatagramPacket(sendData, sendData.length, address, port);
						socket.send(packet);

						fwdisconnect(address, port);
					}
					
					//waiting for client ACK to sent DISCONNECTION SEQ
					else if(receivedData[8]==17){
						try{								
							TimeUnit.SECONDS.sleep(2);
							System.out.println();
							System.out.println("Disconnection ACK from client received..");
						}catch (InterruptedException i) {
							System.out.println("Error in TimeUnit.");
						}		
							
						seqNumber = new byte[4];
						seqNumber[0] = receivedData[0];
						seqNumber[1] = receivedData[1];
						seqNumber[2] = receivedData[2];
						seqNumber[3] = receivedData[3];	
						System.out.println("  DISCONNECTION SEQ NUMBER FROM CLIENT:"+Math.abs(new BigInteger(seqNumber).longValue()));
					
						ackNumber = new byte[4];
						ackNumber[0] = receivedData[4];
						ackNumber[1] = receivedData[5];
						ackNumber[2] = receivedData[6];
						ackNumber[3] = receivedData[7];	
						System.out.println("  DISCONNECTION ACK NUMBER FROM CLIENT:"+Math.abs(new BigInteger(ackNumber).longValue()));

						flags = receivedData[8];				
						winSize = new byte[2];
						winSize[0] = receivedData[9];
						winSize[1] = receivedData[10];
						messageData = new byte[packet.getLength()-11];	
						
						int u=0;
						for(int i=11; i<packet.getLength();i++){
							messageData[u] = receivedData[i];
							u++;
						}
						
						message = new String(messageData, 0, messageData.length, "UTF-8");
						System.out.println();
						System.out.println("  ACK FROM CLIENT ACCEPTED. FOURWAY DISCONNECTION SUCCESSFUL. ");
					}	
				}catch (IOException e) {
					System.out.println("Error while waiting for connections."+e);
				}	
						
			}//end of while
		}//end of run

		public static void fwdisconnect(InetAddress address, int port){
			// initialize headers
			System.out.println();
			byte[] ackNumber = new byte[4];
			ackNumber[0] = 0;
			ackNumber[1] = 0;
			ackNumber[2] = 0;
			ackNumber[3] = 0;			
			byte[] seqNumber = new byte[4];
			new Random().nextBytes(seqNumber);
			System.out.println("  DISCONNECTION SEQ NUMBER SENT TO CLIENT:"+Math.abs(new BigInteger(seqNumber).longValue()));
			disSeqNum = new BigInteger(seqNumber).abs();
			System.out.println();
		
			byte flags = 1;
			byte[] winSize = new byte[2];
			new Random().nextBytes(winSize);
			
			byte[] messageData = new byte[4];
			messageData[0] = 0;
			messageData[1] = 0;
			messageData[2] = 0;
			messageData[3] = 0;
				
			//integrate into 1 packet
			byte[] sendData = new byte[messageData.length+11];
			sendData[0] = seqNumber[0];
			sendData[1] = seqNumber[1];
			sendData[2] = seqNumber[2];
			sendData[3] = seqNumber[3];
			sendData[4] = ackNumber[0];
			sendData[5] = ackNumber[1];
			sendData[6] = ackNumber[2];
			sendData[7] = ackNumber[3];
			sendData[8] = flags;
			sendData[9] = winSize[0];
			sendData[10] = winSize[1];
				
			int u=11;
			for(int i=0; i<messageData.length;i++){
				sendData[u] = messageData[i];
				u++;
			}
						
			DatagramPacket packet = new DatagramPacket(sendData, sendData.length, address, port);
			
			try {
				socket.send(packet);					
			} catch (IOException e) {
				System.out.println("No client was found in this port.");
			}				   
		}//end of fwdisconnect
    }//end of ServerThread
}
