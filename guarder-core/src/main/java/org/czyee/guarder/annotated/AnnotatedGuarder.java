package org.czyee.guarder.annotated;

import org.czyee.guarder.annotated.controller.ResourceController;
import org.czyee.guarder.annotated.interceptor.GuarderInterceptor;
import org.czyee.guarder.annotated.listener.RefreshListener;
import org.czyee.guarder.annotated.permission.PermissionHandler;
import org.czyee.guarder.node.NodeDefiner;
import org.czyee.guarder.session.DefautSessionIdGenerator;
import org.czyee.guarder.session.SessionIdGenerator;
import org.czyee.guarder.util.Utils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.Resource;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.context.support.ServletContextResource;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;
import org.springframework.web.servlet.mvc.HttpRequestHandlerAdapter;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnnotatedGuarder implements InitializingBean {

	/**
	 * setter的字段
	 */
	private String sourceDir = "resource";//资源目录
	private String[] supportMethods = {"GET", "POST"};
	List<HttpMessageConverter<?>> messageConverters;
	private Object[] interceptors = {};
	private SessionIdGenerator sessionIdGenerator;
	private String sessionPermissionKey = "USER_PERMISSION";
	/***********************setters******************************/
	/**
	 * 资源文件目录,所有静态资源都应放在此目录下,默认值:resource
	 *
	 * @param sourceDir 目录名称,两端的'/'字符将被忽略
	 */
	public void setSourceDir(String sourceDir) {
		if (sourceDir == null || sourceDir.trim().length() == 0) {
			throw new IllegalArgumentException("sourceDir must has value");
		}
		this.sourceDir = Utils.stringTrimWith(sourceDir, '/');
	}

	/**
	 * 静态资源支持的请求方式,默认:GET,POST
	 *
	 * @param supportMethods 请求方式
	 */
	public void setSupportMethods(String... supportMethods) {
		this.supportMethods = supportMethods;
	}

	/**
	 * springmvc的消息转换器,一般必定配置stringHttpMessageConverter 和 mappingJacksonConverter
	 *
	 * @param messageConverters 消息转换器列表
	 */
	public void setMessageConverters(List<HttpMessageConverter<?>> messageConverters) {
		this.messageConverters = messageConverters;
	}

	/**
	 * 设置拦截器
	 * @param interceptors 拦截器
	 */
	public void setInterceptors(Object ... interceptors) {
		this.interceptors = interceptors;
	}

	/**
	 * 设置会话ID生成器
	 * @param sessionIdGenerator 会话ID生成器
	 */
	public void setSessionIdGenerator(SessionIdGenerator sessionIdGenerator) {
		this.sessionIdGenerator = sessionIdGenerator;
	}

	/**
	 * 会话权限属性的key值
	 * @param sessionPermissionKey 会话权限属性的key
	 */
	public void setSessionPermissionKey(String sessionPermissionKey) {
		this.sessionPermissionKey = sessionPermissionKey;
	}

	/***********************beans******************************/

	/**
	 * 非静态资源映射mapping
	 *
	 * @return bean
	 */
	@Bean
	private RequestMappingHandlerMapping requestMappingHandlerMapping(GuarderInterceptor guarderInterceptor) {
		RequestMappingHandlerMapping requestMappingHandlerMapping = new RequestMappingHandlerMapping();
		Object[] interceptors = new Object[this.interceptors.length + 1];
		interceptors[this.interceptors.length] = guarderInterceptor;
		for (int i = 0; i < this.interceptors.length; i++) {
			Object interceptor = this.interceptors[i];
			interceptors[i] = interceptor;
		}
		requestMappingHandlerMapping.setInterceptors(guarderInterceptor);
		return requestMappingHandlerMapping;
	}

	/**
	 * 非静态资源适配器
	 *
	 * @return bean
	 */
	@Bean
	public HttpRequestHandlerAdapter simpleControllerHandlerAdapter() {
		HttpRequestHandlerAdapter adapter = new HttpRequestHandlerAdapter();
		return adapter;
	}

	/**
	 * springMVC核心适配器
	 *
	 * @return bean
	 */
	@Bean
	public RequestMappingHandlerAdapter requestMappingHandlerAdapter() {
		RequestMappingHandlerAdapter requestMappingHandlerAdapter = new RequestMappingHandlerAdapter();
		if (messageConverters != null) {
			requestMappingHandlerAdapter.setMessageConverters(messageConverters);
		}
		return requestMappingHandlerAdapter;
	}

	/**
	 * 将返回的Controller方法中的url指向静态资源
	 *
	 * @return bean
	 */
	@Bean
	public SimpleUrlHandlerMapping simpleUrlHandlerMapping(ResourceHttpRequestHandler resourceHttpRequestHandler) {
		SimpleUrlHandlerMapping simpleUrlHandlerMapping = new SimpleUrlHandlerMapping();
		Map<String, Object> urlMap = new HashMap<>();
		urlMap.put("/" + sourceDir + "/**", resourceHttpRequestHandler);
		simpleUrlHandlerMapping.setUrlMap(urlMap);
		//order一定要比requestMappingHandlerMapping 的小 (requestMappingHandlerMapping的是最大int值)
		//否则会无线进入requestMappingHandlerMapping,永远不会获取正确资源
		simpleUrlHandlerMapping.setOrder(10000);
		return simpleUrlHandlerMapping;
	}

	/**
	 * spring上下文刷新时间监听器,当springmvc启动成功后会执行方法,初始化一些参数
	 *
	 * @return bean
	 */
	@Bean
	public RefreshListener refreshListener() {
		RefreshListener refreshListener = new RefreshListener();
		return refreshListener;
	}

	/**
	 * 资源处理器,将指定目录下的文件指定为资源文件
	 * @return bean
	 */
	@Bean
	public ResourceHttpRequestHandler resourceHttpRequestHandler(ServletContext servletContext) {
		ResourceHttpRequestHandler resourceHttpRequestHandler = new ResourceHttpRequestHandler();
		resourceHttpRequestHandler.setSupportedMethods();
		List<Resource> locations = new ArrayList<>();
		locations.add(new ServletContextResource(servletContext, "/" + sourceDir + "/"));
		resourceHttpRequestHandler.setLocations(locations);
		resourceHttpRequestHandler.setSupportedMethods(supportMethods);
		return resourceHttpRequestHandler;
	}

	/**
	 * 静态资源controller,默认无限制访问的资源经由此控制器处理
	 *
	 * @return
	 */
	@Bean
	public ResourceController resourceController() {
		ResourceController resourceController = new ResourceController();
		return resourceController;
	}

	/**
	 * 视图解析器,只需要提供bean,自动注册为默认解析器
	 *
	 * @return bean
	 */
	@Bean
	public InternalResourceViewResolver internalResourceViewResolver() {
		InternalResourceViewResolver internalResourceViewResolver = new InternalResourceViewResolver();
		//默认前缀,指向静态资源
		internalResourceViewResolver.setPrefix("/" + sourceDir + "/");
		return internalResourceViewResolver;
	}

	/**
	 * 拦截器
	 * @return bean
	 */
	@Bean
	public GuarderInterceptor guarderInterceptor(){
		GuarderInterceptor guarderInterceptor = new GuarderInterceptor();
		//设置会话ID生成器
		if (sessionIdGenerator == null){
			guarderInterceptor.setSessionIdGenerator(new DefautSessionIdGenerator());
		}else {
			guarderInterceptor.setSessionIdGenerator(sessionIdGenerator);
		}
		return guarderInterceptor;
	}

	/**
	 * 权限处理器
	 * @return bean
	 */
	@Bean
	@Order(0)
	public PermissionHandler permissionHandler(NodeDefiner nodeDefiner){
		PermissionHandler permissionHandler = new PermissionHandler();
		permissionHandler.setNodeDefiner(nodeDefiner);
		permissionHandler.setSessionPermissionKey(sessionPermissionKey);
		return permissionHandler;
	}

	@Override
	public void afterPropertiesSet() {
		initIndex();
		initNormal();
	}

	private void initNormal(){
		Method index = ReflectionUtils.findMethod(ResourceController.class, "source", HttpServletRequest.class);
		PatternsRequestCondition patternsRequestCondition = new PatternsRequestCondition("/**");
		RequestMappingInfo requestMappingInfo = new RequestMappingInfo(patternsRequestCondition, null, null, null, null, null, null);
		//将ResourceController中的方法注册到requestMapping中
		requestMappingHandlerMapping.registerMapping(requestMappingInfo, resourceController, index);
	}

	private void initIndex(){
		Method index = ReflectionUtils.findMethod(ResourceController.class, "index");
		PatternsRequestCondition patternsRequestCondition = new PatternsRequestCondition("/");
		RequestMappingInfo requestMappingInfo = new RequestMappingInfo(patternsRequestCondition, null, null, null, null, null, null);
		//将ResourceController中的方法注册到requestMapping中
		requestMappingHandlerMapping.registerMapping(requestMappingInfo, resourceController, index);
	}

	/***********************注入字段******************************/
	@Autowired
	private RequestMappingHandlerMapping requestMappingHandlerMapping;

	@Autowired
	private ResourceController resourceController;

}