package com.zhuchao.ticket.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Vector;

import javax.script.ScriptException;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.cqz.dm.UUHelper;
import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.ScriptResult;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlImage;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.util.NameValuePair;
import com.zhuchao.ticket.constant.SeatConstant;
import com.zhuchao.ticket.entity.Train;
import com.zhuchao.ticket.entity.User;
import com.zhuchao.ticket.util.HttpUtilsFinal;
import com.zhuchao.ticket.util.StringUtils;

public class ControllerFinal {

	private Logger logger = Logger.getLogger(this.getClass());
	private HttpUtilsFinal httpUtils = new HttpUtilsFinal();
	private Map<String, String> dataMap = new Hashtable<String, String>();
	private List<NameValuePair> nvpsForInitDc = new Vector<NameValuePair>();
	private List<Thread> threadList = new Vector<Thread>();
	private boolean found;

	public static void main(String args[]) {
		Calendar start = Calendar.getInstance();
		List<String> dates = new ArrayList<String>();
		// String[] trainCodes = { "G590", "G594" };
		String[] trainCodes = { "G594" };
		start.set(2015, 1, 12);
		Calendar end = Calendar.getInstance();
		end.set(2015, 1, 13);
		while (start.before(end)) {
			dates.add(StringUtils.format(start.getTime(), "yyyy-MM-dd"));
			start.add(Calendar.DAY_OF_MONTH, 1);
		}
		// start.set(2015, 1, 13);
		// dates.add(StringUtils.format(start.getTime(), "yyyy-MM-dd"));
		multiple(dates, trainCodes);
	}

	public static void multiple(List<String> dates, String[] trainCodes) {

		User user = new User();
		user.setUsername("ZYS0729");
		user.setPassword("zs920630");
//		user.setUsername("zhuchao941");
//		user.setPassword("1231231");
		ControllerFinal controllerFinal = new ControllerFinal();

		List<Train> trains = new ArrayList<Train>();
		for (String date : dates) {
			for (String trainCode : trainCodes) {

				Train train = new Train();
				/**
				 * 杭州：HZH 武汉：WHN G594 G590 武汉：WHN 十堰：SNN
				 */
				train.setFrom("HZH");
				train.setTo("WHN");
				train.setDate(date);
				train.setTrainCode(trainCode);
				trains.add(train);
			}
		}
		controllerFinal.grabTicket(user, trains);
	}

	// public static void single() {
	// User user = new User();
	// user.setUsername("ZYS0729");
	// user.setPassword("zs920630");
	//
	// Train train = new Train();
	// /**
	// * 杭州：HZH 武汉：WHN G594 G590 武汉：WHN 十堰：SNN
	// */
	// train.setFrom("HZH");
	// train.setTo("WHN");
	// train.setDate("2015-02-10");
	// train.setTrainCode("G590");
	// ControllerFinal controllerFinal = new ControllerFinal();
	// controllerFinal.grabTicket(user, train);
	// }

	/**
	 * 单用户抢多个车票
	 * 
	 * @param user
	 * @param trains
	 */
	public void grabTicket(User user, List<Train> trains) {
		// 首先需要登陆
		logger.debug("开始登陆----------------------------------");
		login(user);

		for (Train train : trains) {
			Thread thread = new Grab(train);
			thread.start();
			threadList.add(thread);
		}
	}

	class Grab extends Thread {

		private Train train;

		public Grab(Train train) {
			this.train = train;
			this.setName(train.getTrainCode() + ":" + train.getDate());
		}

		@Override
		public void run() {
			grab(train);
		}

		private void grab(Train train) {
			List<NameValuePair> nvps = init(); // init也就是查询余票界面，会有一个dynamicJs
			query(train, nvps);
			// foundIt();
			submitOrderRequest(nvps);
			initDc();
			lastThing();
		}

		private void foundIt() {
			for (Thread thread : threadList) {
				if (thread != this) {
					thread.interrupt();
				}
			}
		}
	}

	private void lastThing() {
		checkRandCodeAnsyn();
		checkOrderInfo();
		confirmSingleForQueue();
	}

