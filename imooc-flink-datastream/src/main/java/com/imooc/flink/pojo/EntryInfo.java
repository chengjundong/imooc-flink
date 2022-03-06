package com.imooc.flink.pojo;

import org.apache.flink.api.common.typeinfo.BasicTypeInfo;
import org.apache.flink.api.java.typeutils.PojoField;
import org.apache.flink.api.java.typeutils.PojoTypeInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * aid,aType,jType,cAmount,dAmount,seq,bal,unitId
 *
 * @author jared
 * @since 2021/12/11
 */
public class EntryInfo extends PojoTypeInfo<Entry> {

    private static List<PojoField> fields() throws Exception {
        ArrayList<PojoField> result = new ArrayList<>();
        result.add(new PojoField(Entry.class.getField("aid"), BasicTypeInfo.STRING_TYPE_INFO));
        result.add(new PojoField(Entry.class.getField("aType"), BasicTypeInfo.STRING_TYPE_INFO));
        result.add(new PojoField(Entry.class.getField("jType"), BasicTypeInfo.STRING_TYPE_INFO));
        result.add(new PojoField(Entry.class.getField("cAmount"), BasicTypeInfo.STRING_TYPE_INFO));
        result.add(new PojoField(Entry.class.getField("dAmount"), BasicTypeInfo.STRING_TYPE_INFO));
        result.add(new PojoField(Entry.class.getField("seq"), BasicTypeInfo.STRING_TYPE_INFO));
        result.add(new PojoField(Entry.class.getField("bal"), BasicTypeInfo.STRING_TYPE_INFO));
        result.add(new PojoField(Entry.class.getField("unitId"), BasicTypeInfo.STRING_TYPE_INFO));
        return result;
    }

    public EntryInfo() throws Exception {
        super(Entry.class, fields());
    }
}
