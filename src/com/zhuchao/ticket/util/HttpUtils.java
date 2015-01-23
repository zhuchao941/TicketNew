package com.zhuchao.ticket.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class HttpUtils {

	private CloseableHttpClient httpClient;
	private HttpClientContext context;

	public HttpUtils() {
		System.setProperty("jsse.enableSNIExtension", "false");
		try {
			init();
		} catch (KeyManagementException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}

	public void init() throws NoSuchAlgorithmException, KeyManagementException {
		X509TrustManager xtm = new X509TrustManager() { // 创建TrustManager
			public void checkClientTrusted(X509Certificate[] chain,
					String authType) {
			}

			public void checkServerTrusted(X509Certificate[] chain,
					String authType) {
			}

			public X509Certificate[] getAcceptedIssuers() {
				return null;
			}
		};
		// TLS1.0与SSL3.0基本上没有太大的差别，可粗略理解为TLS是SSL的继承者，但它们使用的是相同的SSLContext
		SSLContext ctx = SSLContext.getInstance("TLS");
		// 使用TrustManager来初始化该上下文，TrustManager只是被SSL的Socket所使用
		ctx.init(null, new TrustManager[] { xtm }, null);
		// 创建SSLSocketFactory
		SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(
				ctx);
		// 通过SchemeRegistry将SSLSocketFactory注册到我们的HttpClient上
		httpClient = HttpClients.custom().setSSLSocketFactory(socketFactory)
				.build();
		context = new HttpClientContext();
	}

	public String sendGet(String url) {
		CloseableHttpResponse response = null;
		String content = null;
		try {
			HttpGet get = new HttpGet(url);
			response = httpClient.execute(get, context);
			HttpEntity entity = response.getEntity();
			content = EntityUtils.toString(entity, "UTF-8");
			EntityUtils.consume(entity);
			return content;
		} catch (Exception e) {
			e.printStackTrace();
			LogUtils.error(getClass(), "catch exception:" + e);
			if (response != null) {
				try {
					response.close();
				} catch (IOException e1) {
					e1.printStackTrace();
					LogUtils.error(getClass(), "catch exception:" + e1);
				}
			}
		}
		return content;
	}

	public String sendGet(HttpGet get) {
		CloseableHttpResponse response = null;
		String content = null;
		try {
			response = httpClient.execute(get, context);
			HttpEntity entity = response.getEntity();
			content = EntityUtils.toString(entity, "UTF-8");
			EntityUtils.consume(entity);
			return content;
		} catch (Exception e) {
			e.printStackTrace();
			LogUtils.error(getClass(), "catch exception:" + e);
			if (response != null) {
				try {
					response.close();
				} catch (IOException e1) {
					e1.printStackTrace();
					LogUtils.error(getClass(), "catch exception:" + e1);
				}
			}
		}
		return content;
	}

	public String sendPost(String url, List<NameValuePair> nvps) {
		CloseableHttpResponse response = null;
		String content = null;
		try {
			HttpPost post = new HttpPost(url);
			if (nvps != null) {
				post.setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));
			}
			response = httpClient.execute(post, context);
			HttpEntity entity = response.getEntity();
			content = EntityUtils.toString(entity, "UTF-8");
			EntityUtils.consume(entity);
			return content;
		} catch (Exception e) {
			e.printStackTrace();
			LogUtils.error(getClass(), "catch exception:" + e);
		} finally {
			if (response != null) {
				try {
					response.close();
				} catch (IOException e) {
					e.printStackTrace();
					LogUtils.error(getClass(), "catch exception:" + e);
				}
			}
		}
		return content;
	}

	public String sendPost(HttpPost post, List<NameValuePair> nvps) {
		CloseableHttpResponse response = null;
		String content = null;
		try {
			if (nvps != null) {
				post.setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));
			}
			response = httpClient.execute(post, context);
			HttpEntity entity = response.getEntity();
			content = EntityUtils.toString(entity, "UTF-8");
			EntityUtils.consume(entity);
			return content;
		} catch (Exception e) {
			e.printStackTrace();
			LogUtils.error(getClass(), "catch exception:" + e);
		} finally {
			if (response != null) {
				try {
					response.close();
				} catch (IOException e) {
					e.printStackTrace();
					LogUtils.error(getClass(), "catch exception:" + e);
				}
			}
		}
		return content;
	}

	public String sendPost(HttpPost post) {
		CloseableHttpResponse response = null;
		String content = null;
		try {
			response = httpClient.execute(post, context);
			HttpEntity entity = response.getEntity();
			content = EntityUtils.toString(entity, "UTF-8");
			EntityUtils.consume(entity);
			return content;
		} catch (Exception e) {
			e.printStackTrace();
			LogUtils.error(getClass(), "catch exception:" + e);
		} finally {
			if (response != null) {
				try {
					response.close();
				} catch (IOException e) {
					e.printStackTrace();
					LogUtils.error(getClass(), "catch exception:" + e);
				}
			}
		}
		return content;
	}

	public String sendJson(HttpPost post, String json) {
		CloseableHttpResponse response = null;
		String content = null;
		try {
			StringEntity s = new StringEntity(json);
			s.setContentEncoding("UTF-8");
			s.setContentType("application/json");
			post.setEntity(s);

			response = httpClient.execute(post, context);
			HttpEntity entity = response.getEntity();
			content = EntityUtils.toString(entity, "UTF-8");
			EntityUtils.consume(entity);
			return content;
		} catch (Exception e) {
			e.printStackTrace();
			LogUtils.error(getClass(), "catch exception:" + e);
		} finally {
			if (response != null) {
				try {
					response.close();
				} catch (IOException e) {
					e.printStackTrace();
					LogUtils.error(getClass(), "catch exception:" + e);
				}
			}
		}
		return content;
	}

	public CloseableHttpClient getHttpClient() {
		return httpClient;
	}

	public void setHttpClient(CloseableHttpClient httpClient) {
		this.httpClient = httpClient;
	}

	public HttpClientContext getContext() {
		return context;
	}

	public void setContext(HttpClientContext context) {
		this.context = context;
	}

	public void downloadImage(HttpGet get, String file) {
		CloseableHttpResponse response = null;
		BufferedInputStream bis = null;
		BufferedOutputStream bos = null;
		try {
			response = httpClient.execute(get, context);
			HttpEntity entity = response.getEntity();
			InputStream in = entity.getContent();
			bis = new BufferedInputStream(in);
			bos = new BufferedOutputStream(new FileOutputStream(file));
			int i = -1;
			while ((i = bis.read()) != -1) {
				bos.write(i);
			}
			EntityUtils.consume(entity);
		} catch (Exception e) {
			e.printStackTrace();
			if (response != null) {
				try {
					response.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		} finally {
			if (bos != null) {
				try {
					bos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (bis != null) {
				try {
					bis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (response != null) {
				try {
					response.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void downloadImage(String url, String path) {
		downloadImage(new HttpGet(url), path);
	}
}
