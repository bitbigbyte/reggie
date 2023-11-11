package com.xiaoke.filter;

import com.alibaba.fastjson.JSON;
import com.xiaoke.common.BaseContext;
import com.xiaoke.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;
import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * TODO:过滤器，判断请求是否需要被拦截
 */
@Slf4j
@WebFilter(filterName = "LoginCheckFilter", urlPatterns = "/*")
public class LoginCheckFilter implements Filter{
    //路径匹配器，支持通配符
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws ServletException, IOException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        //1.获取本次请求的URI
        String requestURI = request.getRequestURI();
        log.info("拦截到请求：{}",requestURI);
        //定义不需要处理的请求路径
        String[] urls = new String[]{
                "/employee/login",
                "/employee/logout",
                "/backend/**/*",
                "/front/**/*",
                "/user/sendMsg",
                "/user/login"
        };
        //2.判断本次请求是否需要处理
        Long employee = (Long)request.getSession().getAttribute("employee");
        Long user = (Long)request.getSession().getAttribute("user");
        if( user != null) {
            log.info("用户已登录，用户id为{}", user);
            //在线程变量中保存id
            BaseContext.setCurrentId(user);
            filterChain.doFilter(request, response);
        }else if( employee != null){
            log.info("用户已登录，用户id为{}",employee);
            //在线程变量中保存id
            BaseContext.setCurrentId(employee);
            filterChain.doFilter(request,response);
        }else if(check(requestURI,urls)){
            log.info("本次请求{}不需要处理",requestURI);
            filterChain.doFilter(request,response);
        }else{
            log.info("向后端发送请求，用户未登录，将跳转到登录页面");
            response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
        }
    }

    //路径匹配
    public boolean check(String requestURI,String[] urls){
        for(String url : urls){
            if(PATH_MATCHER.match(url,requestURI)){
                return true;
            }
        }
        return false;
    }
}
