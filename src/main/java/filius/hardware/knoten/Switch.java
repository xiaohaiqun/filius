/*
 ** This file is part of Filius, a network construction and simulation software.
 ** 
 ** Originally created at the University of Siegen, Institute "Didactics of
 ** Informatics and E-Learning" by a students' project group:
 **     members (2006-2007): 
 **         André Asschoff, Johannes Bade, Carsten Dittich, Thomas Gerding,
 **         Nadja Haßler, Ernst Johannes Klebert, Michell Weyer
 **     supervisors:
 **         Stefan Freischlad (maintainer until 2009), Peer Stechert
 ** Project is maintained since 2010 by Christian Eibl <filius@c.fameibl.de>
 **         and Stefan Freischlad
 ** Filius is free software: you can redistribute it and/or modify
 ** it under the terms of the GNU General Public License as published by
 ** the Free Software Foundation, either version 2 of the License, or
 ** (at your option) version 3.
 ** 
 ** Filius is distributed in the hope that it will be useful,
 ** but WITHOUT ANY WARRANTY; without even the implied
 ** warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 ** PURPOSE. See the GNU General Public License for more details.
 ** 
 ** You should have received a copy of the GNU General Public License
 ** along with Filius.  If not, see <http://www.gnu.org/licenses/>.
 */
package filius.hardware.knoten;

import filius.Main;
import filius.rahmenprogramm.I18n;
import filius.software.system.SwitchFirmware;

@SuppressWarnings("serial")
public class Switch extends LocalNode implements I18n {

	public static final String TYPE = messages.getString("hw_switch_msg1");
	private boolean showAsCloud = false;

	@Override
	public String getHardwareType() {
		return TYPE;
	}

	public Switch() {
		super();
		Main.debug.println("INVOKED-2 (" + this.hashCode() + ") " + getClass() + " (Switch), constr: Switch()");

		this.createPortList(24);
		this.setSystemSoftware(new SwitchFirmware());
		getSystemSoftware().setKnoten(this);
		this.setName(TYPE);
	}

	public void setCloud(boolean newVal) {
		showAsCloud = newVal;
	}

	public boolean isCloud() {
		return showAsCloud;
	}
}
