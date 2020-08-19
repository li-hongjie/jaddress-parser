package com.github.lihongjie.jaddressparser.model;

public class AddressParserResult {

    public String province;
    public String city;
    public String area;
    public String details;
    public String name;
    public String code;
    public String __type;
    public boolean __parse = false;
    public int __score = 0;

    public AddressParserResult() {}

    public AddressParserResult(String __type) {
        this.__type = __type;
    }

    @Override
    public String toString() {
        return "AddressParserResult{" +
                "province='" + province + '\'' +
                ", city='" + city + '\'' +
                ", area='" + area + '\'' +
                ", details='" + details + '\'' +
                ", name='" + name + '\'' +
                ", code='" + code + '\'' +
                ", __type='" + __type + '\'' +
                ", __parse=" + __parse +
                ", __score=" + __score +
                '}';
    }
}
