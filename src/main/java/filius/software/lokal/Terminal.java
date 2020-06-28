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
package filius.software.lokal;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import javax.swing.tree.TreeNode;

import filius.Main;
import filius.rahmenprogramm.I18n;
import filius.rahmenprogramm.Information;
import filius.software.clientserver.ClientAnwendung;
import filius.software.dns.Resolver;
import filius.software.system.Betriebssystem;
import filius.software.system.FiliusFile;
import filius.software.system.FiliusFileNode;
import filius.software.system.FiliusFileSystem;
import filius.software.system.InternetKnotenBetriebssystem;
import filius.software.transportschicht.ServerSocket;
import filius.software.transportschicht.Socket;
import filius.software.transportschicht.SocketSchnittstelle;
import filius.software.transportschicht.TransportProtokoll;
import filius.software.vermittlungsschicht.IP;
import filius.software.vermittlungsschicht.IcmpPaket;
import filius.software.vermittlungsschicht.Route;
import filius.software.vermittlungsschicht.RouteNotFoundException;
import filius.software.vermittlungsschicht.VermittlungsProtokoll;

/**
 * Diese Klasse soll eine Art Eingabeaufforderung oder Unix-Shell darstellen, in der zumindest rudimentaere Befehle wie
 * dir/ls/rename etc. moeglich sein sollen. Auerdem soll hierin auch der Start von bestimmten Serveranwendungen und
 * netcat moeglich sein.
 * 
 * @author Thomas Gerding & Johannes Bade
 * 
 */
public class Terminal extends ClientAnwendung implements I18n {

    // Betriebssystem betriebssystem;
    boolean abfrageVar;

    private FiliusFileNode aktuellerOrdner;
    private boolean interrupted = false;
    private FiliusFileSystem FFS;

    public void setSystemSoftware(InternetKnotenBetriebssystem bs) {
        super.setSystemSoftware(bs);
        FFS = getSystemSoftware().getDateisystem();
        aktuellerOrdner = FFS.getRoot();
    }

    /**
     * Diese Funktion bildet "move" bzw. "rename" ab und erlaubt es eine bestimmte Datei umzubenennen.
     * 
     * @param alterName
     *            Der bisherige Dateiname
     * @param neuerName
     *            Der gewnschte neue Dateiname
     * @return Gibt eine Meldung ueber den Erfolg oder Misserfolg des Umbenennens zurck.
     * @author Thomas Gerding & Johannes Bade
     */
    public String move(String[] args) {
        return mv(args);
    }

    public String mv(String[] args) {
        Main.debug.print("INVOKED (" + this.hashCode() + ", T" + this.getId() + ") " + getClass() + " (Terminal), mv(");
        for (int i = 0; i < args.length; i++) {
            Main.debug.print(i + "='" + args[i] + "' ");
        }
        Main.debug.println(")");

        if (!numParams(args, 2)) {
            benachrichtigeBeobachter(messages.getString("sw_terminal_msg32") + messages.getString("sw_terminal_msg40"));
            return messages.getString("sw_terminal_msg32") + messages.getString("sw_terminal_msg40"); // wrong
                                                                                                      // number
                                                                                                      // of
                                                                                                      // parameters
        }
        if (pureCopy(args)) { // positive case, everything worked fine
//            this.getSystemSoftware().getDateisystem()
//                    .deleteFile(filius.software.system.FiliusFileSystem.nodeToAbsolutePath(getAktuellerOrdner())
//                            + FiliusFileSystem.FILE_SEPARATOR + args[0]);        	
        	String path = getAktuellerOrdner().toPath() + FFS.FILE_SEPARATOR + args[0];
            FFS.deleteFile(path);            
            
            benachrichtigeBeobachter(messages.getString("sw_terminal_msg35"));
            return messages.getString("sw_terminal_msg35");
        } else {
            benachrichtigeBeobachter(messages.getString("sw_terminal_msg36"));
            return messages.getString("sw_terminal_msg36");
        } // negative case, something wrong
    }

    /**
     * delete file
     */
    public String rm(String[] args) {
        return del(args);
    }

    public String del(String[] args) {
        Main.debug
                .print("INVOKED (" + this.hashCode() + ", T" + this.getId() + ") " + getClass() + " (Terminal), del(");
        for (int i = 0; i < args.length; i++) {
            Main.debug.print(i + "='" + args[i] + "' ");
        }
        Main.debug.println(")");
        
        if (!numParams(args, 1)) {
            benachrichtigeBeobachter(messages.getString("sw_terminal_msg32") + messages.getString("sw_terminal_msg41"));
            return messages.getString("sw_terminal_msg32") + messages.getString("sw_terminal_msg41"); // wrong
                                                                                                      // number
                                                                                                      // of
                                                                                                      // parameters
        }
        
        // ToDo : if args[0] is a non empty directory, ask for confirmation.
        
        if (FFS.deleteFile(getAktuellerOrdner().toPath() + FiliusFileSystem.FILE_SEPARATOR + args[0])) {
            benachrichtigeBeobachter(messages.getString("sw_terminal_msg37"));
            return messages.getString("sw_terminal_msg37");
        } else {
            benachrichtigeBeobachter(messages.getString("sw_terminal_msg38"));
            return messages.getString("sw_terminal_msg38");
        }
    }    
    
