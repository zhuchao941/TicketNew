package com.zhuchao.ticket.constant;

import java.util.HashMap;
import java.util.Map;

public class SeatConstant {

	/**
	 * 商务座
	 */
	public final static String SWZ = "swz";
	/**
	 * 特等座
	 */
	public final static String TZ = "tz";
	/**
	 * 一等座
	 */
	public final static String ZY = "zy";
	/**
	 * 二等座
	 */
	public final static String ZE = "ze";
	/**
	 * 高级软卧
	 */
	public final static String GR = "gr";
	/**
	 * 软卧
	 */
	public final static String RW = "rw";
	/**
	 * 硬卧
	 */
	public final static String YW = "yw";
	/**
	 * 软座
	 */
	public final static String RZ = "rz";
	/**
	 * 硬座
	 */
	public final static String YZ = "yz";
	/**
	 * 无座
	 */
	public final static String WZ = "wz";
	/**
	 * 其它
	 */
	public final static String QT = "qt";

	private static final Map<String, String> map;

	/**
	 * if (bS == "ZY") { bR = "M" } if (bS == "ZE") { bR = "O" } if (bS ==
	 * "SWZ") { bR = "9" } if (bS == "TZ") { bR = "P" } if (bS == "YZ") { bR =
	 * "1" } if (bS == "RZ") { bR = "2" } if (bS == "YW") { bR = "3" } if (bS ==
	 * "RW") { bR = "4" } if (bS == "GR") { bR = "6" } if (bS == "WZ") { bR =
	 * "WZ" }
	 */
	static {
		map = new HashMap<String, String>();
		map.put(SeatConstant.ZY, "M");
		map.put(SeatConstant.ZE, "O");
		map.put(SeatConstant.SWZ, "9");
		map.put(SeatConstant.TZ, "P");
		map.put(SeatConstant.YZ, "1");
		map.put(SeatConstant.RZ, "2");
		map.put(SeatConstant.YW, "3");
		map.put(SeatConstant.RW, "4");
		map.put(SeatConstant.GR, "6");
		map.put(SeatConstant.WZ, "WZ");
	}

	public static String getSeatCode(String seat) {
		return map.get(seat);
	}
}