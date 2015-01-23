package com.zhuchao.ticket.test;

import org.apache.http.client.methods.HttpGet;

import com.zhuchao.ticket.util.HttpUtils;

public class QueryTest {
	public static void main(String args[]) {
		HttpUtils httpUtils = new HttpUtils();
//		HttpGet get = new HttpGet(
//				"https://kyfw.12306.cn/otn/leftTicket/log?leftTicketDTO.train_date=2015-02-08&leftTicketDTO.from_station=HGH&leftTicketDTO.to_station=WHN&purpose_codes=ADULT");
//		System.out.println(httpUtils.sendGet(get));
		HttpGet get = new HttpGet(
				"https://kyfw.12306.cn/otn/leftTicket/queryT?leftTicketDTO.train_date=2015-02-08&leftTicketDTO.from_station=HGH&leftTicketDTO.to_station=WHN&purpose_codes=ADULT");
		get.addHeader("If-Modified-Since", "0");
		get.addHeader("Referer", "https://kyfw.12306.cn/otn/leftTicket/init");
		get.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.65 Safari/537.36");
		get.addHeader("X-Requested-With", "XMLHttpRequest");
		get.addHeader("Cache-Control", "no-cache");
		get.addHeader("Host", "kyfw.12306.cn");
		System.out.println(httpUtils.sendGet(get));
	}
}
