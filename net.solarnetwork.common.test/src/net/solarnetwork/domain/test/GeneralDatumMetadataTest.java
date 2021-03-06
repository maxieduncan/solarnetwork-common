/* ==================================================================
 * GeneralNodeDatumSamplesTest.java - Aug 29, 2014 1:09:38 PM
 * 
 * Copyright 2007-2014 SolarNetwork.net Dev Team
 * 
 * This program is free software; you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License as 
 * published by the Free Software Foundation; either version 2 of 
 * the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License 
 * along with this program; if not, write to the Free Software 
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 
 * 02111-1307 USA
 * ==================================================================
 */

package net.solarnetwork.domain.test;

import java.util.HashMap;
import java.util.Map;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.solarnetwork.domain.GeneralDatumMetadata;

/**
 * Test cases for {@link GeneralDatumMetadata}.
 * 
 * @author matt
 * @version 1.1
 */
public class GeneralDatumMetadataTest {

	private ObjectMapper objectMapper;

	@Before
	public void setup() {
		objectMapper = new ObjectMapper();
		objectMapper.setSerializationInclusion(Include.NON_NULL);
		objectMapper.configure(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS, true);
	}

	private GeneralDatumMetadata getTestInstance() {
		GeneralDatumMetadata meta = new GeneralDatumMetadata();

		Map<String, Object> info = new HashMap<String, Object>(2);
		info.put("msg", "Hello, world.");
		meta.setInfo(info);

		meta.addTag("test");

		return meta;
	}

	@Test
	public void serializeJson() throws Exception {
		String json = objectMapper.writeValueAsString(getTestInstance());
		Assert.assertEquals("{\"m\":{\"msg\":\"Hello, world.\"},\"t\":[\"test\"]}", json);
	}

	@Test
	public void serializeJsonWithPropertyMeta() throws Exception {
		GeneralDatumMetadata meta = getTestInstance();
		meta.putInfoValue("watts", "unit", "W");
		String json = objectMapper.writeValueAsString(meta);
		Assert.assertEquals(
				"{\"m\":{\"msg\":\"Hello, world.\"},\"pm\":{\"watts\":{\"unit\":\"W\"}},\"t\":[\"test\"]}",
				json);
	}

	@Test
	public void deserializeJson() throws Exception {
		String json = "{\"m\":{\"ploc\":2502287},\"t\":[\"test\"]}";
		GeneralDatumMetadata samples = objectMapper.readValue(json, GeneralDatumMetadata.class);
		Assert.assertNotNull(samples);
		Assert.assertEquals(Long.valueOf(2502287), samples.getInfoLong("ploc"));
		Assert.assertTrue("Tag exists", samples.hasTag("test"));
	}

	@Test
	public void deserializeJsonWithPropertyMeta() throws Exception {
		String json = "{\"m\":{\"ploc\":2502287},\"pm\":{\"watts\":{\"unit\":\"W\"}},\"t\":[\"test\"]}";
		GeneralDatumMetadata samples = objectMapper.readValue(json, GeneralDatumMetadata.class);
		Assert.assertNotNull(samples);
		Assert.assertEquals(Long.valueOf(2502287), samples.getInfoLong("ploc"));
		Assert.assertEquals("W", samples.getInfoString("watts", "unit"));
		Assert.assertTrue("Tag exists", samples.hasTag("test"));
	}

	@Test
	public void deserializeJsonWithNestedMeta() throws Exception {
		String json = "{\"m\":{\"map\":{\"foo\":1,\"bar\":\"bam\"}}}";
		GeneralDatumMetadata samples = objectMapper.readValue(json, GeneralDatumMetadata.class);
		Assert.assertNotNull(samples);
		Object map = samples.getInfo().get("map");
		Assert.assertTrue("Nested map parsed", map instanceof Map);
		@SuppressWarnings("unchecked")
		Map<String, ?> stringMap = (Map<String, ?>) map;
		Assert.assertEquals("Nested number", Integer.valueOf(1), stringMap.get("foo"));
		Assert.assertEquals("Nested string", "bam", stringMap.get("bar"));
	}

	@Test
	public void removeInfoKey() {
		GeneralDatumMetadata meta = getTestInstance();
		meta.putInfoValue("msg", null);
		meta.putInfoValue("does.not.exist", null);
		meta.putInfoValue("foo", "bar", "bam");
		meta.putInfoValue("foo", "bar", null);
		Assert.assertNull(meta.getInfoString("msg"));
		Assert.assertNull(meta.getInfoString("foo", "bar"));
	}

}
