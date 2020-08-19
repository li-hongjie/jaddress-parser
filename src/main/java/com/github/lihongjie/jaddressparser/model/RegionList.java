package com.github.lihongjie.jaddressparser.model;

import java.util.HashMap;
import java.util.Iterator;

public class RegionList extends HashMap<String, String> implements Iterable<RegionList.RegionEntity> {

    @Override
    public String put(String code, String name) {
        return super.put(code, name);
    }

    @Override
    public String get(Object code) {
        return super.get(code);
    }


    @Override
    public Iterator<RegionEntity> iterator() {
        return new RIterator(this, keySet().iterator());
    }

    private class RIterator implements Iterator {

        private Iterator<String> keyIt;
        private RegionList map;

        RIterator(RegionList map, Iterator<String> keyIt) {
            this.keyIt = keyIt;
            this.map = map;
        }

        @Override
        public boolean hasNext() {
            return keyIt.hasNext();
        }

        @Override
        public Object next() {
            String code = keyIt.next();
            String name = map.get(code);
            return new RegionEntity(code, name);
        }
    }

    public static class RegionEntity {
        private String code;
        private String name;

        public RegionEntity(String code, String name) {
            this.code = code;
            this.name = name;
        }

        public String getCode() {
            return code;
        }

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return "RegionEntity{" +
                    "code='" + code + '\'' +
                    ", name='" + name + '\'' +
                    '}';
        }
    }
}
