/*
 * JSON-RPC-Java - a JSON-RPC to Java Bridge with dynamic invocation
 *
 * $Id: PrimitiveSerializer.java,v 1.4 2005/06/16 23:26:14 mclark Exp $
 *
 * Copyright Metaparadigm Pte. Ltd. 2004.
 * Michael Clark <michael@metaparadigm.com>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public (LGPL)
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details: http://www.gnu.org/
 *
 */

package com.metaparadigm.jsonrpc;

public class PrimitiveSerializer extends AbstractSerializer
{
    private static Class[] _serializableClasses = new Class[]
	{ int.class, byte.class, short.class, long.class,
	  float.class, double.class };

    private static Class[] _JSONClasses = new Class[]
	{ Integer.class, Byte.class, Short.class, Long.class,
	  Float.class, Double.class, String.class };

    public Class[] getSerializableClasses() { return _serializableClasses; }
    public Class[] getJSONClasses() { return _JSONClasses; }

    public ObjectMatch tryUnmarshall(SerializerState state,
				     Class clazz, Object jso)
	throws UnmarshallException
    {
	try {
	    toPrimitive(clazz, jso);
	} catch (NumberFormatException e) {
	    throw new UnmarshallException("not a primitive");
	}
	return ObjectMatch.OKAY;
    }
    
    public Object toPrimitive(Class clazz, Object jso)
    throws NumberFormatException {
      if (int.class.equals(clazz)) {
        if (jso instanceof String) 
          return new Integer((String)jso);
        else 
          return new Integer(((Number) jso).intValue());
      } else if (long.class.equals(clazz)) {
        if (jso instanceof String) 
          return new Long((String)jso);
        else 
          return new Long(((Number) jso).longValue());
      } else if (short.class.equals(clazz)) {
        if (jso instanceof String) 
          return new Short((String)jso);
        else 
          return new Short(((Number) jso).shortValue());
      } else if (byte.class.equals(clazz)) {
        if (jso instanceof String) 
          return new Byte((String)jso);
        else 
          return new Byte(((Number) jso).byteValue());
      } else if (float.class.equals(clazz)) {
        if (jso instanceof String) 
          return new Float((String)jso);
        else 
          return new Float(((Number) jso).floatValue());
      } else if (double.class.equals(clazz)) {
        if (jso instanceof String) 
          return new Double((String)jso);
        else 
          return new Double(((Number) jso).doubleValue());
      }
      return null;
    }
    
    
    public Object unmarshall(SerializerState state, Class clazz, Object jso)
    throws UnmarshallException {
      try {
        return toPrimitive(clazz, jso);
      } catch (NumberFormatException nfe) {
        throw new UnmarshallException("cannot convert object "+jso+" to type "+clazz.getName());
      }
    }

    public Object marshall(SerializerState state, Object o)
	throws MarshallException
    {
	return o;
    }

}
