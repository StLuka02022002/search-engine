package searchengine.dto.message;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ErrorMessage extends Message {

    private String error;

    public ErrorMessage(String error) {
        super(false);
        this.error = error;
    }
}
