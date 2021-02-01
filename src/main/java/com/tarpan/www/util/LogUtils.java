package com.tarpan.www.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author: sekift
 * @date: 2020/10/20 11:15
 * @description: 日志处理统一管理类
 */
public class LogUtils {

    /**
     * 错误输入日志
     */
    public static final Logger log = LoggerFactory.getLogger(LogUtils.class);

    /**
     * 记录info信息
     *
     * @param message
     */
    public static void logInfo(String message) {
        StringBuilder s = new StringBuilder();
        s.append((message));
        log.info(s.toString());
    }

    public static void logInfo(String message, Throwable e) {
        StringBuilder s = new StringBuilder();
        s.append(("exception : -->>"));
        s.append((message));
        log.info(s.toString(), e);
    }

    public static void logWarn(String message) {
        StringBuilder s = new StringBuilder();
        s.append((message));

        log.warn(s.toString());
    }

    public static void logWarn(String message, Throwable e) {
        StringBuilder s = new StringBuilder();
        s.append(("exception : -->>"));
        s.append((message));
        log.warn(s.toString(), e);
    }

    public static void logDebug(String message) {
        StringBuilder s = new StringBuilder();
        s.append((message));
        log.debug(s.toString());
    }

    public static void logDebug(String message, Throwable e) {
        StringBuilder s = new StringBuilder();
        s.append(("exception : -->>"));
        s.append((message));
        log.debug(s.toString(), e);
    }

    public static void logError(String message) {
        StringBuilder s = new StringBuilder();
        s.append(message);
        log.error(s.toString());
    }

    public static void logError(Exception e) {
        log.error(getException(e));
    }

    /**
     * 记录日志错误信息
     *
     * @param message
     * @param e
     */
    public static void logError(String message, Throwable e) {
        StringBuilder s = new StringBuilder();
        s.append(("exception : -->>"));
        s.append((message));
        log.error(s.toString(), e);
    }

    /**
     * 将异常转为明显的string输出
     *
     * @param e
     * @return
     */
    public static String getException(Exception e) {
        StackTraceElement[] ste = e.getStackTrace();
        StringBuffer sb = new StringBuffer();
        sb.append(e.getMessage() + "\r\n");
        for (int i = 0; i < ste.length; i++) {
            sb.append("\t" + ste[i].toString() + "\r\n");
        }
        return sb.toString();
    }
}
