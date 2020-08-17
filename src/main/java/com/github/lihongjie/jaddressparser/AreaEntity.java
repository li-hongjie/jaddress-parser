package com.github.lihongjie.jaddressparser;

import java.util.HashMap;
import java.util.Map;

public class AreaEntity {

    private Map<String,String> provinceList = new HashMap<String, String>();

    private Map<String,String> cityList = new HashMap<String, String>();

    private Map<String,String> areaList = new HashMap<String, String>();

    public Map<String, String> getProvinceList() {
        return provinceList;
    }

    public void setProvinceList(Map<String, String> provinceList) {
        this.provinceList = provinceList;
    }

    public Map<String, String> getCityList() {
        return cityList;
    }

    public void setCityList(Map<String, String> cityList) {
        this.cityList = cityList;
    }

    public Map<String, String> getAreaList() {
        return areaList;
    }

    public void setAreaList(Map<String, String> areaList) {
        this.areaList = areaList;
    }
}
