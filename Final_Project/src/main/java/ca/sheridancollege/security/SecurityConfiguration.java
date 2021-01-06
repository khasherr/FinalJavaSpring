package ca.sheridancollege.security;



import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

	//datasource tells what to use either in memory or jdbc
	// depends on what is outlined in the application.properties file as datasource
	@Autowired
	DataSource dataSource;
  
	@Autowired
	WebConfig webConfig;

	@Autowired
	private LoginAccessDeniedHandler accessDeniedHandler;

	@Autowired
	private UserDetailsServiceImpl userDetailsService;

	//authorization-> what - add in ant matchers 
	protected void configure(HttpSecurity http) throws Exception{ 
		http.authorizeRequests()	
		
		.antMatchers("/registerResearch").hasRole("USER")
		.antMatchers("/manageResearch").hasRole("USER")
		.antMatchers("/apply/**").hasRole("USER")
		.antMatchers("/researchers/**").hasRole("USER")
		.antMatchers("/studies/**").hasRole("ADMINISTRATOR")
		//permit h2 console only if we are using h2-console
		//Permit all since we are not using h2 database 
		//to store any important data
		.antMatchers("/h2-console/**").permitAll()
		.antMatchers("/").permitAll()
		.and()
			.formLogin()
			.loginPage("/login")
			.permitAll()
		.and()
			.logout()
			.invalidateHttpSession(true)
			.clearAuthentication(true)
			.logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
			.logoutSuccessUrl("/login?logout")
			.permitAll()
		.and()
			.exceptionHandling()
			.accessDeniedHandler(accessDeniedHandler);

	http.csrf().disable();
	http.headers().frameOptions().disable();
	
	
	}
	
	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception{
		
		auth.userDetailsService(userDetailsService).passwordEncoder(webConfig.passwordEncoder());
	}
}
