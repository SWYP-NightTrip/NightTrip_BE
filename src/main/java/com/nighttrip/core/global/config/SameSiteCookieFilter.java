package com.nighttrip.core.global.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;

public class SameSiteCookieFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        chain.doFilter(request, response);

        if (response instanceof HttpServletResponse httpResp) {
            Collection<String> headers = httpResp.getHeaders("Set-Cookie");
            boolean firstHeader = true;
            for (String header : headers) {
                if (header.startsWith("SESSION")) {
                    String updatedHeader = header.replace("SameSite=Lax", "SameSite=None; Secure");
                    if (firstHeader) {
                        httpResp.setHeader("Set-Cookie", updatedHeader);
                        firstHeader = false;
                    } else {
                        httpResp.addHeader("Set-Cookie", updatedHeader);
                    }
                }
            }
        }
    }
}
