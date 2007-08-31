/*
 * JSON-RPC-Java - a JSON-RPC to Java Bridge with dynamic invocation
 *
 * $Id: LocalArgResolver.java,v 1.3 2005/10/17 12:28:38 mclark Exp $
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

/**
 * Interface to be implemented by objects registered to locally resolve
 * method arguments using transport context information.
 */

public interface LocalArgResolver
{
    /**
     * Resolve an argument locally using the given context information.
     *
     * @param context   The transport context (the HttpServletRequest
                        object in the case of the HTTP transport).
     */
    public Object resolveArg(Object context) throws LocalArgResolveException;
}
