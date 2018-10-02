package csAZelcs;

//import csAZelcs.DynamoDBUtil;
import csAZelcs.AppointmentModel;
import csAZelcs.NewException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//JAX-RS
import javax.ws.rs.*;
import javax.ws.rs.core.*;

//AWS SDK
import com.amazonaws.auth.*;
import com.amazonaws.services.dynamodbv2.*;
import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig.SaveBehavior;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
// TO CREATE A TABLE - also uncomment block from line 191
//import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
//import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;

@Path("/appo")
// API
public class AppointmentPaths {
	
	public DynamoDBMapper mapper;
	public AmazonDynamoDB dynamoDB;
	
	// Create a mapper object in constructor
	public AppointmentPaths() {
		AWSCredentials credentials = new DefaultAWSCredentialsProviderChain().getCredentials();
		AmazonDynamoDB dynamoDB = new AmazonDynamoDBClient(credentials);
		dynamoDB.setEndpoint("http://localhost:8000");
		this.dynamoDB = dynamoDB;
		DynamoDBMapper mapper = new DynamoDBMapper(dynamoDB);
		this.mapper = mapper;
	}
	
	public DynamoDBMapper getMapper() {
		return this.mapper;
	}
	
	public AmazonDynamoDB getDynamoDB() {
		return this.dynamoDB;
	}
	
@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/user/{username}")
	// Get all appointments of a user from dynamoDB
	public Iterable<AppointmentModel> getAll(@PathParam("username") String username)
	{
		DynamoDBMapper mapper = this.getMapper();
		if (username != null) {
			// Easier way to implement
			Map<String, AttributeValue> eav = new HashMap<String, AttributeValue>();
	        eav.put(":val1", new AttributeValue().withS(username));
	        DynamoDBScanExpression scanExpression  = new DynamoDBScanExpression()
	    	    .withFilterExpression("username = :val1")
	            .withExpressionAttributeValues(eav);
	        // Get all appointments of a user
	        List<AppointmentModel> result = mapper.scan(AppointmentModel.class, scanExpression);
	        return result;
		} else {
			throw new NewException("Username missing");
		}
		
	}
	
@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/user/{username}/{from}/{to}")
	// Get all appointments of a user from dynamoDB in time period from - to
	public Iterable<AppointmentModel> getFromTo(@PathParam("username") String username,
											@PathParam("from") String from,
											@PathParam("to") String to)
	{
		DynamoDBMapper mapper = this.getMapper();
		if (username != null && from != null && to != null) {
			// Easier way to implement
			Map<String, AttributeValue> eav = new HashMap<String, AttributeValue>();
	        eav.put(":val1", new AttributeValue().withS(username));
	        DynamoDBScanExpression scanExpression  = new DynamoDBScanExpression()
	    	    .withFilterExpression("username = :val1")
	            .withExpressionAttributeValues(eav);
	        // Get all appointments of a user
	        List<AppointmentModel> result = mapper.scan(AppointmentModel.class, scanExpression);
	    	long fromParsed = Long.parseLong(from);
			long toParsed = Long.parseLong(to);
			List<AppointmentModel> filteredList = new ArrayList<AppointmentModel>();
	        for (int i = 0; i < result.size(); i++) {
	        	AppointmentModel ap = result.get(i);
	        	Long date = ap.getDate();
				if (fromParsed <= date && date <= toParsed) {
					filteredList.add(ap);
				}
			}
			return filteredList;
		} else {
			throw new NewException("Username, From or To date missing");
		}
		
	}
	
@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/user/{username}/after/{from}")
	// Get all appointments of a user from dynamoDB in time period from - to
	public Iterable<AppointmentModel> getFrom(@PathParam("username") String username,
											@PathParam("from") String from)
	{
		DynamoDBMapper mapper = this.getMapper();
		
		if (username != null && from != null) {
			// Easier way to implement
			Map<String, AttributeValue> eav = new HashMap<String, AttributeValue>();
	        eav.put(":val1", new AttributeValue().withS(username));
	        DynamoDBScanExpression scanExpression  = new DynamoDBScanExpression()
	    	    .withFilterExpression("username = :val1")
	            .withExpressionAttributeValues(eav);
	        // Get all appointments of a user
	        List<AppointmentModel> result = mapper.scan(AppointmentModel.class, scanExpression);
	    	long fromParsed = Long.parseLong(from);
			List<AppointmentModel> filteredList = new ArrayList<AppointmentModel>();
	        for (int i = 0; i < result.size(); i++) {
	        	AppointmentModel ap = result.get(i);
	        	Long date = ap.getDate();
				if (fromParsed <= date) {
					filteredList.add(ap);
				}
			}
			return filteredList;
		} else {
			throw new NewException("Username or From date missing");
		}
		
	}
	
@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/user/{username}/before/{to}")
	// Get all appointments of a user from dynamoDB in time period from - to
	public Iterable<AppointmentModel> getTo(@PathParam("username") String username,
											@PathParam("to") String to)
	{
		DynamoDBMapper mapper = this.getMapper();
		if (username != null && to != null) {
			// Easier way to implement
			Map<String, AttributeValue> eav = new HashMap<String, AttributeValue>();
	        eav.put(":val1", new AttributeValue().withS(username));
	        DynamoDBScanExpression scanExpression  = new DynamoDBScanExpression()
	    	    .withFilterExpression("username = :val1")
	            .withExpressionAttributeValues(eav);
	        // Get all appointments of a user
	        List<AppointmentModel> result = mapper.scan(AppointmentModel.class, scanExpression);
	    	long toParsed = Long.parseLong(to);
			List<AppointmentModel> filteredList = new ArrayList<AppointmentModel>();
	        for (int i = 0; i < result.size(); i++) {
	        	AppointmentModel ap = result.get(i);
	        	Long date = ap.getDate();
				if (date <= toParsed) {
					filteredList.add(ap);
				}
			}
			return filteredList;
		} else {
			throw new NewException("Username or To date missing");
		}
	}
	
@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{id}")
	// Get an appointments by id
	public AppointmentModel getById(@PathParam("id") String id)
	{
		DynamoDBMapper mapper = this.getMapper();
		String hashKey = id;
		System.out.println(hashKey);
		if (hashKey != null) {
			AppointmentModel result = mapper.load(AppointmentModel.class, hashKey);
			return result;
		} else {
			throw new NewException("Id is missing");
		}
	}
	
@POST
	@Produces(MediaType.TEXT_PLAIN)
	// Create a new appointment
	public Response addNew(@FormParam("description") String description,
							@FormParam("apDateTime") String apDateTime,
							@FormParam("duration") String duration,
							@FormParam("username") String username) {
		try	{
			DynamoDBMapper mapper = this.getMapper();
			// Parse strings to numbers
			long dateTimePasred = Long.parseLong(apDateTime);
			long durationParsed = Long.parseLong(duration);
			// Create and populate an appointment object
			AppointmentModel appo = new AppointmentModel();
			appo.setDate(dateTimePasred);
			appo.setDuration(durationParsed);
			appo.setDescription(description);
			appo.setUsername(username);
			
			//CREATE TABLE IN DynamoDB (FOR THE FIRST RUN - uncomment 2 imports above as well)
			/*CreateTableRequest req = mapper.generateCreateTableRequest(AppointmentModel.class);
			req.setProvisionedThroughput(new ProvisionedThroughput(5L, 5L));
			req.getGlobalSecondaryIndexes().forEach(v -> v.setProvisionedThroughput(new ProvisionedThroughput(5L, 5L)));
			this.dynamoDB.createTable(req);*/
			
			// Save to dynamoDB
			mapper.save(appo);
			return Response.status(201)
					.entity("Appointment saved")
					.build();
		} catch (Exception e) {
			return Response.status(400).entity("Error: " + e).build();
		}
	}
	
@PUT
	@Produces(MediaType.TEXT_PLAIN)
	// Create a new appointment
	public Response update( @FormParam("id") String id,
							@FormParam("description") String description,
							@FormParam("apDateTime") String apDateTime,
							@FormParam("duration") String duration,
							@FormParam("username") String username) {
		try	{
			DynamoDBMapper mapper = this.getMapper();
			DynamoDBMapperConfig dynamoDBMapperConfig = new DynamoDBMapperConfig(SaveBehavior.UPDATE);
			// Parse strings to numbers
			long dateTimePasred = Long.parseLong(apDateTime);
			long durationParsed = Long.parseLong(duration);
			// Create and populate an appointment object
			AppointmentModel appo = new AppointmentModel();
			appo.setId(id);
			appo.setDate(dateTimePasred);
			appo.setDuration(durationParsed);
			appo.setDescription(description);
			appo.setUsername(username);
			// Save to dynamoDB
			mapper.save(appo, dynamoDBMapperConfig);
			return Response.status(200)
					.entity("Appointment updated")
					.build();
		} catch (Exception e) {
			return Response.status(400).entity("Error: " + e).build();
		}
	}

@DELETE
	@Produces(MediaType.TEXT_PLAIN)
	// Create a new appointment
	public Response delete(@FormParam("id") String id) {
		try	{
			DynamoDBMapper mapper = this.getMapper();
			AppointmentModel appo = new AppointmentModel();
			appo.setId(id);
			// Delete from dynamoDB
			mapper.delete(appo);
			return Response.status(200)
					.entity("Appointment deleted")
					.build();
		} catch (Exception e) {
			return Response.status(400).entity("Error: " + e).build();
		}
	}
}
