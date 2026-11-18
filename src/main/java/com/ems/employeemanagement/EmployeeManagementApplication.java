package com.ems.employeemanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Employee Management System.
 *
 * <p>@SpringBootApplication is a convenience annotation that combines:
 * <ul>
 *   <li>@Configuration   — marks this as a source of bean definitions</li>
 *   <li>@EnableAutoConfiguration — tells Spring Boot to auto-configure beans
 *       based on classpath, properties and other beans</li>
 *   <li>@ComponentScan   — scans this package and all sub-packages for
 *       Spring-managed components (@Component, @Service, @Repository, etc.)</li>
 * </ul>
 *
 * <p>All packages inside {@code com.ems.employeemanagement} are automatically
 * discovered — controllers, services, repositories and configuration classes.
 */
@SpringBootApplication
public class EmployeeManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(EmployeeManagementApplication.class, args);
    }
}