    /**
     * <b>printSubtree</b> recursively prints the tree structure starting from a given node 
     * 
     * @param indent String used for indentation
     * @param startNode FFSNode from which the printing starts
     */
    private void printSubtree(String indent, FiliusFileNode startNode) {
    	
    	FiliusFileNode node;
        Main.debug.print(indent + "--");
        
        if (startNode.isDirectory()) {
            Main.debug.println("[" + startNode.getName() + "]");
        }
        
        indent = indent + " |";
        for (Enumeration<TreeNode> e = startNode.children(); e.hasMoreElements();) {
            node = (FiliusFileNode) e.nextElement();
            printSubtree(indent, node);
        }
    }

    /**
     * Kopiert eine Datei
     * 
     * @param Parameter
     *            Array (String)
     * @return
     */
    // // common functionality for move and copy...
    private boolean pureCopy(String[] args) {
        Main.debug.print(
                "INVOKED (" + this.hashCode() + ", T" + this.getId() + ") " + getClass() + " (Terminal), pureCopy(");
        for (int i = 0; i < args.length; i++) {
            Main.debug.print(i + "='" + args[i] + "' ");
        }
        Main.debug.println(")");
        printSubtree("", FFS.getRoot());
        String srcString = args[0];
        if (srcString.length() > 0 && srcString.substring(0, 1).equals(FFS.FILE_SEPARATOR)) { // 'pfad'
                                                                                                      // is
                                                                                                      // absolute
                                                                                                      // path!
            srcString = FFS.evaluatePath(srcString);
        } else {
            srcString = FFS.evaluatePath(getAktuellerOrdner().toPath() + FFS.FILE_SEPARATOR + srcString);
        }
        String destString = args[1];
        if (destString.length() > 0 && destString.substring(0, 1).equals(FFS.FILE_SEPARATOR)) { // 'pfad'
                                                                                                        // is
                                                                                                        // absolute
                                                                                                        // path!
            destString = FFS.evaluatePath(destString);
        } else {
            destString = FFS.evaluatePath(getAktuellerOrdner().toPath() + FFS.FILE_SEPARATOR + destString);
        }
        String destDir = FFS.getPathDirectory(destString);
        String destFile = FFS.getPathFilename(destString);

        // Main.debug.println("DEBUG: pureCopy: source '"+srcDir+"'-'"+srcFile+"', destination
        // '"+destDir+"'-'"+destFile+"'");
        FiliusFile sfile = null;
        FiliusFileNode node = FFS.toNode(srcString);
    	if (node != null) sfile = node.getFiliusFile();          
        if (sfile == null) return false;
        
        FiliusFile dfile = new FiliusFile(destFile, sfile.getType(), sfile.getContent());
        
        node = FFS.toNode(destDir);
        if (node != null) return node.saveFiliusFile(dfile); 
        return false;
    }

    // individual functionality for copy only
    public String copy(String[] args) {
        return cp(args);
    }

    public String cp(String[] args) {
        Main.debug.print("INVOKED (" + this.hashCode() + ", T" + this.getId() + ") " + getClass() + " (Terminal), cp(");
        for (int i = 0; i < args.length; i++) {
            Main.debug.print(i + "='" + args[i] + "' ");
        }
        Main.debug.println(")");
        if (!numParams(args, 2)) {
            benachrichtigeBeobachter(messages.getString("sw_terminal_msg32") + messages.getString("sw_terminal_msg40"));
            return messages.getString("sw_terminal_msg32") + messages.getString("sw_terminal_msg40"); // wrong
                                                                                                      // number
                                                                                                      // of
                                                                                                      // parameters
        }
        if (pureCopy(args)) {
            benachrichtigeBeobachter(messages.getString("sw_terminal_msg33"));
            return messages.getString("sw_terminal_msg33");
        } // positive case, everything worked fine
        else {
            benachrichtigeBeobachter(messages.getString("sw_terminal_msg34"));
            return messages.getString("sw_terminal_msg34");
        } // negative case, something wrong
    }

    /* */
    public String ipconfig(String[] args) {
        Main.debug.print(
                "INVOKED (" + this.hashCode() + ", " + this.getId() + ") " + getClass() + " (Terminal), ipconfig(");
        for (int i = 0; i < args.length; i++) {
            Main.debug.print(i + "='" + args[i] + "' ");
        }
        Main.debug.println(")");
        if (!numParams(args, 0)) {
            benachrichtigeBeobachter(messages.getString("sw_terminal_msg32") + messages.getString("sw_terminal_msg42"));
            return messages.getString("sw_terminal_msg32") + messages.getString("sw_terminal_msg42"); // wrong
                                                                                                      // number
                                                                                                      // of
                                                                                                      // parameters
        }
        Betriebssystem bs = (Betriebssystem) getSystemSoftware();
        String ausgabe = "";

        ausgabe += messages.getString("sw_terminal_msg4") + " " + bs.holeIPAdresse() + "\n";
        ausgabe += messages.getString("sw_terminal_msg5") + " " + bs.holeNetzmaske() + "\n";
        ausgabe += messages.getString("sw_terminal_msg26") + " " + bs.holeMACAdresse() + "\n";
        ausgabe += messages.getString("sw_terminal_msg6") + " " + bs.getStandardGateway() + "\n";
        ausgabe += messages.getString("sw_terminal_msg27") + " " + bs.getDNSServer() + "\n";

        benachrichtigeBeobachter(ausgabe);
        return ausgabe;
    }

