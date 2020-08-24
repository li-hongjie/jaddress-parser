package com.github.lihongjie.jaddressparser.core.impl;

import cn.hutool.core.lang.Pair;
import cn.hutool.core.util.StrUtil;
import com.github.lihongjie.jaddressparser.AreaUtils;
import com.github.lihongjie.jaddressparser.core.AddressInterpreter;
import com.github.lihongjie.jaddressparser.core.RegionCache;
import com.github.lihongjie.jaddressparser.model.*;

import java.util.ArrayList;
import java.util.List;

public class DefaultAddressInterpreter implements AddressInterpreter {

    @Override
    public List<AddressParserResult> parser(String address, boolean isParseAll) {
        address = StrUtil.cleanBlank(address);

        List<AddressParserResult> results = new ArrayList<>();

        //正向解析
        results.addAll(0, parseByProvince(address));
        if(isParseAll || results.isEmpty() || !results.get(0).__parse) {
            //逆向城市解析 通过所有CityShort匹配
            results.addAll(0, parseByCity(address));
            if(isParseAll || results.isEmpty() || results.get(0).__parse) {
                //逆向地区解析 通过所有AreaShort匹配
                results.addAll(0, parseByArea(address));
            }

        }

        //计算可靠性分数
        if(results.size() > 1) {
            for(AddressParserResult result : results) {
                String _address = address;
                result.__score += result.__parse ? 1 : 0;
                if(result.__parse && StrUtil.isNotBlank(result.province) && _address.contains(result.province)) {
                    _address = _address.replaceAll(result.province, "");
                    result.__score += 2;
                    if(StrUtil.isNotBlank(result.city) && _address.contains(result.city)) {
                        _address = _address.replaceAll(result.city, "");
                        result.__score += 3;
                        if(StrUtil.isNotBlank(result.area) && _address.contains(result.area)) {
                            result.__score += 1;
                        }
                    }
                }

            }
        }

        // 排序
//        results.sort((a, b) -> {
//            return a.__parse && b.__parse ? -1 :
//                    !a.__parse && b.__parse ? 1:
//                            a.__parse && b.__parse && a.__score > b.__score ? -1:
//                                    a.__parse && b.__parse && a.__score < b.__score ? 1:
//                                            a.__parse && a.__type.equals("parseByProvince") ? -1:
//                                                    b.__parse && b.__type.equals("parseByProvince") ? 1 : 0;
////                                                            a.name.length() > b.name.length() ? 1 : a.name.length() < b.name.length() ? -1 : 0;
//        });

        String finalAddress = address;
        results.sort((a, b) -> {
//            if(a.__parse && b.__parse) {
//                return -1;
//            }
//            if(!a.__parse && b.__parse) {
//                return 1;
//            }
            if(a.__score < b.__score) {
                return 1;
            }
            if(StrUtil.containsAny(b.province, "北京市", "上海市", "重庆市", "天津市") && b.__parse && b.__type.equals("parseByCity")) {
                return 1;
            }
            if((StrUtil.isBlank(b.area) || StrUtil.isBlank(a.area)) && b.__parse && b.__type.equals("parseByArea")) {
                return 1;
            }
            if(b.__parse && b.__type.equals("parseByProvince")) {
                return 1;
            }
            if(!a.__parse && !b.__parse &&
                    (StrUtil.indexOfIgnoreCase(finalAddress,a.province) < StrUtil.indexOfIgnoreCase(finalAddress, b.province)
                            || StrUtil.indexOfIgnoreCase(finalAddress, a.city) < StrUtil.indexOfIgnoreCase(finalAddress, b.city)
                            || StrUtil.indexOfIgnoreCase(finalAddress, a.area) < StrUtil.indexOfIgnoreCase(finalAddress, b.area))) {
                return 1;
            }
            return -1;
        });



        return results;
    }

