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

package com.elasticinbox.core.utils;

import java.util.UUID;

import me.prettyprint.cassandra.utils.TimeUUIDUtils;

//import com.google.common.io.BaseEncoding;

import javax.xml.bind.DatatypeConverter;

/**
 * URL safe Base64 encoding/decoding of UUID
 * <p>
 * Produces a 22 character string which is a URL safe Base64 encoded UUID.
 * </p>
 * 
 * @author Rustam Aliyev
 */
public class Base64UUIDUtils
{
	public static String encode(UUID uuid)
	{
		String base64 = DatatypeConverter.printBase64Binary(TimeUUIDUtils.asByteArray(uuid));

		// remove padding
		if (base64.endsWith("==")) {
			base64 = base64.substring(0, base64.length() - 2);
		}
		
		return base64;
		
		// TODO use with Guava 14 (Jclouds 1.6)
//		return BaseEncoding.base64Url().omitPadding()
//				.encode(TimeUUIDUtils.asByteArray(uuid));
	}

	public static UUID decode(String encoded)
	{
		// TODO use with Guava 14 (Jclouds 1.6)
//		byte[] ba = BaseEncoding.base64Url().decode(encoded);
//		return TimeUUIDUtils.toUUID(ba);
		
		return TimeUUIDUtils.toUUID(DatatypeConverter.parseBase64Binary(encoded + "=="));
	}
}