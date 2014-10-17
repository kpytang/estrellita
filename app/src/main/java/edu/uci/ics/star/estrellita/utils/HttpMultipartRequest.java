/*
 * Copyright (C) 2012 Karen P. Tang, Sen Hirano
 * 
 * This file is part of the Estrellita project.
 * 
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 of 
 * the License, or any later version.
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this program. If not, see
 * 				
 * 				http://www.gnu.org/licenses/
 * 
 */

/**
 * @author Karen P. Tang
 * @author Sen Hirano
 * 
 */

package edu.uci.ics.star.estrellita.utils;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.net.ssl.HttpsURLConnection;

public class HttpMultipartRequest
{
	static final String BOUNDARY = "----------V2ymHFg03ehbqgZCaKO6jy";

	byte[] postBytes = null;
	URL url = null;

	public HttpMultipartRequest(String url, Hashtable<String, String> params, String fileField, String fileName, String fileType, byte[] fileBytes) throws Exception
	{
		this.url = new URL(url);

		String boundary = getBoundaryString();
		String boundaryMessage = getBoundaryMessage(boundary, params, fileField, fileName, fileType);

		String endBoundary = "\r\n--" + boundary + "--\r\n";

		ByteArrayOutputStream bos = new ByteArrayOutputStream();

		bos.write(boundaryMessage.getBytes());
		if(fileBytes != null){
			bos.write(fileBytes);
		}
		bos.write(endBoundary.getBytes());

		this.postBytes = bos.toByteArray();

		bos.close();
	}

	public HttpMultipartRequest(String url, Hashtable<String, String> params) throws Exception {
		this(url, params, null, null, null, null);
	}

	String getBoundaryString()
	{
		return BOUNDARY;
	}

	String getBoundaryMessage(String boundary, Hashtable<String, String> params, String fileField, String fileName, String fileType)
	{
		StringBuffer res = new StringBuffer("--").append(boundary).append("\r\n");

		Enumeration<String> keys = params.keys();

		while(keys.hasMoreElements())
		{
			String key = keys.nextElement();
			String value = params.get(key);
			
			res.append("Content-Disposition: form-data; name=\"").append(key).append("\"\r\n")    
			.append("\r\n").append(value).append("\r\n")
			.append("--").append(boundary).append("\r\n");
		}
		if(fileField != null){
			res.append("Content-Disposition: form-data; name=\"").append(fileField).append("\"; filename=\"").append(fileName).append("\"\r\n") 
			.append("Content-Type: ").append(fileType).append("\r\n\r\n");
		}
		return res.toString();
	}

	public byte[] send() throws Exception
	{
		HttpURLConnection hc = null;

		InputStream is = null;

		ByteArrayOutputStream bos = new ByteArrayOutputStream();

		byte[] res = null;

		try
		{
			try {
				_FakeX509TrustManager.allowAllSSL();
				hc = (HttpsURLConnection)url.openConnection();

			} catch (java.lang.ClassCastException cce) {
				
				hc = (HttpURLConnection)url.openConnection();
			}
			
			hc.setDoOutput(true);
			hc.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + getBoundaryString());

			hc.setRequestProperty("User-Agent","Mozilla/5.0 ( compatible ) ");
			hc.setRequestProperty("Accept","[star]/[star]");
			hc.setRequestMethod("POST");
			DataOutputStream dout = new DataOutputStream( hc.getOutputStream() );

			dout.write(postBytes);

			dout.close();

			int ch;

			is = new DataInputStream ( hc.getInputStream() );

			while ((ch = is.read()) != -1)
			{
				bos.write(ch);
			}
			res = bos.toByteArray();
		}
		finally
		{
			try
			{
				if(bos != null)
					bos.close();

				if(is != null)
					is.close();

				if(hc != null)
					hc.disconnect();
			}
			catch(Exception e2)
			{
				throw e2;
			}
		}
		return res;
	}


}