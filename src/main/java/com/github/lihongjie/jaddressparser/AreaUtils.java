package com.github.lihongjie.jaddressparser;

import cn.hutool.core.lang.Pair;
import cn.hutool.core.util.StrUtil;

import java.util.*;

public class AreaUtils {

    enum TargetType {
        PROVINCE, CITY, AREA;
        private TargetType(){}
    }

    public static List<AreaInfo> getTargetAreaListByCode(TargetType target, String code) {
        return getTargetAreaListByCode(target, code, false);
    }

    public static List<AreaInfo> getTargetAreaListByCode(TargetType target, String code, boolean parent) {
        // 获取父对象
        if(parent) return getTargetParentAreaListByCode(target, code);


        List<AreaInfo> result = new ArrayList<>();
        Map<String, String> list;

        AreaEntity entity = new AreaCache().getEntity();
        switch (target) {
            case CITY:
                list = entity.getCityList();
                break;
            case AREA:
                list = entity.getAreaList();
                break;
            case PROVINCE:
                list = entity.getProvinceList();
                break;
            default:
                throw new RuntimeException("The target is not below.");
        }
        if(null != list && StrUtil.isNotBlank(code)) {
            String provinceCode = code.substring(0, 2);
            String cityCode = code.substring(2, 4);
            if(target == TargetType.AREA && !cityCode.equals("00")) {
                code = provinceCode + cityCode;
                for (int i = 0; i < 100; i++) {
                    String _code = code + String.format("%02d", i);
                    if(null != list.get(_code)) {
                        result.add(new AreaInfo(_code, list.get(_code)));
                    }
                }
            } else {
                for (int i = 0; i < 91; i++) {  //最大编码只到91
                    code = provinceCode + String.format("%02d", i) + (target == TargetType.CITY ? "00" : "");
                    if(target == TargetType.CITY) {
                        if(null != list.get(code)) {
                            result.add(new AreaInfo(code, list.get(code)));
                        }
                    } else {
                        for (int j = 0; j < 100; j++) {
                            String _code = code + String.format("%02d", j);
                            if(null != list.get(_code)) {
                                result.add(new AreaInfo(_code, list.get(_code)));
                            }
                        }
                    }
                }
            }
        } else {
            for (String key : list.keySet()) {
                result.add(new AreaInfo(key, list.get(key)));
            }
        }

        return result;
    }

    /**
     * 根据code取父省市对象
     * @param target
     * @param code
     * @return
     */
    private static List<AreaInfo> getTargetParentAreaListByCode(TargetType target, String code) {
        AreaEntity entity = new AreaCache().getEntity();
        Map<String, String> provinceList = entity.getProvinceList();
        Map<String, String> cityList = entity.getCityList();
        Map<String, String> areaList = entity.getAreaList();
        List<AreaInfo> result = new ArrayList<>();
        result.add(0, new AreaInfo(code, areaList.get(code)));
        if(target == TargetType.CITY || target == TargetType.PROVINCE) {
            code = code.substring(0, 4) + "00";
            result.add(0, new AreaInfo(code, cityList.get(code)));
        }
        if(target == TargetType.PROVINCE) {
            code = code.substring(0, 2) + "0000";
            result.add(0, new AreaInfo(code, provinceList.get(code)));
        }
        return result;
    }



    public static Pair<Integer, String> shortIndexOf(String address, String shortName, String name) {
        int index = address.indexOf(shortName);
        String matchName = shortName;
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
        return new Pair<>(index, matchName);
    }

    public static void main(String[] args) {
        List<AreaInfo> res = AreaUtils.getTargetAreaListByCode(TargetType.AREA, "140900", false);
        res.forEach(System.out::println);
    }


    static class AreaInfo {
        String code;
        String name;

        public AreaInfo(String code, String name) {
            this.code = code;
            this.name = name;
        }

        @Override
        public String toString() {
            return "AreaInfo{" +
                    "code='" + code + '\'' +
                    ", name='" + name + '\'' +
                    '}';
        }
    }
}
