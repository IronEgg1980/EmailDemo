package yzw.ahaqth.emaildemo;

enum ItemType {
    TYPE1(1,"TYPE1,Value = 1"),TYPE2(2,"TYPE2,Value = 2"),TYPE3(3,"TYPE3,Value = 3");

    public String getTextValue() {
        return textValue;
    }
    private int value;

    public int getValue() {
        return value;
    }

    private String textValue;
    ItemType(int value,String textValue){
        this.value = value;
        this.textValue = textValue;
    }

    static ItemType getItemType(int value){
        switch (value){
            case 1:return TYPE1;
            case 2:return TYPE2;
            case 3:return TYPE3;
            default:return TYPE1;
        }
    }
}