    /* Entspricht route print unter windows */
    public String route(String[] args) {
        Main.debug
                .print("INVOKED (" + this.hashCode() + ", " + this.getId() + ") " + getClass() + " (Terminal), route(");
        for (int i = 0; i < args.length; i++) {
            Main.debug.print(i + "='" + args[i] + "' ");
        }
        Main.debug.println(")");
        if (!numParams(args, 0)) {
            benachrichtigeBeobachter(messages.getString("sw_terminal_msg32") + messages.getString("sw_terminal_msg42"));
            return messages.getString("sw_terminal_msg32") + messages.getString("sw_terminal_msg42"); // wrong
                                                                                                      // number
                                                                                                      // of
                                                                                                      // parameters
        }
        String ausgabe = messages.getString("sw_terminal_msg7");

        LinkedList<String[]> routingTabelle = getSystemSoftware().getWeiterleitungstabelle().holeTabelle();
        ListIterator<String[]> it = routingTabelle.listIterator();

        while (it.hasNext()) {
            String[] eintrag = (String[]) it.next();
            ausgabe += "| ";
            for (int i = 0; i < eintrag.length; i++) {
                ausgabe += eintrag[i] + stringFuellen(15 - eintrag[i].length(), " ") + " | ";
            }
            ausgabe += "\n";
        }

        benachrichtigeBeobachter(ausgabe);
        return ausgabe;
    }

    /**
     * Diese Funktion bietet Aehnliches wie "ls" oder "dir" auf der normalen Eingabeaufforderung. Es gibt eine Liste
     * aller Dateien des Rechners und deren Groesse zurueck.
     * 
     * @return Gibt die Liste der vorhandenen Dateien (und Verzeichnisse) in einem formatierten String zurueck, der
     *         direkt ausgegeben werden kann.
     * 
     * @author Thomas Gerding & Johannes Bade
     * @param Parameter
     *            Array (String)
     */
    public String ls(String[] args) {
        return dir(args);
    }

    public String dir(String[] args) {
        Main.debug.print("INVOKED (" + this.hashCode() + ", " + this.getId() + ") " + getClass() + " (Terminal), dir(");
        for (int i = 0; i < args.length; i++) {
            Main.debug.print(i + "='" + args[i] + "' ");
        }
        Main.debug.println(")");
        if (!numParams(args, 0, 1)) {
            benachrichtigeBeobachter(messages.getString("sw_terminal_msg32") + messages.getString("sw_terminal_msg43"));
            return messages.getString("sw_terminal_msg32") + messages.getString("sw_terminal_msg43"); // wrong
                                                                                                      // number
                                                                                                      // of
                                                                                                      // parameters
        }
        List<Object> liste;
        StringBuffer inhalt;
        String currPath;
        int anzahlVerzeichnisse = 0;
        int anzahlDateien = 0;
        FiliusFile tmpDatei;
        int leerzeichen;

        if (args[0].isEmpty()) {
            liste = aktuellerOrdner.getChildObjects();        	
            currPath = aktuellerOrdner.toPath();
        } else {
            if (FFS.isAbsolute(args[0])) {
            	// Absolute path  
            	FiliusFileNode node = FFS.toNode(args[0]);
            	if (node != null) liste = node.getChildObjects();
            	else liste = null;          	
                currPath = FFS.evaluatePath(args[0]);
            } else {
            	// Relative path
            	FiliusFileNode node = aktuellerOrdner.toNode(args[0]); 
            	if (node != null) liste = node.getChildObjects();
            	else liste = null;            
                currPath = FFS.evaluatePath(aktuellerOrdner.toPath() + FFS.FILE_SEPARATOR + args[0]);
            }
        }

        if (liste == null || liste.size() == 0) {
            benachrichtigeBeobachter(messages.getString("sw_terminal_msg8"));
            return messages.getString("sw_terminal_msg8");
        } else {
            inhalt = new StringBuffer();
            inhalt.append(messages.getString("sw_terminal_msg9") + " " + currPath + ":\n");

            for (Object tmp : liste) {
                // Fall Datei:
                if (tmp instanceof FiliusFile) {
                    anzahlDateien++;
                    tmpDatei = (FiliusFile) tmp;
                    leerzeichen = 40 - tmpDatei.getName().length();
                    inhalt.append(tmpDatei.getName() + stringFuellen(leerzeichen, ".") + tmpDatei.getSize() + "\n");
                }
                // Fall Ordner:
                else {
                    anzahlVerzeichnisse++;
                    inhalt.append("[" + tmp + "]\n");
                }
            }

            inhalt.append(messages.getString("sw_terminal_msg10") + anzahlVerzeichnisse);
            inhalt.append(messages.getString("sw_terminal_msg11") + anzahlDateien + "\n");

        }

        benachrichtigeBeobachter(inhalt.toString());
        return inhalt.toString();
    }

