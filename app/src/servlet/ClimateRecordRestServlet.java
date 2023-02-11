package servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Collection;
import javax.ejb.EJB;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import model.ClimateRecord;
import stateless.ClimateRecordServiceBean;

@Path("/climateRecords")
public class ClimateRecordRestServlet {

  // inject a reference to the ClimateRecordServiceBean slsb
  @EJB ClimateRecordServiceBean climateRecordService;

  // Mapea lista de pojo a JSON
  ObjectMapper mapper = new ObjectMapper();

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public String findAll() throws IOException {
    Collection<ClimateRecord> climateRecords = climateRecordService.findAll();
    return mapper.writeValueAsString(climateRecords);
  }

  @GET
  @Path("/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public String find(@PathParam("id") int id) throws IOException {
    ClimateRecord givenClimateRecord = climateRecordService.find(id);
    return mapper.writeValueAsString(givenClimateRecord);
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  public String create(String json) throws IOException  {
    ClimateRecord newClimateRecord = mapper.readValue(json,ClimateRecord.class);
    newClimateRecord = climateRecordService.create(newClimateRecord);
    return mapper.writeValueAsString(newClimateRecord);
  }

  @PUT
  @Path("/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public String modify(@PathParam("id") int id, String json) throws IOException  {
    ClimateRecord modifiedClimateRecord = mapper.readValue(json, ClimateRecord.class);
    modifiedClimateRecord = climateRecordService.modify(id, modifiedClimateRecord);
    return mapper.writeValueAsString(modifiedClimateRecord);
  }

}
