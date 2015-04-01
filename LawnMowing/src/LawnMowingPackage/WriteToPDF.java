/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package LawnMowingPackage;


import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import javax.swing.JTable;

/**
 *
 * @author Alex
 */
public class WriteToPDF {

    private Document ourDoc = new Document();
    private Font titleFont = new Font(Font.FontFamily.TIMES_ROMAN, 24, Font.BOLD);
    private Font columnFont = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.BOLD);
    WriteToPDF() throws FileNotFoundException, DocumentException {
        PdfWriter.getInstance(this.ourDoc, new FileOutputStream(new NewPDF("Invoice")));

        ourDoc.open();
        Paragraph assocName = new Paragraph("Lawn Mowing Association", titleFont);
        addToPage(assocName);
        addToPage(new Paragraph(" "));
        addToPage(new Paragraph(" "));
        addToPage(new Paragraph(" "));
        addToPage(new Paragraph(" "));
    }

    public Document getDoc() {
        return ourDoc;
    }

    public Paragraph newParagraph(String text, boolean alignCenter, boolean alignLeft, boolean alignRight) {
        Paragraph ourPara = new Paragraph();

        if ((alignCenter) && (!alignLeft) && (!alignRight)) {
            ourPara.setAlignment(Element.ALIGN_CENTER);
        } else if ((alignLeft) && (!alignRight) && (!alignCenter) || (!alignRight) && (!alignCenter) && (!alignLeft)) {
            ourPara.setAlignment(Element.ALIGN_LEFT);
        } else if ((alignRight) && (!alignCenter) && (!alignLeft)) {
            ourPara.setAlignment(Element.ALIGN_RIGHT);
        } else {
            System.out.println("Cannot have more than one alignment set.");
        }
        ourPara.add(text);

        return ourPara;
    }

    public void addTable(int totalColumns, String[][] rowData, JTable table) throws DocumentException {

        PdfPTable ourTable = new PdfPTable(totalColumns);

        for (int i = 0; i < totalColumns; i++) {
            Phrase p = new Phrase((table.getColumnModel().getColumn(i).getHeaderValue().toString()), columnFont);
            ourTable.addCell(new PdfPCell(p));
            
        }

        for (String[] data : rowData) {
            for (String element : data) {
                ourTable.addCell(element);
            }
        }

        this.ourDoc.add(ourTable);
    }

    public void addToPage(Element toAdd) throws DocumentException {
        this.ourDoc.add(toAdd);
    }

}
