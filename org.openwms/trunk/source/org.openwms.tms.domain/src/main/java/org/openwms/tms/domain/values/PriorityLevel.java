/*
 * openwms.org, the Open Warehouse Management System.
 *
 * This file is part of openwms.org.
 *
 * openwms.org is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * openwms.org is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software. If not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.openwms.tms.domain.values;

import java.io.Serializable;

import javax.persistence.Embeddable;

import org.openwms.tms.domain.order.TransportOrder;

/**
 * A PriorityLevel.
 * <p>
 * Is used to prioritize {@link TransportOrder}s.
 * </p>
 * 
 * @author <a href="mailto:openwms@googlemail.com">Heiko Scherrer</a>
 * @version $Revision: 810 $
 * @since 0.1
 * @see org.openwms.tms.domain.order.TransportOrder
 */
@Embeddable
public class PriorityLevel implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String lowest = "lowest";

    public static final String low = "low";

    public static final String medium = "medium";

    public static final String high = "high";

    public static final String highest = "highest";

    
    /* ----------------------------- methods ------------------- */
    /**
     * Create a new {@link PriorityLevel}.
     */
    protected PriorityLevel() {
    }

}
