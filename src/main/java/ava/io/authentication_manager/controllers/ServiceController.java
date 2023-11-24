package ava.io.authentication_manager.controllers;

import ava.io.authentication_manager.model.EmailDetails;
import ava.io.authentication_manager.services.GmailService;
import ava.io.authentication_manager.services.KeycloakService;
import ava.io.authentication_manager.services.TwilioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${spring.base_url}" + "/service")
public class ServiceController {

    @Autowired
    private GmailService gmailService;

    @Autowired
    private TwilioService twilioService;
    @Autowired
    private KeycloakService keycloakService;


    @PostMapping("/send-email")
    public void sentMail(@RequestBody EmailDetails emailDetails) {
        gmailService.sendSimpleMail(emailDetails);
    }

    @PostMapping("/send-sms")
    public Boolean sentMail(@RequestParam String gsm, @RequestParam String msg) {

//        return twilioService.sendSms(gsm, msg);
        return twilioService.sendCode(gsm);
    }



    @PostMapping("/verify")
    public Boolean verify(@RequestParam String gsm, @RequestParam String msg) {
        return twilioService.verifyOTP(gsm, msg);
    }





}
