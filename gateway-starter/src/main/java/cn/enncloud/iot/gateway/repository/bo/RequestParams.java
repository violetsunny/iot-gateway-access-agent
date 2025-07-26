package cn.enncloud.iot.gateway.repository.bo;

import lombok.*;

/**
 * @Author: alec
 * Description:
 * @date: 下午2:50 2023/5/25
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class RequestParams<T> {

    private String requestUrl;

    private String requestPath;

    private T params;
}
