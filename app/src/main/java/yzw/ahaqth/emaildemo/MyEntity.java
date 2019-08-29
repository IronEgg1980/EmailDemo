package yzw.ahaqth.emaildemo;

import org.litepal.crud.LitePalSupport;

final class MyEntity extends LitePalSupport {
    String name;
    int itemType;
    String value;

    public MyEntity(){}

    MyEntity(String name,ItemType itemType,String value){
        this.name = name;
        this.itemType = itemType.getValue();
        this.value = value;
    }

    String getName() {
        return name;
    }

    void setName(String name) {
        this.name = name;
    }

    ItemType getItemType() {
        return ItemType.getItemType(itemType);
    }

    void setItemType(ItemType itemType) {
        this.itemType = itemType.getValue();
    }

    String getValue() {
        return value;
    }

    void setValue(String value) {
        this.value = value;
    }
}