    /**
     * 
     * touch
     * 
     */
    public String touch(String[] args) {
        Main.debug
                .print("INVOKED (" + this.hashCode() + ", " + this.getId() + ") " + getClass() + " (Terminal), touch(");
        for (int i = 0; i < args.length; i++) {
            Main.debug.print(i + "='" + args[i] + "' ");
        }
        Main.debug.println(")");
        if (!numParams(args, 1)) {
            benachrichtigeBeobachter(messages.getString("sw_terminal_msg32") + messages.getString("sw_terminal_msg41"));
            return messages.getString("sw_terminal_msg32") + messages.getString("sw_terminal_msg41"); // wrong
                                                                                                      // number
                                                                                                      // of
                                                                                                      // parameters
        }
        String ergebnis = messages.getString("sw_terminal_msg12");
        String absPath;
        if (args[0].length() > 0 && args[0].substring(0, 1).equals(FFS.FILE_SEPARATOR)) { // 'pfad'
                                                                                                  // is
                                                                                                  // absolute
                                                                                                  // path!
            absPath = FFS.evaluatePath(args[0]);
        } else {
            absPath = FFS.evaluatePath(aktuellerOrdner.toPath() + FFS.FILE_SEPARATOR + args[0]);
        }
        String filePath = FFS.getPathDirectory(absPath);
        String dateiName = FFS.getPathFilename(absPath);
        if (!dateiName.isEmpty()) {
        	FiliusFileNode node = FFS.toNode(filePath, dateiName);
            if (node == null) {
            	node = FFS.toNode(filePath);
            	if (node == null) {
            		ergebnis = "Le répertoire parent n'existe pas.";                           // I18n            		
            	} else {
            		node.saveFiliusFile(new FiliusFile(dateiName, "text", "")); 
            		ergebnis = messages.getString("sw_terminal_msg13");
            	}
                
            } else {
                ergebnis = messages.getString("sw_terminal_msg14");
            }
        } else {
            ergebnis = messages.getString("sw_terminal_msg15");
        }
        benachrichtigeBeobachter(ergebnis);
        return ergebnis;
    }

    /**
     * 
     * mkdir
     * 
     */
    public String mkdir(String[] args) {
        Main.debug
                .print("INVOKED (" + this.hashCode() + ", " + this.getId() + ") " + getClass() + " (Terminal), mkdir(");
        for (int i = 0; i < args.length; i++) {
            Main.debug.print(i + "='" + args[i] + "' ");
        }
        Main.debug.println(")");
        if (!numParams(args, 1)) {
            benachrichtigeBeobachter(messages.getString("sw_terminal_msg32") + messages.getString("sw_terminal_msg41"));
            return messages.getString("sw_terminal_msg32") + messages.getString("sw_terminal_msg41"); // wrong
                                                                                                      // number
                                                                                                      // of
                                                                                                      // parameters
        }
        String ergebnis = messages.getString("sw_terminal_msg16");
        String absPath;
        if (args[0].length() > 0 && args[0].substring(0, 1).equals(FFS.FILE_SEPARATOR)) { // 'pfad'
                                                                                                  // is
                                                                                                  // absolute
                                                                                                  // path!
            absPath = FFS.evaluatePath(args[0]);
        } else {
            absPath = FFS.evaluatePath(aktuellerOrdner.toPath() + FFS.FILE_SEPARATOR + args[0]);
        }
        String filePath = FFS.getPathDirectory(absPath);
        String dateiName = FFS.getPathFilename(absPath);
        if (!dateiName.isEmpty()) {
        	FiliusFileNode node = FFS.toNode(filePath, dateiName);
            if (node == null) {
            	node = FFS.toNode(filePath);
            	if (node == null) {
            		ergebnis = "Le répertoire parent n'existe pas.";       // I18n
            	} else if (node.addDirectory(dateiName)) {
            		ergebnis = messages.getString("sw_terminal_msg17");    // OK
            	} else {
            		ergebnis = "Le fichier n'a pas pu être créé.";         // I18n
            	}     
            } else {
                ergebnis = messages.getString("sw_terminal_msg18");
            }
        } else {
            ergebnis = messages.getString("sw_terminal_msg19");
        }
        benachrichtigeBeobachter(ergebnis);
        return ergebnis;
    }

    /**
     * 
     * cd
     * 
     */
    public String cd(String[] args) {
        Main.debug.print("INVOKED (" + this.hashCode() + ", " + this.getId() + ") " + getClass() + " (Terminal), cd(");
        for (int i = 0; i < args.length; i++) {
            Main.debug.print(i + "='" + args[i] + "' ");
        }
        Main.debug.println(")");
        String ergebnis = "";
        if (!numParams(args, 0, 1)) {
            benachrichtigeBeobachter(messages.getString("sw_terminal_msg32") + messages.getString("sw_terminal_msg43"));
            return messages.getString("sw_terminal_msg32") + messages.getString("sw_terminal_msg43"); // wrong
                                                                                                      // number
                                                                                                      // of
                                                                                                      // parameters
        }
        if (numParams(args, 1)) {
            FiliusFileNode newDir;
            if (FFS.isAbsolute(args[0])) 
            	newDir = FFS.toNode(args[0]);
            else
                newDir = aktuellerOrdner.toNode(args[0]);
            if (newDir != null) { // first, check whether directory change was
                                  // successful; otherwise stay in current
                                  // directory
                aktuellerOrdner = newDir;
            } else {
                ergebnis = messages.getString("sw_terminal_msg20");
            }
        } else {
            ergebnis = aktuellerOrdner.toPath();
        }

        benachrichtigeBeobachter(ergebnis);
        return ergebnis;
    }

