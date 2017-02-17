package com.sina.data.util;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.logicalcobwebs.proxool.ProxoolException;
import org.logicalcobwebs.proxool.ProxoolFacade;
import org.logicalcobwebs.proxool.admin.SnapshotIF;
import org.logicalcobwebs.proxool.configuration.PropertyConfigurator;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBPoolManager {

  private static int activeCount = 0;
  private static final Logger logger = Logger.getLogger(DBPoolManager.class);

  static {
    try {
      Properties p = new Properties();
      p.load(Thread.currentThread().getContextClassLoader()
          .getResourceAsStream("proxool.properties"));
      PropertyConfigurator.configure(p);
      // PropertyConfigurator.configure("src/main/resources/proxool.properties");
      Class.forName("org.logicalcobwebs.proxool.ProxoolDriver");// proxool
                                                                // Driver Class
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

  /**
   * get connection
   * 
   * @param name
   * @return
   */
  public static Connection getConnection(String poolname) {
    try {
      Connection conn = DriverManager.getConnection("proxool." + poolname);
      if (logger.getLevel() == Level.DEBUG)
        showSnapshotInfo(poolname);
      return conn;
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    return null;
  }

  /**
   * use to get the infomation of pool,can used when debug showSnapshotInfo
   */
  private static void showSnapshotInfo(String poolname) {
    try {
      SnapshotIF snapshot = ProxoolFacade.getSnapshot(poolname, true);
      int curActiveCount = snapshot.getActiveConnectionCount();// get the active
                                                               // connection num
      int availableCount = snapshot.getAvailableConnectionCount();// get the
                                                                  // available
                                                                  // connection
                                                                  // num
      int maxCount = snapshot.getMaximumConnectionCount();
      if (curActiveCount != activeCount) {
        logger.info("active nums:" + curActiveCount
            + "(active)  available nums:" + availableCount
            + "(available)  max num:" + maxCount + "(max)");
        activeCount = curActiveCount;
      }
    } catch (ProxoolException e) {
      e.printStackTrace();
    }
  }

  /**
   * free connection freeConnection
   * 
   * @param conn
   */
  public static void freeConnection(Connection conn) {
    if (conn != null) {
      try {
        conn.close();
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }
  }

  public static void main(String[] args) throws SQLException {
    Connection con = getConnection("db_cluster_monitor");
    System.out.println(con);
  }

}