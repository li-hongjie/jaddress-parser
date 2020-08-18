package com.github.lihongjie.jaddressparser;

import cn.hutool.core.lang.Pair;
import cn.hutool.core.util.StrUtil;

import java.util.*;

public class AreaParser {

    public static final String[] PROVINCE_KEYS = new String[] {
            "特别行政区", "古自治区", "维吾尔自治区", "壮族自治区", "回族自治区", "自治区", "省省直辖", "省", "市"
    };

    public static final String[] CITY_KEYS = new String[] {
            "布依族苗族自治州", "苗族侗族自治州", "藏族羌族自治州", "哈尼族彝族自治州", "壮族苗族自治州", "傣族景颇族自治州", "蒙古族藏族自治州",
            "傣族自治州", "白族自治州", "藏族自治州", "彝族自治州", "回族自治州", "蒙古自治州", "朝鲜族自治州", "地区", "哈萨克自治州", "盟", "市"
    };

    private static final String[] AREA_KEYS = new String[] {
            "满族自治县", "满族蒙古族自治县", "蒙古族自治县", "朝鲜族自治县",
            "回族彝族自治县", "彝族回族苗族自治县", "彝族苗族自治县", "土家族苗族自治县", "布依族苗族自治县", "苗族布依族自治县",
            "彝族傣族自治县", "傣族彝族自治县", "仡佬族苗族自治县", "黎族苗族自治县", "苗族侗族自治县", "哈尼族彝族傣族自治县", "哈尼族彝族自治县",
            "彝族哈尼族拉祜族自治县", "傣族拉祜族佤族自治县", "傣族佤族自治县", "拉祜族佤族布朗族傣族自治县", "苗族瑶族傣族自治县", "彝族回族自治县",
            "独龙族怒族自治县", "保安族东乡族撒拉族自治县", "回族土族自治县", "撒拉族自治县", "哈萨克自治县", "塔吉克自治县",
            "回族自治县", "畲族自治县", "土家族自治县", "布依族自治县", "苗族自治县", "瑶族自治县", "侗族自治县", "水族自治县", "傈僳族自治县",
            "仫佬族自治县", "毛南族自治县", "黎族自治县", "羌族自治县", "彝族自治县", "藏族自治县", "纳西族自治县", "裕固族自治县", "哈萨克族自治县",
            "哈尼族自治县", "拉祜族自治县", "佤族自治县",
            "左旗", "右旗", "中旗", "后旗", "联合旗", "自治旗", "旗", "自治县",
            "街道办事处",
            "新区", "区", "县", "市"
    };

    private static final Map<String, String> PROVINCE_SHORT;
    private static final Map<String, String> CITY_SHORT;
    private static final Map<String, String> AREA_SHORT;


    static {
        PROVINCE_SHORT = new HashMap<String, String>();
        CITY_SHORT = new HashMap<String, String>();
        AREA_SHORT = new HashMap<String, String>();
        AreaCache cache = new AreaCache();
        AreaEntity areaEntity = cache.getEntity();
        Map<String, String> provinceList = areaEntity.getProvinceList();
        Map<String, String> cityList = areaEntity.getCityList();
        Map<String, String> areaList = areaEntity.getAreaList();
        for(String code : provinceList.keySet()) {
            String province = provinceList.get(code);
            for (String key : PROVINCE_KEYS) {
                province = province.replaceAll(key, "");
            }
            PROVINCE_SHORT.put(code, province);
        }

        for(String code : cityList.keySet()) {
            String city = cityList.get(code);
            for (String key : CITY_KEYS) {
                city = city.replaceAll(key, "");
            }
            CITY_SHORT.put(code, city);
        }

        for(String code : areaList.keySet()) {
            String area = areaList.get(code);
            if("雨花台区".equals(area)) area = "雨花区";
            if(area.length() > 2 && !"高新区".equals(area)) {
                for (String key : AREA_KEYS) {
                    area = area.indexOf(key) > 1 ? area.replaceAll(key, "") : area;
                }
            }
            AREA_SHORT.put(code, area);
        }

    }

