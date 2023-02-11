package servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Collection;
import javax.ejb.EJB;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import model.User;
import stateless.UserServiceBean;

@Path("/users")
public class UserRestServlet {

  // inject a reference to the UserServiceBean slsb
  @EJB UserServiceBean userService;

  // Mapea lista de pojo a JSON
  ObjectMapper mapper = new ObjectMapper();

  @GET
  @Produces(MediaType.APPLICATION_JSON)
	public String findAll() throws IOException {
		Collection<User> users = userService.findAll();
    return mapper.writeValueAsString(users);
	}

  @GET
  @Path("/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public String find(@PathParam("id") int id) throws IOException {
    User givenUser = userService.find(id);
    return mapper.writeValueAsString(givenUser);
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  public String create(String json) throws IOException {
    User newUser = mapper.readValue(json, User.class);
    return mapper.writeValueAsString(userService.create(newUser));
  }

  @DELETE
  @Path("/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public String delete(@PathParam("id") int id) throws IOException {
    return mapper.writeValueAsString(userService.delete(id));
  }

  @PUT
  @Path("/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public String modify(String json, @PathParam("id") int id) throws IOException {
    User modifiedUser = mapper.readValue(json, User.class);
    return mapper.writeValueAsString(userService.modify(id, modifiedUser));
  }

}
