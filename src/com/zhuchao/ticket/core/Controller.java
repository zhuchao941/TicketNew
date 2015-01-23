package com.zhuchao.ticket.core;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlImage;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.util.Cookie;
import com.zhuchao.ticket.constant.SeatConstant;
import com.zhuchao.ticket.entity.Train;
import com.zhuchao.ticket.entity.User;
import com.zhuchao.ticket.util.StringUtils;

public class Controller {
	private static Controller controller;
	private CloseableHttpClient httpClient;
	private HttpClientContext context;
	private List<NameValuePair> nvps;
	private boolean sign = true;
	private Scanner sc = new Scanner(System.in);

	public static void main(String args[]) {
		User user = new User();
		user.setUsername("ZYS0729");
		user.setPassword("zs920630");
		// user.setUsername("zhuchao941");
		// user.setPassword("1231231");
		Train train = new Train();
		/**
		 * 杭州：HZH 武汉：WHN G594 G590 武汉：WHN 十堰：SNN D5216 466 656
		 */
		train.setFrom("HZH");
		train.setTo("WHN");
		train.setDate("2015-02-14");
		train.setTrainCode("G590");
		// Controller buyTicket = new Controller(); 这样居然都可以
		Controller controller = Controller.getInstance();
		controller.getTicket(user, train);
	}

