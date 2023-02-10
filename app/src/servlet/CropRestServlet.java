package servlet;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.ejb.EJB;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import model.Crop;
import stateless.CropServiceBean;
import stateless.Page;

@Path("/cultivo")
public class CropRestServlet {

  // inject a reference to the CropServiceBean slsb
  @EJB CropServiceBean crpService;

  //mapea lista de pojo a JSON
  ObjectMapper mapper = new ObjectMapper();

  @GET
  @Path("/findAllCultivos")
  @Produces(MediaType.APPLICATION_JSON)
  public String findAllCultivos() throws IOException {
    Collection<Crop> crops = crpService.findAll();
    return mapper.writeValueAsString(crops);
  }

  @GET
  public String findAll(@QueryParam("page") Integer page, @QueryParam("cant") Integer cant, @QueryParam("search") String search) throws IOException {
    Map<String, String> map = new HashMap<String, String>();

    // convert JSON string to Map
    map = mapper.readValue(search, new TypeReference<Map<String, String>>(){});

    Page<Crop> crops = crpService.findByPage(page, cant, map);
    return mapper.writeValueAsString(crops);
  }

  @GET
  @Path("/findAllActiveCrops")
  @Produces(MediaType.APPLICATION_JSON)
  public String findAllActive() throws IOException {
    Collection<Crop> activeCrops = crpService.findAllActive();
    return mapper.writeValueAsString(activeCrops);
  }

  @GET
  @Path("/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public String find(@PathParam("id") int id) throws IOException {
    Crop givenCrop = crpService.find(id);
    return mapper.writeValueAsString(givenCrop);
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  public String create(String json) throws IOException  {
    Crop newCrop = mapper.readValue(json,Crop.class);
    newCrop = crpService.create(newCrop);
    return mapper.writeValueAsString(newCrop);
  }

  @DELETE
  @Path("/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public String remove(@PathParam("id") int id) throws IOException {
    Crop givenCrop = crpService.remove(id);
    return mapper.writeValueAsString(givenCrop);
  }

  @PUT
  @Path("/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public String modify(@PathParam("id") int id, String json) throws IOException  {
    Crop modifiedCrop = mapper.readValue(json,Crop.class);
    modifiedCrop = crpService.modify(id, modifiedCrop);
    return mapper.writeValueAsString(modifiedCrop);
  }

}
