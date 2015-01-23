package com.zhuchao.ticket.util;

import java.util.List;

import org.apache.log4j.Logger;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.util.NameValuePair;
import com.zhuchao.ticket.core.MyHttpWebConnection;

public class HttpUtilsFinal {

	@SuppressWarnings("unused")
	private Logger logger = Logger.getLogger(this.getClass());
	private WebClient webClient;

	private MyHttpWebConnection getWebConnection() {
		return (MyHttpWebConnection) webClient.getWebConnection();
	}

	public HttpUtilsFinal() {
		webClient = new WebClient(BrowserVersion.FIREFOX_24);
		webClient.setWebConnection(new MyHttpWebConnection(webClient));
		webClient.getOptions().setUseInsecureSSL(true);
		webClient.getOptions().setCssEnabled(false);
		webClient.getOptions().setJavaScriptEnabled(true);
		webClient.getOptions().setThrowExceptionOnScriptError(false);
	}

	public String sendGet(String url) {
		return getWebConnection().sendGet(url);
	}

	public String sendGet(WebRequest webRequest) {
		return getWebConnection().sendGet(webRequest);
	}

	public String sendPost(String url, List<NameValuePair> nvps) {
		return getWebConnection().sendPost(url, nvps);
	}

	public String sendPost(WebRequest webRequest) {
		return getWebConnection().sendPost(webRequest);
	}

	public void downloadImage(String url, String file) {
		getWebConnection().downloadImage(url, file);
	}

	public WebClient getWebClient() {
		return webClient;
	}

	public void setWebClient(WebClient webClient) {
		this.webClient = webClient;
	}

}
