package com.example.servingwebcontent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import com.example.servingwebcontent.beans.Number;
import com.example.servingwebcontent.forms.Project;
import com.example.servingwebcontent.thedata.Employee;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

// responde aos pedidos do utilizador no browser, escolhe os dados a mostrar 
@Controller // trata os pedidos HTTP e devolver páginas HTML
public class GreetingController {
    // Injeção dos beans de contagem
    @Resource(name = "requestScopedNumberGenerator")
    private Number nRequest; // reinicia a cada pedido HTTP
    @Resource(name = "sessionScopedNumberGenerator")
    private Number nSession;  // mesmo nº durante toda a sessão do utilizador
    @Resource(name = "applicationScopedNumberGenerator")
    private Number nApplication; // contador que é igual para todos

    @Bean
    @Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
    public Number requestScopedNumberGenerator() {
        return new Number();
    }
    @Bean
    @Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
    public Number sessionScopedNumberGenerator() {
        return new Number();
    }
    @Bean
    @Scope(value = WebApplicationContext.SCOPE_APPLICATION, proxyMode = ScopedProxyMode.TARGET_CLASS)
    public Number applicationScopedNumberGenerator() {
        return new Number();
    }

    // Se alguém aceder a http://localhost:8080/, o utilizador é redirecionado para /greeting
    @GetMapping("/")
    public String redirect() {
        return "redirect:/greeting";
    }

	@GetMapping("/greeting")
	public String greeting(@RequestParam(name = "name", required = false) String name, Model model) {
		model.addAttribute("name", "Beatriz");
		model.addAttribute("othername", "DORA");
		return "greeting";
	}

    //  Cria uma lista de empregados e envia para o HTML
    @GetMapping("/givemeatable")
	public String atable(Model model) {
        Employee [] theEmployees = { new Employee(1, "José", "9199999", 1890), new Employee(2, "Marisa", "9488444", 2120), new Employee(3, "Hélio", "93434444", 2500)};
        List<Employee> le = new ArrayList<>();
        Collections.addAll(le, theEmployees);
        model.addAttribute("emp", le);
		return "table"; //table.html
	}

    // from https://attacomsian.com/blog/spring-boot-thymeleaf-form-handling and https://github.com/attacomsian/code-examples
	// Mostra um formulário HTML (em create-project.html) com os campos de um projeto (Project)
    @GetMapping("/create-project")
    public String createProjectForm(Model model) {
        
        model.addAttribute("project", new Project());
        return "create-project";
    }

    //  Recebe os dados preenchidos no formulário e imprime no terminal
    @PostMapping("/save-project")
    public String saveProjectSubmission(@ModelAttribute Project project) {

        // TODO: save project in DB here
        System.out.println("user submited project");
        System.out.println("Project name: " + project.getName());
        System.out.println("Project description: " + project.getDescription());
        return "result";
    }

    @GetMapping("/counters")
	public String counters(Model model) {
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        
        HttpSession session = attr.getRequest().getSession(true);
        Integer counter = (Integer) session.getAttribute("counter"); 
        int c;
        if (counter == null)
            c = 1;
        else
            c = counter + 1;
        session.setAttribute("counter", c);
		model.addAttribute("sessioncounter", c);
		model.addAttribute("requestcounter2", this.nRequest.next()); // muda a cada pedido HTTP
		model.addAttribute("sessioncounter2", this.nSession.next()); // igual enquanto o utilizador não fechar o navegador
		model.addAttribute("applicationcounter2", this.nApplication.next()); // mesmo valor para todos os utilizadores
		return "counter";
	}

}