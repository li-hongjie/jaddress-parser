package com.github.lihongjie.jaddressparser.core;

import cn.hutool.db.Entity;
import com.github.lihongjie.jaddressparser.model.AddressParserResult;
import com.github.lihongjie.jaddressparser.util.DBUtil;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AddressInterpreterTest {


    @Test
    @RepeatedTest(10)
    void parserAll() {
        List<Entity> entities = DBUtil.execQuerySQL("select * from (select * from CHINESE_ADDRESS_MATCHING_ADD where STATE = 2 order by dbms_random.VALUE()) where rownum< 101");

        for(Entity entity : entities) {
            String address = entity.getStr("ADDRESS_FULL");
            String province = entity.getStr("PROVINCE");
            String city = entity.getStr("CITY");
            String district = entity.getStr("DISTRICT");
            List<AddressParserResult> parser = AddressInterpreter.getDefault().parser(address, true);
            AddressParserResult addressParserResult = parser.get(0);
            String msg = "\r\n地址:" + address + "\r\n"
                        + "数据库：" + province + city + district + "\r\n"
                        + "识别：";
            for(AddressParserResult result: parser) {
                msg += result.province + result.city + result.area + ":" + result.__score + ":" + result.__type + "\r\n";
            }
            Assertions.assertEquals(province, addressParserResult.province, msg);
            Assertions.assertEquals(city, addressParserResult.city, msg);
            Assertions.assertEquals(district, addressParserResult.area, msg);
        }
    }

    @Test
    void parser() {
        List<AddressParserResult> parser = AddressInterpreter.getDefault().parser("518107 广东省深圳市光明新区光明新陂头村美盈森厂区A栋 (广东,深圳,宝安区)", true);
        parser.forEach(System.out::println);
    }
}