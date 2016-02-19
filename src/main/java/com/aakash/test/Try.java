package com.aakash.test;

import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;


@Path("/launch")
public class Try {
	/*
	 * @GET public Response getRedirect(@Context ServletContext context) {
	 * UriBuilder builder = UriBuilder.fromPath(context.getContextPath());
	 * builder.path("http://localhost:8080/thisoneislast/launch/start"); return
	 * Response.seeOther(builder.build()).build(); }
	 */
	
	@POST
	@Path("/start")
	//@Produces("text/html")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response printt(@FormParam("user") String user, @Context ServletContext context) {
		// return Response.ok(view).build();
		// return Response.status(200).entity("Username: "+user).build();
		// return Response.seeOther(builder.build()).build();
		UriBuilder builder = UriBuilder.fromPath(context.getContextPath());
		builder.path("/launch.jsp");
		return Response.seeOther(builder.build()).build();
	}
}
