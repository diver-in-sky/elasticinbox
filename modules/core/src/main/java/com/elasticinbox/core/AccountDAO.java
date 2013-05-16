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

package com.elasticinbox.core;

import java.io.IOException;
import java.util.Map;

import com.elasticinbox.core.model.Mailbox;

/**
 * Interface for Account operations
 * 
 * @author Rustam Aliyev
 */
public interface AccountDAO
{
	/**
	 * Add new account and initialize set of predefined labels. 
	 * 
	 * @param mailbox
	 * @throws IOException
	 * @throws IllegalArgumentException
	 */
	public void add(Mailbox mailbox) throws IOException, IllegalArgumentException;

	/**
	 * Add new account with password and initialize set of predefined labels.
	 *
	 * @param mailbox
	 * @param passwordHash
	 * @throws IOException
	 * @throws IllegalArgumentException
	 */
	public void add(Mailbox mailbox, String passwordHash) throws IOException, IllegalArgumentException;

	/**
	 * Get password of account.
	 *
	 *
	 * @param mailbox
	 * @throws IOException
	 * @throws IllegalArgumentException
	 */
	public Map<String, Object> getAttributes(Mailbox mailbox) throws IOException, IllegalArgumentException;

	/**
	 * Delete account and all messages associated with it. Messages will be
	 * deleted immediately from blob store and metadata store. All other
	 * metadata information will also be cleared.
	 * 
	 * @param mailbox
	 * @throws IOException
	 */
	public void delete(Mailbox mailbox) throws IOException;
	
}
