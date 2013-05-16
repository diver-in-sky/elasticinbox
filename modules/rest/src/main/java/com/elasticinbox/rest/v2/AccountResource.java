/**
 * Copyright (c) 2011-2012 Optimax Software Ltd.
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *  * Neither the name of Optimax Software, ElasticInbox, nor the names
 *    of its contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.elasticinbox.rest.v2;

import java.io.IOException;
import java.net.URI;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.elasticinbox.common.utils.JSONUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elasticinbox.core.AccountDAO;
import com.elasticinbox.core.DAOFactory;
import com.elasticinbox.core.model.Mailbox;
import com.elasticinbox.rest.BadRequestException;
import com.elasticinbox.rest.JSONResponse;

/**
 * This JAX-RS resource is responsible for managing user accounts.
 * 
 * @author Rustam Aliyev
 */
@Path("{domain}/{user}")
public final class AccountResource
{
	private final AccountDAO accountDAO;

	private final static Logger logger = 
		LoggerFactory.getLogger(AccountResource.class);

	@Context UriInfo uriInfo;

	public AccountResource() {
		DAOFactory dao = DAOFactory.getDAOFactory();
		accountDAO = dao.getAccountDAO();
	}

	/**
	 * Get account information
	 * 
	 * @param user
	 * @param domain
	 * @return
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response get(
			@PathParam("user") final String user,
			@PathParam("domain") final String domain)
	{
		Mailbox mailbox = new Mailbox(user, domain);

		byte[] response;
		try {
			response = JSONUtils.fromObject(accountDAO.getAttributes(mailbox));
		} catch (IOException e) {
			logger.error("Account get failed: {}", mailbox.getId());
			throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
		}

		return Response.ok(response).build();
	}

	/**
	 * Initialize new account
	 *
	 * @param user
	 * @param domain
	 * @return
	 */
	@POST
	@Path("create")
	@Produces(MediaType.APPLICATION_JSON)
	public Response createWithPassword(
			@PathParam("user") final String user,
			@PathParam("domain") final String domain,
			@QueryParam("password") final String passwordHash)
	{
		Mailbox mailbox = new Mailbox(user, domain);

		try {
			accountDAO.add(mailbox);
		} catch (IllegalArgumentException iae) {
			throw new BadRequestException(iae.getMessage());
		} catch (IOException e) {
			logger.error("Account initialization failed: {}", mailbox.getId());
			throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
		}

		URI messageUri = uriInfo.getAbsolutePathBuilder().path("mailbox").build();

		return Response.created(messageUri).entity(JSONResponse.OK).build();
	}
	
	/**
	 * Initialize new account
	 * 
	 * @param user
	 * @param domain
	 * @return
	 */
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	public Response add(
			@PathParam("user") final String user,
			@PathParam("domain") final String domain)
	{
		Mailbox mailbox = new Mailbox(user, domain);

		try {
			accountDAO.add(mailbox);
		} catch (IllegalArgumentException iae) {
			throw new BadRequestException(iae.getMessage());
		} catch (IOException e) {
			logger.error("Account initialization failed: {}", mailbox.getId());
			throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
		}

		URI messageUri = uriInfo.getAbsolutePathBuilder().path("mailbox").build();

		return Response.created(messageUri).entity(JSONResponse.OK).build();
	}

	/**
	 * Delete account and all associated objects
	 * 
	 * @param user
	 * @param domain
	 * @return
	 */
	@DELETE
	@Produces(MediaType.APPLICATION_JSON)
	public Response delete(
			@PathParam("user") final String user,
			@PathParam("domain") final String domain)
	{
		final Mailbox mailbox = new Mailbox(user, domain);

		try {
			// run deletion work in separate thread
			Thread t = new Thread() {
				@Override
				public void run() {
					try {
						accountDAO.delete(mailbox);
					} catch (IOException e) {
						logger.info("Account deletion failed: ", e);
					}
				}
			};

			// start thread
			t.start();
			t.join();
		} catch (Exception e) {
			logger.error("Account deletion failed: ", e);
			throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
		}

		return Response.noContent().build();
	}
}