	private void confirmSingleForQueue() {
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps
				.add(new NameValuePair("REPEAT_SUBMIT_TOKEN", dataMap
						.get("token")));
		nvps.add(new NameValuePair("key_check_isChange", dataMap
				.get("key_check_isChange")));
		nvps.add(new NameValuePair("leftTicketStr", dataMap
				.get("leftTicketStr")));
		nvps.add(new NameValuePair("oldPassengerStr", dataMap
				.get("oldPassengerStr")));
		nvps.add(new NameValuePair("passengerTicketStr", dataMap
				.get("passengerTicketStr")));
		nvps.add(new NameValuePair("purpose_codes", dataMap
				.get("purpose_codes")));
		nvps.add(new NameValuePair("randCode", dataMap.get("randCode")));
		nvps.add(new NameValuePair("train_location", dataMap
				.get("train_location")));
		System.out.println(nvps);
		String resInfo = httpUtils
				.sendPost(
						"https://kyfw.12306.cn/otn/confirmPassenger/confirmSingleForQueue",
						nvps);
		System.out.println(resInfo);
		JSONObject jsonObject = JSONObject.fromObject(resInfo);
		if (!jsonObject.getJSONObject("data").getBoolean("submitStatus")) {
			confirmSingleForQueue();
		}
	}

	private void checkOrderInfo() {
		String oldPassengerStr = "赵吟霜,1,420321199207290023,1";
		String passengerTicketStr = dataMap.get("seatType")
				+ ",0,1,赵吟霜,1,420321199207290023,18668164422,N";

		dataMap.put("oldPassengerStr", oldPassengerStr);
		dataMap.put("passengerTicketStr", passengerTicketStr);

		nvpsForInitDc.add(new NameValuePair("cancel_flag", "2"));
		nvpsForInitDc.add(new NameValuePair("REPEAT_SUBMIT_TOKEN", dataMap
				.get("token")));
		nvpsForInitDc.add(new NameValuePair("bed_level_order_num",
				"000000000000000000000000000000"));
		nvpsForInitDc
				.add(new NameValuePair("oldPassengerStr", oldPassengerStr));
		nvpsForInitDc.add(new NameValuePair("passengerTicketStr",
				passengerTicketStr));
		nvpsForInitDc
				.add(new NameValuePair("randCode", dataMap.get("randCode")));
		nvpsForInitDc.add(new NameValuePair("tour_flag", "dc"));
		System.out.println(nvpsForInitDc);

		String resInfo = httpUtils.sendPost(
				"https://kyfw.12306.cn/otn/confirmPassenger/checkOrderInfo",
				nvpsForInitDc);
		/**
		 * {"validateMessagesShowId":"_validatorMessage","status":true,
		 * "httpstatus"
		 * :200,"data":{"errMsg":"非法请求","submitStatus":false},"messages"
		 * :[],"validateMessages":{}}
		 */
		JSONObject jsonObject = JSONObject.fromObject(resInfo);
		if (!jsonObject.getBoolean("status")
				|| !jsonObject.getJSONObject("data").getBoolean("submitStatus")) {
			logger.error(resInfo);
		}
	}

	private void checkRandCodeAnsyn() {
		getRandCode("https://kyfw.12306.cn/otn/passcodeNew/getPassCodeNew?module=passenger&rand=randp");
		
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new NameValuePair("rand", "randp"));
		nvps
				.add(new NameValuePair("REPEAT_SUBMIT_TOKEN", dataMap
						.get("token")));
		//inputRandCode(nvps);
		autoInputRandCode(nvps);
		String resInfo = httpUtils.sendPost(
				"https://kyfw.12306.cn/otn/passcodeNew/checkRandCodeAnsyn",
				nvps);
		JSONObject jsonObject = JSONObject.fromObject(resInfo);

