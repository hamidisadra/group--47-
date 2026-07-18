package ir.ac.pvz.model.support;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public final class XlsxTableReader {

    private XlsxTableReader() {
    }

    public static List<Map<String, String>> readFirstSheet(String fileName)
            throws IOException {
        try (InputStream input = DataFileLocator.open(fileName)) {
            return readFirstSheet(input);
        }
    }

    public static List<Map<String, String>> readFirstSheet(InputStream input)
            throws IOException {
        Map<String, byte[]> entries = readEntries(input);
        List<String> sharedStrings = readSharedStrings(entries.get(
                "xl/sharedStrings.xml"));
        byte[] sheet = entries.get("xl/worksheets/sheet1.xml");
        if (sheet == null) {
            throw new IOException("The workbook has no first worksheet.");
        }
        List<List<String>> rows = readRows(sheet, sharedStrings);
        return mapRows(rows);
    }

    private static Map<String, byte[]> readEntries(InputStream input)
            throws IOException {
        Map<String, byte[]> entries = new LinkedHashMap<>();
        try (ZipInputStream zip = new ZipInputStream(input)) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                if (!entry.isDirectory()) {
                    entries.put(entry.getName(), readAll(zip));
                }
                zip.closeEntry();
            }
        }
        return entries;
    }

    private static byte[] readAll(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int count;
        while ((count = input.read(buffer)) >= 0) {
            output.write(buffer, 0, count);
        }
        return output.toByteArray();
    }

    private static List<String> readSharedStrings(byte[] xml)
            throws IOException {
        if (xml == null) {
            return Collections.emptyList();
        }
        Document document = parse(xml);
        NodeList items = document.getElementsByTagNameNS("*", "si");
        List<String> strings = new ArrayList<>();
        for (int index = 0; index < items.getLength(); index++) {
            Element item = (Element) items.item(index);
            strings.add(readTextNodes(item));
        }
        return strings;
    }

    private static List<List<String>> readRows(byte[] xml,
                                               List<String> sharedStrings)
            throws IOException {
        Document document = parse(xml);
        NodeList rowNodes = document.getElementsByTagNameNS("*", "row");
        List<List<String>> rows = new ArrayList<>();
        for (int rowIndex = 0; rowIndex < rowNodes.getLength(); rowIndex++) {
            Element row = (Element) rowNodes.item(rowIndex);
            rows.add(readRow(row, sharedStrings));
        }
        return rows;
    }

    private static List<String> readRow(Element row,
                                        List<String> sharedStrings) {
        List<String> values = new ArrayList<>();
        NodeList children = row.getChildNodes();
        for (int index = 0; index < children.getLength(); index++) {
            Node node = children.item(index);
            if (!(node instanceof Element)
                    || !"c".equals(node.getLocalName())) {
                continue;
            }
            Element cell = (Element) node;
            int column = columnIndex(cell.getAttribute("r"));
            while (values.size() <= column) {
                values.add("");
            }
            values.set(column, readCell(cell, sharedStrings));
        }
        return values;
    }

    private static String readCell(Element cell,
                                   List<String> sharedStrings) {
        String type = cell.getAttribute("t");
        if ("inlineStr".equals(type)) {
            return readTextNodes(cell);
        }
        String raw = firstElementText(cell, "v");
        if ("s".equals(type) && !raw.isEmpty()) {
            int index = Integer.parseInt(raw);
            return index >= 0 && index < sharedStrings.size()
                    ? sharedStrings.get(index) : "";
        }
        return raw;
    }

    private static List<Map<String, String>> mapRows(List<List<String>> rows) {
        if (rows.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> headers = rows.get(0);
        List<Map<String, String>> result = new ArrayList<>();
        for (int rowIndex = 1; rowIndex < rows.size(); rowIndex++) {
            List<String> row = rows.get(rowIndex);
            Map<String, String> values = new LinkedHashMap<>();
            boolean hasValue = false;
            for (int column = 0; column < headers.size(); column++) {
                String header = headers.get(column).trim();
                if (header.isEmpty()) {
                    continue;
                }
                String value = column < row.size() ? row.get(column).trim() : "";
                values.put(header, value);
                hasValue |= !value.isEmpty();
            }
            if (hasValue) {
                result.add(values);
            }
        }
        return result;
    }

    private static Document parse(byte[] xml) throws IOException {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl",
                    true);
            return factory.newDocumentBuilder().parse(
                    new ByteArrayInputStream(xml));
        } catch (ParserConfigurationException | SAXException exception) {
            throw new IOException("Cannot parse workbook XML.", exception);
        }
    }

    private static String readTextNodes(Element element) {
        NodeList nodes = element.getElementsByTagNameNS("*", "t");
        StringBuilder builder = new StringBuilder();
        for (int index = 0; index < nodes.getLength(); index++) {
            builder.append(nodes.item(index).getTextContent());
        }
        return builder.toString();
    }

    private static String firstElementText(Element element, String localName) {
        NodeList nodes = element.getElementsByTagNameNS("*", localName);
        return nodes.getLength() == 0 ? "" : nodes.item(0).getTextContent();
    }

    private static int columnIndex(String reference) {
        int value = 0;
        int index = 0;
        while (index < reference.length()
                && Character.isLetter(reference.charAt(index))) {
            value = value * 26 + Character.toUpperCase(
                    reference.charAt(index)) - 'A' + 1;
            index++;
        }
        return Math.max(0, value - 1);
    }
}
