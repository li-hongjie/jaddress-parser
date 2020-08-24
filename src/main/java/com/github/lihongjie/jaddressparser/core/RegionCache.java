package com.github.lihongjie.jaddressparser.core;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.ZipUtil;
import cn.hutool.json.JSONUtil;
import com.github.lihongjie.jaddressparser.AreaEntity;
import com.github.lihongjie.jaddressparser.model.RegionList;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class RegionCache {

    /**
     * 省列表、短名称列表
     */
    private static final RegionList PROVINCE_LIST;
    private static final RegionList PROVINCE_SHORT;

    /**
     * 市列表、短名称列表
     */
    private static final RegionList CITY_LIST;
    private static final RegionList CITY_SHORT;

    /**
     * 区列表、短名称列表
     */
    private static final RegionList AREA_LIST;
    private static final RegionList AREA_SHORT;


    /**
     * 外国国家列表
     */
    private static final RegionList FOREIGN_COUNTRY_LIST;

    private static final String[] PROVINCE_KEYS = new String[] {
            "特别行政区", "古自治区", "维吾尔自治区", "壮族自治区", "回族自治区", "自治区", "省省直辖", "省", "市"
    };

    private static final String[] CITY_KEYS = new String[] {
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

    static {
        byte[] countryData = IoUtil.readBytes(RegionCache.class.getClassLoader().getResourceAsStream("country.dat"));
        FOREIGN_COUNTRY_LIST = JSONUtil.toBean(decode(countryData), RegionList.class);
        byte[] provinceData = IoUtil.readBytes(RegionCache.class.getClassLoader().getResourceAsStream("province.dat"));
        PROVINCE_LIST = JSONUtil.toBean(decode(provinceData), RegionList.class);
        byte[] cityData = IoUtil.readBytes(RegionCache.class.getClassLoader().getResourceAsStream("city.dat"));
        CITY_LIST = JSONUtil.toBean(decode(cityData), RegionList.class);
        byte[] areaData = IoUtil.readBytes(RegionCache.class.getClassLoader().getResourceAsStream("area.dat"));
        AREA_LIST = JSONUtil.toBean(decode(areaData), RegionList.class);


        PROVINCE_SHORT = new RegionList();
        CITY_SHORT = new RegionList();
        AREA_SHORT = new RegionList();



        for(RegionList.RegionEntity entity : PROVINCE_LIST) {
            String name = entity.getName();
            for(String key : PROVINCE_KEYS) {
                name = name.replaceAll(key, "");
            }
            PROVINCE_SHORT.put(entity.getCode(), name);
        }

        for(RegionList.RegionEntity entity : CITY_LIST) {
            String name = entity.getName();
            for(String key : CITY_KEYS) {
                name = name.replaceAll(key, "");
            }
            CITY_SHORT.put(entity.getCode(), name);
        }

        for(RegionList.RegionEntity entity : AREA_LIST) {
            String name = entity.getName();
            if("雨花台区".equals(name)) name = "雨花区";
            if(name.length() > 2 && !"高新区".equals(name)) {
                for(String key : AREA_KEYS) {
                    name = name.indexOf(key) > 1 ? name.replaceAll(key, "") : name;
                }
            }
            AREA_SHORT.put(entity.getCode(), name);
        }

    }

    public static void main(String[] args) throws FileNotFoundException {
        byte[] bytes0 = IoUtil.readBytes(AreaEntity.class.getClassLoader().getResourceAsStream("country.json"));
        byte[] zips0 = Base64.encode(ZipUtil.gzip(bytes0), true);
        IoUtil.write(new FileOutputStream("C:\\Users\\Administrator\\IdeaProjects\\jaddress-parser\\src\\main\\resources\\country.dat"),true, zips0);

        byte[] bytes1 = IoUtil.readBytes(AreaEntity.class.getClassLoader().getResourceAsStream("province.json"));
        byte[] zips1 = Base64.encode(ZipUtil.gzip(bytes1), true);
        IoUtil.write(new FileOutputStream("C:\\Users\\Administrator\\IdeaProjects\\jaddress-parser\\src\\main\\resources\\province.dat"),true, zips1);

        byte[] bytes2 = IoUtil.readBytes(AreaEntity.class.getClassLoader().getResourceAsStream("city.json"));
        byte[] zips2 = Base64.encode(ZipUtil.gzip(bytes2), true);
        IoUtil.write(new FileOutputStream("C:\\Users\\Administrator\\IdeaProjects\\jaddress-parser\\src\\main\\resources\\city.dat"),true, zips2);

        byte[] bytes3 = IoUtil.readBytes(AreaEntity.class.getClassLoader().getResourceAsStream("area.json"));
        byte[] zips3 = Base64.encode(ZipUtil.gzip(bytes3), true);
        IoUtil.write(new FileOutputStream("C:\\Users\\Administrator\\IdeaProjects\\jaddress-parser\\src\\main\\resources\\area.dat"),true, zips3);

    }

    private static String decode(byte[] bytes) {
        return ZipUtil.unGzip(Base64.decode(bytes), "UTF-8");
    }


    public static RegionList getAreaList() {
        return AREA_LIST;
    }

    public static RegionList getCityList() {
        return CITY_LIST;
    }

    public static RegionList getProvinceList() {
        return PROVINCE_LIST;
    }

    public static RegionList getAreaShort() {
        return AREA_SHORT;
    }

    public static RegionList getCityShort() {
        return CITY_SHORT;
    }

    public static RegionList getProvinceShort() {
        return PROVINCE_SHORT;
    }

    public static String[] getAreaKeys() {
        return AREA_KEYS;
    }

    public static String[] getCityKeys() {
        return CITY_KEYS;
    }

    public static String[] getProvinceKeys() {
        return PROVINCE_KEYS;
    }

    public static RegionList getForeignCountryList() {
        return FOREIGN_COUNTRY_LIST;
    }
}
