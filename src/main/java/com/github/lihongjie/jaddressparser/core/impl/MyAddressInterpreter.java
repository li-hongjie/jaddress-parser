package com.github.lihongjie.jaddressparser.core.impl;

import cn.hutool.core.lang.Pair;
import cn.hutool.core.util.StrUtil;
import com.github.lihongjie.jaddressparser.AreaUtils;
import com.github.lihongjie.jaddressparser.core.AddressInterpreter;
import com.github.lihongjie.jaddressparser.core.RegionCache;
import com.github.lihongjie.jaddressparser.model.AddressParserResult;
import com.github.lihongjie.jaddressparser.model.MiddleParserResult2;
import com.github.lihongjie.jaddressparser.model.ProvinceParserResult;
import com.github.lihongjie.jaddressparser.model.RegionList;

import java.util.ArrayList;
import java.util.List;

public class MyAddressInterpreter implements AddressInterpreter {



    @Override
    public List<AddressParserResult> parser(String address, boolean isParseAll) {

        List<AddressParserResult> results = new ArrayList<>();

        results.addAll(parseByProvince(address));


        return results;
    }


    /**
     * 正向解析
     * 1.1 提取省份
     * @param addressBase 原始地址
     * @return
     */
    private List<AddressParserResult> parseByProvince(String addressBase) {

        List<AddressParserResult> results = new ArrayList<>();
        RegionList provinceList = RegionCache.getProvinceList();
        String address = addressBase;
        AddressParserResult result = new ProvinceParserResult();

        RegionList foreignCountryList = RegionCache.getForeignCountryList();
        for(RegionList.RegionEntity country : foreignCountryList) {
            int index = StrUtil.indexOfIgnoreCase(address, country.getName(), 0);

            if(index > -1) {
                // 该地址可能是国外地址
                result.__foreign = true;
                result.country = country.getCode();
                results.add(result);
                result = new ProvinceParserResult();
            }

        }


        for(RegionList.RegionEntity province : provinceList) {
            int index = StrUtil.indexOfIgnoreCase(address, province.getName(), 0);
            // 获取省的简称 如果已经用长名称匹配到则不做操作
            String shortProvince = index > -1 ? "" : RegionCache.getProvinceShort().get(province.getCode());
            // 计算名称的长度 如果是简称则计算简称的长度
            int provinceLength = StrUtil.isNotBlank(shortProvince) ? shortProvince.length() : StrUtil.length(province.getName());

            if(StrUtil.isNotBlank(shortProvince)) {
                index = StrUtil.indexOfIgnoreCase(address, shortProvince);
            }

            if(index > -1 ) {
                // 匹配成功(这时可能是全称匹配到也可能是简称匹配到的)
                result.province = province.getName();
                result.code = province.getCode();

                //截掉已匹配的部分
                String _address = address.substring(0, index) + address.substring(index + provinceLength);

                address = _address;

                if(StrUtil.isNotBlank(shortProvince)) {
                    for (String key : RegionCache.getProvinceKeys()) {
                        if(0 == StrUtil.indexOfIgnoreCase(address, key, 0)) {
                            address = addressBase.substring(StrUtil.length(key));
                        }
                    }
                }



                // 匹配完省，开始匹配市
                String __address = parseCityByProvince(address, result);


                if(StrUtil.isBlank(result.city) || StrUtil.isBlank(result.area)) {
                    // TODO: 2020/8/21 逆向匹配，通过地区获取市


                }
                result.details = __address;
                result.__parse = StrUtil.isNotBlank(result.city);
                results.add(result);
                address = addressBase;
                result = new ProvinceParserResult();
            }

        }

        return results;
    }

