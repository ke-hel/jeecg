package org.jeecgframework.core.aop;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Pointcut;
import org.jeecgframework.core.annotation.Ehcache;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;

import com.alibaba.fastjson.JSON;

/**
 * redis缓存AOP
 * @author Yandong
 */
//@Component
//@Aspect
public class RedisCacheAspect {
	
	@SuppressWarnings("rawtypes")
	@Resource
	private RedisTemplate redisTemplate;

	@Pointcut("@annotation(org.jeecgframework.core.annotation.Ehcache)")
	public void simplePointcut() {}
	
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Around("simplePointcut()")
	public Object aroundLogCalls(ProceedingJoinPoint joinPoint)
			throws Throwable {
		String targetName = joinPoint.getTarget().getClass().toString();
		String methodName = joinPoint.getSignature().getName();
		Object[] arguments = joinPoint.getArgs();
		Class[] argTypes=new Class[arguments.length];
		if (arguments!=null && arguments.length>0) {
			for (int i = 0; i < arguments.length; i++) {
				argTypes[i]=arguments[i].getClass();
			}
		}
		Method method=joinPoint.getTarget().getClass().getMethod(methodName, argTypes);
		//试图得到标注的Ehcache类
		Ehcache flag = method.getAnnotation(Ehcache.class);
		if(flag==null){
			return null;
		}
		String cacheKey ="";
		if(StringUtils.isNotBlank(flag.cacheName())){
			cacheKey=flag.cacheName();
		}else{
			cacheKey=getCacheKey(targetName, methodName, arguments);
		}
		
		Object result=null;
		BoundValueOperations<String,Object> valueOps = redisTemplate.boundValueOps(cacheKey);
		if(flag.eternal()){
			//永久缓存
			result = valueOps.get();
		}else{
			//临时缓存
			result = valueOps.get();
			valueOps.expire(20, TimeUnit.MINUTES);
		}
		

		if (result == null) {
			if ((arguments != null) && (arguments.length != 0)) {
				result = joinPoint.proceed(arguments);
			} else {
				result = joinPoint.proceed();
			}

			if(flag.eternal()){
				//永久缓存
				valueOps.set(result);
			}else{
				//临时缓存
				valueOps.set(result,20, TimeUnit.MINUTES);
			}
		}
		return result;
	}
	
	/**
	 * 获得cache key的方法，cache key是Cache中一个Element的唯一标识 cache key包括
	 * 包名+类名+方法名，如com.co.cache.service.UserServiceImpl.getAllUser
	 */
	private String getCacheKey(String targetName, String methodName,
			Object[] arguments) {
		StringBuffer sb = new StringBuffer();
		sb.append(targetName).append(".").append(methodName);
		if ((arguments != null) && (arguments.length != 0)) {
			for (int i = 0; i < arguments.length; i++) {
				sb.append(".").append(JSON.toJSONString(arguments[i]));
			}
		}
		return sb.toString();
	}
}
