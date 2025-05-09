package com.example.servingwebcontent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;

@SpringBootApplication // Classe principal da aplicação
public class ServingWebContentApplication {
	// objeto que representa o servlet ThymeleafServlet
	// O ThymeleafServlet é um servlet que processa templates Thymeleaf
	// cada sessao tem o seu proprio servlet ThymeleafServlet
    @Bean
	public ServletRegistrationBean<ThymeleafServlet> thymeleafServletBean() {
		ServletRegistrationBean<ThymeleafServlet> bean = new ServletRegistrationBean<>(new ThymeleafServlet(), "/thymeleafServlet/*");
		bean.setLoadOnStartup(1);
		return bean;
	}

	// objeto que representa o servlet Example
	// O Example é um servlet que responde a pedidos HTTP GET no endereço /exampleServlet
	@Bean
	public ServletRegistrationBean<Example> exampleServletBean() {
		ServletRegistrationBean<Example> bean = new ServletRegistrationBean<>(new Example(), "/exampleServlet/*");
		bean.setLoadOnStartup(1);
		return bean;
	}
    
    public static void main(String[] args) {
        SpringApplication.run(ServingWebContentApplication.class, args);
    }

}