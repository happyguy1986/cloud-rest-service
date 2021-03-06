/**
 * 
 */
package com.iflytek.edu.cloud.oauth2.support;

import java.io.IOException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.List;

import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * Create on @2014年8月4日 @下午6:29:43 
 * @author libinsong1204@gmail.com
 */
public class MappingJackson2HttpMessageConverterExt extends MappingJackson2HttpMessageConverter {
	
	public MappingJackson2HttpMessageConverterExt() {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		objectMapper.setDateFormat(dateFormat);
		this.setObjectMapper(objectMapper);
	}

	@Override
	protected void writeInternal(Object object, HttpOutputMessage outputMessage)
			throws IOException, HttpMessageNotWritableException {
		outputMessage.getHeaders().setContentType(MediaType.parseMediaType("text/javascript"));
		JsonEncoding encoding = getJsonEncoding(outputMessage.getHeaders().getContentType());
		JsonGenerator jsonGenerator =
				this.getObjectMapper().getFactory().createGenerator(outputMessage.getBody(), encoding);

		// A workaround for JsonGenerators not applying serialization features
		// https://github.com/FasterXML/jackson-databind/issues/12
		if (this.getObjectMapper().isEnabled(SerializationFeature.INDENT_OUTPUT)) {
			jsonGenerator.useDefaultPrettyPrinter();
		}
		
		List<String> values = outputMessage.getHeaders().get("callback");
		String callBack = null;
		if(values != null && values.size() > 0)
			callBack = values.get(0); 
		try {
			if(StringUtils.hasText(callBack)) {
				String json = this.getObjectMapper().writeValueAsString(object);
				json = callBack + "( " + json + " )";
				outputMessage.getBody().write(json.getBytes(Charset.forName("UTF-8")));
				outputMessage.getBody().flush();
			} else {
				this.getObjectMapper().writeValue(jsonGenerator, object);
			}
		} catch (JsonProcessingException ex) {
			throw new HttpMessageNotWritableException("Could not write JSON: " + ex.getMessage(), ex);
		}
	}
}