    /**
     * 正向匹配
     * 1.2 根据省匹配市
     * @param address
     * @param result
     * @return
     */
    private String parseCityByProvince(String address, AddressParserResult result) {

        //当前省下的城市列表
        List<RegionList.RegionEntity> cityList = AreaUtils.getTargetAreaListByCode(AreaUtils.TargetType.CITY, result.code);

        List<MiddleParserResult2> results = new ArrayList<>();
        MiddleParserResult2 _result = new MiddleParserResult2();

        if(StrUtil.containsAny(result.province, "北京市", "天津市", "上海市", "重庆市")) {
            address = result.province + address;
        }

        for (RegionList.RegionEntity city : cityList) {
            int index = StrUtil.indexOfIgnoreCase(address, city.getName(), 0);
            String shortCity = index > -1 ? "" : RegionCache.getCityShort().get(city.getCode());
            int cityLength = StrUtil.isNotBlank(shortCity) ? StrUtil.length(shortCity) : StrUtil.length(city.getName());
            if(StrUtil.isNotBlank(shortCity)) {
                index = StrUtil.indexOfIgnoreCase(address, shortCity, 0);
            }

            if(index > -1) {
                _result.city = city.getName();
                _result.code = city.getCode();
                _result.cityIndex = index;
                _result.address = address.substring(0, index) + address.substring(index + cityLength);
                _result.isCityShort = StrUtil.isNotBlank(shortCity);
                if(StrUtil.isNotBlank(shortCity)) {
                    //如果是用短名匹配的 要替换市关键字
                    for(String key : RegionCache.getCityKeys()) {
                        if(_result.address.indexOf(key) == 0) {
                            if(!StrUtil.equals(key, "市") && !StrUtil.containsAny(_result.address, new String[] {"市北区", "市南区", "市中区", "市辖区"})) {
                                _result.address = _result.address.substring(key.length());
                            }
                        }
                    }
                }

                parseAreaByCity(_result);

                results.add(_result);
                _result = new MiddleParserResult2();
            }

        }

        if(results.size() > 0) {
            results.sort((a,b) -> {
                if(a.isCityShort && !b.isCityShort) {
                    return -1;
                }
                if(b.isCityShort && !a.isCityShort) {
                    return 1;
                }
                if(a.cityIndex > b.cityIndex) {
                    return -1;
                }
                return 1;
            });

            _result = results.get(0);
            result.code = _result.code;
            result.city = _result.city;
            result.area = _result.area;
        }

        return _result.address;
    }

    /**
     * 正向匹配
     * 1.3 2.2 根据城市获取地区
     * @param result
     * @return
     */
    private String parseAreaByCity(MiddleParserResult2 result) {
        List<RegionList.RegionEntity> areaList = AreaUtils.getTargetAreaListByCode(AreaUtils.TargetType.AREA, result.code);
        String address = result.address;
        List<MiddleParserResult2> results = new ArrayList<>();
        MiddleParserResult2 _result = new MiddleParserResult2();

        for (RegionList.RegionEntity area : areaList) {
            int index = address.indexOf(area.getName());
            String shortArea = index > -1 ? "" : RegionCache.getAreaShort().get(area.getCode());
            if(StrUtil.isNotBlank(shortArea)) {
                Pair<Integer, String> pair = AreaUtils.shortIndexOf(address, shortArea, area.getName());
                index = pair.getKey();
                shortArea = pair.getValue();
            }

            int areaLength = StrUtil.isNotBlank(shortArea) ? StrUtil.length(shortArea) : StrUtil.length(area.getName());
            if(index > -1) {
                _result.area = area.getName();
                _result.code = area.getCode();
                _result.areaIndex = index;
                _result.address = address.substring(index + areaLength);
                _result.isAreaShort = StrUtil.isNotBlank(shortArea);
                //如果是用短名匹配的 要替换市关键字
                if(StrUtil.isNotBlank(shortArea)) {
                    for(String key : RegionCache.getAreaKeys()) {
                        if(_result.address.indexOf(key) == 0) {
                            _result.address = _result.address.substring(key.length());
                        }
                    }
                }
                results.add(_result);

            }

        }

        if(results.size() != 0) {
            results.sort((a,b) -> {
                if(a.isAreaShort && !b.isAreaShort) {
                    return -1;
                }
                if(b.isAreaShort && !a.isAreaShort) {
                    return 1;
                }
                if(a.areaIndex > b.areaIndex) {
                    return -1;
                }
                return 1;
            });

            _result = results.get(0);
            result.area = _result.area;
            result.code = _result.code;
            result.isAreaShort = _result.isAreaShort;
            result.address = _result.address;
        }
        return result.address;
    }

    public static void main(String[] args) {
        AddressInterpreter interpreter = new MyAddressInterpreter();
        List<AddressParserResult> parser = interpreter.parser("", false);
        parser.forEach(System.out::println);
    }


}