    // Unix Tool 'pwd': print working directory
    public String pwd(String[] args) {
        Main.debug.print("INVOKED (" + this.hashCode() + ", " + this.getId() + ") " + getClass() + " (Terminal), pwd(");
        for (int i = 0; i < args.length; i++) {
            Main.debug.print(i + "='" + args[i] + "' ");
        }
        Main.debug.println(")");
        if (!numParams(args, 0)) {
            benachrichtigeBeobachter(messages.getString("sw_terminal_msg32") + messages.getString("sw_terminal_msg42"));
            return messages.getString("sw_terminal_msg32") + messages.getString("sw_terminal_msg42"); // wrong
                                                                                                      // number
                                                                                                      // of
                                                                                                      // parameters
        }
        String ergebnis = aktuellerOrdner.toPath();
        benachrichtigeBeobachter(ergebnis);
        return ergebnis;
    }

    public String netstat(String[] args) {
        TransportProtokoll transport;
        StringBuffer ergebnis = new StringBuffer();
        String protocol;

        ergebnis.append(messages.getString("sw_terminal_msg49"));
        ergebnis.append("--------------------------------------------------------------------------\n");

        transport = this.getSystemSoftware().holeTcp();
        protocol = "TCP";
        processSockets(ergebnis, transport, protocol);

        transport = this.getSystemSoftware().holeUdp();
        protocol = "UDP";
        processSockets(ergebnis, transport, protocol);

        benachrichtigeBeobachter(ergebnis);
        return ergebnis.toString();
    }

    private void processSockets(StringBuffer ergebnis, TransportProtokoll transport, String protocol) {
        for (SocketSchnittstelle socket : transport.holeAktiveSockets().values()) {
            for (SocketInformation info : getSocketInfo(socket)) {
                ergebnis.append(String.format("| %-7s ", protocol));
                ergebnis.append(String.format("| %15s:%-5s ", info.localHost, info.localPort));
                ergebnis.append(String.format("| %15s:%-5s ", info.remoteHost, info.remotePort));
                ergebnis.append(String.format("| %-12s |\n", info.state));
            }
        }
    }

    private class SocketInformation {
        String localHost;
        String localPort;
        String remoteHost;
        String remotePort;
        String state;
    }

    private List<SocketInformation> getSocketInfo(SocketSchnittstelle socket) {
        List<SocketInformation> infoList = new ArrayList<>();
        if (socket instanceof Socket) {
            infoList.add(getSocketInfo((Socket) socket));
        } else if (socket instanceof ServerSocket) {
            ServerSocket temp = (ServerSocket) socket;
            infoList.add(getSocketInfo(temp));
            for (String port : temp.getSockets().keySet()) {
                infoList.add(getSocketInfo(temp.getSockets().get(port)));
            }
        }
        return infoList;
    }

    private SocketInformation getSocketInfo(ServerSocket serverSocket) {
        SocketInformation info = new SocketInformation();
        info.remoteHost = "-";
        info.remotePort = "-";
        info.localHost = "0.0.0.0";
        info.localPort = String.valueOf(serverSocket.getLocalPort());
        info.state = "LISTEN";
        return info;
    }

    private SocketInformation getSocketInfo(Socket socket) {
        SocketInformation info = new SocketInformation();
        info.remoteHost = socket.holeZielIPAdresse();
        info.remotePort = String.valueOf(socket.holeZielPort());
        try {
            Route routingEntry = ((InternetKnotenBetriebssystem) this.getSystemSoftware())
                    .determineRoute(info.remoteHost);
            info.localHost = routingEntry.getInterfaceIpAddress();
        } catch (RouteNotFoundException e) {
            info.localHost = "<unknown>";
        }
        info.localPort = String.valueOf(socket.holeLokalenPort());
        info.state = socket.getStateAsString();
        return info;
    }

    /**
     * 
     * test
     * 
     */
    public String test(String[] args) {
        String ergebnis = messages.getString("sw_terminal_msg23");

        FiliusFileNode node = FFS.toNode(args[0]);        
        if (node != null && node.saveFiliusFile(new FiliusFile("test", "txt", "blaaa"))) {
            ergebnis = messages.getString("sw_terminal_msg24");
        }

        benachrichtigeBeobachter(ergebnis);
        return ergebnis;
    }

    /**
     * 
     * help command to list all available commands implemented in this terminal application
     * 
     */
    public String help(String[] args) {
        benachrichtigeBeobachter(messages.getString("sw_terminal_msg25"));
        return messages.getString("sw_terminal_msg25");
    }

    public String type(String[] args) {
        return cat(args);
    }