    /**
     * 开始匹配
     * @param address
     * @param isParseAll
     * @return
     */
    public List<AreaParserResult> parse(String address, boolean isParseAll) {

        List<AreaParserResult> results = new ArrayList<>();
        //正向解析
        results.addAll(0, parseByProvince(address));
        if(isParseAll || null == results.get(0) || !results.get(0).__parse) {
            //逆向城市解析 通过所有CityShort匹配
            results.addAll(0, parseByCity(address));
        }

        return results;

    }

    /**
     * 1.1 提取省份
     * @param address
     * @return
     */
    private List<AreaParserResult> parseByProvince(String address) {
        List<AreaParserResult> results = new ArrayList<>();
        AreaCache cache = new AreaCache();
        AreaEntity areaEntity = cache.getEntity();
        Map<String, String> provinceList = areaEntity.getProvinceList();
        ProvinceParserResult result = new ProvinceParserResult();

        String addressSrc = address;

        for(String code : provinceList.keySet()) {
            String province = provinceList.get(code);
            int index = addressSrc.indexOf(province);
            String shortProvince = index > -1 ? "" : PROVINCE_SHORT.get(code);
            int provinceLength = StrUtil.isNotBlank(shortProvince) ? shortProvince.length() : province.length();
            if(StrUtil.isNotBlank(shortProvince)) {
                index = address.indexOf(shortProvince);
            }

            if(index > -1) {
                // 如果省份不是第一位 在省份之前的字段识别为名称
                if(index > 0) {
                    result.name = address.substring(0, index).trim();
                    address = address.substring(index).trim();
                }
                result.province = province;
                result.code = code;
                String _address = address.substring(provinceLength);
                if(_address.charAt(0) != '市' || _address.indexOf(province) > -1) {
                    address = _address;
                }
                //如果是用短名匹配的 要替换关键字
                if(StrUtil.isNotBlank(shortProvince)) {
                    for(String key : PROVINCE_KEYS) {
                        if(address.indexOf(key) == 0) {
                            address = address.substring(key.length());
                        }
                    }
                }

                String __address = parseCityByProvince(address, result);
                if(StrUtil.isBlank(result.city)) {
                    __address = parseAreaByProvince(address, result);
                }

                if (StrUtil.isNotBlank(result.city)) {
                    address = __address;
                    result.__parse = true;
                    break;
                } else {
                    //如果没有识别到地区 缓存本次结果，并重置数据
                    result.details = address.trim();
                    results.add(0, result);
                    result = new ProvinceParserResult();
                    address = addressSrc;
                }

            }

        }

        if(StrUtil.isNotBlank(result.code)) {
            result.details = address.trim();
            results.add(0, result);
        }
        return results;
    }

    /**
     * 1.2 提取城市
     * @param address
     * @param result
     * @return
     */
    private String parseCityByProvince(String address, ProvinceParserResult result) {
        Map<String, String> cityList = AreaUtils.getTargetAreaListByCode(AreaUtils.TargetType.CITY, result.code);
        MiddleParserResult _result = new MiddleParserResult();

        for(String cityCode : cityList.keySet()) {
            int index = address.indexOf(cityList.get(cityCode));
            String shortCity = index > -1 ? "" : CITY_SHORT.get(cityCode);
            int cityLength = StrUtil.isNotBlank(shortCity) ? shortCity.length() : cityList.get(cityCode).length();
            if(StrUtil.isNotBlank(shortCity)) {
                index = address.indexOf(shortCity);
            }
            if(index > -1 && (_result.index == -1 || _result.index > index || (StrUtil.isBlank(shortCity) && _result.isShort))) {
                _result.city = cityList.get(cityCode);
                _result.code = cityCode;
                _result.index = index;
                _result.address = address.substring(index + cityLength);
                _result.isShort = StrUtil.isNotBlank(shortCity);

                if(StrUtil.isNotBlank(shortCity)) {
                    //如果是用短名匹配的 要替换市关键字
                    for(String key : CITY_KEYS) {
                        if(address.indexOf(key) == 0) {
                            if(!StrUtil.equals(key, "市") && !StrUtil.containsAny(address, new String[] {"市北区", "市南区", "市中区", "市辖区"})) {
                                address = address.substring(key.length());
                            }
                        }
                    }
                }
            }

            if(index > -1 && index < 3) {
                result.city = cityList.get(cityCode);
                result.code = cityCode;
                _result.address = address.substring(index + cityLength);
                //如果是短名匹配的 要替换市关键字
                if(StrUtil.isNotBlank(shortCity)) {
                    //如果是用短名匹配的 要替换市关键字
                    for(String key : CITY_KEYS) {
                        if(address.indexOf(key) == 0) {
                            if(!StrUtil.equals(key, "市") && !StrUtil.containsAny(address, new String[] {"市北区", "市南区", "市中区", "市辖区"})) {
                                _result.address = _result.address.substring(key.length());
                            }
                        }
                    }
                }
            }


        }
        if(_result.index > -1) {
            result.city = _result.city;
            result.code = _result.code;
            address = parseAreaByCity(_result.address, result);
        }
        return address;
    }

