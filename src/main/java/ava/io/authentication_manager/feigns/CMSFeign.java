package ava.io.authentication_manager.feigns;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient(value = "validatlicense", url = "http://localhost:7777/api/v1/auth")
public interface CMSFeign {

    @RequestMapping(method = RequestMethod.GET, value = "/isvalid/{license}")
    ResponseEntity<Boolean> validateLicence(@PathVariable(value = "license") String license);

//    ResponseEntity<String> createProvider(@RequestBody );


}