    public String cat(String[] args) {
        StringBuilder result = new StringBuilder();
        if (args == null || args.length < 1 || args[0] == null || "".equals(args[0])) {
            result.append(messages.getString("sw_terminal_msg51"));
        } else {
            FiliusFile file = aktuellerOrdner.getFiliusFile(args[0]);
            if (null != file) {
                result.append(file.getContent());
            } else {
                result.append(messages.getString("sw_terminal_msg54"));
            }
        }
        benachrichtigeBeobachter(result.toString());
        return result.toString();
    }

    public String arp(String[] args) {
        StringBuilder ergebnis = new StringBuilder();

        ergebnis.append(messages.getString("sw_terminal_msg50"));
        ergebnis.append("----------------------------------------\n");

        Map<String, String> arpTable = this.getSystemSoftware().holeARP().holeARPTabelle();
        for (String ipAddress : arpTable.keySet()) {
            ergebnis.append(String.format("| %-15s  ", ipAddress));
            ergebnis.append(String.format("| %-17s |\n", arpTable.get(ipAddress)));

        }
        ergebnis.append("----------------------------------------\n");
        benachrichtigeBeobachter(ergebnis.toString());
        return ergebnis.toString();
    }

    /**
     * 
     * 'host' command to resolve URL to an IP address using the client's DNS server entry
     * 
     */
    public String host(String[] args) {
        Main.debug
                .print("INVOKED (" + this.hashCode() + ", " + this.getId() + ") " + getClass() + " (Terminal), host(");
        for (int i = 0; i < args.length; i++) {
            Main.debug.print(i + "='" + args[i] + "' ");
        }
        Main.debug.println(")");
        if (!numParams(args, 1)) {
            benachrichtigeBeobachter(messages.getString("sw_terminal_msg32") + messages.getString("sw_terminal_msg44"));
            return messages.getString("sw_terminal_msg32") + messages.getString("sw_terminal_msg44"); // wrong
                                                                                                      // number
                                                                                                      // of
                                                                                                      // parameters
        }
        Betriebssystem bs = (Betriebssystem) getSystemSoftware();
        filius.software.dns.Resolver res = bs.holeDNSClient();
        if (res == null) {
            filius.Main.debug.println("ERROR (" + this.hashCode() + "): Terminal 'host': Resolver is null!");
            benachrichtigeBeobachter(messages.getString("sw_terminal_msg30"));
            return messages.getString("sw_terminal_msg30");
        }
        String result;

        try {
            result = res.holeIPAdresse(args[0]);
            if (result != null) {
                benachrichtigeBeobachter(args[0] + " " + messages.getString("sw_terminal_msg28") + " " + result + "\n");
                return args[0] + " " + messages.getString("sw_terminal_msg28") + " " + result + "\n";
            } else {
                filius.Main.debug.println("ERROR (" + this.hashCode() + "): Terminal 'host': result is null!");
                benachrichtigeBeobachter(messages.getString("sw_terminal_msg30"));
                return messages.getString("sw_terminal_msg30");
            }
        } catch (java.util.concurrent.TimeoutException e) {
            benachrichtigeBeobachter(messages.getString("sw_terminal_msg31"));
            return messages.getString("sw_terminal_msg31");
        } catch (Exception e) {
            e.printStackTrace(filius.Main.debug);
            benachrichtigeBeobachter(messages.getString("sw_terminal_msg29"));
            return messages.getString("sw_terminal_msg29");
        }
    }

