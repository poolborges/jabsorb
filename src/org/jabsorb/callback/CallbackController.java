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

package org.jabsorb.callback;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class that is instantiated per bridge to maintain the list of callbacks and
 * provides an interface to invoke them.
 */
public class CallbackController implements Serializable
{
  /**
   * Generated version id.
   */
  private final static long serialVersionUID = 2;

  /**
   * The log used for this class.
   */
  private final static Logger log = LoggerFactory
      .getLogger(CallbackController.class);

  /**
   * Holds all callbacks registered with this controller. Type: CallbackData
   */
  private HashSet callbackSet;

  /**
   * Whether debugging is enabled.
   */
  private boolean debug;

  /**
   * Default constructor.
   */
  public CallbackController()
  {
    callbackSet = new HashSet();
    debug = false;
  }

  /**
   * Enable or disable debugging message from this callback controllerinstance.
   * 
   * @param debug flag to enable or disable debugging messages
   */
  public void setDebug(boolean debug)
  {
    this.debug = debug;
  }

  /**
   * Are debugging messages enabled on this callback controller instance.
   * 
   * @return true or false depending on whether debugging messages are enabled.
   */
  protected boolean isDebug()
  {
    return debug;
  }

  /**
   * Registers a callback to be called before and after method invocation
   * 
   * @param callback The object implementing the InvocationCallback Interface
   * @param contextInterface The type of transport Context interface the
   *          callback is interested in eg. HttpServletRequest.class for the
   *          servlet transport.
   */
  public void registerCallback(InvocationCallback callback,
      Class contextInterface)
  {

    synchronized (callbackSet)
    {
      callbackSet.add(new CallbackData(callback, contextInterface));
    }
    if (debug)
    {
      log.info("registered callback " + callback.getClass().getName()
          + " with context interface " + contextInterface.getName());
    }
  }

  /**
   * Unregisters a callback
   * 
   * @param callback The previously registered InvocationCallback object
   * @param contextInterface The previously registered transport Context
   *          interface.
   */
  public void unregisterCallback(InvocationCallback callback,
      Class contextInterface)
  {

    synchronized (callbackSet)
    {
      callbackSet.remove(new CallbackData(callback, contextInterface));
    }
    if (debug)
    {
      log.info("unregistered callback " + callback.getClass().getName()
          + " with context " + contextInterface.getName());
    }
  }

  /**
   * Calls the 'preInvoke' callback handler. 
   * 
   * @param context The transport context (the HttpServletRequest object in the
   *          case of the HTTP transport).
   * @param instance The object instance or null if it is a static method.
   * @param method The method that is about to be called.
   * @param arguments The argements to be passed to the method.
   * @throws Exception If preInvoke fails
   */
  public void preInvokeCallback(Object context, Object instance, Method method,
      Object arguments[]) throws Exception
  {
    synchronized (callbackSet)
    {
      Iterator i = callbackSet.iterator();
      while (i.hasNext())
      {
        CallbackData cbdata = (CallbackData) i.next();
        if (cbdata.understands(context))
        {
          cbdata.getCallback().preInvoke(context, instance, method, arguments);
        }
      }
    }
  }

  /**
   * Calls the 'postInvoke' callback handler. 
   * 
   * @param context The transport context (the HttpServletRequest object in the
   *          case of the HTTP transport).
   * @param instance The object instance or null if it is a static method.
   * @param method The method that was just called.
   * @param result The object that was returned.
   * @throws Exception if postInvoke fails
   */
  public void postInvokeCallback(Object context, Object instance,
      Method method, Object result) throws Exception
  {
    synchronized (callbackSet)
    {
      Iterator i = callbackSet.iterator();
      while (i.hasNext())
      {
        CallbackData cbdata = (CallbackData) i.next();
        if (cbdata.understands(context))
        {
          cbdata.getCallback().postInvoke(context, instance, method, result);
        }
      }
    }
  }

  /**
   * Calls the 'invocation Error' callback handler.
   * 
   * @param context The transport context (the HttpServletRequest object in the
   *          case of the HTTP transport).
   * @param instance The object instance or null if it is a static method.
   * @param method Method that failed the invocation.
   * @param error Error resulting from the invocation.
   */
  public void errorCallback(Object context, Object instance, Method method,
      Throwable error)
  {
    synchronized (callbackSet)
    {
      Iterator i = callbackSet.iterator();
      while (i.hasNext())
      {
        CallbackData cbdata = (CallbackData) i.next();
        if (cbdata.understands(context)
            && (cbdata.getCallback() instanceof ErrorInvocationCallback))
        {
          ErrorInvocationCallback ecb = (ErrorInvocationCallback) cbdata
              .getCallback();
          try
          {
            ecb.invocationError(context, instance, method, error);
          }
          catch (Throwable th)
          {
            // Ignore all errors in callback, don't want
            // event listener to bring everything to its knees.
          }
        }
      }
    }
  }

}
