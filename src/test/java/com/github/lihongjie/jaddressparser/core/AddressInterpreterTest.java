package com.github.lihongjie.jaddressparser.core;

import cn.hutool.db.Entity;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.github.lihongjie.jaddressparser.model.AddressParserResult;
import com.github.lihongjie.jaddressparser.util.DBUtil;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class AddressInterpreterTest {


    @Test
    @MethodSource("dataSource")
    @ParameterizedTest
    void parserAll(Entity entity) {
            String address = entity.getStr("ADDRESS_FULL");
            String province = entity.getStr("PROVINCE");
            String city = entity.getStr("CITY");
            String district = entity.getStr("DISTRICT");
            List<AddressParserResult> parser = AddressInterpreter.getDefault().parser(address, false);
            AddressParserResult addressParserResult = parser.get(0);
            String msg = "\r\n地址:" + address + "\r\n"
                        + "数据库：" + province + city + district + "\r\n"
                        + "识别：";
            for(AddressParserResult result: parser) {
                msg += result.province + result.city + result.area + ":" + result.__score + ":" + result.__type + "\r\n";
            }
            Assertions.assertEquals(province, addressParserResult.province, msg);
            if(addressParserResult.city != null) {
                Assertions.assertEquals(city, addressParserResult.city, msg);
            }
            if(addressParserResult.area != null) {
                Assertions.assertEquals(district, addressParserResult.area, msg);
            }
    }

    static Stream<Entity> dataSource() {
        List<Entity> entities = DBUtil.execQuerySQL("select * from (select * from CHINESE_ADDRESS_MATCHING_ADD where STATE = 2 order by dbms_random.VALUE()) where rownum< 100001");
        return entities.stream();
    }

    @Test
    @ParameterizedTest
    @MethodSource("dataSource")
    void githubTest(Entity entity) {
        String address = entity.getStr("ADDRESS_FULL");
        String province = entity.getStr("PROVINCE");
        String city = entity.getStr("CITY");
        String district = entity.getStr("DISTRICT");
        JSONObject requestBody = new JSONObject().set("address", address);

        String post = HttpUtil.post("https://wangzc.wang/smAddress", requestBody.toString());
        JSONObject resObj = JSONUtil.parseObj(post);
        String msg = "\r\n地址:" + address + "\r\n"
                + "数据库：" + province + city + district + "\r\n"
                + "识别：" + resObj.getStr("province") + resObj.getStr("city") + resObj.getStr("county");
        Assertions.assertEquals(province, resObj.getStr("province"), msg);
        Assertions.assertEquals(city, resObj.getStr("city"), msg);
        Assertions.assertEquals(district, resObj.getStr("county"), msg);

    }


    @Test
    void parser() {
        List<AddressParserResult> parser = AddressInterpreter.getDefault().parser("830002 新疆维吾尔自治区乌鲁木齐市经济技术开发区（头屯河区）上海路浦东街3号众创空间2层207-2室", false);
        parser.forEach(System.out::println);
    }
}