		if (!jsonObject.getBoolean("status")
				|| !jsonObject.getJSONObject("data").getString("result")
						.equals("1")) {
			logger.info("验证码错误，请重试..................");
			logger.info(resInfo);
			checkRandCodeAnsyn();
		} else {
			logger.info("验证成功");
			logger.info(resInfo);
		}
	}

	private void submitOrderRequest(List<NameValuePair> nvps) {
		logger.info("nvps = " + nvps);

		String content = httpUtils
				.sendPost(
						"https://kyfw.12306.cn/otn/leftTicket/submitOrderRequest",
						nvps);
		logger.info("submitOrderRequest:" + content);
	}

	@SuppressWarnings("unused")
	@Deprecated
	private void buildExtraParam(String content, List<NameValuePair> nvps) {
		Document doc = Jsoup.parse(content);
		Elements elements = doc.getElementsByTag("script");
		String dynamicJs = null;
		for (Element element : elements) {
			if (element.attr("src").startsWith("/otn/dynamicJs")) {
				dynamicJs = element.attr("src");
			}
		}
		content = httpUtils.sendGet("https://kyfw.12306.cn" + dynamicJs);
		int start = content.indexOf("var key='") + "var key='".length();
		int end = content.indexOf("'", start);
		String fuck = null;
		String key = content.substring(start, end);

		try {
			fuck = StringUtils.ExcuteJs("resources/fuck.js", "fuck", key,
					"1111");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (ScriptException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}

		nvps.add(new NameValuePair(key, fuck));
	}

	private void login(User user) {
		List<NameValuePair> nvps = null;
		while (nvps == null) {
			try {
				nvps = getLoginParam();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		nvps
				.add(new NameValuePair("loginUserDTO.user_name", user
						.getUsername()));
		nvps.add(new NameValuePair("userDTO.password", user.getPassword()));

		inputRandCode(nvps);
		//autoInputRandCode(nvps);

		doLogin(nvps);
	}

	/**
	 * 这个是进入查询余票界面时的post请求 这里也是新版才需要的，为了得到新的dynamicJs
	 */
	private synchronized List<NameValuePair> init() {
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		try {
			getDynamicJs2("https://kyfw.12306.cn/otn/leftTicket/init", nvps);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return nvps;
	}

	/**
	 * 这一步貌似一定要在submitOrderRequest成功之后提交才会有效果
	 * 这TM的是一个get请求，不应该用post抓，但是sendPost可以，但是getPage用post为啥不行？ 明天再看看
	 * 这里已经解决了（2014/12/20），不管get和post的关系，而是浏览器若选择chrome就会报错，而现在改用了FF，就没问题了
	 */
	private void initDc() {
		// 初始化预订页面(Dc为单程订票界面)
		logger.info("--------------initDc---------------");
		ArrayList<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new NameValuePair("_json_att", ""));
		// String content = httpUtils.sendPost(
		// "https://kyfw.12306.cn/otn/confirmPassenger/initDc", nvps);
		HtmlPage page = null;
		try {
			WebRequest webRequest = new WebRequest(new URL(
					"https://kyfw.12306.cn/otn/confirmPassenger/initDc"),
					HttpMethod.POST);
			webRequest.setRequestParameters(nvps);
			// 这个请求 如 用 chrome浏览器 来模拟 不知道为什么会报错？现在改用FF
			page = httpUtils.getWebClient().getPage(webRequest);
			getDynamicJs2(page, nvpsForInitDc);
		} catch (Exception e) {
			e.printStackTrace();
		}
		String content = page.asXml();

		String token = StringUtils.extractToken("globalRepeatSubmitToken",
				content, 1);
		token = token.substring(1, token.length() - 1);
		String ticketInfoForPassengerForm = StringUtils.extractToken(
				"ticketInfoForPassengerForm", content, 2);
		logger.info("ticketInfoForPassengerForm = "
				+ ticketInfoForPassengerForm);
		JSONObject jsonObject = JSONObject
				.fromObject(ticketInfoForPassengerForm);
		String leftTicketStr = jsonObject.getString("leftTicketStr");
		String purpose_codes = jsonObject.getString("purpose_codes");
		String train_location = jsonObject.getString("train_location");
		String key_check_isChange = jsonObject.getString("key_check_isChange");

		dataMap.put("token", token);
		dataMap.put("leftTicketStr", leftTicketStr);
		dataMap.put("purpose_codes", purpose_codes);
		dataMap.put("train_location", train_location);
		dataMap.put("key_check_isChange", key_check_isChange);
		logger.info("dataMap = " + dataMap);

	}

	/**
	 * 这个方法本来是想用来模拟点击的,但是不知道为什么模拟点击总是会报错,所以一般放弃模拟点击,还是模拟请求来的靠谱
	 * 
	 * @param train
	 * @throws Exception
	 */
	@SuppressWarnings("unused")
	@Deprecated
	private void query2(Train train) throws Exception {

		WebClient webClient = httpUtils.getWebClient();
		final HtmlPage page = webClient
				.getPage("https://kyfw.12306.cn/otn/leftTicket/init");
		final HtmlForm queryLeftForm = (HtmlForm) page
				.getElementById("queryLeftForm");
		final HtmlInput from = queryLeftForm
				.getInputByName("leftTicketDTO.from_station");
		final HtmlInput to = queryLeftForm
				.getInputByName("leftTicketDTO.to_station");
		final HtmlInput date = queryLeftForm
				.getInputByName("leftTicketDTO.train_date");
		from.setValueAttribute(train.getFrom());
		to.setValueAttribute(train.getTo());
		date.setValueAttribute(train.getDate());

		final HtmlAnchor queryBtn = (HtmlAnchor) page
				.getElementById("query_ticket");
		final HtmlPage page2 = queryBtn.click();
		webClient.waitForBackgroundJavaScript(30000);
		System.out.println(page2.asXml());
	}

	/**
	 * 查询车票,并组织下一步submitOrderRequest需要发送的参数
	 */
	private void query(Train train, List<NameValuePair> nvps) {
		// 至此登陆成功，然后进行购票

		logger.info("-------开始查询----------");

		String type = null; // 票种（硬座、硬卧等）
		String resInfo = null;
		String date = train.getDate();
		String from = train.getFrom();
		String to = train.getTo();
		String isAdult = "ADULT";
		String trainCode = train.getTrainCode();
		String secretStr = null;
		JSONObject jsonObject = null;

		do {
			// 第一步 得到所有火车 余票信息
			JSONArray jsonArray = null; // 火车票余票信息
			while (true) {
				try {
					resInfo = queryLeftTickets(date, from, to, isAdult);
				} catch (MalformedURLException e2) {
					e2.printStackTrace();
				}
				try {
					jsonObject = JSONObject.fromObject(resInfo);
					if (StringUtils.isNotBlank(jsonObject.getString("c_url"))) {
						resInfo = queryLeftTickets2(date, from, to, isAdult);
						jsonObject = JSONObject.fromObject(resInfo);
						jsonArray = jsonObject.getJSONArray("data");
					}
				} catch (Exception e) {
					logger.error(resInfo);
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
					continue;
				}

				if (jsonArray == null) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				} else {
					break;
				}
			}

			// 过滤出 感兴趣的火车的 余票信息
			for (int j = 0; j < jsonArray.size(); j++) {
				JSONObject queryLeftNewDTO = jsonArray.getJSONObject(j)
						.getJSONObject("queryLeftNewDTO");
				if (queryLeftNewDTO.getString("station_train_code").equals(
						trainCode)) {
					System.out.println(queryLeftNewDTO);
					System.out.println(queryLeftNewDTO.getString("canWebBuy"));

					// 座位seatType的优先策略
					String[] seats = { SeatConstant.ZE, SeatConstant.ZY,
							SeatConstant.YZ, SeatConstant.RZ, SeatConstant.YW,
							SeatConstant.RW };
					type = priorityStrategy(queryLeftNewDTO, seats);

					try {
						secretStr = URLDecoder.decode(jsonArray
								.getJSONObject(j).getString("secretStr"),
								"UTF-8");
						System.out.println("secretStr = " + secretStr);
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
					break;
				}
			}

			if (StringUtils.isBlank(type)) {
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

		} while (StringUtils.isBlank(type) && !found);

		logger.info("seatType = " + type);
		// 另外需要再得到 两个参数

		dataMap.put("seatType", type);
		nvps.add(new NameValuePair("secretStr", secretStr));
		nvps.add(new NameValuePair("train_date", date));// TODO
		nvps.add(new NameValuePair("back_train_date", date));
		nvps.add(new NameValuePair("tour_flag", "dc"));// TODO
		nvps.add(new NameValuePair("purpose_codes", isAdult));
		// 这2个参数在这里其实没有用到，只需要secretStr就可以来确定
		nvps.add(new NameValuePair("query_from_station_name", ""));
		nvps.add(new NameValuePair("query_to_station_name", ""));
		nvps.add(new NameValuePair("undefined", ""));// TODO

		found = true;
		try {
			Runtime.getRuntime().exec("cmd /c notepad");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 获得指定url页面的dynamicJs中包含的关键key(无需这样搞了,麻烦,直接调用submitForm,HtmlUnit太屌了)
	 * 
	 * @param url
	 * @param nvps
	 */
	@SuppressWarnings("unused")
	@Deprecated
	private void getDynamicJs(String url, List<NameValuePair> nvps) {
		String content = httpUtils.sendPost(
				"https://kyfw.12306.cn/otn/leftTicket/init",
				new ArrayList<NameValuePair>());

		Document doc = Jsoup.parse(content);
		Elements elements = doc.getElementsByTag("script");
		String dynamicJs = null;
		for (Element element : elements) {
			if (element.attr("src").startsWith("/otn/dynamicJs")) {
				dynamicJs = element.attr("src");
			}
		}
		content = httpUtils.sendGet("https://kyfw.12306.cn" + dynamicJs);
		int start = content.indexOf("var key='") + "var key='".length();
		int end = content.indexOf("'", start);
		String fuck = null;
		String key = content.substring(start, end);

		try {
			fuck = StringUtils.ExcuteJs("resources/fuck.js", "fuck", key,
					"1111");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (ScriptException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}

		logger.info("key = " + key + ", value = " + fuck);
		nvps.add(new NameValuePair(key, fuck));
	}

	/**
	 * 直接调用submitForm来得到，太屌了！
	 * 
	 * @param url
	 * @param nvps
	 * @throws Exception
	 */
	private void getDynamicJs2(String url, List<NameValuePair> nvps)
			throws Exception {
		WebRequest request = new WebRequest(new URL(url), HttpMethod.POST);
		final HtmlPage page = httpUtils.getWebClient().getPage(request);
		// httpUtils.getWebClient().waitForBackgroundJavaScript(3000);
		getDynamicJs2(page, nvps);
	}

	/**
	 * 直接调用submitForm来得到，太屌了！
	 * 
	 * @param url
	 * @param nvps
	 * @throws Exception
	 */
	private void getDynamicJs2(HtmlPage page, List<NameValuePair> nvps)
			throws Exception {
		ScriptResult sr = page.executeJavaScript("submitForm()");
		String result = sr.getJavaScriptResult().toString();
		String[] arr = result.split(":::");
		String key1 = arr[0].split(",-,")[0];
		String value1 = arr[0].split(",-,")[1];
		String key2 = arr[1].split(",-,")[0];
		String value2 = arr[1].split(",-,")[1];
		nvps.add(new NameValuePair(key1, value1));
		nvps.add(new NameValuePair(key2, value2));
	}

	/**
	 * 座位类型的优先策略
	 * 
	 * @param queryLeftNewDTO
	 * @param seats
	 * @return
	 */
	private String priorityStrategy(JSONObject queryLeftNewDTO, String[] seats) {
		for (String seat : seats) {
			if (!queryLeftNewDTO.getString(seat + "_num").equals("无")
					&& !queryLeftNewDTO.getString(seat + "_num").equals("*")
					&& !queryLeftNewDTO.getString(seat + "_num").equals("--")) {
				return SeatConstant.getSeatCode(seat);
			}
		}
		return null;
	}

	public String queryLeftTickets(String date, String from, String to,
			String isAdult) throws MalformedURLException {
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
		WebRequest get = new WebRequest(new URL(
				"https://kyfw.12306.cn/otn/leftTicket/query?leftTicketDTO.train_date="
						+ date + "&leftTicketDTO.from_station=" + from
						+ "&leftTicketDTO.to_station=" + to + "&purpose_codes="
						+ isAdult), HttpMethod.GET);
		get.setAdditionalHeader("If-Modified-Since", "0");
		get.setAdditionalHeader("Referer",
				"https://kyfw.12306.cn/otn/leftTicket/init");
		get
				.setAdditionalHeader(
						"User-Agent",
						"Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.65 Safari/537.36");
		get.setAdditionalHeader("X-Requested-With", "XMLHttpRequest");
		get.setAdditionalHeader("Cache-Control", "no-cache");
		get.setAdditionalHeader("Host", "kyfw.12306.cn");
		return httpUtils.sendGet(get);
	}

	public String queryLeftTickets2(String date, String from, String to,
			String isAdult) throws MalformedURLException {
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
		WebRequest get = new WebRequest(new URL(
				"https://kyfw.12306.cn/otn/leftTicket/queryT?leftTicketDTO.train_date="
						+ date + "&leftTicketDTO.from_station=" + from
						+ "&leftTicketDTO.to_station=" + to + "&purpose_codes="
						+ isAdult), HttpMethod.GET);
		get.setAdditionalHeader("If-Modified-Since", "0");
		get.setAdditionalHeader("Referer",
				"https://kyfw.12306.cn/otn/leftTicket/init");
		get
				.setAdditionalHeader(
						"User-Agent",
						"Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.65 Safari/537.36");
		get.setAdditionalHeader("X-Requested-With", "XMLHttpRequest");
		get.setAdditionalHeader("Cache-Control", "no-cache");
		get.setAdditionalHeader("Host", "kyfw.12306.cn");
		return httpUtils.sendGet(get);
	}

	/**
	 * 登陆 请求的方法 （包含了失败 重新获取 验证码 再次登陆）
	 * 
	 * {"validateMessagesShowId":"_validatorMessage","status":true,"httpstatus":
	 * 200,"data":{"loginCheck":"Y"},"messages":[],"validateMessages":{}}
	 * 
	 * @param nvps
	 */
	public void doLogin(List<NameValuePair> nvps) {
		String resInfo = httpUtils.sendPost(
				"https://kyfw.12306.cn/otn/login/loginAysnSuggest", nvps);
		JSONObject jsonObject = JSONObject.fromObject(resInfo);

		if (!jsonObject.getBoolean("status")
				|| !jsonObject.getJSONArray("messages").isEmpty()) {

			logger.info("登陆失败....................");
			logger.info(resInfo);
			getRandCode("https://kyfw.12306.cn/otn/passcodeNew/getPassCodeNew.do?module=login&rand=sjrand");
			inputRandCode(nvps);
			doLogin(nvps); // 递归执行，直到成功为止
		} else {
			logger.info("登陆成功");
			logger.info(resInfo);
		}
	}

	private void getRandCode(String url) {
		httpUtils.downloadImage(url, "rand.jpg");
	}

	private void inputRandCode(List<NameValuePair> nvps) {
		Iterator<NameValuePair> iterator = nvps.iterator();
		while (iterator.hasNext()) {
			NameValuePair nameValuePair = iterator.next();
			if (nameValuePair.getName().equals("randCode")) {
				iterator.remove();
			}
		}
		System.out.println("Enter:");
		Scanner sc = new Scanner(System.in);
		String randCode = sc.nextLine();
		nvps.add(new NameValuePair("randCode", randCode));
		dataMap.put("randCode", randCode); // 第二部验证码的后续一步要用到
	}
	
	private void autoInputRandCode(List<NameValuePair> nvps) {
		Iterator<NameValuePair> iterator = nvps.iterator();
		while (iterator.hasNext()) {
			NameValuePair nameValuePair = iterator.next();
			if (nameValuePair.getName().equals("randCode")) {
				iterator.remove();
			}
		}
		
		String[] result = null;
		try {
			result = UUHelper.getRandResult("rand.jpg", "zhuchao941", "ZHAOyinshuang0630");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		nvps.add(new NameValuePair("randCode", result[1]));
		dataMap.put("randCode", result[1]); // 第二部验证码的后续一步要用到
	}

	/**
	 * 获取登陆必要的参数
	 * 
	 * @return
	 * @throws Exception
	 */
	private List<NameValuePair> getLoginParam() throws Exception {

		final HtmlPage page = httpUtils.getWebClient().getPage(
				"https://kyfw.12306.cn/otn/login/init");

		final HtmlImage image = (HtmlImage) page
				.getElementById("img_rand_code");
		image.saveAs(new File("rand.jpg"));

		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		final HtmlAnchor button = (HtmlAnchor) page.getElementById("loginSub");
		final HtmlPage page2 = button.click();
		Document doc = Jsoup.parse(page2.asXml());
		Elements elements = doc.select("#loginForm input[type=hidden]");
		for (Element element : elements) {
			nvps.add(new NameValuePair(element.attr("name"), element
					.attr("value")));
		}
		return nvps;
	}

	/**
	 * 运行js，得到额外的一个加密参数和myversion(废弃,理由同buildExtraParam)
	 * 
	 * @param nvps
	 */
	@SuppressWarnings("unused")
	@Deprecated
	private void buildExtraParam(HtmlPage page, List<NameValuePair> nvps) {
		DomNodeList<DomElement> elements = page.getElementsByTagName("script");
		String dynamicJs = null;
		for (DomElement element : elements) {
			if (element.getAttribute("src").startsWith("/otn/dynamicJs")) {
				dynamicJs = element.getAttribute("src");
			}
		}
		String content = httpUtils.sendGet("https://kyfw.12306.cn" + dynamicJs);
		int start = content.indexOf("var key='") + "var key='".length();
		int end = content.indexOf("'", start);
		try {
			nvps.add(new NameValuePair(content.substring(start, end),
					URLEncoder.encode("MWYzY2E1MmQxZTE1NDExOQ==", "utf-8")));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
}
