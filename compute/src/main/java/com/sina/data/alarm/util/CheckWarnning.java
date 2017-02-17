package com.sina.data.alarm.util;



import com.sina.data.alarm.WarnningServer;
import com.sina.data.util.DBPoolManager;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class CheckWarnning {
	private static String loadSql = "select * from monitor_recored;";
	public static void main(String[] args) {
		WarnningServer server = new WarnningServer();
		Connection con = DBPoolManager.getConnection("monitorX");
		Statement stat = null;
		try {
			stat = con.createStatement();
			ResultSet rs = stat.executeQuery(loadSql);
			int counter = 0;
			while(rs.next()){
				counter++;
				System.out.println(counter);
				ReceiveBean bean = new ReceiveBean();
				bean.setCurrent_value(rs.getDouble("current_value"));
				bean.setHost(rs.getString("host"));
				bean.setMetric(rs.getString("metric"));
				bean.setOn_off(rs.getString("on_off"));
				bean.setOthers(rs.getString("others"));
				System.out.println(rs.getString("others"));
				bean.setRegister_name(rs.getString("register_name"));
				bean.setThreshold(rs.getDouble("threshold"));
				bean.setTimestamp(rs.getInt("timestamp"));
				server.putReceive(bean);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				stat.close();
				con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
}
