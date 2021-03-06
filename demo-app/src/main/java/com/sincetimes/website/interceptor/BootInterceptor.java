package com.sincetimes.website.interceptor;

import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.sincetimes.website.app.stats.StatsManager;
import com.sincetimes.website.core.common.port.PortInstance;
import com.sincetimes.website.core.common.support.LogCore;
import com.sincetimes.website.core.spring.HttpHeadUtil;

/***
 * 拦截器,单例
 */
public class BootInterceptor implements HandlerInterceptor {
	public final AtomicLong _count = new AtomicLong();// 计数器

	// 1
	public boolean preHandle(HttpServletRequest req, HttpServletResponse resp, Object arg2) {
		long begin_nao_time = System.nanoTime();
		String realIp = HttpHeadUtil.getRealIpAddr(req);
		if (req.getRequestURI().contains("error")) {
			LogCore.BASE.error("{}-------------------get one error request! {},arg2={}, referer={}", req.getRequestURI(), arg2, req.getHeader("referer"));
			return true;
		}
		if (LogCore.BASE.isDebugEnabled()) {
			LogCore.BASE.debug("{}----------------begin,realip={},req params:{},Origin={}", req.getRequestURI(),
					realIp, HttpHeadUtil.getParamsMap(req), req.getHeader("Origin"));
		}
		req.setAttribute("p_real_ip", realIp);
		req.setAttribute("begin_nao_time", begin_nao_time);
		LogCore.BASE.debug("{}--------------begin req,Uri= {}", this.hashCode(), req.getRequestURI());
		return true;
	}

	/*
	 * 3 会加上下面三个response头 Content-Type=application/json;charset=UTF-8,
	 * Content-Length=30, Date=Thu, 02 Jun 2016 07:42:33 GMT}
	 */
	public void afterCompletion(HttpServletRequest req, HttpServletResponse resp, Object arg2, Exception arg3)
			throws Exception {
		String uri = req.getRequestURI();
		Object begin_nao_time_str = req.getAttribute("begin_nao_time");
		if(null == begin_nao_time_str){
			LogCore.BASE.warn("{} get null,referer:{}, the uri looks useless " ,uri, req.getHeader("referer"));
			return;
		}
		long begin_nao_time = (Long)begin_nao_time_str ;
		String real_ip = (String) req.getAttribute("p_real_ip");
		long interval = System.nanoTime() - begin_nao_time;
		
		LogCore.BASE.info(this.hashCode() + "{}==========={}=========end,id={},params:{}, from:{},e:{}", uri,
				interval / 1000000, _count.getAndIncrement(), HttpHeadUtil.getParamsMap(req), real_ip, arg3);
		statEvent(uri, interval);
	}

	/* 2 */
	public void postHandle(HttpServletRequest req, HttpServletResponse resp, Object arg2, ModelAndView arg3)
			throws Exception {
	}
	
	/** 记录时间消耗,放入队列处理*/
	private void statEvent(String uri, long interval) {
		PortInstance.inst().addQueue((p)->{
			StatsManager.inst().statsUri(uri, interval);
			StatsManager.inst().statsUriByDb(uri, interval);
		});
	}
}
