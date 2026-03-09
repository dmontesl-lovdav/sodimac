/**
 * 
 */
package com.sodimac.cfdi.client;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.springframework.stereotype.Service;

@Service
public class ServiceLogin {

	@SuppressWarnings("resource")
	public String getMacAdress() throws UnknownHostException, SocketException {

		DatagramSocket socket = new DatagramSocket();
		socket.connect(InetAddress.getByName("1.1.1.1"), 10002);
		String Host_Ip = socket.getLocalAddress().getHostAddress();
		String MacAddress = "";

		InetAddress address = InetAddress.getByName(Host_Ip);

		/*
		 * Get NetworkInterface for the current host and then read the hardware address.
		 */
		NetworkInterface ni = NetworkInterface.getByInetAddress(address);
		if (ni != null) {
			byte[] mac = ni.getHardwareAddress();
			if (mac != null) {
				/*
				 * Extract each array of mac address and convert it to hexa with the following
				 * format 08-00-27-DC-4A-9E.
				 */
				for (int i = 0; i < mac.length; i++) {
					MacAddress += String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : "");
					// System.out.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : "");
				}
			} else {
				// Address doesn't exist or is not accessible.
				System.out.println("La direcci�n no existe o no es accesible." + " " + "<br/>");
				MacAddress = "";
			}
		} else {

			// Network Interface for the specified address is not found.
			System.out.println("No se encuentra la interfaz de red para la direcci�n especificada." + " " + "<br/>");
			MacAddress = "";
		}

		return MacAddress;
	}

	/**
	 * Get MAC address
	 * 
	 * @throws UnknownHostException
	 * @throws SocketException
	 */
	public static String getMACaddress() throws UnknownHostException, SocketException {

		InetAddress localHost = InetAddress.getLocalHost();
		NetworkInterface ni = NetworkInterface.getByInetAddress(localHost);
		byte[] hardwareAddress = ni.getHardwareAddress();

		String[] hexadecimal = new String[hardwareAddress.length];
		for (int i = 0; i < hardwareAddress.length; i++) {
			hexadecimal[i] = String.format("%02X", hardwareAddress[i]);
		}
		String macAddress = String.join("-", hexadecimal);

		return macAddress;
	}

	/**
	 * Get Operating System
	 * 
	 * @throws UnknownHostException
	 * @throws SocketException
	 */
	public String getOperatingSystem() {
		String os = System.getProperty("os.name");
		return os;
	}

}