    /**
     * 1.3  2.2 已匹配城市的地址 提取地区
     * @param address
     * @param result
     * @return
     */
    private String parseAreaByCity(String address, AreaParserResult result) {
        Map<String, String> areaList = AreaUtils.getTargetAreaListByCode(AreaUtils.TargetType.AREA, result.code);
        MiddleParserResult _result = new MiddleParserResult();
        for(String areaCode : areaList.keySet()) {
            int index = address.indexOf(areaList.get(areaCode));
            String shortArea = index > -1 ? "" : AREA_SHORT.get(areaCode);
            if(StrUtil.isNotBlank(shortArea)) {
                Pair<Integer, String> pair = AreaUtils.shortIndexOf(address, shortArea, areaList.get(areaCode));
                index = pair.getKey();
                shortArea = pair.getValue();
            }
            int areaLength = StrUtil.isNotBlank(shortArea) ? shortArea.length() : areaList.get(areaCode).length();
            if(index > -1 && (_result.index == -1 || _result.index > index || (StrUtil.isBlank(shortArea) && _result.isShort))) {
                _result.area = areaList.get(areaCode);
                _result.code = areaCode;
                _result.index = index;
                _result.address = address.substring(index + areaLength);
                _result.isShort = StrUtil.isNotBlank(shortArea);
                //如果是用短名匹配的 要替换市关键字
                if(StrUtil.isNotBlank(shortArea)) {
                    for(String key : AREA_KEYS) {
                        if(_result.address.indexOf(key) == 0) {
                            _result.address = _result.address.substring(key.length());
                        }
                    }
                }
            }
        }
        if(_result.index > -1) {
            result.area = _result.area;
            result.code = _result.code;
            address = _result.address;
        }
        return address;
    }


    /**
     * 1.4.提取省份但没有提取到城市的地址尝试通过省份下地区匹配
     * @param address
     * @param result
     * @return
     */
    private String parseAreaByProvince(String address, ProvinceParserResult result) {
        Map<String, String> areaList = AreaUtils.getTargetAreaListByCode(AreaUtils.TargetType.AREA, result.code);
        for(String areaKey : areaList.keySet()) {
            int index = address.indexOf(areaList.get(areaKey));
            String shortArea = index > -1 ? "" : AREA_SHORT.get(areaKey);
            if(StrUtil.isNotBlank(shortArea)) {
                Pair<Integer, String> pair = AreaUtils.shortIndexOf(address, shortArea, areaList.get(areaKey));
                index = pair.getKey();
                shortArea = pair.getValue();
            }
            int areaLength = StrUtil.isNotBlank(shortArea) ? shortArea.length() : areaList.get(areaKey).length();

            if(index > -1 && index < 6) {
                Map<String, String> city = AreaUtils.getTargetAreaListByCode(AreaUtils.TargetType.CITY, areaKey, true);

                result.city = city.values().iterator().next();
                result.area = areaList.get(areaKey);
                result.code = areaKey;

                address = address.substring(index + areaLength);
                //如果是用短名匹配的 要替换地区关键字
                if(StrUtil.isNotBlank(shortArea)) {
                    for(String key : AREA_KEYS) {
                        if(address.indexOf(key) == 0) {
                            address = address.substring(key.length());
                        }
                    }
                }

                break;

            }
        }

        return address;
    }

