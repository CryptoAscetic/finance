package com.gate.rest.util;

import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 @Description: 时间处理类.
 <pre>
 ### 时间戳与UTC 时间

 时间戳定义为自UTC 时区的1970年1 月1日 0时0点0 分至今的毫秒数

 由定义可知时间戳无时区概念. 任意服务器在同一时间点时无论自身时区如何配置应该得到同样的时间戳.

 UTC 时间是以一定格式格式化后的时间表现. 代码中常用的格式为：yyyy-MM-dd HH:mm:ss

 一个例子为：2020-01-02 03:04:05 指代2020 年1 月2 日 3 时4 分5 秒

 此格式与Mysql中默认时间格式相同

 ### Mysql 中放入时间方法

 - 需要保证与以下几点无关
 - 服务器运行时区无关
 - 数据库本身设定时区无关
 - 数据库连接设定时区无关

 确定好的使用方式

 - 使用字符串放入
 - 放入的时间为UTC时区时间
 - 代码中实现为 DateUtil.getUTCDateTime()

 ### Mysql 取出时间方法

 - 需要保证与以下几点无关
 - 服务器运行时区无关
 - 数据库本身设定时区无关
 - 数据库连接设定时区无关

 确定好的使用方式

 - 使用字符串取出
 - 取出的时间为UTC时区时间
 - SQL实现样例为 select cast(time as char) time from table;
 - 取出后Java 转化为时间戳方法为 DateUtil.fromUTCDateTime(), 返回值为一个Date 对象
 </pre>
 @author: zrs
 @UpdateDate: 20-11-6 上午11:07
 @UpdateRemark: 类及方法的注释描述
 */
