package app.wallet.smart_wallet.config;

import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RazorpayConfig {

    @Bean
    public RazorpayClient razorpayClient(RazorpayProperties razorpayProperties) throws RazorpayException {
        return new RazorpayClient(razorpayProperties.getKeyId(), razorpayProperties.getKeySecret());
    }
}