    /**
     * 2.1 通过城市识别地址
     * @param addressBase
     * @return
     */
    private List<AreaParserResult> parseByCity(String addressBase) {
        AreaEntity entity = new AreaCache().getEntity();
        Map<String, String> cityList = entity.getCityList();
        List<AreaParserResult> results = new ArrayList<>();
        AreaParserResult result = new CityParserResult();

        String address = addressBase;
        for(String cityCode :cityList.keySet()) {
            String city = cityList.get(cityCode);
            if(city.length() < 2) break;
            int index = address.indexOf(city);
            String shortCity = index > -1 ? "" : CITY_SHORT.get(cityCode);
            int cityLength = StrUtil.isNotBlank(shortCity) ? shortCity.length() : city.length();
            if(StrUtil.isNotBlank(shortCity)) {
                index = address.indexOf(shortCity);
            }
            if(index > -1) {
                Map<String, String> targetAreaListByCode = AreaUtils.getTargetAreaListByCode(AreaUtils.TargetType.PROVINCE, cityCode, true);
                String province = targetAreaListByCode.values().iterator().next();
                String provinceCode = targetAreaListByCode.keySet().iterator().next();

                result.province = province;
                result.city = city;
                result.code = cityCode;
                // 左侧排除省份名剩下的内容识别为姓名
                String leftAddress = address.substring(0, index);
                String _provinceName = "";
                if(StrUtil.isNotBlank(leftAddress)) {
                    _provinceName = province;
                    int _index = leftAddress.indexOf(_provinceName);
                    if(_index == -1) {
                        _provinceName = PROVINCE_SHORT.get(provinceCode);
                        _index = leftAddress.indexOf(_provinceName);
                        if(_index == -1) _provinceName = "";
                    }
                    if(StrUtil.isNotBlank(_provinceName)) {
                        leftAddress = leftAddress.replaceAll(_provinceName, "");
                    }
                    if(StrUtil.isNotBlank(leftAddress)) {
                        result.name = leftAddress;
                    }
                }

                address = address.substring(index + cityLength);
                address = parseAreaByCity(address, result);
                if(StrUtil.isNotBlank(_provinceName) || StrUtil.isNotBlank(result.area)) {
                    result.__parse = true;
                    break;
                } else {
                    //如果没有识别到省份和地区 缓存本次结果，并重置数据
                    result.details = address.trim();
                    results.add(result);
                    result = new CityParserResult();
                    address = addressBase;
                }



            }
        }

        if(StrUtil.isNotBlank(result.code)) {
            result.details = address.trim();
            results.add(result);
        }

        return results;

    }



    public static void main(String[] args) {
        AreaParser parser = new AreaParser();
        List<AreaParserResult> list = parser.parse("山西省武汉市", true);
        list.forEach(System.out::println);
    }


}

class AreaParserResult {
    String province;
    String city;
    String area;
    String details;
    String name;
    String code;
    String __type;
    boolean __parse = false;

    public AreaParserResult() {}

    public AreaParserResult(String type) {
        this.__type = type;
    }

    @Override
    public String toString() {
        return "AreaParserResult{" +
                "province='" + province + '\'' +
                ", city='" + city + '\'' +
                ", area='" + area + '\'' +
                ", details='" + details + '\'' +
                ", name='" + name + '\'' +
                ", code='" + code + '\'' +
                ", __type='" + __type + '\'' +
                ", __parse=" + __parse +
                '}';
    }
}

class ProvinceParserResult extends AreaParserResult {
    ProvinceParserResult() {
        super("parseByProvince");
    }
}

class CityParserResult extends AreaParserResult {
    CityParserResult() {
        super("parseByCity");
    }
}

class MiddleParserResult {
    String city;
    String area;
    String code;
    int index = -1;
    String address;
    boolean isShort = false;
}