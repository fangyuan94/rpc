package com.fc.rpc.common.utils;

import com.fc.rpc.common.exception.SerializationException;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.serializer.support.DeserializingConverter;
import org.springframework.core.serializer.support.SerializingConverter;


/**
 * Java原生版的 Serialize
 * @author fangyuan
 */
public class SerializeUtil {

	private final static Converter<Object, byte[]> serializer = new SerializingConverter();
	private final static Converter<byte[], Object> deserializer =  new DeserializingConverter();


	public static Object deserialize(byte[] bytes) {
		if (bytes == null || bytes.length == 0) {
			throw new SerializationException("bytes 不能为空");
		}

		try {
			return deserializer.convert(bytes);
		} catch (Exception ex) {
			throw new SerializationException("Cannot deserialize", ex);
		}
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T deserialize(byte[] bytes,Class<T> t) {

		try {
			
			return  (T) deserializer.convert(bytes);
		} catch (Exception ex) {
			throw new SerializationException("Cannot deserialize", ex);
		}
	}
	
	public static byte[] serialize(Object object) {
		if (object == null) {
			throw new SerializationException("object 不能为空");
		}
		try {
			return serializer.convert(object);
		} catch (Exception ex) {
			throw new SerializationException("Cannot serialize", ex);
		}
	}

}
