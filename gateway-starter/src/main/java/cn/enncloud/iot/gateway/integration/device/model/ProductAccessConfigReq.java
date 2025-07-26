package cn.enncloud.iot.gateway.integration.device.model;

import lombok.Data;

@Data
public class ProductAccessConfigReq {
    private String productId;

    public ProductAccessConfigReq() {
    }

    public ProductAccessConfigReq(String productId) {
        this.productId = productId;
    }
}
