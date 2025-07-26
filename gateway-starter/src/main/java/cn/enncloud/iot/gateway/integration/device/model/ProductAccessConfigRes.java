package cn.enncloud.iot.gateway.integration.device.model;

import lombok.Data;

import java.util.List;

@Data
public class ProductAccessConfigRes {

    private List<Item> messageProtocolList;


    @Data
    public static class Item {

        private String itemKey;

        private String itemName;

        private String itemValue;

    }
}
