package fr.rawz06.audit.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Configuration
@EnableAspectJAutoProxy
@ConditionalOnClass(name = "org.springframework.aop.aspectj.annotation.AspectJProxyFactory")
public class AuditAutoConfiguration {
    // AspectJ aspect beans are auto-discovered via classpath scanning
    // @Aspect @Component classes are automatically registered
}
