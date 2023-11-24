package ava.io.authentication_manager.services;


import ava.io.authentication_manager.utils.ErrorCode;
import com.twilio.Twilio;
import com.twilio.rest.verify.v2.service.Verification;
import com.twilio.rest.verify.v2.service.VerificationCheck;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;


import java.net.URI;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
public class TwilioService {

//    @Autowired
//    @Qualifier("notSecureRestTemplate")
    private final RestTemplate restTemplate;

    @Value("${twilio.account_id}")
    private String account_id;
    @Value("${twilio.auth_token}")
    private String auth_token;

    @Value("${twilio.SendUrl}")
    private String SendUrl;
    @Value("${twilio.checkUrl}")
    private String checkUrl;

    @Value("${twilio.service_id}")
    private String serviceId;

    public TwilioService(@Qualifier("notSecureRestTemplate") RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    //********************************* In case u want Twilio to host the code *********************************//
    //********************************* ٧٧٧٧٧٧٧٧٧٧٧٧٧٧٧٧٧٧٧٧٧٧٧٧٧٧٧٧٧٧٧٧٧٧٧٧٧٧ *********************************//
    @SneakyThrows
    public Boolean sendSms(String gsm) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBasicAuth(account_id, auth_token);
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
            map.add("To", gsm);
            map.add("Channel", "sms");
            val entity = new HttpEntity<>(map, headers);


            Map<String, String> res = restTemplate.exchange(new URI(SendUrl),
                    HttpMethod.POST,
                    entity,
                    Map.class
            ).getBody();
            return Objects.requireNonNull(res).get("status").equalsIgnoreCase("pending");
        } catch (Exception e) {
            throw new RuntimeException(ErrorCode.MESSAGING_SERVICE_ISSUE.getMessage());
        }
    }

    @SneakyThrows
    public Boolean sendCode(String gsm) {
        Twilio.init(account_id, auth_token);
        Verification verification = Verification.creator(serviceId,null,null)
                .setTo(gsm)
                .setChannel("sms")
                .create();
        Twilio.destroy();

        return Objects.requireNonNull(verification).getStatus().equalsIgnoreCase("pending");
    }

    @SneakyThrows
    public Boolean verifyOTP(String gsm, String code) {

        Twilio.init(account_id, auth_token);
        VerificationCheck verificationCheck = VerificationCheck.creator(serviceId)
                .setTo(gsm)
                .setCode(code)
                .create();
        Twilio.destroy();

        return Objects.requireNonNull(verificationCheck).getStatus().equalsIgnoreCase("approved");

//        HttpHeaders headers = new HttpHeaders();
//        headers.setBasicAuth(account_id, auth_token);
//        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
//
//        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
//        map.add("To", gsm);
//        map.add("Code", code);
//        val entity = new HttpEntity<>(map, headers);
//        Map<String, String> res = restTemplate.exchange(new URI(checkUrl),
//                HttpMethod.POST,
//                entity,
//                Map.class
//        ).getBody();
//
//        return Objects.requireNonNull(res).get("status").equalsIgnoreCase("approved");
    }


    //*********************************     In case u want to host the code    *********************************//
    //********************************* ٧٧٧٧٧٧٧٧٧٧٧٧٧٧٧٧٧٧٧٧٧٧٧٧٧٧٧٧٧٧٧٧٧٧٧٧٧٧ *********************************//

    @SneakyThrows
    public Boolean sendSms(String gsm, String msg) {

        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth("ACa457f406c1c2463a19d7c1043dd8df81", "d14afee65d01d2b7e736970b072578a8");
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.add("To", gsm);
        map.add("Body", msg);
        map.add("MessagingServiceSid", "MG72b9328071a5f1f0e5b4261c4d051a47");
        val entity = new HttpEntity<>(map, headers);
        Map<String, String> res = restTemplate.exchange(new URI("https://api.twilio.com/2010-04-01/Accounts/ACa457f406c1c2463a19d7c1043dd8df81/Messages.json"),
                HttpMethod.POST,
                entity,
                Map.class
        ).getBody();

        return Objects.requireNonNull(res).get("status").equalsIgnoreCase("approved");

    }
}
