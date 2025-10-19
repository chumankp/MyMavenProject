package com.chumbana.app;


import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import java.io.IOException;

@WebServlet("/")
public class HelloApplication extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/html");
        resp.getWriter().println("<html><body><h1>Hello World from MyMavenProject</h1></body></html>");
    }
}
