package com.sodimac.rebates.service;

import java.io.File;

public interface IUploadGateway {

	public boolean uploadFile(File file, String path);

	public boolean delete(String path, String nombreArchivo);
	
}
