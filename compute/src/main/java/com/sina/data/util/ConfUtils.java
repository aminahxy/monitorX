package com.sina.data.util;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.log4j.Logger;

import java.io.File;
import java.net.URL;

/**
 * Using apache common-configuration jar to reload configuration file
 * 
 * @date Oct 18, 2013
 * 
 */
public class ConfUtils {
  private static final Logger LOG = Logger.getLogger(ConfUtils.class);

  private static final PropertiesConfiguration conf = new PropertiesConfiguration();

  static {
    URL url = ConfUtils.class.getClassLoader()
        .getResource("monitor.properties");
    File file = new File(url.getFile());
    try {
      conf.load(file);
      FileChangedReloadingStrategy strategy = new FileChangedReloadingStrategy();
      conf.setReloadingStrategy(strategy);
    } catch (ConfigurationException e) {
      LOG.error("Failed to load configuration file=" + file.toString(), e);
    }
    LOG.info("Successfully load configuration file=" + file.toString());
  }

  public static String getString(String key, String defaultValue) {
    return conf.getString(key, defaultValue);
  }

  public static int getInt(String key, int defaultValue) {
    return conf.getInt(key, defaultValue);
  }

  public static long getLong(String key, long defaultValue) {
    return conf.getLong(key, defaultValue);
  }

  public static double getDouble(String key, double defaultValue) {
    return conf.getDouble(key, defaultValue);
  }

  public static float getFloat(String key, float defaultValue) {
    return conf.getFloat(key, defaultValue);
  }

}
