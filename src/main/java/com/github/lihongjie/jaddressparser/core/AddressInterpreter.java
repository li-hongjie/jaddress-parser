package com.github.lihongjie.jaddressparser.core;

import com.github.lihongjie.jaddressparser.core.impl.DefaultAddressInterpreter;
import com.github.lihongjie.jaddressparser.model.AddressParserResult;

import java.util.List;

public interface AddressInterpreter {

    List<AddressParserResult> parser(String address, boolean isParseAll);

    static AddressInterpreter getDefault() {
        return new DefaultAddressInterpreter();
    }

}