    /**
     * 'ping' command to check connectivity via ICMP echo request/reply
     */
    public String ping(String[] args) {
        Main.debug
                .print("INVOKED (" + this.hashCode() + ", " + this.getId() + ") " + getClass() + " (Terminal), ping(");
        for (int i = 0; i < args.length; i++) {
            Main.debug.print(i + "='" + args[i] + "' ");
        }
        Main.debug.println(")");
        if (!numParams(args, 1)) {
            // wrong number of parameters
            benachrichtigeBeobachter(messages.getString("sw_terminal_msg32") + messages.getString("sw_terminal_msg44"));
            return messages.getString("sw_terminal_msg32") + messages.getString("sw_terminal_msg44");
        }
        Resolver res = getSystemSoftware().holeDNSClient();
        if (res == null) {
            Main.debug.println("ERROR (" + this.hashCode() + "): Terminal 'host': Resolver is null!");
            benachrichtigeBeobachter(messages.getString("sw_terminal_msg30"));
            return messages.getString("sw_terminal_msg30");
        }

        // first: resolve host name
        String destIp;
        try {
            destIp = IP.ipCheck(args[0]);
            if (destIp == null) { // args[0] is not an IP address
                destIp = res.holeIPAdresse(args[0]);
            }
            if (destIp == null) { // args[0] could also not be resolved
                Main.debug.println("ERROR (" + this.hashCode() + "): Terminal 'host': result is null!");
                benachrichtigeBeobachter(messages.getString("sw_terminal_msg30"));
                return messages.getString("sw_terminal_msg30");
            }
        } catch (TimeoutException e) {
            benachrichtigeBeobachter(messages.getString("sw_terminal_msg31") + " (DNS)");
            return messages.getString("sw_terminal_msg31" + " (DNS)");
        } catch (Exception e) {
            e.printStackTrace(Main.debug);
            benachrichtigeBeobachter(messages.getString("sw_terminal_msg29"));
            return messages.getString("sw_terminal_msg29");
        }
        try {
            Route route = ((InternetKnotenBetriebssystem) getSystemSoftware()).determineRoute(destIp);
            if (VermittlungsProtokoll.isBroadcast(destIp, route.getInterfaceIpAddress(), route.getNetMask())) {
                benachrichtigeBeobachter(messages.getString("sw_terminal_msg53"));
                return messages.getString("sw_terminal_msg53");
            }
        } catch (RouteNotFoundException e1) {
            benachrichtigeBeobachter(messages.getString("sw_terminal_msg52"));
            return messages.getString("sw_terminal_msg52");
        }

        // second: send several ICMP echo requests
        long timeStart, timeDiff;
        // inform about a multiple data transmission to the observer
        benachrichtigeBeobachter(Boolean.TRUE);
        benachrichtigeBeobachter("PING " + args[0] + " (" + destIp + ")");

        int receivedReplies = 0;
        int num;
        int loopNumber = Information.isPosixCommandLineToolBehaviour() ? 10 : 4;
        for (num = 0; !interrupted && num < loopNumber; num++) {
            try {
                timeStart = System.currentTimeMillis();
                // / CAVE: wahrscheinlich hier Queue nötig und blockieren, bis
                // Ergebnis da ist!!!
                int resTTL = getSystemSoftware().holeICMP().startSinglePing(destIp, num + 1);
                // wait 1s between single ping executions subtract needed time
                // for former ping
                timeDiff = 1000 - (System.currentTimeMillis() - timeStart);
                // Main.debug.println("DEBUG: Terminal, ping (num="+(num+1)+"), resTTL="+resTTL+",
                // delay="+(1000-timeDiff)+", timeDiff="+timeDiff);
                if (resTTL >= 0) {
                    benachrichtigeBeobachter("\nFrom " + args[0] + " (" + destIp + "): icmp_seq=" + (num + 1) + " ttl="
                            + resTTL + " time=" + (System.currentTimeMillis() - timeStart) + "ms");
                    receivedReplies++;
                }
                if (timeDiff > 0) {
                    try {
                        // Main.debug.println("DEBUG: Terminal wartet für "+timeDiff+"ms");
                        Thread.sleep(timeDiff);
                        // Main.debug.println("DEBUG: Terminal fertig mit Warten");
                    } catch (InterruptedException e) {}
                }
            } catch (java.util.concurrent.TimeoutException e) {
                benachrichtigeBeobachter(
                        "\nFrom " + args[0] + " (" + destIp + "): icmp_seq=" + (num + 1) + "   -- Timeout!");
            } catch (Exception e) {
                e.printStackTrace(filius.Main.debug);
            }
        }
        benachrichtigeBeobachter(Boolean.FALSE); // inform about a multiple
                                                 // data transmission to
                                                 // the observer
        // print statistics
        benachrichtigeBeobachter("\n--- " + args[0] + " " + messages.getString("sw_terminal_msg45") + " ---\n" + num
                + " " + messages.getString("sw_terminal_msg46") + ", " + receivedReplies + " "
                + messages.getString("sw_terminal_msg47") + ", "
                + ((int) Math.round((1 - (((double) receivedReplies) / ((double) num))) * 100)) + "% "
                + messages.getString("sw_terminal_msg48") + "\n");
        return "";
    }

    /**
     * 'traceroute' prints the route packets take to the network host (using ICMP Echo Request and ICMP Time Exceeded)
     */
    public String traceroute(String[] args) {
        if (!numParams(args, 1)) {
            benachrichtigeBeobachter(messages.getString("sw_terminal_trace_msg1"));
            return null;
        }

        int maxHops = 20;

        // 1.: Hostnamen auflösen
        String destIP = IP.ipCheck(args[0]);
        if (destIP == null) {
            filius.software.dns.Resolver res = getSystemSoftware().holeDNSClient();

            try {
                destIP = res.holeIPAdresse(args[0]);
            } catch (TimeoutException e) {
                benachrichtigeBeobachter(messages.getString("sw_terminal_trace_msg2"));
                return null;
            }

        }
        if (destIP == null) {
            benachrichtigeBeobachter(messages.getString("sw_terminal_trace_msg3"));
            return null;
        }

        benachrichtigeBeobachter(Boolean.TRUE);
        if (destIP.equals(args[0])) {
            benachrichtigeBeobachter(
                    String.format(messages.getString("sw_terminal_trace_msg4") + "\n", args[0], maxHops));
        } else {
            benachrichtigeBeobachter(
                    String.format(messages.getString("sw_terminal_trace_msg5") + "\n", args[0], destIP, maxHops));
        }

        // 2.: Pings senden und gucken, was alles zurueckkommt
        IcmpPaket recv = null;
        int seqNr = 42 * 23;
        int fehler = 0;
        int ttl;

        for (ttl = 1; ttl <= maxHops && !interrupted; ttl++) {
            benachrichtigeBeobachter(" " + ttl + "    ");

            for (int i = 0; i < 3 && !interrupted; i++) {
                seqNr++;
                recv = getSystemSoftware().holeICMP().sendProbe(destIP, ttl, seqNr);
                if (recv != null && recv.getSeqNr() == seqNr) {
                    fehler = 0;
                    break;
                }
                fehler++;
                benachrichtigeBeobachter("* ");
            }

            if (fehler == 0) {
                benachrichtigeBeobachter(recv.getQuellIp());
                if (recv.getIcmpType() != 11) {
                    break;
                }
            } else if (fehler > 5) {
                break;
            }

            benachrichtigeBeobachter("\n");
        }

        benachrichtigeBeobachter(Boolean.FALSE);
        if (ttl >= maxHops) {
            benachrichtigeBeobachter(
                    "\n\n" + String.format(messages.getString("sw_terminal_trace_msg6"), args[0], maxHops));
        } else if (interrupted) {
            benachrichtigeBeobachter("\n\n" + messages.getString("sw_terminal_trace_msg7"));
        } else if (recv != null && recv.getIcmpType() == 3) {
            switch (recv.getIcmpCode()) {
            case 0:
                benachrichtigeBeobachter(
                        "\n\n" + String.format(messages.getString("sw_terminal_trace_msg8"), recv.getQuellIp()));
                break;
            case 1:
                benachrichtigeBeobachter(
                        "\n\n" + String.format(messages.getString("sw_terminal_trace_msg9"), recv.getQuellIp()));
                break;
            default:
                benachrichtigeBeobachter(
                        "\n\n" + String.format(messages.getString("sw_terminal_trace_msg10"), recv.getQuellIp()));
                break;
            }
        } else if (fehler == 0) {
            if (ttl == 1) {
                benachrichtigeBeobachter(
                        "\n\n" + String.format(messages.getString("sw_terminal_trace_msg11"), args[0]));
            } else {
                benachrichtigeBeobachter(
                        "\n\n" + String.format(messages.getString("sw_terminal_trace_msg12"), args[0], ttl));
            }
        } else {
            benachrichtigeBeobachter("\n\n" + messages.getString("sw_terminal_trace_msg13"));
        }

        return null;
    }

