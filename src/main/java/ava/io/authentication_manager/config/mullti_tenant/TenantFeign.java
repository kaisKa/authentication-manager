package ava.io.authentication_manager.config.mullti_tenant;

import ava.io.authentication_manager.model.GeneralResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;




@FeignClient(value = "getAllIssuer", url = "${spring.auth-service-url}/api/v1")
public interface TenantFeign {
    @RequestMapping(method = RequestMethod.GET ,value = "/manage/tenant/{tenant}")
    ResponseEntity<GeneralResponse<String>> getIssuer(@PathVariable(value = "tenant") String tenant);

    @RequestMapping(method = RequestMethod.GET ,value = "/manage/tenant")
    ResponseEntity<GeneralResponse<Object>> getAllIssuer(@RequestParam String type);

//    @RequestMapping(method = RequestMethod.GET , value = "/manage/tenant")
//    ResponseEntity<GeneralResponse<Map<String,String>>> mapAllIssuer(@RequestParam String map);
}


