package com.github.lihongjie.jaddressparser;

import cn.hutool.core.util.ReUtil;


public class AddressParser {

    private static final String[] EXCLUDE_KEYS = new String[]{"发件人", "收货地址", "收货人", "收件人", "收货", "手机号码", "邮编", "电话", "所在地区", "详细地址", "地址", "：", ":", "；", ";", "，", ",", "。", "、"};
    private String text;
    private ParserResult result;

    public AddressParser(String text) {
        this.text = text;
    }

    public void parse(String address, boolean isParseAll) {
//        this.replace();
//        this.parseMobile();
//        this.parsePhone();
//        this.parseZipCode();
//        address = address.replaceAll("/ {2,}/", " ");
//        List<AreaParserResult> parse = new AreaParser().parse(address, isParseAll);
//        parse.forEach(System.out::println);
    }

    /**
     * 替换无效字符
     */
    private void replace() {
        String text = this.text;
        for (String reg : EXCLUDE_KEYS) {
            text = text.replaceAll(reg, " ");
        }
        text = text.replaceAll("/\\r\\n/g", " ").replace("/\n/g", " ").replace("/\t/g", " ").replace("/ {2,}/g", " ");
        text = ReUtil.replaceAll(text,"/(\\d{3})-(\\d{4})-(\\d{4})/g", "$1$2$3").replaceAll("/(\\d{3}) (\\d{4}) (\\d{4})/g", "$1$2$3");
        this.text = text;
    }

    /**
     * 提取手机号码
     */
    private void parseMobile() {
        String text = this.text;
        String mobile = ReUtil.getGroup0("/(86-[1][0-9]{10})|(86[1][0-9]{10})|([1][0-9]{10})/g", text);
        if(null != mobile) {
            this.result.mobile = mobile;
            this.text = text.replaceAll(mobile, " ");
        }

    }

    /**
     * 提取电话号码
     */
    private void parsePhone() {
        String text = this.text;
        String phone = ReUtil.getGroup0("/(([0-9]{3,4}-)[0-9]{7,8})|([0-9]{12})|([0-9]{11})|([0-9]{10})|([0-9]{9})|([0-9]{8})|([0-9]{7})/g", text);
        if(null != phone) {
            this.result.phone = phone;
            this.text = text.replaceAll(phone, " ");
        }
    }

    /**
     * 提取邮编
     */
    private void parseZipCode() {
        String text = this.text;
        String zipCode = ReUtil.getGroup0("/([0-9]{6})/g", text);
        if(null != zipCode) {
            this.result.zipCode = zipCode;
            this.text = text.replaceAll(zipCode, " ");
        }
    }


    // TODO: 2020/8/17 提取姓名




}

class ParserResult {
    String mobile;
    String phone;
    String zipCode;
    String address;
}
