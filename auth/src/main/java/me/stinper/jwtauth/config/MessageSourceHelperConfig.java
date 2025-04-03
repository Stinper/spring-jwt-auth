package me.stinper.jwtauth.config;

import me.stinper.jwtauth.utils.MessageSourceHelper;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MessageSourceHelperConfig {

    @Bean
    public MessageSourceHelper messageSourceHelper(MessageSource messageSource) {
        return new MessageSourceHelper(messageSource, MessageSourceHelper.MessageNotFoundAction.RETURN_MESSAGE_CODE);
    }

}