    /**
     * 1.1 提取省份
     * @param address
     * @return
     */
    private List<AddressParserResult> parseByProvince(String address) {

        List<AddressParserResult> results = new ArrayList<>();
        RegionList provinceList = RegionCache.getProvinceList();
        ProvinceParserResult result = new ProvinceParserResult();

        String addressSrc = address;

        for(RegionList.RegionEntity province : provinceList) {
            int index = addressSrc.indexOf(province.getName());
            String shortProvince = index > -1 ? "" : RegionCache.getProvinceShort().get(province.getCode());
            int provinceLength = StrUtil.isNotBlank(shortProvince) ? shortProvince.length() : province.getName().length();
            if(StrUtil.isNotBlank(shortProvince)) {
                index = address.indexOf(shortProvince);
            }

            if(index > -1) {
                // 如果省份不是第一位 在省份之前的字段识别为名称
                if(index > 0) {
                    result.name = address.substring(0, index).trim();
                    address = address.substring(index).trim();
                }
                result.province = province.getName();
                result.code = province.getCode();
                String _address = address.substring(provinceLength);

                // TODO: 2020/8/21 无作用
                if(StrUtil.isBlank(_address) || _address.charAt(0) != '市' || _address.indexOf(province.getName()) > -1) {
                    address = _address;
                }
                //如果是用短名匹配的 要替换关键字
                if(StrUtil.isNotBlank(shortProvince)) {
                    for(String key : RegionCache.getProvinceKeys()) {
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
//                    address = __address;
                    result.__parse = true;

                    // FIXME: 2020/8/20 重置数据
                    result.details = __address.trim();
                    results.add(0, result);
                    result = new ProvinceParserResult();
                    address = addressSrc;

//                    break;
                } else {
                    //如果没有识别到地区 缓存本次结果，并重置数据
                    result.details = address.trim();
                    results.add(0, result);
                    result = new ProvinceParserResult();
                    address = addressSrc;
                }

            }

        }

        // FIXME: 2020/8/20 删除
//        if(StrUtil.isNotBlank(result.code)) {
//            result.details = address.trim();
//            results.add(0, result);
//        }
        return results;

    }



    /**
     * 1.2 提取城市
     * @param address
     * @param result
     * @return
     */
    private String parseCityByProvince(String address, AddressParserResult result) {

        List<RegionList.RegionEntity> cityList = AreaUtils.getTargetAreaListByCode(AreaUtils.TargetType.CITY, result.code);
        MiddleParserResult _result = new MiddleParserResult();

        for(RegionList.RegionEntity city : cityList) {
            int index = address.indexOf(city.getName());
            String shortCity = index > -1 ? "" : RegionCache.getCityShort().get(city.getCode());
            int cityLength = StrUtil.isNotBlank(shortCity) ? shortCity.length() : city.getName().length();
            if(StrUtil.isNotBlank(shortCity)) {
                index = address.indexOf(shortCity);
            }
            if(index > -1 && (_result.index == -1 || _result.index > index || (StrUtil.isBlank(shortCity) && _result.isShort))) {
                _result.city = city.getName();
                _result.code = city.getCode();
                _result.index = index;
                // FIXME: 2020/8/19
//                _result.address = address.substring(index + cityLength);
                _result.address = address.substring(0 ,index) + address.substring(index + cityLength);
                _result.isShort = StrUtil.isNotBlank(shortCity);

                if(StrUtil.isNotBlank(shortCity)) {
                    //如果是用短名匹配的 要替换市关键字
                    for(String key : RegionCache.getCityKeys()) {
                        if(address.indexOf(key) == 0) {
                            if(!StrUtil.equals(key, "市") && !StrUtil.containsAny(address, new String[] {"市北区", "市南区", "市中区", "市辖区"})) {
                                address = address.substring(key.length());
                            }
                        }
                    }
                }
            }

            if(index > -1 && index < 3) {
                result.city = city.getName();
                result.code = city.getCode();
                _result.address = address.substring(index + cityLength);
                //如果是短名匹配的 要替换市关键字
                if(StrUtil.isNotBlank(shortCity)) {
                    //如果是用短名匹配的 要替换市关键字
                    for(String key : RegionCache.getCityKeys()) {
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
    private String parseAreaByCity(String address, AddressParserResult result) {
        List<RegionList.RegionEntity> areaList = AreaUtils.getTargetAreaListByCode(AreaUtils.TargetType.AREA, result.code);
        MiddleParserResult _result = new MiddleParserResult();
        //循环所有城市下地区的列表
        for(RegionList.RegionEntity area : areaList) {
//            if(StrUtil.containsAny(area.getName(), "开发区", "经济开发区", "高新区")) continue;
            int index = address.indexOf(area.getName());
            //尝试使用简写匹配
            String shortArea = index > -1 ? "" : RegionCache.getAreaShort().get(area.getCode());
            if(StrUtil.isNotBlank(shortArea)) {
                Pair<Integer, String> pair = AreaUtils.shortIndexOf(address, shortArea, area.getName());
                index = pair.getKey();
                shortArea = pair.getValue();
            }
            int areaLength = StrUtil.isNotBlank(shortArea) ? shortArea.length() : area.getName().length();
//            if(index > -1
//                    && (_result.index == -1 || (StrUtil.isBlank(shortArea) && _result.isShort))
//                    && (_result.index == -1 || _result.index > index)) {
            if(index > -1 &&
                    (_result.index == -1
                            || (StrUtil.isBlank(shortArea) && _result.isShort)
                            || (((StrUtil.isBlank(shortArea) && !_result.isShort) || ((StrUtil.isNotBlank(shortArea) && _result.isShort))) && _result.index > index)
                            || (StrUtil.containsAny(_result.area, "开发区", "高新区", "工业园区")) )) {
                _result.area = area.getName();
                _result.code = area.getCode();
                _result.index = index;
                _result.address = address.substring(index + areaLength);
                _result.isShort = StrUtil.isNotBlank(shortArea);
                //如果是用短名匹配的 要替换市关键字
                if(StrUtil.isNotBlank(shortArea)) {
                    for(String key : RegionCache.getAreaKeys()) {
                        if(_result.address.indexOf(key) == 0) {
                            _result.address = _result.address.substring(key.length());
                        }
                    }
                }
//                break;
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
     * 1.4 提取省份但没有提取到城市的地址尝试通过省份下地区匹配
     * @param address
     * @param result
     * @return
     */
    // TODO: 2020/8/20 改写，多次匹配
    private String parseAreaByProvince(String address, AddressParserResult result) {
        List<RegionList.RegionEntity> areaList = AreaUtils.getTargetAreaListByCode(AreaUtils.TargetType.AREA, result.code);
        MiddleParserResult _result = new MiddleParserResult();
        for(RegionList.RegionEntity area : areaList) {
//            if(StrUtil.containsAny(area.getName(), "开发区", "经济开发区", "高新区")) continue;
            int index = address.indexOf(area.getName());
            String shortArea = index > -1 ? "" : RegionCache.getAreaShort().get(area.getCode());
            if(StrUtil.isNotBlank(shortArea)) {
                Pair<Integer, String> pair = AreaUtils.shortIndexOf(address, shortArea, area.getName());
                index = pair.getKey();
                shortArea = pair.getValue();
            }
            int areaLength = StrUtil.isNotBlank(shortArea) ? shortArea.length() : area.getName().length();

            if(index > -1 &&
                    (_result.index == -1
                            || (StrUtil.isBlank(shortArea) && _result.isShort)
                            || (((StrUtil.isBlank(shortArea) && !_result.isShort) || ((StrUtil.isNotBlank(shortArea) && _result.isShort))) && _result.index > index)
                            || (StrUtil.containsAny(_result.area, "开发区", "高新区", "工业园区")) )) {
                _result.area = area.getName();
                _result.code = area.getCode();
                _result.index = index;
                _result.address = address.substring(index + areaLength);
                _result.isShort = StrUtil.isNotBlank(shortArea);
                //如果是用短名匹配的 要替换市关键字
                if(StrUtil.isNotBlank(shortArea)) {
                    for(String key : RegionCache.getAreaKeys()) {
                        if(_result.address.indexOf(key) == 0) {
                            _result.address = _result.address.substring(key.length());
                        }
                    }
                }
            }
        }

        if(_result.index > -1) {
            RegionList.RegionEntity city = AreaUtils.getTargetAreaListByCode(AreaUtils.TargetType.CITY, _result.code, true).get(0);

            result.city = city.getName();
            result.area = _result.area;
            result.code = _result.code;
            address = _result.address;
        }

        return address;
    }

    /**
     * 2.1 通过城市识别地址
     * @param addressBase
     * @return
     */
    private List<AddressParserResult> parseByCity(String addressBase) {
        RegionList cityList = RegionCache.getCityList();
        List<AddressParserResult> results = new ArrayList<>();
        AddressParserResult result = new CityParserResult();

        String address = addressBase;
        for(RegionList.RegionEntity city :cityList) {
            if(city.getName().length() < 2) break;
            int index = address.indexOf(city.getName());
            String shortCity = index > -1 ? "" : RegionCache.getCityShort().get(city.getCode());
            int cityLength = StrUtil.isNotBlank(shortCity) ? shortCity.length() : city.getName().length();
            if(StrUtil.isNotBlank(shortCity)) {
                index = address.indexOf(shortCity);
            }
            if(index > -1) {
                RegionList.RegionEntity province = AreaUtils.getTargetAreaListByCode(AreaUtils.TargetType.PROVINCE, city.getCode(), true).get(0);

                result.province = province.getName();
                result.city = city.getName();
                result.code = city.getCode();
                // 左侧排除省份名剩下的内容识别为姓名
                String leftAddress = address.substring(0, index);
                String _provinceName = "";
                if(StrUtil.isNotBlank(leftAddress)) {
                    _provinceName = province.getName();
                    int _index = leftAddress.indexOf(_provinceName);
                    if(_index == -1) {
                        _provinceName = RegionCache.getProvinceShort().get(province.getCode());
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

    /**
     * 3 通过地区识别地址
     * @param addressBase
     * @return
     */
    private List<AddressParserResult> parseByArea(String addressBase) {

        RegionList areaList = RegionCache.getAreaList();
        List<AddressParserResult> results = new ArrayList<>();
        AreaParserResult result = new AreaParserResult();
        String address = addressBase;
        for(RegionList.RegionEntity area : areaList) {
            if(area.getName().length() < 2) break;
            int index = address.indexOf(area.getName());
            String shortArea = index > -1 ? "" : RegionCache.getAreaShort().get(area.getCode());
            if(StrUtil.isNotBlank(shortArea)) {
                Pair<Integer, String> pair = AreaUtils.shortIndexOf(address, shortArea, area.getName());
                index = pair.getKey();
                shortArea = pair.getValue();
            }
            int areaLength = StrUtil.isNotBlank(shortArea) ? shortArea.length() : area.getName().length();
            if(index > -1) {
                List<RegionList.RegionEntity> targetAreaListByCode = AreaUtils.getTargetAreaListByCode(AreaUtils.TargetType.PROVINCE, area.getCode(), true);
                RegionList.RegionEntity province = targetAreaListByCode.get(0);
                RegionList.RegionEntity city = targetAreaListByCode.get(1);
                result.province = province.getName();
                result.city = city.getName();
                result.area = area.getName();
                result.code = area.getCode();
                // 左侧排除省份城市名剩下的内容识别为姓名
                String leftAddress = address.substring(0, index);
                String _provinceName = "";
                String _cityName = "";
                if(StrUtil.isNotBlank(leftAddress)) {
                    _provinceName = province.getName();
                    int _index = leftAddress.indexOf(_provinceName);
                    if(_index == -1) {
                        _provinceName = RegionCache.getProvinceShort().get(province.getCode());
                        _index = leftAddress.indexOf(_provinceName);
                        if(_index == -1) _provinceName = "";
                    }
                    if(StrUtil.isNotBlank(_provinceName)) {
                        leftAddress = leftAddress.replaceAll(_provinceName, "");
                    }

                    _cityName = city.getName();
                    _index = leftAddress.indexOf(_cityName);
                    if(_index == -1) {
                        _cityName = RegionCache.getCityShort().get(city.getCode());
                        _index = leftAddress.indexOf(_cityName);
                        if(_index == -1) _cityName = "";
                    }
                    if(StrUtil.isNotBlank(_cityName)) {
                        leftAddress = leftAddress.replaceAll(_cityName, "");
                    }
                    if(StrUtil.isNotBlank(leftAddress)) {
                        result.name = leftAddress;
                    }
                }
                address = address.substring(index + areaLength);
                if(StrUtil.isNotBlank(_provinceName) || StrUtil.isNotBlank(_cityName)) {
                    result.__parse = true;
                    break;
                } else {
                    result.details = address.trim();
                    results.add(0, result);
                    result = new AreaParserResult();
                    address = addressBase;
                }
            }
        }

        if(StrUtil.isNotBlank(result.code)) {
            result.details = address.trim();
            results.add(0, result);
        }

        return results;

    }


}
