/**
	Author: Charmaine T. Matienzo
	Section: B - 1L
	CMSC 137 Project 1
**/

import java.io.*;
import java.net.*;
import java.util.Arrays;


class UDPClient{
	public static void main(String args[]) throws Exception{
		DatagramSocket clientSocket = new DatagramSocket();
		System.out.println("client port: "+clientSocket.getLocalPort());	
		byte[] sendData = new byte[1024];
		byte[] receiveData = new byte[1024];
		InetAddress IPAddress = InetAddress.getByName("localhost");

		boolean isConnected = false;

		String header;
		String[] tcpHeader;
		int syn, ack, fin;
		DatagramPacket sendPacket, receivePacket;
		syn = 1;
		ack = 0;
		fin = 0;
			
		//sends packet data to the desitination port
		header = "SEQN=1000,ACKN=2000,SYN="+syn+",ACK="+ack+",FIN="+fin+",WINSIZE=1000";
		sendData = header.getBytes();
		sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 5000);
		System.out.println("destination to port: "+ sendPacket.getPort()); 
		clientSocket.send(sendPacket);
		//timeout and resend of 'TCP' packet
		clientSocket.setSoTimeout(4000);	
		while(true){	
			receivePacket = new DatagramPacket(receiveData, receiveData.length);
			try{
				clientSocket.receive(receivePacket);
				break;
			}catch(SocketTimeoutException e){
				//resend
				System.out.println("timeout!");
				clientSocket.send(sendPacket);
				continue;
			}
		}
		

		header = new String(receivePacket.getData());
		tcpHeader = header.split(",");
		syn = Integer.parseInt(tcpHeader[2].substring(tcpHeader[2].lastIndexOf("=")+1));
		ack = Integer.parseInt(tcpHeader[3].substring(tcpHeader[3].lastIndexOf("=")+1));
		fin = Integer.parseInt(tcpHeader[4].substring(tcpHeader[4].lastIndexOf("=")+1));

		Thread.sleep(2000);	//2 sec delay
		System.out.println("Received from server: "+syn+" ,"+ack+" ,"+fin);

		if(syn == 1 && ack == 1 && fin == 0){
			syn = 0;
			ack = 1;
			fin = 0;
			header = "SEQN=1000,ACKN=2000,SYN="+syn+",ACK="+ack+",FIN="+fin+",WINSIZE=1000";
			sendData = header.getBytes();
			sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 5000);
			clientSocket.send(sendPacket);
			isConnected = true;
		}
		
		if(isConnected){
			System.out.println("Connection Established!");
		}

	
		clientSocket.close();

		
	}
}


