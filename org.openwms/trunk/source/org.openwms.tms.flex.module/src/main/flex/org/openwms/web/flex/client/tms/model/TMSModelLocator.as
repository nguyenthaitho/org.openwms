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
package org.openwms.web.flex.client.tms.model
{

    import mx.collections.ArrayCollection;
    
    import org.openwms.tms.domain.order.TransportOrder$TRANSPORT_ORDER_STATE;

    /**
     * A TMSModelLocator.
     *
     * @author <a href="mailto:openwms@googlemail.com">Heiko Scherrer</a>
     * @version $Revision: 796 $
     */
    [Name("tmsModelLocator")]
    [Bindable]
    public class TMSModelLocator
    {

        public var allTransportOrders:ArrayCollection = new ArrayCollection();
        public var allStates:ArrayCollection = new ArrayCollection(TransportOrder$TRANSPORT_ORDER_STATE.constants);

        /**
         * Constructor.
         */
        public function TMSModelLocator()
        {
            allStates.removeItemAt(allStates.getItemIndex(TransportOrder$TRANSPORT_ORDER_STATE.CREATED));
            allStates.removeItemAt(allStates.getItemIndex(TransportOrder$TRANSPORT_ORDER_STATE.INITIALIZED));
            allStates.removeItemAt(allStates.getItemIndex(TransportOrder$TRANSPORT_ORDER_STATE.STARTED));
        }
    }
}