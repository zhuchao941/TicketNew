package com.zhuchao.ticket.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import net.sf.json.JSONObject;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlImage;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.util.Cookie;
import com.zhuchao.ticket.entity.User;
import com.zhuchao.ticket.util.HttpUtils;
import com.zhuchao.ticket.util.LogUtils;

public class ControllerNew {

	private HttpUtils httpUtils = new HttpUtils();
	private Scanner sc = new Scanner(System.in);

	public String login(User user, String randCode) {
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		// 登陆
		LogUtils.info(getClass(), "-------开始登陆----------");
		String username = user.getUsername();
		String password = user.getPassword();
		JSONObject jsonObject = null;

		nvps.add(new BasicNameValuePair("loginUserDTO.user_name", username));
		nvps.add(new BasicNameValuePair("userDTO.password", password));
		nvps.add(new BasicNameValuePair("myversion", "undefined"));
		buildExtraParam(nvps);

		// nvps.add(new BasicNameValuePair("randCode_validate", ""));
		// nvps.add(new BasicNameValuePair("myversion", "undefined"));
		LogUtils.info(getClass(), "-------开始验证码----------");

		String resInfo = "";
		do {
			// 得到验证码
			httpUtils
					.downloadImage(
							"https://kyfw.12306.cn/otn/passcodeNew/getPassCodeNew.do?module=login&rand=sjrand",
							"rand.jpg");
			System.out.println("Please input the randCode:");
			randCode = sc.next();
			// 先移除randCode
			// if (nvps.size() > 2) {
			// nvps.remove(2);
			// }
			nvps.add(new BasicNameValuePair("randCode", randCode));
			System.out.println(nvps);
			resInfo = httpUtils.sendPost(
					"https://kyfw.12306.cn/otn/login/loginAysnSuggest", nvps);
			System.out.println(resInfo);
			jsonObject = JSONObject.fromObject(resInfo);
		} while (!jsonObject.getString("messages").equals("[]"));
		return resInfo;
	}

	public void buildExtraParam2(List<NameValuePair> nvps) throws Exception {
		final WebClient webClient = new WebClient();
		webClient.getOptions().setUseInsecureSSL(true);
		final HtmlPage page = webClient
				.getPage("https://kyfw.12306.cn/otn/login/init");

		final HtmlAnchor button = (HtmlAnchor) page.getElementById("loginSub");

		final HtmlForm form = (HtmlForm) page.getElementById("loginForm");
		final HtmlImage image = (HtmlImage) page
				.getElementById("img_rand_code");
		image.saveAs(new File("rand.jpg"));
		System.out.println(page.asXml());
		System.out.println("Enter:");
		Scanner sc = new Scanner(System.in);
		String randCode = sc.next();
		final HtmlInput randCodeInput = form.getInputByName("randCode");
		final HtmlInput username = form
				.getInputByName("loginUserDTO.user_name");
		final HtmlInput password = form.getInputByName("userDTO.password");
		username.setValueAttribute("zhuchao941");
		password.setValueAttribute("1231231");
		randCodeInput.setValueAttribute(randCode);
		final HtmlPage page2 = button.click();
		System.out.println(page2.asText());
		Set<Cookie> cookies = webClient.getCookies(new URL(
				"https://kyfw.12306.cn/otn/login/init"));
		for (Cookie cookie : cookies) {
			System.out.println(cookie.getValue());
		}
		webClient.closeAllWindows();
	}

	private void buildExtraParam(List<NameValuePair> nvps) {
		String content = httpUtils
				.sendGet("https://kyfw.12306.cn/otn/login/init");
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
			fuck = ExcuteJs(null, key, "1111");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (ScriptException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}

		nvps.add(new BasicNameValuePair(key, fuck));
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
	public static String ExcuteJs(String script, Object... paras)
			throws ScriptException, FileNotFoundException,
			NoSuchMethodException {
		ScriptEngineManager engineManager = new ScriptEngineManager();
		ScriptEngine engine = engineManager.getEngineByName("JavaScript"); // 得到脚本引擎
		engine.eval(new java.io.FileReader("resources/fuck.js"));
		// engine.eval(script);
		Invocable inv = (Invocable) engine;
		Object a = inv.invokeFunction("fuck", paras);
		return a.toString();
	}

	public static void main(String args[]) {
		// try {
		// String a = ExcuteJs(null, "aaa", "bbb");
		// System.out.println(a);
		// } catch (FileNotFoundException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// } catch (ScriptException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// } catch (NoSuchMethodException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		User user = new User();
		user.setUsername("ZYS0729");
		user.setPassword("zs920630");
		new ControllerNew().login(user, "");
	}
}
