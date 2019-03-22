package org.czyee.guarder.annotated.listener;

import org.czyee.guarder.annotated.annotation.Module;
import org.czyee.guarder.annotated.annotation.Perm;
import org.czyee.guarder.annotated.permission.ModuleSet;
import org.czyee.guarder.annotated.permission.Permission;
import org.czyee.guarder.annotated.permission.PermissionHandler;
import org.czyee.guarder.session.SessionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.*;

public class RefreshListener implements ApplicationListener<ContextRefreshedEvent> {

	private static Set<ApplicationContext> initContexts = new HashSet<>();

	@Autowired
	private RequestMappingHandlerMapping requestMappingHandlerMapping;

	@Autowired
	private PermissionHandler permissionHandler;

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		ApplicationContext applicationContext = event.getApplicationContext();
		if (initContexts.contains(applicationContext)){
			return;
		}
		initContexts.add(applicationContext);
		SessionUtil.init(applicationContext);

		List<Permission> permissions = new ArrayList<>();
		List<ModuleSet> moduleSets = new ArrayList<>();

		Map<RequestMappingInfo, HandlerMethod> handlerMethods = requestMappingHandlerMapping.getHandlerMethods();
		for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : handlerMethods.entrySet()) {
			RequestMappingInfo requestMappingInfo = entry.getKey();
			HandlerMethod handlerMethod = entry.getValue();
			PatternsRequestCondition patternsCondition = requestMappingInfo.getPatternsCondition();
			Set<String> patterns = patternsCondition.getPatterns();
			for (String pattern : patterns) {
				Perm perm = handlerMethod.getMethodAnnotation(Perm.class);

				if (perm != null && perm.value()){
					Permission permission = new Permission();
					permission.setPath(pattern);
					permission.setPerm(perm);
					permissions.add(permission);
				}

				Module module = handlerMethod.getMethodAnnotation(Module.class);
				if (module != null){
					ModuleSet moduleSet = new ModuleSet();
					moduleSet.setModule(module);
					moduleSet.setPath(pattern);
					if (perm != null && perm.value()){
						moduleSet.setPerm(perm);
					}
					moduleSets.add(moduleSet);
				}
			}
		}
		permissionHandler.initPermissions(permissions,moduleSets);
	}
}
