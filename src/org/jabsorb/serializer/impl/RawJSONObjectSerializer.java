/*
 * jabsorb - a Java to JavaScript Advanced Object Request Broker
 * http://www.jabsorb.org
 *
 * Copyright 2007 Arthur Blake and William Becker
 *
 * based on original code from
 * JSON-RPC-Java - a JSON-RPC to Java Bridge with dynamic invocation
 *
 * Copyright Metaparadigm Pte. Ltd. 2004.
 * Michael Clark <michael@metaparadigm.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.jabsorb.serializer.impl;

import org.jabsorb.json.JSONObject;
import org.jabsorb.serializer.AbstractSerializer;
import org.jabsorb.serializer.MarshallException;
import org.jabsorb.serializer.ObjectMatch;
import org.jabsorb.serializer.SerializerState;
import org.jabsorb.serializer.UnmarshallException;

public class RawJSONObjectSerializer extends AbstractSerializer
{
  private final static long serialVersionUID = 2;

  private static Class[] _serializableClasses = new Class[]{JSONObject.class};

  private static Class[] _JSONClasses = new Class[]{JSONObject.class};

  public Class[] getSerializableClasses()
  {
    return _serializableClasses;
  }

  public Class[] getJSONClasses()
  {
    return _JSONClasses;
  }

  public ObjectMatch tryUnmarshall(SerializerState state, Class clazz,
                                   Object jso) throws UnmarshallException
  {
    return ObjectMatch.OKAY;
  }

  public Object unmarshall(SerializerState state, Class clazz, Object jso)
    throws UnmarshallException
  {
    return jso;
  }

  public Object marshall(SerializerState state, Object o)
    throws MarshallException
  {
    return o;
  }

}
