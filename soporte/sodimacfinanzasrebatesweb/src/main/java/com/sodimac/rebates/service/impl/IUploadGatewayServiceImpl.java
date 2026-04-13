package com.sodimac.rebates.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Vector;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.sodimac.rebates.service.IUploadGateway;

@Service
public class IUploadGatewayServiceImpl implements IUploadGateway {

	@Autowired
	private Environment env;
	
	@Override
	public boolean uploadFile(File file, String path) {
		
		String host = this.env.getProperty("sodimac.rebates.sftp.host");
		Integer port = Integer.parseInt( this.env.getProperty("sodimac.rebates.sftp.port") );
		String username = this.env.getProperty("sodimac.rebates.sftp.user");
		String password = this.env.getProperty("sodimac.rebates.sftp.password");
		
		int mode = ChannelSftp.OVERWRITE;
		
		ChannelSftp channel = null;
		Session session = null;									    	    			
		JSch jsch = new JSch();
		try {
			InputStream inputStream = new FileInputStream(file);
			session = jsch.getSession(username, host, port);
			session.setPassword(password);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();                                                   
            channel = (ChannelSftp)session.openChannel("sftp");
            channel.connect();
            
            System.out.println("Conectado");
            ChannelSftp sftp = (ChannelSftp) channel;

            sftp.cd(path);
            String nomArchivo = file.getName();
			sftp.put( inputStream, nomArchivo , null, mode);
            System.out.println("Se subió el Archivo:" + nomArchivo);
            session.disconnect();
    		channel.disconnect();
		} catch (NumberFormatException | JSchException | SftpException | FileNotFoundException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public boolean delete(String path, String nombreArchivo) {
		
		String host = this.env.getProperty("sodimac.rebates.sftp.host");
		Integer port = Integer.parseInt( this.env.getProperty("sodimac.rebates.sftp.port") );
		String username = this.env.getProperty("sodimac.rebates.sftp.user");
		String password = this.env.getProperty("sodimac.rebates.sftp.password");
		
		ChannelSftp channel = null;
		Session session = null;									    	    			
		JSch jsch = new JSch();
		try {
			session = jsch.getSession(username, host, port);
			session.setPassword(password);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();                                                   
            channel = (ChannelSftp)session.openChannel("sftp");
            channel.connect();
            
            System.out.println("Conectado");
            ChannelSftp sftp = (ChannelSftp) channel;

            sftp.cd(path);
            
            Vector ls = sftp.ls(path);
            if (ls != null) {
            	String pathSftp = ls.toString();
                if (pathSftp.contains(nombreArchivo)) {
                	sftp.rm(nombreArchivo);
                	System.out.println("Se elimina el Archivo:" + nombreArchivo);
                }
            }
            
			
            session.disconnect();
    		channel.disconnect();
		} catch (NumberFormatException | JSchException | SftpException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

}
