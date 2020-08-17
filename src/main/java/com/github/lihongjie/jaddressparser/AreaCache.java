package com.github.lihongjie.jaddressparser;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.ZipUtil;
import cn.hutool.json.JSONUtil;

import java.io.FileNotFoundException;

public class AreaCache {

    public static AreaEntity areaEntity;

    public AreaEntity getEntity() {
        if(areaEntity == null) {    // dc
            synchronized (AreaEntity.class) {
                if(areaEntity == null) {
                    areaEntity = getCache();
                }
            }

        }
        return areaEntity;
    }

    public AreaEntity getCache() {
        byte[] data = IoUtil.readBytes(this.getClass().getClassLoader().getResourceAsStream("area.dat"));
        return JSONUtil.toBean(decode(data), AreaEntity.class);
    }

    private String decode(byte[] bytes) {
        return ZipUtil.unGzip(Base64.decode(bytes), "UTF-8");
    }


    public static void main(String[] args) throws FileNotFoundException {
//        byte[] bytes = IoUtil.readBytes(AreaEntity.class.getClassLoader().getResourceAsStream("area.json"));
//        byte[] zips = Base64.encode(ZipUtil.gzip(bytes), true);
//        IoUtil.write(new FileOutputStream("/Users/mac/Documents/code/java/jaddress-parser/src/main/resources/area.dat"),true, zips);

        AreaCache cache = new AreaCache();
        AreaEntity entity = cache.getCache();
        System.out.println(entity);


    }

}