	private Controller() {
		try {
			init();
		} catch (KeyManagementException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}

	public static Controller getInstance() {
		if (controller == null) {
			controller = new Controller();
		}
		return controller;
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
		nvps = new ArrayList<NameValuePair>();
	}

	public void buildExtraParam2(List<NameValuePair> nvps) throws Exception {
		List<com.gargoylesoftware.htmlunit.util.NameValuePair> nameValuePairs = new ArrayList<com.gargoylesoftware.htmlunit.util.NameValuePair>();
		for (NameValuePair nameValuePair : nvps) {
			nameValuePairs
					.add(new com.gargoylesoftware.htmlunit.util.NameValuePair(
							nameValuePair.getName(), nameValuePair.getValue()));
		}
		final WebClient webClient = new WebClient();
		webClient.getOptions().setUseInsecureSSL(true);
		final HtmlPage page = webClient
				.getPage("https://kyfw.12306.cn/otn/login/init");

		final HtmlImage image = (HtmlImage) page
				.getElementById("img_rand_code");
		image.saveAs(new File("rand.jpg"));
		System.out.println(page.asXml());
		System.out.println("Enter:");
		Scanner sc = new Scanner(System.in);
		String randCode = sc.next();
		nameValuePairs
				.add(new com.gargoylesoftware.htmlunit.util.NameValuePair(
						"randCode", randCode));
		final HtmlAnchor button = (HtmlAnchor) page.getElementById("loginSub");
		final HtmlPage page2 = button.click();
		Document doc = Jsoup.parse(page2.asXml());
		Elements elements = doc.select("#loginForm input[type=hidden]");
		for (Element element : elements) {
			nameValuePairs
					.add(new com.gargoylesoftware.htmlunit.util.NameValuePair(
							element.attr("name"), element.attr("value")));
		}
		WebRequest webRequest = new WebRequest(new URL(
				"https://kyfw.12306.cn/otn/login/loginAysnSuggest"),
				HttpMethod.POST);
		webRequest.setRequestParameters(nameValuePairs);
		WebResponse webResponse = webClient.getWebConnection().getResponse(
				webRequest);
		System.out.println(webResponse.getContentAsString("utf-8"));
		Set<Cookie> cookies = webClient.getCookies(new URL(
				"https://kyfw.12306.cn/otn/login/init"));
		for (Cookie cookie : cookies) {
			context.setCookieStore(new BasicCookieStore());
			context.getCookieStore().addCookie(
					new BasicClientCookie(cookie.getName(), cookie.getValue()));
		}
		// webClient.closeAllWindows();
	}

	public BufferedImage getRandCode(String url) {
		CloseableHttpResponse response = null;
		try {
			HttpGet get = new HttpGet(url);
			response = httpClient.execute(get, context);
			HttpEntity entity = response.getEntity();
			BufferedImage image = ImageIO.read(entity.getContent());
			EntityUtils.consume(entity);
			return image;
		} catch (Exception e) {
			e.printStackTrace();
			if (response != null) {
				try {
					response.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}
		return null;
	}

	public String sendGet(String url) {
		CloseableHttpResponse response = null;
		String content = null;
		try {
			HttpGet get = new HttpGet(url);
			response = httpClient.execute(get, context);
			HttpEntity entity = response.getEntity();
			content = EntityUtils.toString(entity);
			EntityUtils.consume(entity);
			return content;
		} catch (Exception e) {
			e.printStackTrace();
			if (response != null) {
				try {
					response.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}
		return content;
	}

	public void getRandCode(String url, String path) {
		CloseableHttpResponse response = null;
		BufferedInputStream bis = null;
		BufferedOutputStream bos = null;
		try {
			HttpGet get = new HttpGet(url);
			response = httpClient.execute(get, context);
			HttpEntity entity = response.getEntity();
			InputStream in = entity.getContent();
			bis = new BufferedInputStream(in);
			bos = new BufferedOutputStream(new FileOutputStream(path
					+ "random.jpg"));
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

	public String sendPost(String url, List<NameValuePair> nvps) {
		CloseableHttpResponse response = null;
		String content = null;
		try {
			HttpPost post = new HttpPost(url);
			if (nvps != null) {
				post.setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));
			}
			post
					.addHeader(
							"User-Agent",
							"Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.65 Safari/537.36");
			post.addHeader("Referer", "https://kyfw.12306.cn/otn/login/init");
			response = httpClient.execute(post, context);
			HttpEntity entity = response.getEntity();
			content = EntityUtils.toString(entity);
			EntityUtils.consume(entity);
			return content;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (response != null) {
				try {
					response.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return content;
	}

	public String login(User user, String randCode) {
		// 登陆
		print("-------开始登陆----------");
		String username = user.getUsername();
		String password = user.getPassword();
		JSONObject jsonObject = null;

		nvps.add(new BasicNameValuePair("loginUserDTO.user_name", username));
		nvps.add(new BasicNameValuePair("userDTO.password", password));
		// nvps.add(new BasicNameValuePair("randCode_validate", ""));
		// nvps.add(new BasicNameValuePair("myversion", "undefined"));
		print("-------开始验证码----------");

		String resInfo = "";
		do {
			// 得到验证码
			getRandCode(
					"https://kyfw.12306.cn/otn/passcodeNew/getPassCodeNew.do?module=login&rand=sjrand",
					"");
			System.out.println("Please input the randCode:");
			randCode = sc.next();
			// 先移除randCode
			if (nvps.size() > 2) {
				nvps.remove(2);
			}
			nvps.add(new BasicNameValuePair("randCode", randCode));
			System.out.println(nvps);
			resInfo = sendPost(
					"https://kyfw.12306.cn/otn/login/loginAysnSuggest", nvps);
			System.out.println(resInfo);
			jsonObject = JSONObject.fromObject(resInfo);
		} while (!jsonObject.getString("messages").equals("[]"));
		return resInfo;
	}

	public String login(String url, List<NameValuePair> nvps) {
		return sendPost(url, nvps);
	}

	public String queryLeftTickets(String date, String from, String to,
			String isAdult) {
		// 2014/09/09从之前的query改成queryT了，我靠
		// 2014/09/10又改成query了我靠
		System.out
				.println("https://kyfw.12306.cn/otn/leftTicket/query?leftTicketDTO.train_date="
						+ date
						+ "&leftTicketDTO.from_station="
						+ from
						+ "&leftTicketDTO.to_station="
						+ to
						+ "&purpose_codes="
						+ isAdult);
		return sendGet("https://kyfw.12306.cn/otn/leftTicket/query?leftTicketDTO.train_date="
				+ date
				+ "&leftTicketDTO.from_station="
				+ from
				+ "&leftTicketDTO.to_station="
				+ to
				+ "&purpose_codes="
				+ isAdult);
	}

	public String queryLeftTickets2(String date, String from, String to,
			String isAdult) {
		// 2014/09/09从之前的query改成queryT了，我靠
		// 2014/09/10又改成query了我靠
		System.out
				.println("https://kyfw.12306.cn/otn/leftTicket/queryT?leftTicketDTO.train_date="
						+ date
						+ "&leftTicketDTO.from_station="
						+ from
						+ "&leftTicketDTO.to_station="
						+ to
						+ "&purpose_codes="
						+ isAdult);
		return sendGet("https://kyfw.12306.cn/otn/leftTicket/queryT?leftTicketDTO.train_date="
				+ date
				+ "&leftTicketDTO.from_station="
				+ from
				+ "&leftTicketDTO.to_station="
				+ to
				+ "&purpose_codes="
				+ isAdult);
	}

	public void checkUser(String url) {
		sendPost(url, null);
	}

	public String submitOrderRequest(String url, List<NameValuePair> nvps) {
		return sendPost(url, nvps);
	}

	public String initDc(String url, List<NameValuePair> nvps) {
		return sendPost(url, nvps);
	}

	public String extractToken(String key, String content, int type) {
		String startSign = "'";
		String endSign = ";";
		if (type == 2) {
			startSign = "{";
		}
		int i = -1;
		if ((i = content.indexOf(key)) != -1) {
			content = content.substring(i);
			int start = content.indexOf(startSign);
			int end = content.indexOf(endSign);
			return content.substring(start, end);
		}
		return "notFound";
	}

	public String checkRandCodeAnsyn(String url, List<NameValuePair> nvps) {
		return sendPost(url, nvps);
	}

	public String checkOrderInfo(String url, List<NameValuePair> nvps) {
		return sendPost(url, nvps);
	}

	public String getQueueCount(String url, List<NameValuePair> nvps) {
		return sendPost(url, nvps);
	}

	public String confirmSingleForQueue(String url, List<NameValuePair> nvps) {
		return sendPost(url, nvps);
	}

	public String getPassengerDTOs(String url, List<NameValuePair> nvps) {
		return sendPost(url, nvps);
	}

	// 客户端调用的方法
	public void getTicket(User user, Train train) {
		String randCode = "";
		String resInfo = "";

		// login(user, randCode);
		try {
			buildExtraParam2(nvps);
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		// sc.next();

		do {

			String type = null; // 票种（硬座、硬卧等）

			// 至此登陆成功，然后进行购票
			print("-------开始查询----------");
			String date = train.getDate();
			String from = train.getFrom();
			String to = train.getTo();
			String isAdult = "ADULT";
			String trainCode = train.getTrainCode();
			String secretStr = null;
			JSONObject jsonObject = null;
			do {
				JSONArray jsonArray = null;
				do {
					resInfo = queryLeftTickets(date, from, to, isAdult);
					try {
						// 防止解析错误，有时候会报错Exception in thread "main"
						// net.sf.json.JSONException: A JSONObject text must
						// begin with '{' at character 1 of <!DOCTYPE HTML
						// PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
						// "http://www.w3.org/TR/html4/loose.dtd">
						jsonObject = JSONObject.fromObject(resInfo);
						if (StringUtils.isNotBlank(jsonObject
								.getString("c_url"))) {
							resInfo = queryLeftTickets2(date, from, to, isAdult);
							jsonObject = JSONObject.fromObject(resInfo);
							// 防止Exception in thread "main"
							// net.sf.json.JSONException: JSONObject["data"] is
							// not a JSONArray.
							jsonArray = jsonObject.getJSONArray("data");
						}
					} catch (Exception e) {
						e.printStackTrace();
						continue;
					}

					if (jsonArray == null) {
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				} while (jsonArray == null);

				for (int j = 0; j < jsonArray.size(); j++) {
					JSONObject queryLeftNewDTO = jsonArray.getJSONObject(j)
							.getJSONObject("queryLeftNewDTO");
					if (queryLeftNewDTO.getString("station_train_code").equals(
							trainCode)) {
						System.out.println(queryLeftNewDTO);
						System.out.println(queryLeftNewDTO
								.getString("canWebBuy"));

						// 　单单为此量车，因为可能会去到无座
						String[] seats = { SeatConstant.ZE, SeatConstant.ZY,
								SeatConstant.YZ, SeatConstant.RZ,
								SeatConstant.YW, SeatConstant.RW };
						type = priorityStrategy(queryLeftNewDTO, seats);

						try {
							secretStr = URLDecoder.decode(jsonArray
									.getJSONObject(j).getString("secretStr"),
									"UTF-8");
							System.out.println(secretStr);
						} catch (UnsupportedEncodingException e) {
							e.printStackTrace();
						}
						break;
					}
				}

				/**
				 * if (StringUtils.isBlank(secretStr)) { try {
				 * Thread.sleep(1000); } catch (InterruptedException e) {
				 * e.printStackTrace(); } }
				 **/

				if (StringUtils.isBlank(type)) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

			} while (StringUtils.isBlank(type));

			// 预订之前还要再次检查是否登陆(是否可以去掉2014/09/10)可以去掉
			// print("--------再次检查是否登陆-----------");
			// checkUser("https://kyfw.12306.cn/otn/login/checkUser");

			// 提交预订请求
			print("----------提交预订请求-----------");
			nvps.clear();
			nvps.add(new BasicNameValuePair("secretStr", secretStr));
			nvps.add(new BasicNameValuePair("train_date", date));// TODO
			nvps.add(new BasicNameValuePair("back_train_date", date));
			nvps.add(new BasicNameValuePair("tour_flag", "dc"));// TODO
			nvps.add(new BasicNameValuePair("purpose_codes", isAdult));
			// 这2个参数在这里其实没有用到，只需要secretStr就可以来确定
			nvps.add(new BasicNameValuePair("query_from_station_name", ""));
			nvps.add(new BasicNameValuePair("query_to_station_name", ""));
			nvps.add(new BasicNameValuePair("undefined", ""));// TODO
			System.out.println(nvps);
			resInfo = submitOrderRequest(
					"https://kyfw.12306.cn/otn/leftTicket/submitOrderRequest",
					nvps);
			System.out.println(resInfo);

			// 初始化预订页面(Dc为单程订票界面)
			print("--------------initDc---------------");
			String content = initDc(
					"https://kyfw.12306.cn/otn/confirmPassenger/initDc", null);
			String token = extractToken("globalRepeatSubmitToken", content, 1);
			token = token.substring(1, token.length() - 1);
			String ticketInfoForPassengerForm = extractToken(
					"ticketInfoForPassengerForm", content, 2);
			jsonObject = JSONObject.fromObject(ticketInfoForPassengerForm);
			String leftTicketStr = jsonObject.getString("leftTicketStr");
			String purpose_codes = jsonObject.getString("purpose_codes");
			String train_location = jsonObject.getString("train_location");
			String key_check_isChange = jsonObject
					.getString("key_check_isChange");
			JSONObject orderRequestDTO = jsonObject
					.getJSONObject("orderRequestDTO");
			String station_train_code = orderRequestDTO
					.getString("station_train_code");
			String fromStationTelecode = orderRequestDTO
					.getString("from_station_telecode");
			String toStationTelecode = orderRequestDTO
					.getString("to_station_telecode");
			String train_no = orderRequestDTO.getString("train_no");

			// 测试数据
			/**
			 * passengerTicketStr是以下划线"_"分隔当每一个乘客信息组成的字符串
			 * 座位编号(O、代表二等座),0,票类型(1、成人票),乘客名,证件类型,证件号,手机号码,保存常用联系人(Y或N)
			 */
			String passengerTicketStr = type
					+ ",0,1,赵吟霜,1,420321199207290023,18668164422,N";
			// String passengerTicketStr = type
			// + ",0,1,朱超,1,340202199201110533,18668164422,N";
			// String passengerTicketStr = type
			// + ",0,1,张宇,1,430523199309181122,18668164422,N";
			// String passengerTicketStr = type
			// + ",0,1,沈文娟,1,330281196412200424,,N";

			/**
			 * oldPassengersStr也是以下划线"_"分隔每个乘客信息组成的字符串 对应每个乘客信息字符串组成如下:
			 * 乘客名,证件类型,证件号,乘客类型
			 */
			String oldPassengerStr = "赵吟霜,1,420321199207290023,1";
			// String oldPassengerStr = "张宇,1,430523199309181122,1";
			// String oldPassengerStr = "沈文娟,1,330281196412200424,1";
			// String oldPassengerStr = "朱超,1,340202199201110533,1";

			// 得到并检查预订验证码
			getRandCode(
					"https://kyfw.12306.cn/otn/passcodeNew/getPassCodeNew?module=passenger&rand=randp",
					"");
			System.out.println("Please insert the random code:");

			try {
				Runtime.getRuntime().exec("notepad.exe");
			} catch (IOException e) {
				e.printStackTrace();
			}

			randCode = sc.next();
			System.out.println("------checkRandCodeAnsyn--------");
			nvps.clear();
			nvps.add(new BasicNameValuePair("randCode", randCode));
			nvps.add(new BasicNameValuePair("rand", "randp"));
			nvps.add(new BasicNameValuePair("REPEAT_SUBMIT_TOKEN", token));
			System.out.println(nvps);
			resInfo = checkRandCodeAnsyn(
					"https://kyfw.12306.cn/otn/passcodeNew/checkRandCodeAnsyn",
					nvps);
			System.out.println(resInfo);

			// CheckOrderInfo(是否可以去掉 2014/09/11)
			System.out.println("--------checkOrderInfo-----------");
			nvps.clear();
			nvps.add(new BasicNameValuePair("cancel_flag", "2"));
			nvps.add(new BasicNameValuePair("REPEAT_SUBMIT_TOKEN", token));
			nvps.add(new BasicNameValuePair("bed_level_order_num",
					"000000000000000000000000000000"));
			nvps
					.add(new BasicNameValuePair("oldPassengerStr",
							oldPassengerStr));
			nvps.add(new BasicNameValuePair("passengerTicketStr",
					passengerTicketStr));
			nvps.add(new BasicNameValuePair("randCode", randCode));
			nvps.add(new BasicNameValuePair("tour_flag", "dc"));
			System.out.println(nvps);
			resInfo = checkOrderInfo(
					"https://kyfw.12306.cn/otn/confirmPassenger/checkOrderInfo",
					nvps);
			print(resInfo);

			// getQueueCount 是否可以去掉(2014/09/11)
			// print("------------getQueueCount------------");
			// nvps.clear();
			// nvps.add(new BasicNameValuePair("REPEAT_SUBMIT_TOKEN", token));
			// nvps.add(new BasicNameValuePair("fromStationTelecode",
			// fromStationTelecode));
			// nvps.add(new BasicNameValuePair("leftTicket", leftTicketStr));
			// nvps.add(new BasicNameValuePair("purpose_codes", purpose_codes));
			// nvps.add(new BasicNameValuePair("seatType", "0")); // TODO
			// nvps
			// .add(new BasicNameValuePair("stationTrainCode",
			// station_train_code));
			// nvps
			// .add(new BasicNameValuePair("toStationTelecode",
			// toStationTelecode));
			// nvps.add(new BasicNameValuePair("train_date",
			// "Mon Mar 31 2014 00:00:00 GMT+0800")); // TODO
			// nvps.add(new BasicNameValuePair("train_no", train_no));
			// resInfo = getQueueCount(
			// "https://kyfw.12306.cn/otn/confirmPassenger/getQueueCount",
			// nvps);
			// print(resInfo);

			// confirmSingleForQueue
			print("-----------confirmSingleForQueue---------------");
			nvps.clear();
			nvps.add(new BasicNameValuePair("REPEAT_SUBMIT_TOKEN", token));
			nvps.add(new BasicNameValuePair("key_check_isChange",
					key_check_isChange));
			nvps.add(new BasicNameValuePair("leftTicketStr", leftTicketStr));
			nvps
					.add(new BasicNameValuePair("oldPassengerStr",
							oldPassengerStr));
			nvps.add(new BasicNameValuePair("passengerTicketStr",
					passengerTicketStr));
			nvps.add(new BasicNameValuePair("purpose_codes", purpose_codes));
			nvps.add(new BasicNameValuePair("randCode", randCode));
			nvps.add(new BasicNameValuePair("train_location", train_location));
			resInfo = confirmSingleForQueue(
					"https://kyfw.12306.cn/otn/confirmPassenger/confirmSingleForQueue",
					nvps);
			print(resInfo);
		} while (!sc.next().equals("bye"));

		print("----------------Finish-------------------");
		finish();
	}

	// 客户端调用的方法
	public void getTicket2() throws IOException {
		Scanner sc = new Scanner(System.in);
		String randCode = "";
		String resInfo = "";
		// 得到验证码
		print("-------开始验证码----------");
		getRandCode(
				"https://kyfw.12306.cn/otn/passcodeNew/getPassCodeNew.do?module=login&rand=sjrand",
				"");
		System.out.println("Please input the randCode:");
		randCode = sc.next();
		// 登陆
		print("-------开始登陆----------");
		String username = "zhuchao941";
		String password = "1231231";
		nvps.add(new BasicNameValuePair("loginUserDTO.user_name", username));
		nvps.add(new BasicNameValuePair("userDTO.password", password));
		nvps.add(new BasicNameValuePair("randCode", randCode));
		// resInfo = login("https://kyfw.12306.cn/otn/login/loginAysnSuggest",
		// TODO
		// nvps);
		System.out.println(resInfo);
		// 至此登陆成功，然后进行购票
		print("-------开始查询----------");
		String date = "2014-09-28";
		String from = "HZH";
		String to = "XMS";
		String isAdult = "ADULT";
		// 这里发的url可能是query也可能是queryT
		resInfo = queryLeftTickets(date, from, to, isAdult);
		System.out.println(resInfo);
		JSONObject jsonObject = JSONObject.fromObject(resInfo);
		JSONArray jsonArray = jsonObject.getJSONArray("data");
		int jsonLen = jsonArray.size();
		String[] secretStrs = new String[jsonLen];
		for (int j = 0; j < jsonLen; j++) {
			JSONObject train = jsonArray.getJSONObject(j).getJSONObject(
					"queryLeftNewDTO");
			secretStrs[j] = jsonArray.getJSONObject(j).getString("secretStr");
			secretStrs[j] = URLDecoder.decode(secretStrs[j], "UTF-8");
			print("车次:" + train.getString("station_train_code") + ", 出发:"
					+ train.getString("from_station_name") + ", 到达："
					+ train.getString("to_station_name") + ", 一灯座剩余:"
					+ train.getString("zy_num") + ", 二等座剩余:"
					+ train.getString("ze_num"));
		}

		// 预订之前还要再次检查是否登陆(是否可以去掉2014/09/10)
		// print("--------再次检查是否登陆-----------");
		// checkUser("https://kyfw.12306.cn/otn/login/checkUser");

		// 提交预订请求
		print("----------提交预订请求-----------");
		System.out.println("Please input the train number:");
		int trainNum = sc.nextInt();
		String secretStr = secretStrs[trainNum];
		nvps.clear();
		nvps.add(new BasicNameValuePair("secretStr", secretStr));
		nvps.add(new BasicNameValuePair("train_date", date));// TODO
		nvps.add(new BasicNameValuePair("back_train_date", date));
		nvps.add(new BasicNameValuePair("tour_flag", "dc"));// TODO
		nvps.add(new BasicNameValuePair("purpose_codes", isAdult));
		// 这2个参数在这里其实没有用到，只需要secretStr就可以来确定
		nvps.add(new BasicNameValuePair("query_from_station_name", "杭州东"));
		nvps.add(new BasicNameValuePair("query_to_station_name", "厦门"));
		nvps.add(new BasicNameValuePair("undefined", ""));// TODO
		submitOrderRequest(
				"https://kyfw.12306.cn/otn/leftTicket/submitOrderRequest", nvps);
		// 初始化预订页面(Dc为单程订票界面)
		print("--------------initDc---------------");
		String content = initDc(
				"https://kyfw.12306.cn/otn/confirmPassenger/initDc", null);
		String token = extractToken("globalRepeatSubmitToken", content, 1);
		token = token.substring(1, token.length() - 1);
		String ticketInfoForPassengerForm = extractToken(
				"ticketInfoForPassengerForm", content, 2);
		jsonObject = JSONObject.fromObject(ticketInfoForPassengerForm);
		String leftTicketStr = jsonObject.getString("leftTicketStr");
		String purpose_codes = jsonObject.getString("purpose_codes");
		String train_location = jsonObject.getString("train_location");
		String key_check_isChange = jsonObject.getString("key_check_isChange");
		JSONObject orderRequestDTO = jsonObject
				.getJSONObject("orderRequestDTO");
		String station_train_code = orderRequestDTO
				.getString("station_train_code");
		String fromStationTelecode = orderRequestDTO
				.getString("from_station_telecode");
		String toStationTelecode = orderRequestDTO
				.getString("to_station_telecode");
		String train_no = orderRequestDTO.getString("train_no");

		// 测试数据
		/**
		 * passengerTicketStr是以下划线"_"分隔当每一个乘客信息组成的字符串
		 * 座位编号(O、代表二等座),0,票类型(1、成人票),乘客名,证件类型,证件号,手机号码,保存常用联系人(Y或N)
		 */
		String passengerTicketStr = "O,0,1,赵吟霜,1,420321199207290023,18668164422,N";
		/**
		 * oldPassengersStr也是以下划线"_"分隔每个乘客信息组成的字符串 对应每个乘客信息字符串组成如下:
		 * 乘客名,证件类型,证件号,乘客类型
		 */
		String oldPassengerStr = "赵吟霜,1,420321199207290023,1";

		// 得到并检查预订验证码
		getRandCode(
				"https://kyfw.12306.cn/otn/passcodeNew/getPassCodeNew?module=passenger&rand=randp",
				"");
		System.out.println("Please insert the random code:");
		randCode = sc.next();
		System.out.println("------checkRandCodeAnsyn--------");
		nvps.clear();
		nvps.add(new BasicNameValuePair("randCode", randCode));
		nvps.add(new BasicNameValuePair("rand", "randp"));
		nvps.add(new BasicNameValuePair("REPEAT_SUBMIT_TOKEN", token));
		System.out.println(nvps);
		resInfo = checkRandCodeAnsyn(
				"https://kyfw.12306.cn/otn/passcodeNew/checkRandCodeAnsyn",
				nvps);
		System.out.println(resInfo);

		// CheckOrderInfo

		System.out.println("--------checkOrderInfo-----------");
		nvps.clear();
		nvps.add(new BasicNameValuePair("cancel_flag", "2"));
		nvps.add(new BasicNameValuePair("REPEAT_SUBMIT_TOKEN", token));
		nvps.add(new BasicNameValuePair("bed_level_order_num",
				"000000000000000000000000000000"));
		nvps.add(new BasicNameValuePair("oldPassengerStr", oldPassengerStr));
		nvps.add(new BasicNameValuePair("passengerTicketStr",
				passengerTicketStr));
		nvps.add(new BasicNameValuePair("randCode", randCode));
		nvps.add(new BasicNameValuePair("tour_flag", "dc"));
		System.out.println(nvps);
		resInfo = checkOrderInfo(
				"https://kyfw.12306.cn/otn/confirmPassenger/checkOrderInfo",
				nvps);
		print(resInfo);

		// getQueueCount
		print("------------getQueueCount------------");
		nvps.clear();
		nvps.add(new BasicNameValuePair("REPEAT_SUBMIT_TOKEN", token));
		nvps.add(new BasicNameValuePair("fromStationTelecode",
				fromStationTelecode));
		nvps.add(new BasicNameValuePair("leftTicket", leftTicketStr));
		nvps.add(new BasicNameValuePair("purpose_codes", purpose_codes));
		nvps.add(new BasicNameValuePair("seatType", "0")); // TODO
		nvps
				.add(new BasicNameValuePair("stationTrainCode",
						station_train_code));
		nvps
				.add(new BasicNameValuePair("toStationTelecode",
						toStationTelecode));
		nvps.add(new BasicNameValuePair("train_date",
				"Mon Mar 31 2014 00:00:00 GMT+0800")); // TODO
		nvps.add(new BasicNameValuePair("train_no", train_no));
		resInfo = getQueueCount(
				"https://kyfw.12306.cn/otn/confirmPassenger/getQueueCount",
				nvps);
		print(resInfo);

		// confirmSingleForQueue
		print("-----------confirmSingleForQueue---------------");
		nvps.clear();
		nvps.add(new BasicNameValuePair("REPEAT_SUBMIT_TOKEN", token));
		nvps.add(new BasicNameValuePair("key_check_isChange",
				key_check_isChange));
		nvps.add(new BasicNameValuePair("leftTicketStr", leftTicketStr));
		nvps.add(new BasicNameValuePair("oldPassengerStr", oldPassengerStr));
		nvps.add(new BasicNameValuePair("passengerTicketStr",
				passengerTicketStr));
		nvps.add(new BasicNameValuePair("purpose_codes", purpose_codes));
		nvps.add(new BasicNameValuePair("randCode", randCode));
		nvps.add(new BasicNameValuePair("train_location", train_location));
		resInfo = confirmSingleForQueue(
				"https://kyfw.12306.cn/otn/confirmPassenger/confirmSingleForQueue",
				nvps);
		print(resInfo);

		print("----------------Finish-------------------");
		finish();
	}

	private String priorityStrategy(JSONObject queryLeftNewDTO, String[] seats) {
		for (String seat : seats) {
			if (!queryLeftNewDTO.getString(seat + "_num").equals("无")
					&& !queryLeftNewDTO.getString(seat + "_num").equals("*")) {
				return SeatConstant.getSeatCode(seat);
			}
		}
		return null;
	}

	private void print(String str) {
		if (sign) {
			System.out.println(str);
		}
	}

	private void finish() {
		try {
			httpClient.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
