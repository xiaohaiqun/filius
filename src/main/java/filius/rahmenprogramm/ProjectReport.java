/*
 ** This file is part of Filius, a network construction and simulation software.
 **
 ** Originally created at the University of Siegen, Institute "Didactics of
 ** Informatics and E-Learning" by a students' project group:
 **     members (2006-2007):
 **         AndrÃ© Asschoff, Johannes Bade, Carsten Dittich, Thomas Gerding,
 **         Nadja HaÃŸler, Ernst Johannes Klebert, Michell Weyer
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
package filius.rahmenprogramm;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.lang3.StringUtils;

import com.itextpdf.text.BadElementException;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.Section;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPHeaderCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import filius.gui.GUIContainer;
import filius.gui.JMainFrame;
import filius.gui.netzwerksicht.GUINodeItem;
import filius.hardware.NetworkInterface;
import filius.hardware.knoten.InternetNode;
import filius.rahmenprogramm.nachrichten.PacketAnalyzer;
import filius.software.Application;
import filius.software.system.HostOS;
import filius.software.system.InternetNodeOS;

public class ProjectReport  implements I18n {
	
    private static final Font SMALL_FONT = FontFactory.getFont(FontFactory.HELVETICA, 8, BaseColor.BLACK);
    private static final Font SMALL_BOLD_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, BaseColor.BLACK);
    private static final Font BOLD_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.BLACK);
    private static final Font DEFAULT_FONT = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.BLACK);
    private PacketAnalyzer packetsAnalyzer;
    private static ProjectReport projectReport;
    private int sectionNoLevel1;
    private int sectionNoLevel2;

    ProjectReport() {}

    public static ProjectReport getInstance() {
        if (null == projectReport) {
        	projectReport = new ProjectReport();
        	projectReport.packetsAnalyzer = PacketAnalyzer.getInstance();
        }
        return projectReport;
    }
    
    public void createReport() {
    	
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle(messages.getString("guimainmemu_msg20"));
        FileNameExtensionFilter pdfFileFilter = new FileNameExtensionFilter("PDF", "pdf");
        fileChooser.addChoosableFileFilter(pdfFileFilter);
        fileChooser.setAcceptAllFileFilterUsed(false);
        String path = ProjectManager.getInstance().getPath();
        if (path != null) {
            String szenarioFile = new File(path).getAbsolutePath();
            File preselectedFile = new File(szenarioFile.substring(0, szenarioFile.lastIndexOf(".")));
            fileChooser.setSelectedFile(preselectedFile);
        }

        if (fileChooser.showSaveDialog(JMainFrame.getInstance()) == JFileChooser.APPROVE_OPTION) {
            String reportPath = fileChooser.getSelectedFile().getAbsolutePath();
            reportPath = reportPath.endsWith(".pdf") ? reportPath : reportPath + ".pdf";

            int choice = JOptionPane.YES_OPTION;
            if (reportPath != null && new File(reportPath).exists()) {
                choice = JOptionPane.showConfirmDialog(JMainFrame.getInstance(), messages.getString("guimainmemu_msg17"), 
                		                               messages.getString("guimainmemu_msg10"), JOptionPane.YES_NO_OPTION);
            }
            if (choice == JOptionPane.YES_OPTION) {
                try {
                    generateReport(reportPath);
                } catch (DocumentException | IOException e) {
                    JOptionPane.showMessageDialog(JMainFrame.getInstance(), messages.getString("guimainmemu_msg11"));
                }
            }
        }
    }

    private void resetSections() {
        sectionNoLevel1 = 0;
        sectionNoLevel2 = 0;
    }

    public void generateReport(String pdfFilepath) throws DocumentException, IOException {
        Document document = initDocument(pdfFilepath);
        resetSections();

        addOverviewSection(document);
        document.add(Chunk.NEXTPAGE);

        addComponentConfigSection(document);
        document.add(Chunk.NEXTPAGE);

        addNetworkTrafficSection(document);

        closeDocument(document);
    }

    void addOverviewSection(Document document) throws BadElementException, IOException, DocumentException {
        createSection(document, "Overview", 1);

        Image img = Image.getInstance(ProjectExport.getInstance().createNetworkImage(), null);
        float percent = (document.right() - document.left()) / img.getWidth() * 100;
        img.scalePercent(percent);
        document.add(img);
    }

    void addComponentConfigSection(Document document) throws BadElementException, IOException, DocumentException {
        createSection(document, "Component Configuration", 1);

        List<GUINodeItem> components = GUIContainer.getInstance().getNodeItems();
        for (GUINodeItem item : components) {
            if (item.getNode() instanceof InternetNode) {
                createSection(document, item.getNode().getDisplayName(), 2);
                InternetNodeOS systemSoftware = (InternetNodeOS) item.getNode()
                        .getSystemSoftware();

                document.add(Chunk.NEWLINE);
                PdfPTable configTable = new PdfPTable(2);
                configTable.setTotalWidth(new float[] { 180, 360 });
                configTable.setLockedWidth(false);

                addConfigParam(I18n.messages.getString("jhostkonfiguration_msg5"), systemSoftware.getStandardGateway(),
                        configTable);
                addConfigParam(I18n.messages.getString("jhostkonfiguration_msg6"), systemSoftware.getDNSServer(),
                        configTable);
                if (systemSoftware instanceof HostOS) {
                    addConfigParam(I18n.messages.getString("jhostkonfiguration_msg7"),
                            String.valueOf(((HostOS) systemSoftware).getUseDHCPConfiguration()), configTable);
                }
                document.add(configTable);
                document.add(Chunk.NEWLINE);

                InternetNode node = (InternetNode) item.getNode();
                PdfPTable interfaceTable = new PdfPTable(3);
                interfaceTable.setTotalWidth(new float[] { 180, 180, 180 });
                interfaceTable.setLockedWidth(false);
                addHeaderCell(I18n.messages.getString("jhostkonfiguration_msg9"), interfaceTable);
                addHeaderCell(I18n.messages.getString("jhostkonfiguration_msg3"), interfaceTable);
                addHeaderCell(I18n.messages.getString("jhostkonfiguration_msg4"), interfaceTable);

                for (NetworkInterface networkInterface : node.getNICList()) {
                    addCell(networkInterface.getMac(), interfaceTable);
                    addCell(networkInterface.getIp(), interfaceTable);
                    addCell(networkInterface.getSubnetMask(), interfaceTable);
                }

                document.add(interfaceTable);
                document.add(Chunk.NEWLINE);

                if (systemSoftware instanceof HostOS) {
                    Chunk chunk = new Chunk(I18n.messages.getString("installationsdialog_msg3") + " ", BOLD_FONT);
                    document.add(chunk);
                    Application[] apps = ((HostOS) systemSoftware).getInstalledAppsArray();
                    String[] appNames = new String[apps.length];
                    for (int i = 0; i < apps.length; i++) {
                        appNames[i] = apps[i].getAppName();
                    }
                    String appList = StringUtils.join(appNames, ", ");
                    chunk = new Chunk(appList.isBlank() ? "-" : appList, DEFAULT_FONT);
                    document.add(chunk);
                    document.add(Chunk.NEWLINE);
                    document.add(Chunk.NEWLINE);
                }
            }
        }
    }

    private void addConfigParam(String key, String value, PdfPTable table) {
        addHeaderCell(key, table);
        addCell(value, table);
    }

    private void addCell(String value, PdfPTable table) {
        PdfPCell cell = new PdfPCell(new Phrase(value, DEFAULT_FONT));
        cell.setBorder(Rectangle.NO_BORDER);
        table.addCell(cell);
    }

    private void addHeaderCell(String key, PdfPTable table) {
        PdfPCell header = new PdfPHeaderCell();
        header.setBorder(Rectangle.NO_BORDER);
        header.setPhrase(new Phrase(key, BOLD_FONT));
        table.addCell(header);
    }

    void addNetworkTrafficSection(Document document) throws BadElementException, IOException, DocumentException {
        String[] columnHeader = packetsAnalyzer.getHeader();
        Collection<String> interfaceIDs = packetsAnalyzer.getInterfaceIDs();
        if (interfaceIDs.size() > 0) {
            createSection(document, "Network Traffic", 1);
        }
        for (String interfaceId : interfaceIDs) {
            String hostname = "Unknown";
            String ipAddress = "0.0.0.0";
            for (GUINodeItem item : GUIContainer.getInstance().getNodeItems()) {
                if (item.getNode() instanceof InternetNode) {
                	InternetNode node = (InternetNode) item.getNode();
                	NetworkInterface nic = node.getNICbyMAC(interfaceId);
                    if (nic != null) {
                        hostname = item.getNode().getDisplayName();
                        ipAddress = nic.getIp();
                        break;
                    }
                }
            }
            createSection(document, hostname + " - " + ipAddress, 2);
            document.add(Chunk.NEWLINE);

            PdfPTable table = new PdfPTable(columnHeader.length);
            table.setTotalWidth(new float[] { 20, 60, 80, 80, 40, 50, 180 });
            table.setLockedWidth(true);

            for (int i = 0; i < columnHeader.length; i++) {
                PdfPCell header = new PdfPHeaderCell();
                header.setBackgroundColor(new BaseColor(230, 240, 255));
                header.setBorder(Rectangle.NO_BORDER);
                header.setPhrase(new Phrase(columnHeader[i], SMALL_BOLD_FONT));
                table.addCell(header);
            }

            Object[][] data = packetsAnalyzer.getDataEntries(interfaceId, true);
            for (int i = 0; i < data.length; i++) {
                for (int j = 0; j < data[i].length; j++) {
                    PdfPCell cell = new PdfPCell(new Phrase((String) data[i][j], SMALL_FONT));
                    cell.setBorder(Rectangle.NO_BORDER);
                    if (i % 2 == 1) {
                        cell.setBackgroundColor(new BaseColor(240, 240, 240));
                    }
                    table.addCell(cell);
                }
            }
            document.add(table);
            document.add(Chunk.NEWLINE);
        }
    }

    private void createSection(Document document, String title, int level) throws DocumentException {
        ArrayList<Integer> numbers = new ArrayList<Integer>();
        if (level == 1) {
            numbers.add(++sectionNoLevel1);
            sectionNoLevel2 = 0;
        } else if (level == 2) {
            numbers.add(++sectionNoLevel2);
            numbers.add(sectionNoLevel1);
        }
        Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16 - 2 * level, BaseColor.DARK_GRAY);
        Paragraph sectionHeader = Section.constructTitle(new Paragraph(title, font), numbers, level,
                Section.NUMBERSTYLE_DOTTED);
        document.add(sectionHeader);
    }

    void closeDocument(Document document) {
        document.close();
        GUIContainer.getInstance().updateViewport();
    }

    Document initDocument(String pdfFilepath) throws DocumentException, FileNotFoundException {
        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(pdfFilepath));

        document.open();

        String storagePath = ProjectManager.getInstance().getPath();
        String title;
        if (storagePath != null) {
            String filename = new File(storagePath).getName();
            if (filename.contains(".")) {
                filename = filename.substring(0, filename.lastIndexOf('.'));
            }
            title = "Filius Documentation: " + filename;
        } else {
            title = "Filius Documentation";
        }
        document.addTitle(title);
        document.addCreator("Filius (www.lernsoftware-filius.de)");

        Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, new BaseColor(30, 50, 120));
        Paragraph paragraph = new Paragraph(title, font);
        paragraph.setAlignment(Paragraph.ALIGN_CENTER);
        document.add(paragraph);
        document.add(new Chunk(storagePath, SMALL_FONT));
        document.add(Chunk.NEWLINE);
        return document;
    }
}
