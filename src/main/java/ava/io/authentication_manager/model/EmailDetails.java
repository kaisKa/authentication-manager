package ava.io.authentication_manager.model;

// Importing required classes
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

// Annotations
@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
// Class
public class EmailDetails {

    private static final String SUBJECT = "Verification Code";

    // Class data members
    private String recipient;
    private String msgBody;
    private String subject;
    private String attachment;


    public EmailDetails(String recipient, String msgBody) {
        this.recipient = recipient;
        this.msgBody = msgBody;

        this.subject = SUBJECT;

    }
}