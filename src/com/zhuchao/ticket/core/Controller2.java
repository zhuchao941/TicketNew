package com.zhuchao.ticket.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import net.sf.json.JSONObject;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlImage;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.util.NameValuePair;
import com.zhuchao.ticket.entity.User;
import com.zhuchao.ticket.util.LogUtils;

public class Controller2 {

	public Controller2() {
		webClient = new WebClient();
		webClient.getOptions().setUseInsecureSSL(true);
		webClient.setWebConnection(new MyHttpWebConnection(webClient));
	}

	private final WebClient webClient;

	public String login(User user, String randCode) {
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		// 登陆
		LogUtils.info(getClass(), "-------开始登陆----------");
		String username = user.getUsername();
		String password = user.getPassword();
		JSONObject jsonObject = null;

		nvps.add(new NameValuePair("loginUserDTO.user_name", username));
		nvps.add(new NameValuePair("userDTO.password", password));
		try {
			buildExtraParam2(nvps);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// nvps.add(new BasicNameValuePair("randCode_validate", ""));
		// nvps.add(new BasicNameValuePair("myversion", "undefined"));
		LogUtils.info(getClass(), "-------开始验证码----------");

		String resInfo = "";
		do {
			// // 得到验证码
			// ((MyHttpWebConnection) webClient.getWebConnection())
			// .downloadImage(
			// "https://kyfw.12306.cn/otn/passcodeNew/getPassCodeNew.do?module=login&rand=sjrand",
			// "rand.jpg");
			// System.out.println("Please input the randCode:");
			// randCode = sc.next();
			// 先移除randCode
			// if (nvps.size() > 2) {
			// nvps.remove(2);
			// }
			// nvps.add(new NameValuePair("randCode", randCode));
			System.out.println(nvps);
			resInfo = ((MyHttpWebConnection) webClient.getWebConnection())
					.sendPost(
							"https://kyfw.12306.cn/otn/login/loginAysnSuggest",
							nvps);
			System.out.println(resInfo);
			jsonObject = JSONObject.fromObject(resInfo);
		} while (!jsonObject.getString("messages").equals("[]"));
		return resInfo;
	}

	public void buildExtraParam2(List<NameValuePair> nvps) throws Exception {
		final HtmlPage page = webClient
				.getPage("https://kyfw.12306.cn/otn/login/init");

		final HtmlImage image = (HtmlImage) page
				.getElementById("img_rand_code");
		image.saveAs(new File("rand.jpg"));
		System.out.println(page.asXml());
		System.out.println("Enter:");
		Scanner sc = new Scanner(System.in);
		String randCode = sc.next();
		nvps.add(new NameValuePair("randCode", randCode));
		final HtmlAnchor button = (HtmlAnchor) page.getElementById("loginSub");
		final HtmlPage page2 = button.click();
		Document doc = Jsoup.parse(page2.asXml());
		Elements elements = doc.select("#loginForm input[type=hidden]");
		for (Element element : elements) {
			nvps.add(new NameValuePair(element.attr("name"), element
					.attr("value")));
		}

		// Set<Cookie> cookies = webClient.getCookies(new URL(
		// "https://kyfw.12306.cn/otn/login/init"));
		// for (Cookie cookie : cookies) {
		// System.out.println(cookie.getValue());
		// }
		// webClient.closeAllWindows();
	}

	/**
	 * 执行js函数，得到需要的值的值
	 * 
	 * @param paras
	 * @return
	 * @throws ScriptException
	 * @throws FileNotFoundException
	 * @throws NoSuchMethodException
	 */
	public static String ExcuteJs(String script, String paras)
			throws ScriptException, FileNotFoundException,
			NoSuchMethodException {
		ScriptEngineManager engineManager = new ScriptEngineManager();
		ScriptEngine engine = engineManager.getEngineByName("JavaScript"); // 得到脚本引擎
		engine.eval(new java.io.FileReader("resources/lhbb.js"));
		// engine.eval(script);
		Invocable inv = (Invocable) engine;
		Object a = inv.invokeFunction("gc", paras);
		return a.toString();
	}

	public static void main(String args[]) {
		User user = new User();
		user.setUsername("ZYS0729");
		user.setPassword("zs920630");
		new Controller2().login(user, "");
	}
}
