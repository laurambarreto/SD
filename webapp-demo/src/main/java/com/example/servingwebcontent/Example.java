package com.example.servingwebcontent;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//este servlet responde a um pedido GET no endereço /AnnotationExample
@WebServlet(
  name = "AnnotationExample", // nome do servlet
  description = "Example Servlet Using Annotations", // descrição do servlet
  urlPatterns = {"/AnnotationExample"} // URL que ativa o servlet "http://localhost:8080/AnnotationExample
)

// classe que trata de pedidos HTTP
public class Example extends HttpServlet {	
    // método chamado quando alguem fizer um pedido GET para o servlet
    // recebe o pedido e a resposta como argumentos
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // o cliente vai receber um HTML com a resposta "Hello World!"
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.println("<p>Hello World!</p>");
    }
}