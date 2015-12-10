/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.edu.icm.comac.vis.server;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 *
 * @author Aleksander Nowinski <a.nowinski@icm.edu.pl>
 */
@Component
public class TimingInterceptor implements HandlerInterceptor {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(TimingInterceptor.class.getName());

    private static final String HANDLING_START_ATT = "HandlingStart";
    private static final String HANDLING_END_ATTR = "HandlingEnd";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        request.setAttribute(HANDLING_START_ATT, System.currentTimeMillis());
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        request.setAttribute(HANDLING_END_ATTR, System.currentTimeMillis());
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        
        Long start = (Long) request.getAttribute(HANDLING_START_ATT);
        Long end = (Long) request.getAttribute(HANDLING_END_ATTR);
        long t = System.currentTimeMillis();
        log.debug("Request: {} handling: {}ms full processing: {}ms; url: {} query: {}", request.getContextPath(),
                end - start, t - start, request.getRequestURL(), request.getQueryString());
    }

}
