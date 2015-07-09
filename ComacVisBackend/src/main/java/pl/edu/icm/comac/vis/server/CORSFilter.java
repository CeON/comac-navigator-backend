package pl.edu.icm.comac.vis.server;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;

/**
 * A _very_ basic
 * <a href="https://en.wikipedia.org/wiki/Cross-origin_resource_sharing">CORS</a>
 * implementation in a filter. Adds "Access-Control-Allow-Origin: *" header to
 * every response to a CORS request.
 */
@Component
public class CORSFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
        if (((HttpServletRequest) req).getHeader("Origin") != null) {
            ((HttpServletResponse) resp).setHeader("Access-Control-Allow-Origin", "*");
        }
        chain.doFilter(req, resp);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void destroy() {
    }
}