    public void setInterrupt(boolean val) {
        this.interrupted = val;
    }

    public void beenden() {
        setInterrupt(true);
        super.beenden();
    }

    public void terminalEingabeAuswerten(String enteredCommand, String[] enteredParameters) {
        Main.debug.println("INVOKED (" + this.hashCode() + ", " + this.getId() + ") " + getClass()
                + " (Terminal), terminalEingabeAuswerten(" + enteredCommand + "," + enteredParameters + ")");
        Object[] args = new Object[1];
        args[0] = enteredParameters;
        try {
            // test, whether method exists; if not, exception will be evaluated
            this.getClass().getDeclaredMethod(enteredCommand, enteredParameters.getClass());

            setInterrupt(false);
            ausfuehren(enteredCommand, args);
        } catch (NoSuchMethodException e) {
            benachrichtigeBeobachter(
                    messages.getString("terminal_msg2") + "\n" + messages.getString("terminal_msg3") + "\n");
        } catch (Exception e) {
            e.printStackTrace(Main.debug);
        }
    }

    public FiliusFileNode getAktuellerOrdner() {
        return aktuellerOrdner;
    }

    public void setAktuellerOrdner(FiliusFileNode aktuellerOrdner) {
        this.aktuellerOrdner = aktuellerOrdner;
    }

    public String addSlashes(String sl) {
        Main.debug.println("INVOKED (" + this.hashCode() + ", " + this.getId() + ") " + getClass()
                + " (Terminal), addSlashes(" + sl + ")");
        String slNeu = "";
        String letztesZ = "" + sl.charAt(sl.length() - 1);
        if (!letztesZ.equals("/")) {
            slNeu = sl + "/";
        }

        return slNeu;
    }

    /**
     * 
     * @author Hannes Johannes Bade & Thomas Gerding
     * 
     *         fuellt einen String mit Leerzeichen auf (bis zur länge a)
     * 
     * @param a
     * @param fueller
     * @return
     */
    // // welche der beiden Methoden wird denn wirklich verwendet?
    // // bisher war Implementierung exakt identisch --> Verweis aufeinander
    // eingefügt!
    private String stringFuellen(int a, String fueller) {
        Main.debug.println("INVOKED (" + this.hashCode() + ", " + this.getId() + ") " + getClass()
                + " (Terminal), stringFuellen(" + a + "," + fueller + ")");
        String tmp = "";
        for (int i = 0; i < a; i++) {
            tmp = tmp + fueller;
        }
        return tmp;
    }

    public String makeEmptyString(int a, String fueller) {
        return stringFuellen(a, fueller);
    }

    /**
     * method to check for correct number of parameters
     */
    private int countParams(String[] args) {
        Main.debug.println("INVOKED (" + this.hashCode() + ", " + this.getId() + ") " + getClass()
                + " (Terminal), countParams(" + args + ")");
        int count = 0;
        for (int i = 0; i < args.length; i++) {
            if (!args[i].isEmpty()) {
                count++;
            } else
                return count; // return on first empty entry
        }
        return count;
    }

    private boolean numParams(String[] args, int exactNum) {
        return (exactNum == countParams(args));
    }

    private boolean numParams(String[] args, int minNum, int maxNum) {
        int count = countParams(args);
        return ((count >= minNum) && (count <= maxNum));
    }
}
