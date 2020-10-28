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
	
	/*
	 * Codes for PostgreSQL
	@Override 
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
	
		// authentication -> who 
		// if you want in memory authentication than use inMemory but here I am using external db
		// here using JDBC authentication will use jdbc authentication
		auth.jdbcAuthentication().dataSource(dataSource)
		//use bCryptPasswordEncoder 
		.passwordEncoder(bCryptPasswordEncoder)
		//get username and password the authentication part. username password enabled are columns defined in postgres
		.usersByUsernameQuery("select username, password, enabled " + "from user_accounts where username= ?")
		//get the roles associated with username - username and roles are columns . user_accounts is table name in postgres we defined
		.authoritiesByUsernameQuery("select username, role " + "from user_accounts where username=?");
	}
	*/

	//authorization-> what - add in ant matchers 
	protected void configure(HttpSecurity http) throws Exception{ 
		http.authorizeRequests()	
		
		.antMatchers("/registerResearch").hasRole("USER")
		.antMatchers("/manageResearch").hasRole("USER")
		.antMatchers("/apply/**").hasRole("USER")
		.antMatchers("/researchers/**").hasRole("USER")
		.antMatchers("/studies/**").hasRole("ADMINISTRATOR")
		//permit h2 console only if we are using h2-console
		.antMatchers("/h2-console/**").permitAll()
		.antMatchers("/").permitAll()
		.and()
			.formLogin()
		.and()
			.logout()
			.invalidateHttpSession(true)
			.clearAuthentication(true)
			.logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
			.logoutSuccessUrl("/")
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
