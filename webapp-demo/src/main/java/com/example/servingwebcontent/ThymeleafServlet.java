package com.example.servingwebcontent;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

//Sempre que alguém pedir um ficheiro .html, usa este servlet para responder
@WebServlet(name = "thymeleaf", urlPatterns = "*.html") 
public class ThymeleafServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private ServletContextTemplateResolver resolver;

    @Override
    // Este método corre uma vez apenas, quando o servidor arranca
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        
        //Cria o "resolutor" de templates, que sabe onde estão os ficheiros HTML (os templates)
        resolver = new ServletContextTemplateResolver(this.getServletContext());

        System.out.println("+--------------------------+");
        System.out.println(this.getServletContext());
        System.out.println(this.getServletContext().getRealPath("index.html"));
        System.out.println("+--------------------------+");

        //Diz que os ficheiros HTML estão na pasta src/main/resources/templates
        resolver.setPrefix("/templates/");
        //Resultado das páginas pode ser guardado em cache (mais rápido)
        resolver.setCacheable(true);
        resolver.setCacheTTLMs(60000L);
        // Usa a codificação UTF-8 (permite acentos e emojis)
        resolver.setCharacterEncoding("utf-8");
    }

    // doGet e doPost chamam o metodo doService, que trata o pedido

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doService(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doService(request, response);
    }

    protected void doService(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setCharacterEncoding(resolver.getCharacterEncoding());
        // Cria o "motor" que vai transformar o HTML com variáveis num HTML final
        TemplateEngine engine = new TemplateEngine();
        engine.setTemplateResolver(resolver);

        // Este ctx é uma espécie de mochila com dados (como variáveis) que vais usar no HTML
        WebContext ctx = new WebContext(request, response, getServletContext(), request.getLocale());
        // Estas variáveis vão aparecer no HTML assim
        ctx.setVariable("name", "friendly student!!!!!");
        ctx.setVariable("thename", "Jonas");
        ctx.setVariable("completeurl", "http://localhost:8080/thymeleafServlet/hellofromservlet.html");

        // pega o nome do template (ficheiro HTML) que vai ser processado
        String templateName = getTemplateName(request);
        // substitui as variáveis do HTML pelos valores que estão na mochila (ctx)
        String result = engine.process(templateName, ctx);

        PrintWriter out = new PrintWriter(response.getOutputStream());
        try {
            out = response.getWriter();
            out.println(result);
        } finally {
            out.close();
        }
    }

    // Vai buscar o nome do ficheiro HTML pedido
    protected String getTemplateName(HttpServletRequest request) {
        String requestPath = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (contextPath == null) {
            contextPath = "";
        }

        return requestPath.substring(contextPath.length());
    }
}
