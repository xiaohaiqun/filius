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
package filius.software.vermittlungsschicht;

import filius.Main;
import filius.hardware.NetworkInterface;
import filius.software.ProtocolThread;
import filius.software.netzzugangsschicht.EthernetFrame;
import filius.software.system.InternetNodeOS;

/**
 * Klasse zur Ueberwachung des Puffers fuer eingehende ARP-Pakete
 * 
 */
public class ARPThread extends ProtocolThread {

    /**
     * die Implementierung des Address Resolution Protocols mit der Verwaltung der ARP-Eintraege
     */
    private ARP vermittlung;

    /**
     * Konstruktor zur Initialisierung des zu ueberwachenden Puffers und der ARP-Implementierung
     */
    public ARPThread(ARP vermittlung) {
        super(((InternetNodeOS) vermittlung.getSystemSoftware()).getEthernet().holeARPPuffer());
        Main.debug.println("INVOKED-2 (" + this.hashCode() + ", T" + this.getId() + ") " + getClass()
                + " (ARPThread), constr: ARPThread(" + vermittlung + ")");
        this.vermittlung = vermittlung;
    }

    /**
     * Methode zur Verarbeitung eingehender ARP-Pakete <br />
     * Aus jedem ARP-Paket wird ein neuer Eintrag fuer die ARP-Tabelle erzeugt (unabhaengig davon, ob es eine Anfrage
     * oder eine Antwort ist). Wenn die Anfrage eine eigene IP-Adresse betrifft, wird ein Antwort-Paket verschickt.
     */
    protected void processFrame(Object datenEinheit) {
        Main.debug.println("INVOKED (" + this.hashCode() + ", T" + this.getId() + ") " + getClass()
                + " (ARPThread), verarbeiteDatenEinheit(" + datenEinheit.toString() + ")");
        ArpPaket arpPaket, antwortArp;
        InternetNodeOS bs;
        NetworkInterface nic;

        arpPaket = (ArpPaket) datenEinheit;

        // Aus jedem ARP-Paket wird ein neuer ARP-Eintrag erzeugt
        if (!arpPaket.getQuellIp().equalsIgnoreCase("0.0.0.0")) {
            vermittlung.hinzuARPTabellenEintrag(arpPaket.getQuellIp(), arpPaket.getQuellMacAdresse());
        }

        bs = (InternetNodeOS) vermittlung.getSystemSoftware();
        nic = vermittlung.getBroadcastNic(arpPaket.getQuellIp());

        // wenn die Anfrage eine Anfrage fuer eine eigene
        // IP-Adresse ist, wird eine Antwort verschickt
        if (nic != null && arpPaket.getZielMacAdresse().equalsIgnoreCase("ff:ff:ff:ff:ff:ff")
                && arpPaket.getZielIp().equalsIgnoreCase(nic.getIp())) {
            antwortArp = new ArpPaket();
            antwortArp.setProtokollTyp(arpPaket.getProtokollTyp());
            antwortArp.setQuellIp(nic.getIp());
            antwortArp.setQuellMacAdresse(nic.getMac());

            if (arpPaket.getQuellIp().equalsIgnoreCase("0.0.0.0")) {
                antwortArp.setZielIp("255.255.255.255");
                antwortArp.setZielMacAdresse("ff:ff:ff:ff:ff:ff");
            } else {
                antwortArp.setZielIp(arpPaket.getQuellIp());
                antwortArp.setZielMacAdresse(arpPaket.getQuellMacAdresse());
            }

            bs.getEthernet().senden(antwortArp, nic.getMac(), antwortArp.getZielMacAdresse(), EthernetFrame.ARP);
        }
    }
}
