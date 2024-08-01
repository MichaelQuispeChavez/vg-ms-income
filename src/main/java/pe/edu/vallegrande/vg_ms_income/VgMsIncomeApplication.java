package pe.edu.vallegrande.vg_ms_income;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;


@EnableFeignClients
@SpringBootApplication
public class VgMsIncomeApplication {

    public static void main(String[] args) {
        SpringApplication.run(VgMsIncomeApplication.class, args);
    }

}
