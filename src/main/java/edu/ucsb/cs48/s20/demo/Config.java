package edu.ucsb.cs48.s20.demo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;

@Configuration
public class Config {

    /**
     * Conversion service to allow using Lists on Value annotations for
     * application.properties values. See:
     * https://stackoverflow.com/a/29970335/6454116
     *
     * @return a default conversion service used automatically
     */

    @Bean
    public ConversionService conversionService() {
        DefaultConversionService service = new DefaultConversionService();
        return service;
    }
}