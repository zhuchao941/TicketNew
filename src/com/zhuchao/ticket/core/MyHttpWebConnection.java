package com.zhuchao.ticket.core;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import com.gargoylesoftware.htmlunit.FormEncodingType;
import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.HttpWebConnection;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.util.NameValuePair;

public class MyHttpWebConnection extends HttpWebConnection {

	public MyHttpWebConnection(WebClient webClient) {
		super(webClient);
	}

	public String sendGet(String url) {
		WebResponse response;
		try {
			response = getResponse(new WebRequest(new URL(url), HttpMethod.GET));
			return response.getContentAsString("utf-8");
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public String sendGet(WebRequest webRequest) {
		WebResponse response;
		try {
			response = getResponse(webRequest);
			return response.getContentAsString("utf-8");
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public String sendPost(String url, List<NameValuePair> nvps) {
		WebResponse response;
		try {
			WebRequest webRequest = new WebRequest(new URL(url),
					HttpMethod.POST);
			webRequest.setRequestParameters(nvps);
			webRequest.setCharset("utf-8");
			webRequest.setEncodingType(FormEncodingType.URL_ENCODED);
			response = getResponse(webRequest);
			return response.getContentAsString("utf-8");
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public String sendPost(WebRequest webRequest) {
		WebResponse response;
		try {
			response = getResponse(webRequest);
			return response.getContentAsString("utf-8");
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void downloadImage(String url, String file) {
		BufferedInputStream bis = null;
		BufferedOutputStream bos = null;
		try {
			WebRequest webRequest = new WebRequest(new URL(url), HttpMethod.GET);
			WebResponse response = getResponse(webRequest);
			InputStream in = response.getContentAsStream();
			bis = new BufferedInputStream(in);
			bos = new BufferedOutputStream(new FileOutputStream(file));
			int i = -1;
			while ((i = bis.read()) != -1) {
				bos.write(i);
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (bis != null) {
				try {
					bis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (bos != null) {
				try {
					bos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