public class DateUtil {
    /** 03:04:05 **/
    public static final String TIME_PATTERN = "HH:mm:ss";
    /** 2020-01-02 **/
    public static final String DATE_PATTERN = "yyyy-MM-dd";
    /** 2020-01-02 03:04:05 **/
    public static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    /** 01-02 2020 03:04 **/
    public static final String MONTH_HOUR_PATTERN = "MM-dd yyyy HH:mm";
    /** 2020-01-01T03:04:05Z  UTC时间格式 **/
    public static final String SOLR_PATTERN = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    /** 20200102030405678 **/
    public static final String ORDER_PATTERN = "yyyyMMddHHmmssSSS";
    /** 用于LocalDateTime的标准格式器 **/
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DATE_TIME_PATTERN);
    /** 用于LocalDateTime的仅日期格式器 **/
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DATE_PATTERN);
    /** 用于LocalDateTime的仅时间格式器 **/
    public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern(TIME_PATTERN);

    static final TimeZone UTC = TimeZone.getTimeZone("UTC");

    static interface FormatWorker<R> {

        R doWork(SimpleDateFormat formatter) throws Exception;
    }

    static String formatInUTC(String pattern) {
        return formatInZone(pattern, UTC, formatter -> formatter.format(new Date()));
    }

    static <R> R formatInUTC(String pattern, FormatWorker<R> worker) {
        return formatInZone(pattern, UTC, worker);
    }

    static <R> R formatInZone(String pattern, TimeZone zone, FormatWorker<R> worker) {
        SimpleDateFormat formatter = new SimpleDateFormat(pattern);
        formatter.setTimeZone(zone);
        try {
            R result = worker.doWork(formatter);
            return result;
        } catch (Exception ex) {
            throw new IllegalStateException("格式操作失败，匹配字串：" + pattern + "，时区：" + zone, ex);
        }
    }

    interface CalendarWorker<R> {

        R doWork(Calendar calendar) throws Exception;
    }

    static <R> R doInUTC(CalendarWorker<R> worker) {
        return doInUTC(new Date(), worker);
    }

    static <R> R doInUTC(Date date, CalendarWorker<R> worker) {
        return doInZone(date, UTC, worker);
    }

    static <R> R doInZone(Date date, TimeZone zone, CalendarWorker<R> worker) {
        Calendar calendar = Calendar.getInstance(zone);
        calendar.setTime(date);
        try {
            R result = worker.doWork(calendar);
            return result;
        } catch (Exception ex) {
            throw new IllegalStateException("日历操作失败，初始时间：" + date + "，时区：" + zone, ex);
        }
    }

    interface CalendarDateWorker {

        void doWork(Calendar calendar) throws Exception;
    }

    static Date changeInUTC(CalendarDateWorker worker) {
        return doInUTC(new Date(), calendar -> {
            worker.doWork(calendar);
            return calendar.getTime();
        });
    }

    static Date changeInUTC(Date date, CalendarDateWorker worker) {
        return doInZone(date, UTC, calendar -> {
            worker.doWork(calendar);
            return calendar.getTime();
        });
    }

    /**
     * String 至Date 的默认转换方法, 支持以下3 种方式
     * - long string 时间戳
     * - utc date time (2021-09-01 11:34:21)
     * - utc date (2021-09-01)
     * 其它格式会返回运行时异常
     * @param dateStr
     * @return
     */
    public static Date toDate(String dateStr) {
        try {
            long timestamp = Long.parseLong(dateStr);
            return new Date(timestamp);
        } catch (Exception ex) {
        }
        try {
            return fromUTCDateTime(dateStr);
        } catch (Exception ex) {
        }

        try {
            return fromUTCDate(dateStr);
        } catch (Exception ex) {
        }
        throw new IllegalArgumentException("错误格式的时间字符串: " + dateStr);
    }

    /**
     * @Description 将date日期转换为UTC格式的日期字符串
     * @param date Mon Nov 09 10:15:34 CST 2020
     * @return 2020-11-09T02:30:32Z
     */
    public static String toUTCSolrFormat(Date date) {
        return formatInUTC(SOLR_PATTERN, formatter -> formatter.format(date));
    }

    /**
     * @param dateTime 2020-01-01T03:04:05Z
     * @return 1577847845000
     * @Description 将UTC格式的日期字符串转换为时间戳
     */
    public static long fromUTCSolrFormat(String dateTime) {
        return formatInUTC(SOLR_PATTERN, formatter -> formatter.parse(dateTime).getTime());
    }

    /**
     * @param dateTime 2020-01-01T03:04:05
     * @return 1577847845000
     * @Description 将UTC格式的日期字符串转换为时间戳
     */
    public static long fromDateFormat(String dateTime) {
        return formatInUTC(DATE_TIME_PATTERN, formatter -> formatter.parse(dateTime).getTime());
    }

    /**
     * @param timestamp 1604887517919
     * @return 2020-11-09 02:05:17
     * @Description 将时间戳转换为日期格式的字符串
     */
    public static String toUTCDateTime(long timestamp) {
        return formatInUTC(DATE_TIME_PATTERN, formatter -> formatter.format(new Date(timestamp)));
    }

    /**
     * @Description 将Date日期转换为日期格式的字符串
     * @param  date Mon Nov 09 10:15:34 CST 2020
     * @return 2020-11-09 02:15:34
     */
    public static String toUTCDateTime(Date date) {
        return formatInUTC(DATE_TIME_PATTERN, formatter -> formatter.format(date));
    }

    /**
     * @Description 将日期格式的字符串转换为Date日期
     * @param utcDateTimeStr 2020-11-09 02:15:34
     * @return Mon Nov 09 10:15:34 CST 2020
     */
    public static Date fromUTCDateTime(String utcDateTimeStr) {
        return formatInUTC(DATE_TIME_PATTERN, formatter -> formatter.parse(utcDateTimeStr));
    }

    /**
     * @Description 将Date日期转换为年-月-日格式的日期字符串
     * @param date Mon Nov 09 10:37:09 CST 2020
     * @return 2020-11-09
     */
    public static String toUTCDate(Date date) {
        return formatInUTC(DATE_PATTERN, formatter -> formatter.format(date));
    }

    /**
     * @Description 将年-月-日格式的日期字符串转换成Date日期
     * @param utcDateStr 2020-11-09
     * @return Mon Nov 09 10:37:09 CST 2020
     */
    public static Date fromUTCDate(String utcDateStr) {
        return formatInUTC(DATE_PATTERN, formatter -> formatter.parse(utcDateStr));
    }

    /**
     * @Description 获取当前时间的年月日格式的字符串
     * @return 2020-11-09
     */
    public static String getUTCDateOnly() {
        return formatInUTC(DATE_PATTERN, formatter -> formatter.format(new Date()));
    }

    /**
     * @Description 获取当前时间的时分秒格式的字符串
     * @return 00:00:00
     */
    public static String getUTCTimeOnly() {
        return formatInUTC(TIME_PATTERN, formatter -> formatter.format(new Date()));
    }

    /**
     * @Description 获取当前时间的年月日时分秒格式的字符串
     ** @return 2020-11-09 02:45:35
     */
    public static String getUTCDateTime() {
        return formatInUTC(DATE_TIME_PATTERN, formatter -> formatter.format(new Date()));
    }

    /**
     * @Description 获取当前时间的UTC格式的字符串
     * @return 2020-11-09T02:45:35Z
     */
    public static String getUTCSolrPattern() {
        return formatInUTC(SOLR_PATTERN, formatter -> formatter.format(new Date()));
    }

    /**
     * @Description 将指定Date日期转换成指定格式的日期字符串
     * @param date
     * @param pattern
     * @return
     */
    public static String getUTCWithPattern(Date date, String pattern) {
        return formatInUTC(pattern, formatter -> formatter.format(date));
    }

    /**
     * @Description 将当前时间转换成指定格式的日期字符串
     * @param pattern
     * @return
     */
    public static String getUTCWithPattern(String pattern) {
        return formatInUTC(pattern, formatter -> formatter.format(new Date()));
    }

    /**
     * @Description 将当前时间转换成指定格式的日期字符串
     * @param pattern
     * @return
     */
    public static Date fromUTCInPattern(String dateStr, String pattern) {
        return formatInUTC(pattern, formatter -> formatter.parse(dateStr));
    }

    /**
     *
     * @param startDate 包含
     * @param endDate 包含
     * @return
     */
    public static List<String> daysIn(String startDate, String endDate) {
        List<String> days = new ArrayList<>();
        for (String day = startDate; day.compareTo(endDate) <= 0; day = dateAddDays(day, 1)) {
            days.add(day);
        }
        return days;
    }

    /**
     * @Description 根据年龄换算出生日期格式的字符串
     * @param age
     * @return
     */
    public static String getBirthdayFromAge(int age) {
        return toUTCDate(addYears(-age));
    }

    /**
     * @Description 根据指定日期格式的字符串获取到指定天数后的日期格式的字符串
     * @param day 1
     * @return 2020-01-02
     */
    public static String dateAddDays(int day) {
        return toUTCDate(addDays(day));
    }

    /**
     * @Description 根据指定日期格式的字符串获取到指定天数后的日期格式的字符串
     * @param sourceDate 2020-01-01
     * @param day 1
     * @return 2020-01-02
     */
    public static String dateAddDays(String sourceDate, int day) {
        return toUTCDate(addDays(fromUTCDate(sourceDate), day));
    }

    /**
     * @Description 根据指定日期格式的字符串获取到指定天数后的日期格式的字符串
     * @param sourceDate 2020-01-01 00:00:00
     * @param day 1
     * @return 2020-01-02 00:00:00
     */
    public static String dateTimeAddDays(String sourceDate, int day) {
        return dateTimeAddWrapper(sourceDate, day, DateUtil::addDays);
    }

    /**
     * @Description 根据指定日期格式的字符串获取到指定月份后的日期格式的字符串
     * @param sourceDate  2020-12-01 00:00:00
     * @param month 5
     * @return 2021-05-01 00:00:00
     */
    public static String dateTimeAddMonths(String sourceDate, int month) {
        return dateTimeAddWrapper(sourceDate, month, DateUtil::addMonths);
    }

    /**
     * @Description 根据指定日期格式的字符串获取到指定小时后的时间
     * @param sourceDate 2020-12-01 00:00:00
     * @param minutes 3
     * @return 2020-12-01 00:03:00
     */
    public static String dateTimeAddMinutes(String sourceDate, int minutes) {
        return dateTimeAddWrapper(sourceDate, minutes, DateUtil::addMinutes);
    }

    /**
     * @Description 计算出Date日期转换为日期格式的字符串
     * @param sourceDate
     * @param addNumber
     * @param coreFunction
     * @return String 时间格式的字符串
     */
    public static String dateTimeAddWrapper(String sourceDate, int addNumber,
                                            BiFunction<Date, Integer, Date> coreFunction) {
        return toUTCDateTime(coreFunction.apply(fromUTCDateTime(sourceDate), addNumber));
    }

    /**
     * @Description 获取当前日期时间经过指定天数后的日期格式的字符串
     * @param day
     * @return 2020-01-01 00:00:00
     */
    public static String dateTimeAddDays(int day) {
        return dateTimeAddWrapper(day, DateUtil::addDays);
    }

    /**
     * @Description 获取当前日期格式的字符串 经过指定月份 后的日期格式的字符串
     * @param month
     * @return 2020-01-01 00:00:00
     */
    public static String dateTimeAddMonths(int month) {
        return dateTimeAddWrapper(month, DateUtil::addMonths);
    }


    /**
     * @Description 获取当前日期格式的字符串 经过指定分钟 后的日期格式的字符串
     * @param minutes
     * @return 2020-01-01 00:00:00
     */
    public static String dateTimeAddMinutes(int minutes) {
        return dateTimeAddWrapper(minutes, DateUtil::addMinutes);
    }

    /**
     * @Description 将Date日期转换为日期格式的字符串
     * @param addNumber
     * @param coreFunction
     * @return String 时间格式的字符串
     */
    public static String dateTimeAddWrapper(int addNumber, Function<Integer, Date> coreFunction) {
        return toUTCDateTime(coreFunction.apply(addNumber));
    }

    /**
     * @Description 根据指定Date日期格式 经过几天后 返回的Date日期数据
     * @param sourceDate
     * @param day 需要增加的天数
     * @return Sat Nov 14 11:44:23 CST 2020
     * @author wenhao
     */
    public static Date addDays(Date sourceDate, int day) {
        return changeInUTC(sourceDate, cal -> cal.add(Calendar.DATE, day));
    }

    /**
     * @Description 获取当前时间的Date日期 经过指定天数后 返回的Date日期数据
     * @param day
     * @return Sat Nov 14 11:44:23 CST 2020
     */
    public static Date addDays(int day) {
        return addDays(new Date(), day);
    }

    /**
     * @Description 根据指定Date日期格式 经过指定秒后 返回的Date日期数据
     * @param sourceDate
     * @param second
     * @return Sat Nov 14 11:44:23 CST 2020
     */
    public static Date addSeconds(Date sourceDate, int second) {
        return changeInUTC(sourceDate, cal -> cal.add(Calendar.SECOND, second));
    }

    /**
     * @Description 根据指定Date日期格式 经过指定秒后 返回的Date日期数据
     * @param second
     * @return Sat Nov 14 11:44:23 CST 2020
     */
    public static Date addSeconds(int second) {
        return addSeconds(new Date(), second);
    }

    /**
     * @Description 根据指定Date日期格式 经过指定分钟后 返回的Date日期数据
     * @param sourceDate
     * @param minute
     * @return Sat Nov 14 11:44:23 CST 2020
     */
    public static Date addMinutes(Date sourceDate, int minute) {
        return changeInUTC(sourceDate, cal -> cal.add(Calendar.MINUTE, minute));
    }

    /**
     * @Description 获取当前时间的Date日期 经过指定分钟后 返回的Date日期数据
     * @param minute
     * @return Sat Nov 14 11:44:23 CST 2020
     */
    public static Date addMinutes(int minute) {
        return addMinutes(new Date(), minute);
    }

    /**
     * @Description 根据指定Date日期格式 经过指定月份后 返回的Date日期数据
     * @param sourceDate
     * @param month 需要增加的月数
     * @return Sat Nov 14 11:44:23 CST 2020
     * @author wenhao
     */
    public static Date addMonths(Date sourceDate, int month) {
        return changeInUTC(sourceDate, cal -> cal.add(Calendar.MONTH, month));
    }

    /**
     * @Description 获取当前时间的Date日期 经过指定月份后 返回的Date日期数据
     * @param month
     * @return Sat Nov 14 11:44:23 CST 2020
     */
    public static Date addMonths(int month) {
        return addMonths(new Date(), month);
    }

    /**
     * @Description 根据指定Date日期格式 经过指定年份后 返回的的Date日期数据
     * @param sourceDate
     * @param year 需要增加的年数
     * @return Sat Nov 14 11:44:23 CST 2020
     * @author wenhao
     */
    public static Date addYears(Date sourceDate, int year) {
        return changeInUTC(sourceDate, cal -> cal.add(Calendar.YEAR, year));
    }

    /**
     * @Description 获取当前时间的Date日期 经过指定年份后 返回的Date日期数据
     * @param year
     * @return Sat Nov 14 11:44:23 CST 2020
     */
    public static Date addYears(int year) {
        return addYears(new Date(), year);
    }

    public static int age(String birthday) {
        return age(birthday, getUTCDateOnly());
    }

    public static int age(String birthday, String checkDay) {
        LocalDate birthdayDate = LocalDate.parse(
                birthday, DATE_FORMATTER
        );
        LocalDate checkDate = LocalDate.parse(checkDay, DATE_FORMATTER);
        if (birthdayDate.compareTo(checkDate) >= 0) {
            throw new IllegalStateException(String.format("生日%s应当比当前时间 %s 小", birthday, checkDate));
        }
        if (birthdayDate.getYear() == checkDate.getYear()) {
            return 0;
        } else {
            LocalDate adjustedDate2 = checkDate.withYear(birthdayDate.getYear());
            if (adjustedDate2.compareTo(birthdayDate) >= 0) {
                return checkDate.getYear() - birthdayDate.getYear();
            } else {
                return checkDate.getYear() - birthdayDate.getYear() - 1;
            }
        }
    }

    /**
     * 得到两个指定Date 间的持续
     * @param d1
     * @param d2
     * @return
     */
    public static Duration between(Date d1, Date d2) {
        return Duration.between(Instant.ofEpochMilli(d1.getTime()), Instant.ofEpochMilli(d2.getTime()));
    }

    /**
     * 得到从现在到指定时间的持续
     * 如: 当前为10 点, 指定时间为11 点. 则得到1h
     * @param date
     * @return
     */
    public static Duration durationTo(Date date) {
        return Duration.between(Instant.ofEpochMilli(System.currentTimeMillis()), Instant.ofEpochMilli(date.getTime()));
    }

    /**
     * 得到从现在到指定时间的持续
     * 如: 指定时间为9 点, 当前为10 点, 则得到1h
     * @param date
     * @return
     */
    public static Duration durationFrom(Date date) {
        return Duration.between(Instant.ofEpochMilli(date.getTime()), Instant.ofEpochMilli(System.currentTimeMillis()));
    }

    /**
     * 将指定的UTC LocalDateTime 转换为Date
     * @param utcLocalDateTime
     * @return
     */
    public static Date fromUTCLocalDateTime(LocalDateTime utcLocalDateTime) {
        return Date.from(utcLocalDateTime.toInstant(ZoneOffset.UTC));
    }

    /**
     * 将指定的Date 转换为UTC LocalDateTime
     * @param date
     * @return
     */
    public static LocalDateTime toUTCLocalDateTime(Date date) {
        return toUTCLocalDateTime(date.getTime());
    }

    /**
     * 将指定的时间戳 转换为UTC LocalDateTime
     * @param timestamp
     * @return
     */
    public static LocalDateTime toUTCLocalDateTime(long timestamp) {
        Instant instant = Instant.ofEpochMilli(timestamp);
        LocalDateTime utcLocalDateTime = LocalDateTime.ofEpochSecond(
                instant.getEpochSecond(), instant.getNano(), ZoneOffset.UTC);
        return utcLocalDateTime;
    }

    /**
     * 将指定的时间戳转换为UTC LocalDate
     * @param timestamp
     * @return
     */
    public static LocalDate toUTCLocalDate(long timestamp) {
        return toUTCLocalDateTime(timestamp).toLocalDate();
    }

    /**
     * 将指定的Date 转换为UTC LocalDate
     * @param date
     * @return
     */
    public static LocalDate toUTCLocalDate(Date date) {
        return toUTCLocalDateTime(date).toLocalDate();
    }

    /**
     * 将指定的UTC LocalDateTime 转换为Date
     * @param utcLocalDate
     * @return
     */
    public static Date fromUTCLocalDate(LocalDate utcLocalDate) {
        return Date.from(utcLocalDate.atStartOfDay().toInstant(ZoneOffset.UTC));
    }

    /**
     * @Description 得到当月的最后一天是几号(换月根据UTC 时区判断)
     * @return lastDayOfCurrentMonth 比如 30,31
     */
    public static int getLastDayOfCurrentMonth() {
        return doInUTC(cal -> {
            cal.set(Calendar.DATE, 1);
            cal.roll(Calendar.DATE, -1);
            int lastDayOfCurrentMonth = cal.get(Calendar.DATE);
            return lastDayOfCurrentMonth;
        });
    }

    /**
     * @Description 得到今天还剩余多少秒(换天根据UTC 时区判断)
     * @return 比如 82497秒
     */
    public static long getTodayRemainSeconds() {
        LocalDateTime localDateTime = getUTCLocalDateTime();
        return ChronoUnit.SECONDS.between(
                localDateTime, localDateTime.plusDays(1).truncatedTo(ChronoUnit.DAYS));
    }

    /**
     * @Description 检查一个UTC时间串是否到期（是否小于当前系统时间）
     * @param checking 2020-01-01 00:00:00
     * @return 到期 True， 未到期False
     */
    public static boolean dateTimeExpired(String checking) {
        return fromUTCDateTime(checking).getTime() < System.currentTimeMillis();
    }

    /**
     * @Description 检查是否到期（是否小于当前系统时间）
     * @param checking
     * @return 到期 True， 未到期False
     */
    public static boolean expired(Date checking) {
        return checking.getTime() < System.currentTimeMillis();
    }


    /**
     * @Description 得到一个表示当前时间的LocalDateTime 对象(UTC 时区)
     * @return 2020-11-10T00:57:28
     */
    public static LocalDateTime getUTCLocalDateTime() {
        return LocalDateTime.now(Clock.systemUTC());
    }


    /**
     * @Description 得到一个表示当前时间的LocalDate 对象(UTC 时区)
     * @return 2020-11-10T00:57:28
     */
    public static LocalDate getUTCLocalDate() {
        return LocalDate.now(Clock.systemUTC());
    }

    /**
     * @Description 将DateTime 字符串 用LocalDateTime 对象表示
     * @param dateTime 2020-01-01 00:00:00
     * @return 2020-01-01T00:00
     */
    public static LocalDateTime toLocalDateTime(String dateTime) {
        return LocalDateTime.parse(dateTime, DATE_TIME_FORMATTER);
    }

    /**
     * @Description 将LocalDateTime(本地日期的当前时间) 对象用DateTime 字符串表示. 注意LocalDateTime 类不包含任何时区信息.
     * @param localDateTime (2020-01-01T00:00)
     * @return String 2020-01-01 00:00:00
     */
    public static String toDateTime(LocalDateTime localDateTime) {
        return localDateTime.format(DATE_TIME_FORMATTER);
    }

    /**
     * 将时间转化为可读字符串
     * @param costInMillis
     * @return
     */
    public static String readableMillis(long costInMillis) {
        if (costInMillis < 10000) { // 10 秒
            return String.format("%d毫秒", costInMillis); // 毫秒
        } else if (costInMillis < 100000) { // 100 秒
            long second = costInMillis / 1000;
            long ms = costInMillis % 1000;
            return String.format("%d秒%d毫秒", second, ms); // 秒, 毫秒
        } else if (costInMillis < 3600000) { // 1 小时
            long minute = costInMillis / 60000;
            costInMillis %= 60000;
            long second = costInMillis / 1000;
            long ms = costInMillis % 1000;
            return String.format("%d分钟%d秒", minute, second); // 分钟, 秒
        } else if (costInMillis < 86400000) { // 1 天
            long hour = costInMillis / 3600000;
            costInMillis %= 3600000;
            long minute = costInMillis / 60000;
            costInMillis %= 60000;
            long second = costInMillis / 1000;
            long ms = costInMillis % 1000;
            return String.format("%d小时%d分钟%d秒", // 小时, 分钟, 秒, 毫秒
                    hour, minute, second, ms);
        } else { // 大于1 天
            long day = costInMillis / 86400000;
            costInMillis %= 86400000;
            long hour = costInMillis / 3600000;
            costInMillis %= 3600000;
            long minute = costInMillis / 60000;
            costInMillis %= 60000;
            long second = costInMillis / 1000;
            long ms = costInMillis % 1000;
            return String.format("%d天%d小时%d分钟", // 天, 小时, 分钟, 秒, 毫秒
                    day, hour, minute, second, ms);
        }
    }
}
