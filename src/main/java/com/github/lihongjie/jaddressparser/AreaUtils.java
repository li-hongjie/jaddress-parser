package com.github.lihongjie.jaddressparser;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AreaUtils {

    enum TargetType {
        PROVINCE, CITY, AREA;
        private TargetType(){}
    }

    public static Map<String, String> getTargetAreaListByCode(TargetType target, String code) {
        return getTargetAreaListByCode(target, code, false);
    }

    public static Map<String, String> getTargetAreaListByCode(TargetType target, String code, boolean parent) {
        if(parent); // TODO: 2020/8/18 获取父对象  getTargetParentAreaListByCode

        Map<String, String> result = new HashMap<>();
        Map<String, String> list;

        AreaEntity entity = new AreaCache().getEntity();
        switch (target) {
            case CITY:
                list = entity.getCityList();
                break;
            case AREA:
                list = entity.getAreaList();
                break;
            default:
                throw new RuntimeException("The target is not below.");
        }
        Assert.notNull(list);
        Assert.notNull(code);
        String provinceCode = code.substring(0, 2);
        String cityCode = code.substring(2, 4);
        if(target == TargetType.AREA && !cityCode.equals("00")) {
            code = provinceCode + cityCode;
            for (int i = 0; i < 100; i++) {
                String _code = code + String.format("%02d", i);
                if(null != list.get(_code)) {
                    result.put(_code, list.get(_code));
                }
            }
        } else {
            for (int i = 0; i < 91; i++) {  //最大编码只到91
                code = provinceCode + String.format("%02d", i) + (target == TargetType.CITY ? "00" : "");
                if(target == TargetType.CITY) {
                    if(null != list.get(code)) {
                        result.put(code, list.get(code));
                    }
                } else {
                    for (int j = 0; j < 100; j++) {
                        String _code = code + String.format("%02d", j);
                        if(null != list.get(_code)) {
                            result.put(_code, list.get(_code));
                        }
                    }
                }
            }
        }
        return result;
    }

    public static int shortIndexOf(String address, String shortName, String name, String matchName) {
        int index = address.indexOf(shortName);
        matchName = shortName;
        if(index > -1) {
            for (int i = shortName.length(); i <= name.length(); i++) {
                String _name = name.substring(0, i);
                int _index = address.indexOf(_name);
                if(_index > -1) {
                    index = _index;
                    matchName = _name;
                } else {
                    break;
                }
            }
        }
        return index;
    }

    public static void main(String[] args) {
        Map<String, String> code = AreaUtils.getTargetAreaListByCode(TargetType.AREA, "140900", false);
        code.keySet().stream().forEach((k) -> System.out.println(k + ":" + code.get(k)));
    }


}
