package csAZelcs;

import javax.ws.rs.*;
import javax.ws.rs.core.*;

public class NewException extends WebApplicationException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public NewException(String msg) {
		super(Response.status(Response.Status.NOT_FOUND).entity(msg).type("text/plain").build());
	}
}